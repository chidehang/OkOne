package com.cdh.okonedemo

import android.app.Application
import com.cdh.okone.OkOne

/**
 * Created by chidehang on 2020/12/12
 */
class DemoAPP : Application() {
    override fun onCreate() {
        super.onCreate()
        // 配置OkOne
        OkOneConfigur.config()
    }
}