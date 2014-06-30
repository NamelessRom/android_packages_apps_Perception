package com.google.android.hotword.client;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowId;

import com.google.android.hotword.service.IHotwordService;

public class HotwordServiceClient {
    private static final String TAG = "HotwordServiceClient";
    private static final boolean DBG = false;

    private static final String HOTWORD_SERVICE =
            "com.google.android.googlequicksearchbox.HOTWORD_SERVICE";
    private static final String VEL_PACKAGE = "com.google.android.googlequicksearchbox";

    private final Activity mActivity;
    private final ServiceConnection mConnection;
    private final WindowId.FocusObserver mFocusObserver;

    private IHotwordService mHotwordService;

    private boolean mHotwordStart;
    private boolean mIsAvailable = true;
    private boolean mIsBound;
    private boolean mIsFocused = false;
    private boolean mIsRequested = true;

    public HotwordServiceClient(Activity activity) {
        mActivity = activity;
        mConnection = new HotwordServiceConnection();
        mFocusObserver = new WindowFocusObserver();
    }

    private void assertMainThread() {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalStateException("Must be called on the main thread.");
        }
    }

    public final void onAttachedToWindow() {
        if (!mIsBound) {
            return;
        }

        assertMainThread();
        mActivity.getWindow().getDecorView().getWindowId().registerFocusObserver(mFocusObserver);
        internalBind();
    }

    public final void onDetachedFromWindow() {
        if (!mIsBound) {
            return;
        }

        assertMainThread();
        mActivity.getWindow().getDecorView().getWindowId().unregisterFocusObserver(mFocusObserver);
        mActivity.unbindService(mConnection);
        mIsBound = false;
    }

    public final void requestHotwordDetection(boolean detect) {
        assertMainThread();
        mIsRequested = detect;
        internalRequestHotword();
    }

    private void internalBind() {
        if (!mIsAvailable || mIsBound) {
            if (DBG && !mIsAvailable) Log.w(TAG, "Hotword service is not available.");
            return;
        }

        Intent bindIntent = new Intent(HOTWORD_SERVICE).setPackage(VEL_PACKAGE);
        mIsAvailable = mActivity.bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = mIsAvailable;
    }

    private void internalRequestHotword() {
        if (mIsFocused && mIsRequested) {
            if (!mHotwordStart) {
                mHotwordStart = true;
                if (!mIsBound) {
                    internalBind();
                }
            }
        }

        if (mHotwordService != null) {
            try {
                mHotwordService.requestHotwordDetection(mActivity.getPackageName(),
                        mIsFocused && mIsRequested);
            } catch (RemoteException e) {
                if (DBG) Log.w(TAG, "requestHotwordDetection - remote call failed", e);
            }
        }
    }

    private class HotwordServiceConnection implements ServiceConnection {
        private HotwordServiceConnection() {}

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mHotwordService = IHotwordService.Stub.asInterface(iBinder);
            internalRequestHotword();
        }

        public void onServiceDisconnected(ComponentName componentName) {
            mIsBound = false;
            mHotwordService = null;
        }
    }

    private class WindowFocusObserver extends WindowId.FocusObserver {
        private WindowFocusObserver() {}

        public void onFocusGained(WindowId wid) {
            mIsFocused = true;
            internalRequestHotword();
        }

        public void onFocusLost(WindowId wid) {
            mIsFocused = false;
            internalRequestHotword();
        }
    }
}
