package com.lxh.util.pullview;

import com.lxh.util.pullview.AbPullToRefreshView.OnHeaderRefreshListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class AbListViewAlert extends LinearLayout {

	private Context mContext;
	private LinearLayout alertView;
	private ImageView imageView;
	private TextView textView;
	private OnHeaderRefreshListener monHeaderRefreshListener;

	public AbListViewAlert(Context context,
			OnHeaderRefreshListener onHeaderRefreshListener) {
		this(context, null, 0, onHeaderRefreshListener);
	}

	public AbListViewAlert(Context context, AttributeSet attrs,
			OnHeaderRefreshListener onHeaderRefreshListener) {
		this(context, null, 0, onHeaderRefreshListener);
	}

	@SuppressLint("NewApi")
	public AbListViewAlert(Context context, AttributeSet attrs, int defStyle,
			OnHeaderRefreshListener onHeaderRefreshListener) {
		super(context, attrs, defStyle);
		initView(context, onHeaderRefreshListener);
	}

	private void initView(Context context,
			OnHeaderRefreshListener onHeaderRefreshListener) {
		mContext = context;
		monHeaderRefreshListener = onHeaderRefreshListener;

		// 中间显示布局
		alertView = new LinearLayout(mContext);
		alertView.setOrientation(LinearLayout.VERTICAL);
		alertView.setGravity(Gravity.CENTER);

		imageView = new ImageView(mContext);
		textView = new TextView(mContext);
		textView.setGravity(Gravity.CENTER);
		textView.setText("无数据");

		LinearLayout.LayoutParams layoutParamsWW = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		alertView.addView(imageView, layoutParamsWW);
		layoutParamsWW.setMargins(0, 40, 0, 0);
		alertView.addView(textView, layoutParamsWW);
		
		alertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (monHeaderRefreshListener != null) {
					monHeaderRefreshListener.onHeaderRefresh(null);
				}
			}
		});

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		this.addView(alertView, layoutParams);

		// 获取View的高度
		AbViewUtil.measureView(this);
	}

	public ImageView getImageView() {
		return (ImageView) alertView.getChildAt(0);
	}

	public TextView getTextView() {
		return (TextView) alertView.getChildAt(1);
	}

	public OnHeaderRefreshListener getOnHeaderRefreshListener() {
		return monHeaderRefreshListener;
	}

	public void setOnHeaderRefreshListener(
			OnHeaderRefreshListener monHeaderRefreshListener) {
		this.monHeaderRefreshListener = monHeaderRefreshListener;
	}

}
