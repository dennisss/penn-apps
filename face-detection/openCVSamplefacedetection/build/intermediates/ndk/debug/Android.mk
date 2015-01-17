LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := detection_based_tracker
LOCAL_SRC_FILES := \
	/home/tomer/Desktop/penn-apps/face-detection/openCVSamplefacedetection/src/main/jni/DetectionBasedTracker_jni.cpp \
	/home/tomer/Desktop/penn-apps/face-detection/openCVSamplefacedetection/src/main/jni/Application.mk \
	/home/tomer/Desktop/penn-apps/face-detection/openCVSamplefacedetection/src/main/jni/Android.mk \

LOCAL_C_INCLUDES += /home/tomer/Desktop/penn-apps/face-detection/openCVSamplefacedetection/src/main/jni
LOCAL_C_INCLUDES += /home/tomer/Desktop/penn-apps/face-detection/openCVSamplefacedetection/src/debug/jni

include $(BUILD_SHARED_LIBRARY)
