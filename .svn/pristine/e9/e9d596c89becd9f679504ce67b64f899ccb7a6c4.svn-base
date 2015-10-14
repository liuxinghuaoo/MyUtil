/*
 * Copyright (C) 2013  WhiteCat 白猫 (www.thinkandroid.cn)
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
package com.lxh.util.download;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.lxh.util.http.FileHttpResponseHandler;
import com.lxh.util.http.LXH_HttpClient;
import com.lxh.util.http.LXH_HttpResponseHandler;
import com.lxh.util.http.LXH_Logger;
import com.lxh.util.image.OtherUtils;

public class LXH_DownloadManager extends Thread
{

	private static final int MAX_handler_COUNT = 100;
	private static final int MAX_DOWNLOAD_THREAD_COUNT = 3;
	private handlerQueue mhandlerQueue;
	private List<LXH_HttpResponseHandler> mDownloadinghandlers;
	private List<LXH_HttpResponseHandler> mPausinghandlers;
	private LXH_HttpClient syncHttpClient;
	private Boolean isRunning = false;
	private static final String SDCARD_ROOT = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/";
	public static String FILE_ROOT =SDCARD_ROOT+ "download" + File.separator+ "cache_files" + File.separator;;
//	public static final String FILE_ROOT = SDCARD_ROOT+"tecentmobileDownload"+File.separator;
	private LXH_DownLoadCallback mDownLoadCallback;
	private String rootPath = "";
	private static LXH_DownloadManager downloadManager;

	/**
	 * 設置當前下載路徑，默認是download/cache_files
	 * @param context 當前上下文
	 * @param path 下載的文件路徑
	 * 
	 */
	public static void setPath(Context context,String path){
		if(!path.equals("")&&path != null){
			FILE_ROOT = OtherUtils.getDiskCacheDir(context, path);
		}
	}
	public static LXH_DownloadManager getDownloadManager()
	{
		if (downloadManager == null)
		{
			downloadManager = new LXH_DownloadManager(FILE_ROOT);
		}
		return downloadManager;
	}

	public static LXH_DownloadManager getDownloadManager(String rootPath)
	{

		if (downloadManager == null)
		{
			downloadManager = new LXH_DownloadManager(rootPath);
		}
		return downloadManager;
	}

	private LXH_DownloadManager()
	{
		this(FILE_ROOT);
	}

	private LXH_DownloadManager(String rootPath)
	{
		this.rootPath = rootPath;
		mhandlerQueue = new handlerQueue();
		mDownloadinghandlers = new ArrayList<LXH_HttpResponseHandler>();
		mPausinghandlers = new ArrayList<LXH_HttpResponseHandler>();
		syncHttpClient = new LXH_HttpClient();
		if (!LXH_StringUtils.isEmpty(rootPath))
		{
			File rootFile = new File(rootPath);
			if (!rootFile.exists())
			{
				rootFile.mkdir();
			}
		}
	}

	public String getRootPath()
	{
		if (LXH_StringUtils.isEmpty(rootPath))
		{
			rootPath = FILE_ROOT;
		}
		return rootPath;
	}

	public void setDownLoadCallback(LXH_DownLoadCallback downLoadCallback)
	{
		this.mDownLoadCallback = downLoadCallback;
	}

	public void startManage()
	{

		isRunning = true;
		this.start();
		if (mDownLoadCallback != null)
		{
			mDownLoadCallback.sendStartMessage();
		}
		// checkUncompletehandlers();
	}

	@SuppressWarnings("deprecation")
	public void close()
	{

		isRunning = false;
		pauseAllHandler();
		if (mDownLoadCallback != null)
		{
			mDownLoadCallback.sendStopMessage();
		}
		this.stop();
	}

	public boolean isRunning()
	{

		return isRunning;
	}

	@Override
	public void run()
	{

		super.run();
		while (isRunning)
		{
			FileHttpResponseHandler handler = (FileHttpResponseHandler) mhandlerQueue
					.poll();
			if (handler != null)
			{
				mDownloadinghandlers.add(handler);
				handler.setInterrupt(false);
				syncHttpClient.download(handler.getUrl(), handler);
			}
		}
	}

	public void addHandler(String url)
	{
		if (getTotalhandlerCount() >= MAX_handler_COUNT)
		{
			if (mDownLoadCallback != null)
			{
				mDownLoadCallback.sendFailureMessage(url, "任务列表已满");
			}
			return;
		}
		if (TextUtils.isEmpty(url) || hasHandler(url))
		{
			// 任务中存在这个任务,或者任务不满足要求
			return;
		}
		try
		{
			addHandler(newAsyncHttpResponseHandler(url));
		} catch (MalformedURLException e)
		{
			e.printStackTrace();
		}

	}

	private void addHandler(LXH_HttpResponseHandler handler)
	{
		broadcastAddHandler(((FileHttpResponseHandler) handler).getUrl());
		mhandlerQueue.offer(handler);

		if (!this.isAlive())
		{
			this.startManage();
		}
	}

	private void broadcastAddHandler(String url)

	{
		broadcastAddHandler(url, false);
	}

	private void broadcastAddHandler(String url, boolean isInterrupt)
	{

		if (mDownLoadCallback != null)
		{
			mDownLoadCallback.sendAddMessage(url, false);
		}

	}

	public void reBroadcastAddAllhandler()
	{

		FileHttpResponseHandler handler;
		for (int i = 0; i < mDownloadinghandlers.size(); i++)
		{
			handler = (FileHttpResponseHandler) mDownloadinghandlers.get(i);
			broadcastAddHandler(handler.getUrl(), handler.isInterrupt());
		}
		for (int i = 0; i < mhandlerQueue.size(); i++)
		{
			handler = (FileHttpResponseHandler) mhandlerQueue.get(i);
			broadcastAddHandler(handler.getUrl());
		}
		for (int i = 0; i < mPausinghandlers.size(); i++)
		{
			handler = (FileHttpResponseHandler) mPausinghandlers.get(i);
			broadcastAddHandler(handler.getUrl());
		}
	}

	public boolean hasHandler(String url)
	{

		FileHttpResponseHandler handler;
		for (int i = 0; i < mDownloadinghandlers.size(); i++)
		{
			handler = (FileHttpResponseHandler) mDownloadinghandlers.get(i);
			if (handler.getUrl().equals(url))
			{
				return true;
			}
		}
		for (int i = 0; i < mhandlerQueue.size(); i++)
		{
			handler = (FileHttpResponseHandler) mhandlerQueue.get(i);
			if (handler.getUrl().equals(url))
			{
				return true;
			}
		}
		return false;
	}

	public FileHttpResponseHandler getHandler(String url)
	{
		FileHttpResponseHandler handler = null;
		for (int i = 0; i < mDownloadinghandlers.size(); i++)
		{
			handler = (FileHttpResponseHandler) mDownloadinghandlers.get(i);

		}
		for (int i = 0; i < mhandlerQueue.size(); i++)
		{
			handler = (FileHttpResponseHandler) mhandlerQueue.get(i);
		}
		return handler;
	}

	public LXH_HttpResponseHandler gethandler(int position)
	{

		if (position >= mDownloadinghandlers.size())
		{
			return mhandlerQueue.get(position - mDownloadinghandlers.size());
		} else
		{
			return mDownloadinghandlers.get(position);
		}
	}

	public int getQueuehandlerCount()
	{

		return mhandlerQueue.size();
	}

	public int getDownloadinghandlerCount()
	{

		return mDownloadinghandlers.size();
	}

	public int getPausinghandlerCount()
	{

		return mPausinghandlers.size();
	}

	public int getTotalhandlerCount()
	{

		return getQueuehandlerCount() + getDownloadinghandlerCount()
				+ getPausinghandlerCount();
	}

	public void checkUncompletehandlers()
	{

		List<String> urlList = LXH_DownLoadConfigUtil.getURLArray();
		if (urlList.size() >= 0)
		{
			for (int i = 0; i < urlList.size(); i++)
			{
				addHandler(urlList.get(i));
			}
		}
	}

	public synchronized void pauseHandler(String url)
	{

		FileHttpResponseHandler handler;
		for (int i = 0; i < mDownloadinghandlers.size(); i++)
		{
			handler = (FileHttpResponseHandler) mDownloadinghandlers.get(i);
			if (handler != null && handler.getUrl().equals(url))
			{
				pausehandler(handler);
			}
		}
	}

	public synchronized void pauseAllHandler()
	{

		LXH_HttpResponseHandler handler;

		for (int i = 0; i < mhandlerQueue.size(); i++)
		{
			handler = mhandlerQueue.get(i);
			mhandlerQueue.remove(handler);
			mPausinghandlers.add(handler);
		}

		for (int i = 0; i < mDownloadinghandlers.size(); i++)
		{
			handler = mDownloadinghandlers.get(i);
			if (handler != null)
			{
				pausehandler(handler);
			}
		}
	}

	public synchronized void deleteHandler(String url)
	{

		FileHttpResponseHandler handler;
		for (int i = 0; i < mDownloadinghandlers.size(); i++)
		{
			handler = (FileHttpResponseHandler) mDownloadinghandlers.get(i);
			if (handler != null && handler.getUrl().equals(url))
			{
				File file = handler.getFile();
				if (file.exists())
					file.delete();
				File tempFile = handler.getTempFile();
				if (tempFile.exists())
				{
					tempFile.delete();
				}
				handler.setInterrupt(true);
				completehandler(handler);
				return;
			}
		}
		for (int i = 0; i < mhandlerQueue.size(); i++)
		{
			handler = (FileHttpResponseHandler) mhandlerQueue.get(i);
			if (handler != null && handler.getUrl().equals(url))
			{
				mhandlerQueue.remove(handler);
			}
		}
		for (int i = 0; i < mPausinghandlers.size(); i++)
		{
			handler = (FileHttpResponseHandler) mPausinghandlers.get(i);
			if (handler != null && handler.getUrl().equals(url))
			{
				mPausinghandlers.remove(handler);
			}
		}
	}

	public synchronized void continueHandler(String url)
	{

		FileHttpResponseHandler handler;
		for (int i = 0; i < mPausinghandlers.size(); i++)
		{
			handler = (FileHttpResponseHandler) mPausinghandlers.get(i);
			if (handler != null && handler.getUrl().equals(url))
			{
				continuehandler(handler);
			}

		}
	}

	public synchronized void pausehandler(LXH_HttpResponseHandler handler)
	{

		FileHttpResponseHandler fileHttpResponseHandler = (FileHttpResponseHandler) handler;
		if (handler != null)
		{
			fileHttpResponseHandler.setInterrupt(true);
			// move to pausing list
			String url = fileHttpResponseHandler.getUrl();
			try
			{
				mDownloadinghandlers.remove(handler);
				handler = newAsyncHttpResponseHandler(url);
				mPausinghandlers.add(handler);
			} catch (MalformedURLException e)
			{
				e.printStackTrace();
			}

		}
	}

	public synchronized void continuehandler(LXH_HttpResponseHandler handler)
	{

		if (handler != null)
		{
			mPausinghandlers.remove(handler);
			mhandlerQueue.offer(handler);
		}
	}

	public synchronized void completehandler(LXH_HttpResponseHandler handler)
	{

		if (mDownloadinghandlers.contains(handler))
		{
			LXH_DownLoadConfigUtil.clearURL(mDownloadinghandlers.indexOf(handler));
			mDownloadinghandlers.remove(handler);

			if (mDownLoadCallback != null)
			{
				mDownLoadCallback
						.sendFinishMessage(((FileHttpResponseHandler) handler)
								.getUrl());
			}
		}
	}

	private LXH_HttpResponseHandler newAsyncHttpResponseHandler(String url)
			throws MalformedURLException
	{
		FileHttpResponseHandler handler = new FileHttpResponseHandler(url,
				rootPath, LXH_StringUtils.getFileNameFromUrl(url))
		{

			@Override
			public void onProgress(long totalSize, long currentSize, long speed)
			{
				// TODO Auto-generated method stub
				super.onProgress(totalSize, currentSize, speed);
				if (mDownLoadCallback != null)
				{
					mDownLoadCallback.sendLoadMessage(this.getUrl(), totalSize,
							currentSize, speed);
				}
			}

			@Override
			public void onSuccess(String content)
			{
				// TODO Auto-generated method stub
				if (mDownLoadCallback != null)
				{
					mDownLoadCallback.sendSuccessMessage(this.getUrl());
				}
			}

			public void onFinish()
			{
				completehandler(this);
			}

			public void onStart()
			{
				LXH_DownLoadConfigUtil.storeURL(mDownloadinghandlers.indexOf(this),
						getUrl());
			}

			@Override
			public void onFailure(Throwable error)
			{
				// TODO Auto-generated method stub
				LXH_Logger.d(LXH_DownloadManager.this, "Throwable");
				if (error != null)
				{

					if (mDownLoadCallback != null)
					{
						mDownLoadCallback.sendFailureMessage(this.getUrl(),
								error.getMessage());
					}
				}
				// completehandler(this);
			}
		};

		return handler;
	}

	private class handlerQueue
	{
		private Queue<LXH_HttpResponseHandler> handlerQueue;

		public handlerQueue()
		{
			handlerQueue = new LinkedList<LXH_HttpResponseHandler>();
		}

		public void offer(LXH_HttpResponseHandler handler)
		{

			handlerQueue.offer(handler);
		}

		public LXH_HttpResponseHandler poll()
		{

			LXH_HttpResponseHandler handler = null;
			while (mDownloadinghandlers.size() >= MAX_DOWNLOAD_THREAD_COUNT
					|| (handler = handlerQueue.poll()) == null)
			{
				try
				{
					Thread.sleep(1000); // sleep
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			return handler;
		}

		public LXH_HttpResponseHandler get(int position)
		{

			if (position >= size())
			{
				return null;
			}
			return ((LinkedList<LXH_HttpResponseHandler>) handlerQueue)
					.get(position);
		}

		public int size()
		{

			return handlerQueue.size();
		}

		@SuppressWarnings("unused")
		public boolean remove(int position)
		{

			return handlerQueue.remove(get(position));
		}

		public boolean remove(LXH_HttpResponseHandler handler)
		{

			return handlerQueue.remove(handler);
		}
	}

}
