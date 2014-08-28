#include <string.h>
#include <jni.h>
#include "soapH.h"
#include <android/log.h>
#include <android/sensor.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

/** Implements server GET method */
int http_get(struct soap *soap);
/** Reads WSDL from file */
char* readWsdl(char* wsdlName);
/** Processes request in separate thread */
void* process_request(void *soap);

#define APP_TAG "ServerRunner"
#define logI(msg) __android_log_write(ANDROID_LOG_INFO, APP_TAG, msg)
#define logW(msg) __android_log_write(ANDROID_LOG_WARN, APP_TAG, msg)
#define logE(msg) __android_log_write(ANDROID_LOG_ERROR, APP_TAG, msg)
#define Sensor ns__Sensor
#define Location ns__Location

extern "C" {
JNIEXPORT jint JNICALL Java_edu_agh_wsserver_soap_ServerRunner_runServer(JNIEnv* env, jobject thiz);
JNIEXPORT jboolean JNICALL Java_edu_agh_wsserver_soap_ServerRunner_stopServer(JNIEnv* env, jobject thiz);
JNIEXPORT jint JNICALL Java_edu_agh_wsserver_soap_ServerRunner_setAssetManager(JNIEnv* env, jobject thiz, jobject am);
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved);
};

bool isRunning = true;
AAssetManager* assetManager = NULL;
static JavaVM *jvm;
static jclass deviceUtilsClass;
static jclass sensorDtoClass;
static jclass locationUtilClass;
static jclass locationDtoClass;

JNIEXPORT jint JNICALL Java_edu_agh_wsserver_soap_ServerRunner_runServer(JNIEnv* env, jobject thiz) {
	char buff[200];
	struct soap soap;
	int m, s;
	soap_init(&soap);
	// soap.accept_timeout = 60; // die if no requests are made within 1 minute
	soap.fget = http_get;
	m = soap_bind(&soap, NULL, 8080, 100); // host MUST be NULL to be accessible from external network

	if (m < 0) {
		sprintf(buff, "Soap bind error status: %d", m);
		logE(buff);
		exit(-1);
	}
	sprintf(buff, "Socket connection successful %d\n", m);
	logI(buff);

	struct soap *tsoap;
	pthread_t tid;

	for (int i = 1; isRunning == true; i++) {
		s = soap_accept(&soap);
		if (s < 0) {
			exit(-1);
		}
		sprintf(buff, "%d: accepted %d IP=%d.%d.%d.%d ... ", i, s, (int) (soap.ip >> 24) & 0xFF, (int) (soap.ip >> 16) & 0xFF, (int) (soap.ip >> 8) & 0xFF, (int) soap.ip & 0xFF);
		logI(buff);

		tsoap = soap_copy(&soap); // make a safe copy
		if (!tsoap) {
			logE("Error when making copy of soap struct.");
			break;
		}
		pthread_create(&tid, NULL, (void*(*)(void*))process_request, (void*)tsoap);

		//soap_serve(&soap);	// process RPC skeletons
		//sprintf(buff, "served\n");
		//logI(buff);
		//soap_destroy(&soap);
		//soap_end(&soap);	// clean up
	}
	soap_done(&soap); // detach soap struct
	return 0;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
	JNIEnv* env;
	if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
		return -1;
	}
	jvm = vm; // caching JVM
	// caching classes
	// NewGlobalRef for not allowing GC to delete reference
	deviceUtilsClass = (jclass) env->NewGlobalRef(env->FindClass("edu/agh/wsserver/utils/DeviceUtils"));
	sensorDtoClass = (jclass) env->NewGlobalRef(env->FindClass("edu/agh/wsserver/utils/dto/SensorDto"));
	locationUtilClass = (jclass) env->NewGlobalRef(env->FindClass("edu/agh/wsserver/utils/location/LocationUtil"));
	locationDtoClass = (jclass) env->NewGlobalRef(env->FindClass("edu/agh/wsserver/utils/dto/LocationDto"));
	return JNI_VERSION_1_6;
}

JNIEXPORT jint JNICALL Java_edu_agh_wsserver_soap_ServerRunner_setAssetManager(JNIEnv* env, jobject thiz, jobject am) {
	assetManager = AAssetManager_fromJava(env, am);
	if (assetManager == NULL) {
		logE("AssetMgr is null.");
		return -1;
	} else {
		logI("AssetMgr set.");
	}
	return 0;
}

JNIEXPORT jboolean JNICALL Java_edu_agh_wsserver_soap_ServerRunner_stopServer(JNIEnv* env, jobject thiz) {
	return isRunning = false;
}

// multi-threaded server http://www.cs.fsu.edu/~engelen/soapdoc2.html#sec:mt
void* process_request(void *soap) {
	char buff[200];
	pthread_t id = pthread_self();
	pthread_detach(id);
	soap_serve((struct soap*) soap);
	sprintf(buff, "Served. Thread ID: %lu", (long) id);
	logI(buff);
	soap_destroy((struct soap* )soap); // dealloc C++ data
	soap_end((struct soap*) soap); // dealloc data and clean up
	soap_done((struct soap*) soap); // detach soap struct
	free(soap);
	return NULL;
}

int http_get(struct soap *soap) {

//	Sensor* sensors = getAvailableSensorsInfo(); //test
//	Location* location = getCurrentDeviceLocation(); //test

	char *s = strchr(soap->path, '?');
	if (!s || strcmp(s, "?wsdl")) {
		return SOAP_GET_METHOD;
	}
	char* wsdl = readWsdl((char *) "DeviceServices.wsdl");
	if (wsdl == JNI_FALSE) {
		return 404; // return HTTP not found error
	}
	soap->http_content = "text/xml"; // HTTP header with text/xml content
	soap_response(soap, SOAP_FILE);
	soap_send(soap, wsdl);
	soap_end_send(soap);
	return SOAP_OK;
}

char* readWsdl(char* wsdlName) {
	if (assetManager == NULL) {
		return JNI_FALSE;
	}
	AAsset* asset = AAssetManager_open(assetManager, wsdlName, AASSET_MODE_UNKNOWN);
	if (NULL == asset) {
		logE("_ASSET_NOT_FOUND_");
		return JNI_FALSE;
	} else {
		logI("Asset read.");
	}
	long size = AAsset_getLength(asset);
	char* buffer = (char*) malloc(sizeof(char) * size + 1);
	AAsset_read(asset, buffer, size);
	buffer[size] = '\0'; // to remove some junk at the end, NOTE '+ 1' above
	AAsset_close(asset);
	return buffer;
}

char* jstringToCharArray(JNIEnv *env, jstring str) {
	int size = env->GetStringLength(str);
	char *buff = new char[size + 1];
	const char *utfChars = env->GetStringUTFChars(str, NULL);
	sprintf(buff, "%s", utfChars);
	buff[size] = '\0';
	env->ReleaseStringUTFChars(str, utfChars);
	return buff;
}

Sensor* getAvailableSensorsInfo(int *resArrSize) {
	JNIEnv* env;
	jint rs = jvm->AttachCurrentThread(&env, NULL);
	if (rs == JNI_OK) {
		jmethodID getDeviceSensorsMethod = env->GetStaticMethodID(deviceUtilsClass, "getDeviceSensors", "()[Ledu/agh/wsserver/utils/dto/SensorDto;");
		jobjectArray sensorsDtoArray = (jobjectArray) env->CallStaticObjectMethod(deviceUtilsClass, getDeviceSensorsMethod, NULL);
		int arrSize = env->GetArrayLength(sensorsDtoArray);
		Sensor* sensors = new Sensor[arrSize];
		jstring tmp;
		for (int i = 0; i < arrSize; i++) {
			jobject sensorDto = env->GetObjectArrayElement(sensorsDtoArray, i);
			sensors[i].mMaxRange = env->GetIntField(sensorDto, env->GetFieldID(sensorDtoClass, "mMaxRange", "F"));
			sensors[i].mMinDelay = env->GetIntField(sensorDto, env->GetFieldID(sensorDtoClass, "mMinDelay", "I"));

			tmp = (jstring) env->GetObjectField(sensorDto, env->GetFieldID(sensorDtoClass, "mName", "Ljava/lang/String;"));
			sensors[i].mName = jstringToCharArray(env, tmp);

			sensors[i].mPower = env->GetFloatField(sensorDto, env->GetFieldID(sensorDtoClass, "mPower", "F"));
			sensors[i].mResolution = env->GetIntField(sensorDto, env->GetFieldID(sensorDtoClass, "mResolution", "F"));
			sensors[i].mType = env->GetIntField(sensorDto, env->GetFieldID(sensorDtoClass, "mType", "I"));

			tmp = (jstring) env->GetObjectField(sensorDto, env->GetFieldID(sensorDtoClass, "mVendor", "Ljava/lang/String;")); // must end with ';'
			sensors[i].mVendor = jstringToCharArray(env, tmp);

			sensors[i].mVersion = env->GetIntField(sensorDto, env->GetFieldID(sensorDtoClass, "mVersion", "I"));
		}
		(*resArrSize) = arrSize;
		jvm->DetachCurrentThread();
		return sensors;
	} else {
		logE("Cannot get JNIEnv.");
		return NULL;
	}
}

/**
 * Return null if location cannot be found, because all sensors are disabled.
 */
Location* getCurrentDeviceLocation() {
	JNIEnv* env;
	jint rs = jvm->AttachCurrentThread(&env, NULL);
	if (rs == JNI_OK) {
		jmethodID getCurrentLocationMethod = env->GetStaticMethodID(locationUtilClass, "getCurrentLocation", "()Ledu/agh/wsserver/utils/dto/LocationDto;");
		jobject locationDto = env->CallStaticObjectMethod(locationUtilClass, getCurrentLocationMethod, NULL);
		Location* result = new Location();
		if (locationDto != NULL) {
			result->latitude = env->GetDoubleField(locationDto, env->GetFieldID(locationDtoClass, "latitude", "D"));
			result->longitude = env->GetDoubleField(locationDto, env->GetFieldID(locationDtoClass, "longitude", "D"));
		} else {
			result = NULL;
			logI("Location is NULL");
		}
		jvm->DetachCurrentThread();
		return result;
	} else {
		logE("Cannot get JNIEnv.");
		return NULL;
	}
}

/** Services implementation */

SOAP_FMAC5 int SOAP_FMAC6 ns__getAvailableSensorsInfo(struct soap* soap, void *_, struct ns__SensorArray *sensors) {
	int arrSize = 0;
	Sensor* result = getAvailableSensorsInfo(&arrSize);
	if (result == NULL) {
		return soap_receiver_fault(soap, "NULL result", "Method getting sensors information returned null.");
	}
	sensors->__size = arrSize;
	sensors->sensors = result;
	return SOAP_OK;
}

SOAP_FMAC5 int SOAP_FMAC6 ns__getCurrentDeviceLocation(struct soap* soap, void *_, struct ns__Location *location) {
	Location* result = getCurrentDeviceLocation();
	if (result == NULL) {
		return soap_receiver_fault(soap, "Cannot obtain device location", "Probably GPS sensor is disabled or location have not been found yet.");
	}
	location->latitude = result->latitude;
	location->longitude = result->longitude;
	return SOAP_OK;
}

/** -------------------------- */

struct Namespace namespaces[] = {
		{ "SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/" }, // must be first
		{ "SOAP-ENC", "http://schemas.xmlsoap.org/soap/encoding/" }, // must be second
		{ "xsi", "http://www.w3.org/1999/XMLSchema-instance", "http://www.w3.org/*/XMLSchema-instance" },
		{ "xsd", "http://www.w3.org/1999/XMLSchema", "http://www.w3.org/*/XMLSchema" },
		{ "ns", "urn:DeviceServices" }, // "ns1" namespace prefix
		{ NULL, NULL }
};
