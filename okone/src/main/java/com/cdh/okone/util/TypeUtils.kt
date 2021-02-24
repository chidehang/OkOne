package com.cdh.okone.util

/**
 * Created by chidehang on 2021/2/24
 */
object TypeUtils {

    fun isPrimitiveOrString(obj: Any) : Boolean {
        return obj.javaClass.isPrimitive
                || obj is Int
                || obj is Long
                || obj is Float
                || obj is Double
                || obj is Byte
                || obj is Short
                || obj is Char
                || obj is Boolean
                || obj is String
    }
}