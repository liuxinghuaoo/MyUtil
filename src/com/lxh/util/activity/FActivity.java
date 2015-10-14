package com.lxh.util.activity;



import com.lxh.util.annotation.AnnotateUtil;
import com.lxh.util.iactivity.I_BroadcastReg;
import com.lxh.util.iactivity.I_LXHActivity;
import com.lxh.util.pullview.BottomBar;
import com.lxh.util.pullview.TitleBar;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
/**
 * LXH_Activity
 * @author arthur.liu
 *
 */
public abstract class FActivity extends Activity implements
OnClickListener, I_BroadcastReg, I_LXHActivity{

	public static final int WHICH_MSG = 0X37210;

	/** 全局的LayoutInflater对象，已经完成初始化. */
	public LayoutInflater mInflater;

	/** 全局的Application对象，已经完成初始化. */
	public Application abApplication = null;

	/** 全局的SharedPreferences对象，已经完成初始化. */
	public SharedPreferences abSharedPreferences = null;

	/**
	 * LinearLayout.LayoutParams，已经初始化为FILL_PARENT, FILL_PARENT
	 */
	public LinearLayout.LayoutParams layoutParamsFF = null;

	/**
	 * LinearLayout.LayoutParams，已经初始化为FILL_PARENT, WRAP_CONTENT
	 */
	public LinearLayout.LayoutParams layoutParamsFW = null;

	/**
	 * LinearLayout.LayoutParams，已经初始化为WRAP_CONTENT, FILL_PARENT
	 */
	public LinearLayout.LayoutParams layoutParamsWF = null;

	/**
	 * LinearLayout.LayoutParams，已经初始化为WRAP_CONTENT, WRAP_CONTENT
	 */
	public LinearLayout.LayoutParams layoutParamsWW = null;

	/** 总布局. */
	public RelativeLayout ab_base = null;

	/** 标题栏布局. */
	private TitleBar mAbTitleBar = null;

	/** 副标题栏布局. */
	private BottomBar mAbBottomBar = null;

	/** 主内容布局. */
	protected RelativeLayout contentLayout = null;

	/**
	 * 一个私有回调类，线程中初始化数据完成后的回调
	 */
	private interface ThreadDataCallBack {
		void onSuccess();
	}

	private static ThreadDataCallBack callback;

	// 当线程中初始化的数据初始化完成后，调用回调方法
	private static Handler threadHandle = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == WHICH_MSG) {
				callback.onSuccess();
			}
		};
	};

	/**
	 * 如果调用了initDataFromThread()，则当数据初始化完成后将回调该方法。
	 */
	protected void threadDataInited() {
	}

	/**
	 * 在线程中初始化数据，注意不能在这里执行UI操作
	 */
	@Override
	public void initDataFromThread() {
		callback = new ThreadDataCallBack() {
			@Override
			public void onSuccess() {
				threadDataInited();
			}
		};
	}

	@Override
	public void initData() {
	}

	@Override
	public void initWidget() {
	}

	// 仅仅是为了代码整洁点
	private void initializer() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				initDataFromThread();
				threadHandle.sendEmptyMessage(WHICH_MSG);
			}
		}).start();
		initData();
		initWidget();
	}

	/** listened widget's click method */
	@Override
	public void widgetClick(View v) {
	}

	@Override
	public void onClick(View v) {
		widgetClick(v);
	}

	@Override
	public void registerBroadcast() {
	}

	@Override
	public void unRegisterBroadcast() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInflater = LayoutInflater.from(this);
		layoutParamsFF = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layoutParamsFW = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParamsWF = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		layoutParamsWW = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		// 主标题栏
		mAbTitleBar = new TitleBar(this);
		// 最外层布局
		ab_base = new RelativeLayout(this);
		ab_base.setBackgroundColor(Color.rgb(255, 255, 255));
		// 内容布局
		contentLayout = new RelativeLayout(this);
		contentLayout.setPadding(0, 0, 0, 0);
		// 副标题栏
		mAbBottomBar = new BottomBar(this);

		// 填入View
		ab_base.addView(mAbTitleBar, layoutParamsFW);

		mAbTitleBar.setVisibility(View.GONE);

		RelativeLayout.LayoutParams layoutParamsBottomBar = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParamsBottomBar.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
				RelativeLayout.TRUE);
		ab_base.addView(mAbBottomBar, layoutParamsBottomBar);

		RelativeLayout.LayoutParams layoutParamsContent = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParamsContent.addRule(RelativeLayout.BELOW, mAbTitleBar.getId());
		layoutParamsContent.addRule(RelativeLayout.ABOVE, mAbBottomBar.getId());
		ab_base.addView(contentLayout, layoutParamsContent);
		// Application初始化
		abApplication = getApplication();
		// SharedPreferences初始化
		abSharedPreferences = getSharedPreferences("app_share",
				Context.MODE_PRIVATE);
		// 设置ContentView
		setContentView(ab_base, layoutParamsFF);
		setView(); // 必须放在annotate之前调用
	}

	/**
	 * 描述：用指定的View填充主界面.
	 * 
	 * @param contentView
	 *            指定的View
	 */
	public void setAbContentView(View contentView) {
		contentLayout.removeAllViews();
		contentLayout.addView(contentView, layoutParamsFF);
		AnnotateUtil.initBindView(this);
		initializer();
		registerBroadcast();
	}
	/**
	 * 描述：用指定资源ID表示的View填充主界面.
	 * @param resId  指定的View的资源ID
	 */
	public void setAbContentView(int resId) {
		setAbContentView(mInflater.inflate(resId, null));
		AnnotateUtil.initBindView(this);
		initializer();
		registerBroadcast();
	}
	
	/**
	 * 获取主标题栏布局.
	 * @return the title layout
	 */
	public TitleBar getTitleBar() {
		mAbTitleBar.setVisibility(View.VISIBLE);
		return mAbTitleBar;
	}
	
	/**
	 * 获取副标题栏布局.
	 * @return the bottom layout
	 */
	public BottomBar getBottomBar() {
		return mAbBottomBar;
	}
	@Override
	protected void onDestroy() {
		unRegisterBroadcast();
		super.onDestroy();
	}
	
	
}
