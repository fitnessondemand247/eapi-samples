package com.sample.eapi;

import android.app.Application;

public class EapiApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        new GoogleAnalyticsTracker(this);
    }
}
