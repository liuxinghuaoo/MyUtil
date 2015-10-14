package com.lxh.util.app;

import java.lang.reflect.Field;
import java.util.Stack;

import com.lxh.util.annotation.MyAnnotation;
import com.lxh.util.download.LXH_DownloadManager;
import com.lxh.util.download.LXH_IConfig;
import com.lxh.util.utils.MyLog;

import android.app.Activity;
import android.app.Application;
import android.view.View;

public class LXH_Application extends Application {

	private static LXH_Application application;
	/** 配置器 */
	private LXH_IConfig mCurrentConfig;
	/** 配置器 为Preference */
	public final static int PREFERENCECONFIG = 0;
	/** 配置器 为PROPERTIESCONFIG */
	public final static int PROPERTIESCONFIG = 1;

	private final static String TAIDENTITYCOMMAND = "TAIdentityCommand";
	public static Stack<Activity> activityStack;// activity栈，用于管理打开过的Activity

	@Override
	public void onCreate() {
		super.onCreate();
		this.application = this;
		LXH_DownloadManager.setPath(this.getApplicationContext(),
				"filedownload/");
	}

	public LXH_IConfig getConfig(int confingType) {
		if (confingType == PREFERENCECONFIG) {
			mCurrentConfig = LXH_PreferenceConfig.getPreferenceConfig(this);

		} else if (confingType == PROPERTIESCONFIG) {
			mCurrentConfig = LXH_PropertiesConfig.getPropertiesConfig(this);
		} else {
			mCurrentConfig = LXH_PropertiesConfig.getPropertiesConfig(this);
		}
		if (!mCurrentConfig.isLoadConfig()) {
			mCurrentConfig.loadConfig();
		}
		return mCurrentConfig;
	}

	/**
	 * 把一个activity压入栈中
	 * 
	 * @param actvity
	 */
	public void pushOneActivity(Activity actvity) {
		if (activityStack == null) {
			activityStack = new Stack<Activity>();
		}
		activityStack.add(actvity);
		MyLog.d("MyActivityManager ", "size = " + activityStack.size());
	}

	/**
	 * 移除一个activity
	 * 
	 * @param activity
	 */
	public void popOneActivity(Activity activity) {
		if (activityStack != null && activityStack.size() > 0) {
			if (activity != null) {
				activity.finish();
				activityStack.remove(activity);
				activity = null;
			}
		}
	}

	/**
	 * 退出所有activity
	 */
	public void finishAllActivity() {
		if (activityStack != null) {
			while (activityStack.size() > 0) {
				Activity activity = getLastActivity();
				if (activity == null)
					break;
				popOneActivity(activity);
			}
		}
	}

	// 获取栈顶的activity，先进后出原则
	public Activity getLastActivity() {
		return activityStack.lastElement();
	}

	/**
	 * 获取Application
	 * 
	 * @return
	 */
	public static LXH_Application getApplication() {
		return application != null ? application : new LXH_Application();
	}

	public LXH_IConfig getCurrentConfig() {
		if (mCurrentConfig == null) {
			getPreferenceConfig();
		}
		return mCurrentConfig;
	}

	public LXH_IConfig getPreferenceConfig() {
		return getConfig(PREFERENCECONFIG);
	}

	public LXH_IConfig getPropertiesConfig() {
		return getConfig(PROPERTIESCONFIG);
	}

}
