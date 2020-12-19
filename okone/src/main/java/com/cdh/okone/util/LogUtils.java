package com.cdh.okone.util;

import android.util.Log;


/**
 * LogUtils
 * @author business
 */
public final class LogUtils {

    private static final String GLOBAL_TAG = "okone";

    /**
     * 打包更新时间，通过查看log的update_time可以检查测试安装的包是不是最新包
     */
    private static final String MAKE_UPDATE_TIME = "1";

    private static final int INDEX = 4;

    public static boolean isEnabled = true;

    public static boolean isToastEnable;

    private LogUtils() {
    }

    public static void setEnableLog(boolean enable) {
        isEnabled = enable;
    }

    public static void v(String tag, String msg) {
        v(tag, msg, INDEX);
    }

    public static void v(String tag, String msg, int deep) {
        if (isEnabled) {
            Log.v(GLOBAL_TAG, formatMsg(tag, msg, deep));
        }
    }

    /**
     * 由于华为手机打印不出debug类型的日志，所以所有的Log.d的全部改为Log.i
     */
    public static void d(String tag, String msg, int deep) {
        if (isEnabled) {
            Log.d(GLOBAL_TAG, formatMsg(tag, msg, deep));
        }
    }

    public static void d(String tag, String msg) {
        d(tag, msg, INDEX);
    }


    public static void i(String tag, String msg) {
        i(tag, msg, INDEX);
    }

    public static void i(String tag, String msg, int deep) {
        if (isEnabled) {
            Log.i(GLOBAL_TAG, formatMsg(tag, msg, deep));
        }
    }

    public static void e(String tag, String msg) {
        e(tag, msg, INDEX);
    }

    public static void e(String tag, String msg, int deep) {
        if (isEnabled) {
            Log.e(GLOBAL_TAG, formatMsg(tag, msg, deep));
        }
    }

    public static void w(String tag, String msg) {
        w(tag, msg, INDEX);
    }

    public static void w(String tag, String msg, int deep) {
        if (isEnabled) {
            Log.w(GLOBAL_TAG, formatMsg(tag, msg, deep));
        }
    }

    public static void e(String subTag, String pMessage, final Throwable e) {
        e(subTag, pMessage, e, INDEX);
    }


    public static void e(String subTag, String pMessage, final Throwable e, int deep) {
        if (isEnabled) {
            if (pMessage == null) {
                pMessage = "noMsg";
            }

            if (e == null) {
                Log.e(GLOBAL_TAG, formatMsg(subTag, pMessage, deep));
            } else {
                Log.e(GLOBAL_TAG, formatMsg(subTag, pMessage, deep), e);
            }
        }
    }

    private static String formatMsg(String tag, String msg, int deep) {
        return String.format("%s[%s][%s][%s]%s%s", MAKE_UPDATE_TIME, android.os.Process.myPid(), Thread.currentThread(), tag, msg, getTrace(deep));
    }

    private static String getTrace(int index) {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        if (index <= 0) {
            index = INDEX;
        }
        if (stacks.length <= index) {
            return "";
        } else {
            return String.format("(%s:%d)", stacks[index].getFileName(), stacks[index].getLineNumber());
        }
    }

    public static void printStackTrace(Throwable t) {
        if (isEnabled && t != null) {
            t.printStackTrace();
            e(GLOBAL_TAG,  t.getMessage(), INDEX);
        }
    }

    public static void d(String tag, String msg, long time) {
        if (isEnabled) {
            Log.i(GLOBAL_TAG, formatMsg(tag, String.format("%s [t1=%d][t2=%d]", msg, time, (System.currentTimeMillis() - time)), INDEX));
        }
    }

}
