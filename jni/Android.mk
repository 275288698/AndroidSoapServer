LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := soap_server
LOCAL_SRC_FILES := gsoapServer.cpp soapC.cpp soapServer.cpp stdsoap2.cpp

LOCAL_LDLIBS += -llog
LOCAL_LDLIBS += -landroid

include $(BUILD_SHARED_LIBRARY)