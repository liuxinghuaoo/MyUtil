package com.lxh.util.iactivity;

import android.view.View;

/**
 * 界面回调接口
 * @author arthur.liu
 *
 */
public interface I_LXHActivity {
	/** 设置root界面 */
	void setView();

	/** 初始化数据 */
	void initData();

	/** 在线程中初始化数据 */
	void initDataFromThread();

	/** 初始化控件 */
	void initWidget();

	/** 点击事件回调方法 */
	void widgetClick(View v);
}
