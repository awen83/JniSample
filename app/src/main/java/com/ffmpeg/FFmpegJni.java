package com.ffmpeg;

import android.graphics.Bitmap;

public class FFmpegJni {
    public static boolean forceShutdown;
    public static Object lockingObject;
    private static boolean opened = false;

    static {
        forceShutdown = false;
        lockingObject = new Object();
//        System.loadLibrary("ffmpeg");
        System.loadLibrary("ffmpegjni");
        _init();
    }

    public static int _getTargetHeight() {
        synchronized (lockingObject) {
            return getTargetHeight();
        }
    }

    public static int _getTargetWidth() {
        synchronized (lockingObject) {
            return getTargetWidth();
        }
    }

    public static void _init() {
        synchronized (lockingObject) {
            init();
            return;
        }
    }

    public static void _exit() {
        synchronized (lockingObject) {
            exit();
            opened = false;
            return;
        }


    }

    public static String _getVersion() {
        synchronized (lockingObject) {
            return getVersion();
        }
    }

    public static int _openFile(String path, int screenWidth, int screenHeight, int fixWH) {
        synchronized (lockingObject) {
            int ret = openFile(path, screenWidth, screenHeight, fixWH);
            if (ret >= 0) {
                opened = true;
            } else {
                exit();
            }
            return ret;
        }
    }

    public static int _drawFrame(Bitmap bitmap) {
        synchronized (lockingObject) {
            return drawFrame(bitmap);
        }
    }

    public static int _drawFrameAt(Bitmap bitmap, int sec) {
        synchronized (lockingObject) {
            return drawFrameAt(bitmap, sec);
        }
    }

    public static int _seekTo(int sec) {
        synchronized (lockingObject) {
            return seekTo(sec);
        }
    }

    public static float _getTimeBase() {
        synchronized (lockingObject) {
            return getTimeBase();
        }
    }

    public static boolean _isFileOpen() {
        synchronized (lockingObject) {
            return opened;
        }
    }

    private static native void init();

    private static native void exit();

    private static native int openFile(String path, int screenWidth, int screenHeight, int fixWH);

    private static native int drawFrame(Bitmap bitmap);

    private static native int drawFrameAt(Bitmap bitmap, int sec);

    private static native int seekTo(int sec);

    private static native int getTargetWidth();

    private static native int getTargetHeight();

    private static native String getVersion();

    private static native float getTimeBase();


}