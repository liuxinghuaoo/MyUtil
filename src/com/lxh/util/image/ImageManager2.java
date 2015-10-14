package com.lxh.util.image;

import com.lxh.util.utils.MyLog;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.widget.ImageView;

public class ImageManager2 {
	private static Context mContext;
	private static ImageManager2 imageManager;
	private String TAG = "IMAGE";
	public ImageManager2() {

	}

	public ImageManager2(Context context) {

	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	public static ImageManager2 from(Context context) {
		mContext=context;
		if (imageManager == null) {
			imageManager = new ImageManager2(context);
		}

		return imageManager;
	}
	
	/**
	 * ��ʾͼƬ
	 * 
	 * @param imageView
	 * @param url
	 * @param resId
	 */
	public void displayImage(ImageView imageView, String url, int resId) {
		MyLog.e(TAG, url);
		Picasso.with(mContext).load(url).placeholder(resId).into(imageView);
	}

	
	public void displayImage(ImageView imageView, String url, int resId,
			int width, int height) {
		Picasso.with(mContext).load(url).resize(width, height).placeholder(resId).into(imageView);
	        
	}
	
}
