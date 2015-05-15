package com.willme.appmanager;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Wen on 5/14/15.
 */
public class ManagerApp extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
