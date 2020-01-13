package com.beetech.card_detect;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.beetech.card_detect.di.DaggerAppComponent;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

public class BaseApplication extends Application implements HasActivityInjector {
    private static Context context;

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        DaggerAppComponent
                .builder()
                .application(this)
                .build()
                .inject(this);
    }

    public static Context getContext() {
        return context;
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }
}

