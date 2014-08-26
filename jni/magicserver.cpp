#include <string.h>
#include <jni.h>
#include "soapH.h"
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

int http_get(struct soap *soap);
char* readWsdl(char* wsdlName);

#define APP_TAG "HelloJni"
#define logI(msg) __android_log_write(ANDROID_LOG_INFO, APP_TAG, msg)
#define logW(msg) __android_log_write(ANDROID_LOG_WARN, APP_TAG, msg)
#define logE(msg) __android_log_write(ANDROID_LOG_ERROR, APP_TAG, msg)

////////////////////////////////////////////////////////////////////////////////
//
//	Magic Squares Server
//
////////////////////////////////////////////////////////////////////////////////

// Install as a CGI application.
// Alternatively, run from command line with arguments IP (which must be the
// IP of the current machine you are using) and PORT to run this as a
// stand-alone server on a port. For example:
// > magicserver.cgi machine 18081 &
// To let 'magic' talk to this service, change the URL in magic.cpp into
// "http://machine:18081"
// where "machine" is the name of your machine or e.g. "localhost"

extern "C" {
JNIEXPORT jint JNICALL Java_com_example_hellojni_HelloJni_runServer(JNIEnv* env, jobject thiz);
JNIEXPORT jboolean JNICALL Java_com_example_hellojni_HelloJni_stopServer(JNIEnv* env, jobject thiz);
JNIEXPORT jint JNICALL Java_com_example_hellojni_HelloJni_setAssetManager(JNIEnv* env, jobject thiz, jobject am);
};

int main(int argc, char **argv) {
	return 0;
}

bool run = true;
AAssetManager* assetManager = NULL;

JNIEXPORT jint JNICALL Java_com_example_hellojni_HelloJni_setAssetManager(JNIEnv* env, jobject thiz, jobject am) {
	assetManager = AAssetManager_fromJava(env, am);
	if (assetManager == NULL) {
		logE("AssetMgr is null.");
	} else {
		logI("AssetMgr set.");
	}
	return 0;
}

JNIEXPORT jboolean JNICALL Java_com_example_hellojni_HelloJni_stopServer(JNIEnv* env, jobject thiz) {
	return run = false;
}

JNIEXPORT jint JNICALL Java_com_example_hellojni_HelloJni_runServer(JNIEnv* env, jobject thiz) {
	char buff[200];
	int argc = 3;
	struct soap soap;
	int m, s;
	soap_init(&soap);
	// soap.accept_timeout = 60; // die if no requests are made within 1 minute
	soap.fget = http_get;
	if (argc < 3) {
		soap_serve(&soap);
		soap_destroy(&soap);
		soap_end(&soap);	// clean up
	} else {
		m = soap_bind(&soap, NULL, 8080, 100); // host MUST be NULL to be accessible from external network

		if (m < 0) {
			sprintf(buff, "Soap bind error status: %d", m);
			logE(buff);
			exit(-1);
		}
		sprintf(buff, "Socket connection successful %d\n", m);
		logI(buff);
		for (int i = 1; run == true; i++) {
			s = soap_accept(&soap);
			if (s < 0) {
				exit(-1);
			}
			sprintf(buff, "%d: accepted %d IP=%d.%d.%d.%d ... ", i, s,
					(int) (soap.ip >> 24) & 0xFF, (int) (soap.ip >> 16) & 0xFF,
					(int) (soap.ip >> 8) & 0xFF, (int) soap.ip & 0xFF);
			logI(buff);
			soap_serve(&soap);	// process RPC skeletons
			sprintf(buff, "served\n");
			logI(buff);
			soap_destroy(&soap);
			soap_end(&soap);	// clean up
		}
	}
	return 0;
}

int http_get(struct soap *soap) {
//	FILE *fd = NULL;
	char *s = strchr(soap->path, '?');
	if (!s || strcmp(s, "?wsdl")) {
		return SOAP_GET_METHOD;
	}
	char* wsdl = readWsdl("myservice.wsdl");
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

////////////////////////////////////////////////////////////////////////////////
//
//	Magic Square Algorithm
//
////////////////////////////////////////////////////////////////////////////////

int ns1__magic(struct soap *soap, int n, matrix *square) {
	int i, j, k, l, key = 2;
	if (n < 1)
		return soap_receiver_fault(soap, "Negative or zero size",
				"The input parameter must be positive");
	if (n > 100)
		return soap_receiver_fault(soap, "size > 100",
				"The input parameter must not be too large");
	square->resize(n, n);
	for (i = 0; i < n; i++)
		for (j = 0; j < n; j++)
			(*square)[i][j] = 0;
	i = 0;
	j = (n - 1) / 2;
	(*square)[i][j] = 1;
	while (key <= n * n) {
		if (i - 1 < 0)
			k = n - 1;
		else
			k = i - 1;
		if (j - 1 < 0)
			l = n - 1;
		else
			l = j - 1;
		if ((*square)[k][l])
			i = (i + 1) % n;
		else {
			i = k;
			j = l;
		}
		(*square)[i][j] = key;
		key++;
	}
	return SOAP_OK;
}

////////////////////////////////////////////////////////////////////////////////
//
//	Class vector Methods
//
////////////////////////////////////////////////////////////////////////////////

vector::vector() {
	__ptr = 0;
	__size = 0;
}

vector::vector(int n) {
	__ptr = (int*) soap_malloc(soap, n * sizeof(int));
	__size = n;
}

vector::~vector() {
	soap_unlink(soap, this); // not required, but just to make sure if someone calls delete on this
}

void vector::resize(int n) {
	int *p;
	if (__size == n)
		return;
	p = (int*) soap_malloc(soap, n * sizeof(int));
	if (__ptr) {
		for (int i = 0; i < (n <= __size ? n : __size); i++)
			p[i] = __ptr[i];
		soap_unlink(soap, __ptr);
		free(__ptr);
	}
	__size = n;
	__ptr = p;
}

int& vector::operator[](int i) {
	if (!__ptr || i < 0 || i >= __size)
		fprintf(stderr, "Array index out of bounds\n");
	return (__ptr)[i];
}

////////////////////////////////////////////////////////////////////////////////
//
//	Class matrix Methods
//
////////////////////////////////////////////////////////////////////////////////

matrix::matrix() {
	__ptr = 0;
	__size = 0;
}

matrix::matrix(int rows, int cols) {
	__ptr = soap_new_vector(soap, rows);
	for (int i = 0; i < cols; i++)
		__ptr[i].resize(cols);
	__size = rows;
}

matrix::~matrix() {
	soap_unlink(soap, this); // not required, but just to make sure if someone calls delete on this
}

void matrix::resize(int rows, int cols) {
	int i;
	vector *p;
	if (__size != rows) {
		if (__ptr) {
			p = soap_new_vector(soap, rows);
			for (i = 0; i < (rows <= __size ? rows : __size); i++) {
				if (this[i].__size != cols)
					(*this)[i].resize(cols);
				(p + i)->__ptr = __ptr[i].__ptr;
				(p + i)->__size = cols;
			}
			for (; i < rows; i++)
				__ptr[i].resize(cols);
		} else {
			__ptr = soap_new_vector(soap, rows);
			for (i = 0; i < rows; i++)
				__ptr[i].resize(cols);
			__size = rows;
		}
	} else
		for (i = 0; i < __size; i++)
			__ptr[i].resize(cols);
}

vector& matrix::operator[](int i) {
	if (!__ptr || i < 0 || i >= __size)
		fprintf(stderr, "Array index out of bounds\n");
	return __ptr[i];
}

////////////////////////////////////////////////////////////////////////////////
//
//	Namespace Definition Table
//
////////////////////////////////////////////////////////////////////////////////

struct Namespace namespaces[] = { { "SOAP-ENV",
		"http://schemas.xmlsoap.org/soap/envelope/" }, // must be first
		{ "SOAP-ENC", "http://schemas.xmlsoap.org/soap/encoding/" }, // must be second
		{ "xsi", "http://www.w3.org/1999/XMLSchema-instance",
				"http://www.w3.org/*/XMLSchema-instance" }, { "xsd",
				"http://www.w3.org/1999/XMLSchema",
				"http://www.w3.org/*/XMLSchema" }, { "ns1", "urn:MagicSquare" }, // "ns1" namespace prefix
		{ NULL, NULL } };
