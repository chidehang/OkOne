package com.cdh.okone.priority;

import com.cdh.okone.util.LogUtils;

import java.lang.reflect.Field;

import okhttp3.Request;

/**
 * 设置Call请求的优先级
 * Created by chidehang on 2021/1/17
 */
public class RequestPriorityProcessor {

    public static volatile boolean enableRequestPriority = false;

    public static final int PRIORITY_DEFAULT = 0;
    public static final int PRIORITY_MIN= -10;
    public static final int PRIORITY_MAX = 10;

    public static void setRequestPriority(Request request, int priority) {
        if (enableRequestPriority) {
            try {
                Field field = request.getClass().getDeclaredField("priority");
                field.setInt(request, checkBounds(priority));
            } catch (Throwable t) {
                LogUtils.printStackTrace(t);
            }
        }
    }

    public static int getRequestPriority(Request request) {
        if (enableRequestPriority) {
            try {
                Field field = request.getClass().getDeclaredField("priority");
                field.getInt(request);
            } catch (Throwable t) {
                LogUtils.printStackTrace(t);
            }
        }
        return PRIORITY_DEFAULT;
    }

    private static int checkBounds(int priority) {
        if (priority > PRIORITY_MAX) {
            return PRIORITY_MAX;
        } else if (priority < PRIORITY_MIN) {
            return PRIORITY_MIN;
        } else {
            return priority;
        }
    }
}
