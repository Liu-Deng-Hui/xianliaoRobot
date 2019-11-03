package com.xianliaoRobot;

import android.app.Application;
import android.content.Context;

import com.arialyy.aria.core.Aria;


/**
 * Created by junwen on 2017/10/31.
 */

public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Aria.init(this);
        mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }
}
