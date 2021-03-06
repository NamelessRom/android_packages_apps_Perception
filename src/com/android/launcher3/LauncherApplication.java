/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.HashMap;
import java.util.Map;

public class LauncherApplication extends Application {
    public static boolean LAUNCHER_SHOW_UNREAD_NUMBER;

    private static LauncherApplication sInstance;

    private String mStkAppName = new String();
    Map<String,String> mStkMsimNames = new HashMap<String, String>();
    private final String STK_PACKAGE_INTENT_ACTION_NAME =
            "org.codeaurora.carrier.ACTION_TELEPHONY_SEND_STK_TITLE";
    private final String STK_APP_NAME = "StkTitle";
    private final String STK_ACTIVITY_NAME = "StkActivity";


    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        LAUNCHER_SHOW_UNREAD_NUMBER = getResources().getBoolean(
                R.bool.config_launcher_show_unread_number);
        LauncherAppState.setApplicationContext(this);
        LauncherAppState.getInstance();
        if (getResources().getBoolean(R.bool.config_launcher_stkAppRename)) {
            registerAppNameChangeReceiver();
        }
    }

    private void registerAppNameChangeReceiver() {
        IntentFilter intentFilter = new IntentFilter(STK_PACKAGE_INTENT_ACTION_NAME);
        registerReceiver(appNameChangeReceiver, intentFilter);
    }

    /**
     * Receiver for STK Name change broadcast
     */
    private BroadcastReceiver appNameChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mStkAppName = intent.getStringExtra(STK_APP_NAME);
            if (intent.getStringExtra(STK_ACTIVITY_NAME) != null)
                mStkMsimNames.put(intent.getStringExtra(STK_ACTIVITY_NAME),mStkAppName);
        }
    };

    public String getStkAppName(String activityName){
        return mStkMsimNames.get(activityName) != null ? mStkMsimNames.get(activityName) : mStkAppName;
    }

    @Override
    public void onTerminate() {
        LauncherAppState.getInstance().onTerminate();
        super.onTerminate();
    }

    public static String getStr(final int resId) {
        return sInstance.getString(resId);
    }
}
