package com.cdh.okonedemo;

import android.app.Application;

import com.cdh.okone.OkOne;

/**
 * Created by chidehang on 2020/12/12
 */
public class DemoAPP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        OkOne.setLogEnable(true);
    }
}
