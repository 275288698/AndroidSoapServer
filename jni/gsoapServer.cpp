#include <string.h>
#include <jni.h>
#include "soapH.h"
#include <android/log.h>
#include <android/sensor.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#define APP_TAG "GSoapServer"
#define logI(msg) __android_log_write(ANDROID_LOG_INFO, APP_TAG, msg)
#define logW(msg) __android_log_write(ANDROID_LOG_WARN, APP_TAG, msg)
#define logE(msg) __android_log_write(ANDROID_LOG_ERROR, APP_TAG, msg)
#define logD(msg) __android_log_write(ANDROID_LOG_DEBUG, APP_TAG, msg)
#define Sensor ns__Sensor
#define Location ns__Location
#define WSDL_FILE_NAME "DeviceServices.wsdl"
#define MAX_THREADS_POOL_SIZE 50
#define MAX_ALLOWED_PORT 65535
#define MIN_ALLOWED_PORT 1024
#define REQ_BACKLOG (500)
#define USE_THREAD_POOL true

/** Implements server GET method */
int http_get(struct soap *soap);
/** Reads WSDL from file */
char* readWsdl(char* wsdlName);
/** Processes request in separate thread */
void* process_request(void *soap);
int runServerWithoutThreadPool();
int runServerWithThreadPool();

extern "C" {
JNIEXPORT jint JNICALL Java_edu_agh_wsserver_soap_ServerRunner_runServer(JNIEnv* env, jobject thiz);
JNIEXPORT jboolean JNICALL Java_edu_agh_wsserver_soap_ServerRunner_stopServer(JNIEnv* env, jobject thiz);
JNIEXPORT jint JNICALL Java_edu_agh_wsserver_soap_ServerRunner_setAssetManager(JNIEnv* env, jobject thiz, jobject am);
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved);
JNIEXPORT jint JNICALL Java_edu_agh_wsserver_soap_ServerRunner_setServerPort(JNIEnv* env, jobject thiz, jint port);
JNIEXPORT jint JNICALL Java_edu_agh_wsserver_soap_ServerRunner_setServerThreadsPoolSize(JNIEnv* env, jobject thiz, jint poolSize);
};

/* Global variables */
static bool isRunning = false;
static bool serverStopped = true;
static AAssetManager* assetManager = NULL;
static JavaVM *jvm;
static jclass deviceUtilsClass;
static jclass sensorDtoClass;
static jclass locationUtilClass;
static jclass locationDtoClass;
static int serverPort = 8080;
static int threadsPoolSize = 25;
static pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

JNIEXPORT jint JNICALL Java_edu_agh_wsserver_soap_ServerRunner_setServerThreadsPoolSize(JNIEnv* env, jobject thiz, jint poolSize) {
	char buff[100];
	if (poolSize > 0 && poolSize <= MAX_THREADS_POOL_SIZE) {
		threadsPoolSize = poolSize;
		sprintf(buff, "Threads pool size set to: %d", threadsPoolSize);
		logI(buff);
		return 0;
	}
	sprintf(buff, "Wrong threads pool size: %d", poolSize);
	logW(buff);
	return -1;
}

JNIEXPORT jint JNICALL Java_edu_agh_wsserver_soap_ServerRunner_setServerPort(JNIEnv* env, jobject thiz, jint port) {
	char buff[100];
	if (port >= MIN_ALLOWED_PORT && port <= MAX_ALLOWED_PORT) {
		serverPort = port;
		sprintf(buff, "Server port set to: %d", serverPort);
		logI(buff);
		return 0;
	}
	sprintf(buff, "Wrong port number: %d", port);
	logW(buff);
	return -1;
}

JNIEXPORT jint JNICALL Java_edu_agh_wsserver_soap_ServerRunner_runServer(JNIEnv* env, jobject thiz) {
	if (USE_THREAD_POOL) {
		return runServerWithThreadPool();
	} else {
		return runServerWithoutThreadPool();
	}
}

// multi-threaded server http://www.cs.fsu.edu/~engelen/soapdoc2.html#sec:mt
int runServerWithoutThreadPool() {
	if (!serverStopped) {
		logW("Server already running. Cannot start another instance without stopping previous one.");
		return -2;
	}
	isRunning = true;
	serverStopped = false;
	char buff[200];
	struct soap soap;
	int m, s;
	soap_init(&soap);
	soap.bind_flags = SO_REUSEADDR;
	soap.fget = http_get;
	soap.accept_timeout = 3;

	m = soap_bind(&soap, NULL, serverPort, REQ_BACKLOG); // host MUST be NULL to be accessible from external network

	if (m < 0) {
		sprintf(buff, "Soap bind error status: %d", m);
		logE(buff);
		return -1;
	}
	sprintf(buff, "Socket connection successful. Status: %d. Server running on port: %d", m, serverPort);
	logI(buff);

	struct soap *tsoap;
	pthread_t tid;

	for (int i = 1; isRunning == true; i++) {
		s = soap_accept(&soap);
		if (s >= 0) {
			sprintf(buff, "%d: accepted %d IP=%d.%d.%d.%d ... ", i, s, (int) (soap.ip >> 24) & 0xFF, (int) (soap.ip >> 16) & 0xFF, (int) (soap.ip >> 8) & 0xFF, (int) soap.ip & 0xFF);
			logI(buff);

			tsoap = soap_copy(&soap); // make a safe copy
			if (!tsoap) {
				logE("Error when making copy of soap struct.");
				break;
			}
			pthread_create(&tid, NULL, (void*(*)(void*))process_request, (void*)tsoap);
		}
//		else {
//			sprintf(buff, "soap_accept() returned: %d. It could be caused by accept_timeout.", s);
//			logD(buff);
//		}
	}
//	soap_destroy(&soap);
//	soap_end(&soap);
	soap_done(&soap); // detach soap struct
	logI("GSoap server stopped gracefully.");
	serverStopped = true;
	return 0;
}

// multi-threaded server http://www.cs.fsu.edu/~engelen/soapdoc2.html#sec:mt
int runServerWithThreadPool() {
	if (!serverStopped) {
		logW("Server already running. Cannot start another instance without stopping previous one.");
		return -2;
	}
	isRunning = true;
	serverStopped = false;
	char buff[200];
	char soapFaultBuff[1000];

	int localThreadsPoolSize = threadsPoolSize;
	struct soap soap;
	soap_init(&soap);
	soap.fget = http_get;
	soap.bind_flags = SO_REUSEADDR;
	soap.accept_timeout = 3;
	struct soap *soap_thr[localThreadsPoolSize]; // each thread needs a runtime context
	pthread_t tid[localThreadsPoolSize];
	SOAP_SOCKET m, s;
	int i;

	m = soap_bind(&soap, NULL, serverPort, REQ_BACKLOG);
	if (!soap_valid_socket(m)) {
		sprintf(buff, "Soap bind error status: %d", m);
		logE(buff);
		return -1;
	}
	sprintf(buff, "Socket connection successful. Status: %d. Server running on port: %d. Threads pool size: %d", m, serverPort, localThreadsPoolSize);
	logI(buff);

	for (i = 0; i < localThreadsPoolSize; i++) {
		soap_thr[i] = NULL;
	}

	while (isRunning == true) {
		for (i = 0; i < localThreadsPoolSize && isRunning == true; i++) {
			s = soap_accept(&soap);
			if (!soap_valid_socket(s)) {
				if (soap.errnum) {
					soap_print_fault(&soap, soapFaultBuff);
					logW(soapFaultBuff);
					continue; // retry
				} else {
//					sprintf(buff, "Server timed out");
//					logW(buff);
//					break;
					continue;
				}
			}
			sprintf(buff, "Thread %d accepts socket %d connection from IP %d.%d.%d.%d", i, s, (int) (soap.ip >> 24) & 0xFF, (int) (soap.ip >> 16) & 0xFF, (int) (soap.ip >> 8) & 0xFF,
					(int) soap.ip & 0xFF);
			logI(buff);
			if (!soap_thr[i]) // first time around
			{
				soap_thr[i] = soap_copy(&soap);
				if (!soap_thr[i]) {
					sprintf(buff, "Critical error. Could not allocate memory for soap struct.");
					logE(buff);
					return -1; // could not allocate
				}
			} else { // recycle soap context
				pthread_join(tid[i], NULL);
				sprintf(buff, "Thread %d completed", i);
				logI(buff);
				soap_destroy(soap_thr[i]); // deallocate C++ data of old thread
				soap_end(soap_thr[i]); // deallocate data of old thread
			}
			soap_thr[i]->socket = s; // new socket fd
			pthread_create(&tid[i], NULL, (void*(*)(void*))process_request, (void*)soap_thr[i]);
		}
	}
	for (i = 0; i < localThreadsPoolSize; i++) {
		if (soap_thr[i]) {
//			soap_destroy(soap_thr[i]);
//			soap_end(soap_thr[i]);
			soap_done(soap_thr[i]); // detach context
			free(soap_thr[i]); // free up
		}
	}

//	soap_destroy(&soap);
//	soap_end(&soap);
	soap_done(&soap); // detach soap struct
	logI("GSoap server stopped gracefully.");
	serverStopped = true;
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
	if (!USE_THREAD_POOL) {
		soap_destroy((struct soap* )soap); // dealloc C++ data
		soap_end((struct soap*) soap); // dealloc data and clean up
		soap_done((struct soap*) soap); // detach soap struct
		free(soap);
	}
	return NULL;
}

int http_get(struct soap *soap) {
	char *s = strchr(soap->path, '?');
	if (!s || strcmp(s, "?wsdl")) {
		return SOAP_GET_METHOD;
	}
	char* wsdl = readWsdl((char *) WSDL_FILE_NAME);
	if (wsdl == JNI_FALSE) {
		return 404; // return HTTP not found error
	}
	soap->http_content = "text/xml"; // HTTP header with text/xml content
	soap_response(soap, SOAP_FILE);
	soap_send(soap, wsdl);
	soap_end_send(soap);
	free(wsdl);
	return SOAP_OK;
}

char* readWsdl(char* wsdlName) {
	if (assetManager == NULL) {
		return JNI_FALSE;
	}
	pthread_mutex_lock(&mutex);
	AAsset* asset = AAssetManager_open(assetManager, wsdlName, AASSET_MODE_UNKNOWN);
	if (NULL == asset) {
		logE("_ASSET_NOT_FOUND_");
		return JNI_FALSE;
	} else {
		logD("WSDL file successfully loaded.");
	}
	long size = AAsset_getLength(asset);
	char* buffer = (char*) malloc(sizeof(char) * size + 1);
	AAsset_read(asset, buffer, size);
	buffer[size] = '\0'; // to remove some junk at the end, NOTE '+ 1' above
	AAsset_close(asset);
	pthread_mutex_unlock(&mutex);
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
