package com.lxh.util.activity;


import com.lxh.util.app.LXHActivityStack;
import com.lxh.util.utils.MyLog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public abstract class LXH_Activity extends FActivity{

	private String TAG = "LXH_Activity";
    /**
     * 当前Activity状态
     */
    public static enum ActivityState {
        RESUME, PAUSE, STOP, DESTROY
    }

    public Activity aty;
    /** Activity状态 */
    public ActivityState activityState = ActivityState.DESTROY;

    /***************************************************************************
     * 
     * print Activity callback methods
     * 
     ***************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        aty = this;
        LXHActivityStack.create().addActivity(this);
        MyLog.d(TAG, "============执行onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
    	 MyLog.d(TAG, "============执行onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityState = ActivityState.RESUME;
        MyLog.d(TAG, "============执行onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityState = ActivityState.PAUSE;
        MyLog.d(TAG, "============执行onPause");
    }

    @Override
    protected void onStop() {
        super.onResume();
        activityState = ActivityState.STOP;
        MyLog.d(TAG, "============执行onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        MyLog.d(TAG, "============执行onRestart");
    }

    @Override
    protected void onDestroy() {
        activityState = ActivityState.DESTROY;
        super.onDestroy();
        MyLog.d(TAG, "============执行onDestroy");
        LXHActivityStack.create().finishActivity(this);
    }
}
