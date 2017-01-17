LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
PATH_TO_FFMPEG_SOURCE:=$(LOCAL_PATH)/ffmpeg
LOCAL_C_INCLUDES += $(PATH_TO_FFMPEG_SOURCE)
LOCAL_LDLIBS := -lffmpeg -llog -ljnigraphics
LOCAL_MODULE    := testjni
LOCAL_SRC_FILES := TestJni_zyw.cpp

include $(BUILD_SHARED_LIBRARY)