#define LOG_TAG "TestJni_jni"


#include <android/log.h>
#include <stdio.h>
#include <jni.h>
#include <android/bitmap.h>
#include <assert.h>

#ifndef INT64_C
#define INT64_C(c) (c ## LL)
#define UINT64_C(c) (c ## ULL)
#endif


#include "Jnilogger.h"

extern "C" {
}

static uint8_t *buffer = NULL;
static int target_width = 320;
static int target_height = 240;
static struct SwsContext *img_convert_ctx = NULL;
static int videoStream = -1;
static uint8_t debug = 1;

/*
 * Class:     com_ffmpeg_FFmpegJni
 * Method:    init
 * Signature: ()V
 */
static void
Java_com_ffmpeg_FFmpegJni_init
        (JNIEnv *env, jclass thiz) {
    return;
}

/*
 * Class:     com_ffmpeg_FFmpegJni
 * Method:    getInt
 * Signature: ()I
 */
static jint
Java_com_ffmpeg_FFmpegJni_getTargetWidth
        (JNIEnv *env, jclass thiz) {
    return target_width;
}

/*
 * Class:     com_ffmpeg_FFmpegJni
 * Method:    getInt
 * Signature: ()I
 */
static jint
Java_com_ffmpeg_FFmpegJni_getTargetHeight
        (JNIEnv *env, jclass thiz) {
    return target_height;
}


/*
 * Class:     com_ffmpeg_FFmpegJni
 * Method:    getVersion
 * Signature: ()Ljava/lang/String;
 */
static jstring
Java_com_ffmpeg_FFmpegJni_getVersion
        (JNIEnv *env, jclass thiz) {
    char str[25];
    sprintf(str, "%u", 99);
    if (debug) LOGI(">>>>>>>>>>Java_com_ffmpeg_FFmpegJni_getVersion");
    return env->NewStringUTF(str);
}

/*
 * Class:     com_ffmpeg_FFmpegJni
 * Method:    getTimeBase
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL Java_com_ffmpeg_FFmpegJni_getTimeBase
        (JNIEnv *env, jclass thiz) {
    return -1;
}


/*
 * Class:     com_ffmpeg_FFmpegJni
 * Method:    openFile
 * Signature: (Ljava/lang/String;III)I
 */
static jint
Java_com_ffmpeg_FFmpegJni_openFile
        (JNIEnv *env, jclass thiz, jstring path, jint screenWidth, jint screenHeight, jint fixWH) {
    int ret;
    int err;
    int i;
    if (debug) LOGI("open file end");
    return 0;
}

/*
 * Class:     com_ffmpeg_FFmpegJni
 * Method:    drawFrame
 * Signature: (Landroid/graphics/Bitmap;)I
 */
static jint
Java_com_ffmpeg_FFmpegJni_drawFrame
        (JNIEnv *env, jclass thiz, jobject bitmap) {
    AndroidBitmapInfo info;
    void *pixels = NULL;
    int ret;

    int err;
    int i;
    int frameFinished;
    if (debug) LOGI("drawFrame end");
    return ret;
}

static int seek_frame(int tsms) {
    int64_t frame;
    int ret;
    if (debug) LOGI("seek return %i", ret);
    return 1;
}

/*
 * Class:     com_ffmpeg_FFmpegJni
 * Method:    seek_frame
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_ffmpeg_FFmpegJni_seekTo
        (JNIEnv *env, jclass thiz, jint secs) {
    return seek_frame(secs * 1000);
}

/*
 * Class:     com_ffmpeg_FFmpegJni
 * Method:    drawFrameAt
 * Signature: (Landroid/graphics/Bitmap;I)I
 */
static jint
Java_com_ffmpeg_FFmpegJni_drawFrameAt
        (JNIEnv *env, jclass thiz, jobject bitmap, jint secs) {
    AndroidBitmapInfo info;
    void *pixels = NULL;
    int ret;
    if (debug) LOGI("drawFrameAt end");
    return ret;
}

/*
 * Class:     com_ffmpeg_FFmpegJni
 * Method:    exit
 * Signature: ()V
 */
static void
Java_com_ffmpeg_FFmpegJni_exit
        (JNIEnv *env, jclass thiz) {
    if (debug) LOGI("jni exit");
}

// ----------------------------------------------------------------------------
static JNINativeMethod gMethods[] = {
        {"init",            "()V",                           (void *) Java_com_ffmpeg_FFmpegJni_init},
        {"exit",            "()V",                           (void *) Java_com_ffmpeg_FFmpegJni_exit},
        {"openFile",        "(Ljava/lang/String;III)I",      (void *) Java_com_ffmpeg_FFmpegJni_openFile},
        {"drawFrame",       "(Landroid/graphics/Bitmap;)I",  (void *) Java_com_ffmpeg_FFmpegJni_drawFrame},
        {"drawFrameAt",     "(Landroid/graphics/Bitmap;I)I", (void *) Java_com_ffmpeg_FFmpegJni_drawFrameAt},
        {"seekTo",          "(I)I",                          (void *) Java_com_ffmpeg_FFmpegJni_seekTo},
        {"getTargetWidth",  "()I",                           (void *) Java_com_ffmpeg_FFmpegJni_getTargetWidth},
        {"getTargetHeight", "()I",                           (void *) Java_com_ffmpeg_FFmpegJni_getTargetHeight},
        {"getVersion",      "()Ljava/lang/String;",          (void *) Java_com_ffmpeg_FFmpegJni_getVersion},
        {"getTimeBase",     "()F",                           (void *) Java_com_ffmpeg_FFmpegJni_getTimeBase},
};

static const char *const kClassPathName = "com/ffmpeg/FFmpegJni";

static int jniRegisterNativeMethods(JNIEnv *env, const char *className,
                                    const JNINativeMethod *gMethods, int numMethods) {
    jclass clazz;

    LOGI("Registering %s natives\n", className);
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGE("Native registration unable to find class '%s'\n", className);
        return -1;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        LOGE("RegisterNatives failed for '%s'\n", className);
        return -1;
    }
    return 0;
}


static int register_com_ffmpeg(JNIEnv *env) {
    return jniRegisterNativeMethods(env,
                                    kClassPathName, gMethods,
                                    sizeof(gMethods) / sizeof(gMethods[0]));
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    jint result = -1;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("ERROR: GetEnv failed!");
        goto bail;
    }

    assert(env != NULL);

    if (register_com_ffmpeg(env) < 0) {
        LOGE("ERROR: jni native registeration failed!");
        goto bail;
    }

    result = JNI_VERSION_1_4;

    bail:
    return result;
}

