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
package com.lxh.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;
import android.content.Context;

public class LXH_HttpClient {
	private static final String VERSION = "1.1";
	/** 线程池维护线程的�?少数�? */
	private static final int DEFAULT_CORE_POOL_SIZE = 5;
	private static final int DEFAULT_MAXIMUM_POOL_SIZE = 10;
	/** 线程池维护线程所允许的空闲时�? */
	private static final int DEFAULT_KEEP_ALIVETIME = 0;
	/** http请求�?大并发连接数 */
	private static final int DEFAULT_MAX_CONNECTIONS = 10;
	/** 超时时间，默�?10�? */
	private static final int DEFAULT_SOCKET_TIMEOUT = 50 * 1000;
	/** 默认错误尝试次数 */
	private static final int DEFAULT_MAX_RETRIES = 5;
	/** 默认的套接字缓冲区大�? */
	private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String ENCODING_GZIP = "gzip";
	private static int maxConnections = DEFAULT_MAX_CONNECTIONS;
	private static int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
	private final DefaultHttpClient httpClient;
	private final HttpContext httpContext;
	private ThreadPoolExecutor threadPool;
	private final Map<Context, List<WeakReference<Future<?>>>> requestMap;
	private final Map<String, String> clientHeaderMap;
	public LXH_HttpClient() {
		BasicHttpParams httpParams = new BasicHttpParams();

		ConnManagerParams.setTimeout(httpParams, socketTimeout);
		ConnManagerParams.setMaxConnectionsPerRoute(httpParams,
				new ConnPerRouteBean(maxConnections));
		ConnManagerParams.setMaxTotalConnections(httpParams,
				DEFAULT_MAX_CONNECTIONS);

		HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);
		HttpConnectionParams.setConnectionTimeout(httpParams, socketTimeout);
		HttpConnectionParams.setTcpNoDelay(httpParams, true);
		HttpConnectionParams.setSocketBufferSize(httpParams,
				DEFAULT_SOCKET_BUFFER_SIZE);

		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUserAgent(httpParams, String.format(
				"thinkandroid/%s (http://www.thinkandroid.cn)", VERSION));

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(
				httpParams, schemeRegistry);

		httpContext = new SyncBasicHttpContext(new BasicHttpContext());
		httpClient = new DefaultHttpClient(cm, httpParams);
		httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
			@Override
			public void process(HttpRequest request, HttpContext context) {
				if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
					request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
				}
				for (String header : clientHeaderMap.keySet()) {
					request.addHeader(header, clientHeaderMap.get(header));
				}
			}
		});

		httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
			@Override
			public void process(HttpResponse response, HttpContext context) {
				final HttpEntity entity = response.getEntity();
				if (entity == null) {
					return;
				}
				final Header encoding = entity.getContentEncoding();
				if (encoding != null) {
					for (HeaderElement element : encoding.getElements()) {
						if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
							response.setEntity(new InflatingEntity(response
									.getEntity()));
							break;
						}
					}
				}
			}
		});

		httpClient.setHttpRequestRetryHandler(new RetryHandler(
				DEFAULT_MAX_RETRIES));

		threadPool = new ThreadPoolExecutor(DEFAULT_CORE_POOL_SIZE,
				DEFAULT_MAXIMUM_POOL_SIZE, DEFAULT_KEEP_ALIVETIME,
				TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),
				new ThreadPoolExecutor.CallerRunsPolicy());

		requestMap = new WeakHashMap<Context, List<WeakReference<Future<?>>>>();
		clientHeaderMap = new HashMap<String, String>();
	}

	/**
	 * Get the underlying HttpClient instance. This is useful for setting
	 * additional fine-grained settings for requests by accessing the client's
	 * ConnectionManager, HttpParams and SchemeRegistry.
	 */
	public HttpClient getHttpClient() {
		return this.httpClient;
	}

	/**
	 * Get the underlying HttpContext instance. This is useful for getting and
	 * setting fine-grained settings for requests by accessing the context's
	 * attributes such as the CookieStore.
	 */
	public HttpContext getHttpContext() {
		return this.httpContext;
	}

	/**
	 * Sets an optional CookieStore to use when making requests
	 * 
	 * @param cookieStore
	 *            The CookieStore implementation to use, usually an instance of
	 *            {@link PersistentCookieStore}
	 */
	public void setCookieStore(CookieStore cookieStore) {
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}

	/**
	 * Overrides the threadpool implementation used when queuing/pooling
	 * requests. By default, Executors.newCachedThreadPool() is used.
	 * 
	 * @param threadPool
	 *            an instance of {@link ThreadPoolExecutor} to use for
	 *            queuing/pooling requests.
	 */
	public void setThreadPool(ThreadPoolExecutor threadPool) {
		this.threadPool = threadPool;
	}

	/**
	 * Sets the User-Agent header to be sent with each request. By default,
	 * "Android Asynchronous Http Client/VERSION (http://loopj.com/android-async-http/)"
	 * is used.
	 * 
	 * @param userAgent
	 *            the string to use in the User-Agent header.
	 */
	public void setUserAgent(String userAgent) {
		HttpProtocolParams.setUserAgent(this.httpClient.getParams(), userAgent);
	}

	/**
	 * Sets the connection time oout. By default, 10 seconds
	 * 
	 * @param timeout
	 *            the connect/socket timeout in milliseconds * @param超时 connect
	 *            /套接字超时以毫秒为单�?
	 */
	public void setTimeout(int timeout) {
		final HttpParams httpParams = this.httpClient.getParams();
		ConnManagerParams.setTimeout(httpParams, timeout);
		HttpConnectionParams.setSoTimeout(httpParams, timeout);
		HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
	}

	/**
	 * Sets the SSLSocketFactory to user when making requests. By default, a
	 * new, default SSLSocketFactory is used.
	 * 
	 * @param sslSocketFactory
	 *            the socket factory to use for https requests.
	 *            �?,使用默认SSLSocketFactory�?
	 * 
	 * @param sslSocketFactory
	 *            套接字工厂使用https请求�?
	 */
	public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.httpClient.getConnectionManager().getSchemeRegistry()
				.register(new Scheme("https", sslSocketFactory, 443));
	}

	/**
	 * Sets headers that will be added to all requests this client makes (before
	 * sending).
	 * 
	 * @param header
	 *            the name of the header
	 * @param value
	 *            the contents of the header 标题的名�?
	 * @param价�?? 标题的内�?
	 */
	public void addHeader(String header, String value) {
		clientHeaderMap.put(header, value);
	}

	/**
	 * Sets basic authentication for the request. Uses AuthScope.ANY. This is
	 * the same as setBasicAuth('username','password',AuthScope.ANY)
	 * 
	 * @param username
	 * @param password
	 *            设置基本身份验证的请求�?�使用AuthScope.ANY。这�?
	 *            setBasicAuth�?�?(“用户名�?,“密码�??,AuthScope.ANY)
	 * @param用户�?
	 * @param密码
	 */
	public void setBasicAuth(String user, String pass) {
		AuthScope scope = AuthScope.ANY;
		setBasicAuth(user, pass, scope);
	}

	/**
	 * Sets basic authentication for the request. You should pass in your
	 * AuthScope for security. It should be like this
	 * setBasicAuth("username","password", new
	 * AuthScope("host",port,AuthScope.ANY_REALM))
	 * 
	 * @param username
	 * @param password
	 * @param scope
	 *            - an AuthScope object 设置基本身份验证的请求�?�你应该通过你的 AuthScope安全。它应该是这�?
	 *            setBasicAuth(“用户名”�?��?�密码�?��?�新
	 *            AuthScope(“主机�?��?�端口�?�AuthScope.ANY_REALM))
	 * 
	 * @param用户�?
	 * @param密码
	 * @param范围 —�?�一个AuthScope对象
	 */
	public void setBasicAuth(String user, String pass, AuthScope scope) {
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
				user, pass);
		this.httpClient.getCredentialsProvider().setCredentials(scope,
				credentials);
	}

	/**
	 * Cancels any pending (or potentially active) requests associated with the
	 * passed Context.
	 * <p>
	 * <b>Note:</b> This will only affect requests which were created with a
	 * non-null android Context. This method is intended to be used in the
	 * onDestroy method of your android activities to destroy all requests which
	 * are no longer required.
	 * 
	 * @param context
	 *            the android Context instance associated to the request.
	 * @param mayInterruptIfRunning
	 *            specifies if active requests should be cancelled along with
	 *            pending requests. / * * 取消任何悬�?�未决的(或�?�可能活�?)相关的请�? 通过上下文�??
	 *            < p >
	 *            < b >注意:< / b >这只会影响请求创建一�? 非空android上下文�?�这种方法旨在用�? onDestroy
	 *            android活动摧毁�?有请求的方法 不再是必�?的�??
	 * 
	 * @param上下�? android上下文实例相关的请求�?
	 * @param mayInterruptIfRunning
	 *            指定如果主动请求应该被取�? 挂起的请求�?? /
	 */
	public void cancelRequests(Context context, boolean mayInterruptIfRunning) {
		List<WeakReference<Future<?>>> requestList = requestMap.get(context);
		if (requestList != null) {
			for (WeakReference<Future<?>> requestRef : requestList) {
				Future<?> request = requestRef.get();
				if (request != null) {
					request.cancel(mayInterruptIfRunning);
				}
			}
		}
		requestMap.remove(context);
	}

	//
	// HTTP GET Requests
	//

	/**
	 * Perform a HTTP GET request, without any parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            发�?�请求的URL�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void get(String url, LXH_HttpResponseHandler responseHandler) {
		get(null, url, null, responseHandler);
	}

	/**
	 * Perform a HTTP GET request with parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional GET parameters to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP GET请求参数�?
	 * 
	 * @param url
	 *            发�?�请求的URL�?
	 * @param参数 附加参数发�?�的请求�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void get(String url, RequestParams params,
			LXH_HttpResponseHandler responseHandler) {
		get(null, url, params, responseHandler);
	}

	/**
	 * Perform a HTTP GET request without any parameters and track the Android
	 * Context which initiated the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP GET请求没有任何参数和跟踪Android 发起请求的上下文�?
	 * 
	 * @param上下�? Android发起请求的上下文�?
	 * @param url
	 *            发�?�请求的URL�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void get(Context context, String url,
			LXH_HttpResponseHandler responseHandler) {
		get(context, url, null, responseHandler);
	}

	/**
	 * Perform a HTTP GET request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional GET parameters to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行�?个HTTP GET请求,发起和跟踪Android上下�? 请求�?
	 * 
	 * @param上下�? Android发起请求的上下文�?
	 * @param url
	 *            发�?�请求的URL�?
	 * @param参数 附加参数发�?�的请求�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void get(Context context, String url, RequestParams params,
			LXH_HttpResponseHandler responseHandler) {
		sendRequest(context,httpClient, httpContext,
				new HttpGet(getUrlWithQueryString(url, params)), null,
				responseHandler, context);
	}

	//
	// HTTP download Requests
	//
	public void download(String url, LXH_HttpResponseHandler responseHandler) {
		download(null, url, null, responseHandler);
	}

	public void download(String url, RequestParams params,
			LXH_HttpResponseHandler responseHandler) {
		download(null, url, params, responseHandler);
	}

	/**
	 * Perform a HTTP GET request without any parameters and track the Android
	 * Context which initiated the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP GET请求没有任何参数和跟踪Android 发起请求的上下文�?
	 * 
	 * @param上下�? Android发起请求的上下文�?
	 * @param url
	 *            发�?�请求的URL�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void download(Context context, String url,
			LXH_HttpResponseHandler responseHandler) {
		download(context, url, null, responseHandler);
	}

	/**
	 * Perform a HTTP GET request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional GET parameters to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行�?个HTTP GET请求,发起和跟踪Android上下�? 请求�?
	 * 
	 * @param上下�? Android发起请求的上下文�?
	 * @param url
	 *            发�?�请求的URL�?
	 * @param参数 附加参数发�?�的请求�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void download(Context context, String url, RequestParams params,
			LXH_HttpResponseHandler responseHandler) {
		sendRequest(context,httpClient, httpContext,
				new HttpGet(getUrlWithQueryString(url, params)), null,
				responseHandler, context);
	}

	/**
	 * Perform a HTTP GET request and track the Android Context which initiated
	 * the request with customized headers
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param headers
	 *            set headers only for this request
	 * @param params
	 *            additional GET parameters to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行�?个HTTP GET请求,发起和跟踪Android上下�? 请求与定制的标题
	 * 
	 * @param url
	 *            发�?�请求的URL�?
	 * @param�? 设置标题仅为这个请求
	 * @param参数 附加参数发�?�的请求�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void get(Context context, String url, Header[] headers,
			RequestParams params, LXH_HttpResponseHandler responseHandler) {
		HttpUriRequest request = new HttpGet(getUrlWithQueryString(url, params));
		if (headers != null)
			request.setHeaders(headers);
		sendRequest(context,httpClient, httpContext, request, null, responseHandler,
				context);
	}

	//
	// HTTP POST Requests
	//

	/**
	 * Perform a HTTP POST request, without any parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP POST请求,没有任何参数�?
	 * 
	 * @param url
	 *            发�?�请求的URL�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void post(String url, LXH_HttpResponseHandler responseHandler) {
		post(null, url, null, responseHandler);
	}

	/**
	 * Perform a HTTP POST request with parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional POST parameters or files to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP POST请求参数�?
	 * 
	 * @param url
	 *            发�?�请求的URL�?
	 * @param参数 附加POST参数或文件发送请求�??
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void post(String url, RequestParams params,
			LXH_HttpResponseHandler responseHandler) {
		post(null, url, params, responseHandler);
	}

	/**
	 * Perform a HTTP POST request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional POST parameters or files to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP POST请求和跟踪发起的Android上下�? 请求�?
	 * 
	 * @param上下�? Android发起请求的上下文�?
	 * @param url
	 *            发�?�请求的URL�?
	 * @param参数 附加POST参数或文件发送请求�??
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void post(Context context, String url, RequestParams params,
			LXH_HttpResponseHandler responseHandler) {
		post(context, url, paramsToEntity(params), null, responseHandler);
	}

	/**
	 * Perform a HTTP POST request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param entity
	 *            a raw {@link HttpEntity} to send with the request, for
	 *            example, use this to send string/json/xml payloads to a server
	 *            by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType
	 *            the content type of the payload you are sending, for example
	 *            application/json if sending a json payload.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP POST请求和跟踪发起的Android上下�? 请求�?
	 * 
	 * @param上下�? Android发起请求的上下文�?
	 * @param url
	 *            发�?�请求的URL�?
	 * @param实体 生{ @link HttpEntity }发�?�请�?, 示例�?,使用这个字符�?/ json / xml有效负载发�?�到服务�? 通过{ @link
	 *          org.apache.http.entity.StringEntity }�?
	 * @param contentType
	 *            您发送负载的内容类型,例如 如果发�?�json负载* application / json�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void post(Context context, String url, HttpEntity entity,
			String contentType, LXH_HttpResponseHandler responseHandler) {
		sendRequest(context,httpClient, httpContext,
				addEntityToRequestBase(new HttpPost(url), entity), contentType,
				responseHandler, context);
	}

	/**
	 * Perform a HTTP POST request and track the Android Context which initiated
	 * the request. Set headers only for this request
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param headers
	 *            set headers only for this request
	 * @param params
	 *            additional POST parameters to send with the request.
	 * @param contentType
	 *            the content type of the payload you are sending, for example
	 *            application/json if sending a json payload.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP POST请求和跟踪发起的Android上下�? 请求。设置标题仅为这个请�?
	 * 
	 * @param上下�? Android发起请求的上下文�?
	 * @param url
	 *            发�?�请求的URL�?
	 * @param�? 设置标题仅为这个请求
	 * @param参数 附加POST参数发�?�的请求�?
	 * @param contentType
	 *            您发送负载的内容类型,例如 如果发�?�json负载* application / json�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void post(Context context, String url, Header[] headers,
			RequestParams params, String contentType,
			LXH_HttpResponseHandler responseHandler) {
		HttpEntityEnclosingRequestBase request = new HttpPost(url);
		if (params != null)
			request.setEntity(paramsToEntity(params));
		if (headers != null)
			request.setHeaders(headers);
		sendRequest(context,httpClient, httpContext, request, contentType,
				responseHandler, context);
	}

	/**
	 * Perform a HTTP POST request and track the Android Context which initiated
	 * the request. Set headers only for this request
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param headers
	 *            set headers only for this request
	 * @param entity
	 *            a raw {@link HttpEntity} to send with the request, for
	 *            example, use this to send string/json/xml payloads to a server
	 *            by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType
	 *            the content type of the payload you are sending, for example
	 *            application/json if sending a json payload.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP POST请求和跟踪发起的Android上下�? 请求。设置标题仅为这个请�?
	 * 
	 * @param上下�? Android发起请求的上下文�?
	 * @param url
	 *            发�?�请求的URL�?
	 * @param�? 设置标题仅为这个请求
	 * @param实体 生{ @link HttpEntity }发�?�请�?, 示例�?,使用这个字符�?/ json / xml有效负载发�?�到服务�? 通过{ @link
	 *          org.apache.http.entity.StringEntity }�?
	 * @param contentType
	 *            您发送负载的内容类型,例如 如果发�?�json负载* application / json�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void post(Context context, String url, Header[] headers,
			HttpEntity entity, String contentType,
			LXH_HttpResponseHandler responseHandler) {
		HttpEntityEnclosingRequestBase request = addEntityToRequestBase(
				new HttpPost(url), entity);
		if (headers != null)
			request.setHeaders(headers);
		sendRequest(context,httpClient, httpContext, request, contentType,
				responseHandler, context);
	}

	//
	// HTTP PUT Requests
	//

	/**
	 * Perform a HTTP PUT request, without any parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP PUT请求,没有任何参数�?
	 * 
	 * @param url
	 *            发�?�请求的URL�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void put(String url, LXH_HttpResponseHandler responseHandler) {
		put(null, url, null, responseHandler);
	}

	/**
	 * Perform a HTTP PUT request with parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional PUT parameters or files to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP PUT请求参数�?
	 * 
	 * @param url
	 *            发�?�请求的URL�?
	 * @param参数 附加参数或文件发送的请求�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void put(String url, RequestParams params,
			LXH_HttpResponseHandler responseHandler) {
		put(null, url, params, responseHandler);
	}

	/**
	 * Perform a HTTP PUT request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional PUT parameters or files to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP PUT请求和跟踪发起的Android上下�? 请求�?
	 * 
	 * @param上下�? Android发起请求的上下文�?
	 * @param url
	 *            发�?�请求的URL�?
	 * @param参数 附加参数或文件发送的请求�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 * 
	 */
	public void put(Context context, String url, RequestParams params,
			LXH_HttpResponseHandler responseHandler) {
		put(context, url, paramsToEntity(params), null, responseHandler);
	}

	/**
	 * Perform a HTTP PUT request and track the Android Context which initiated
	 * the request. And set one-time headers for the request
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param entity
	 *            a raw {@link HttpEntity} to send with the request, for
	 *            example, use this to send string/json/xml payloads to a server
	 *            by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType
	 *            the content type of the payload you are sending, for example
	 *            application/json if sending a json payload.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP PUT请求和跟踪发起的Android上下�? 请求。并设置为请求一次�?�头
	 * 
	 * @param上下�? Android发起请求的上下文�?
	 * @param url
	 *            发�?�请求的URL�?
	 * @param实体 生{ @link HttpEntity }发�?�请�?, 示例�?,使用这个字符�?/ json / xml有效负载发�?�到服务�? 通过{ @link
	 *          org.apache.http.entity.StringEntity }�?
	 * @param contentType
	 *            您发送负载的内容类型,例如 如果发�?�json负载* application / json�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void put(Context context, String url, HttpEntity entity,
			String contentType, LXH_HttpResponseHandler responseHandler) {
		sendRequest(context,httpClient, httpContext,
				addEntityToRequestBase(new HttpPut(url), entity), contentType,
				responseHandler, context);
	}

	/**
	 * Perform a HTTP PUT request and track the Android Context which initiated
	 * the request. And set one-time headers for the request
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param headers
	 *            set one-time headers for this request
	 * @param entity
	 *            a raw {@link HttpEntity} to send with the request, for
	 *            example, use this to send string/json/xml payloads to a server
	 *            by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType
	 *            the content type of the payload you are sending, for example
	 *            application/json if sending a json payload.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP PUT请求和跟踪发起的Android上下�? 请求。并设置为请求一次�?�头
	 * 
	 * @param上下�? Android发起请求的上下文�?
	 * @param url
	 *            发�?�请求的URL�?
	 * @param�? 设置�?次�?�为这个请求�?
	 * @param实体 生{ @link HttpEntity }发�?�请�?, 示例�?,使用这个字符�?/ json / xml有效负载发�?�到服务�? 通过{ @link
	 *          org.apache.http.entity.StringEntity }�?
	 * @param contentType
	 *            您发送负载的内容类型,例如 如果发�?�json负载* application / json�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void put(Context context, String url, Header[] headers,
			HttpEntity entity, String contentType,
			LXH_HttpResponseHandler responseHandler) {
		HttpEntityEnclosingRequestBase request = addEntityToRequestBase(
				new HttpPut(url), entity);
		if (headers != null)
			request.setHeaders(headers);
		sendRequest(context,httpClient, httpContext, request, contentType,
				responseHandler, context);
	}

	//
	// HTTP DELETE Requests
	//

	/**
	 * Perform a HTTP DELETE request.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP DELETE请求�?
	 * 
	 * @param url
	 *            发�?�请求的URL�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 */
	public void delete(String url, LXH_HttpResponseHandler responseHandler) {
		delete(null, url, responseHandler);
	}

	/**
	 * Perform a HTTP DELETE request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP DELETE请求�?
	 * 
	 * @param上下�? Android发起请求的上下文�?
	 * @param url
	 *            发�?�请求的URL�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 * 
	 */
	public void delete(Context context, String url,
			LXH_HttpResponseHandler responseHandler) {
		final HttpDelete delete = new HttpDelete(url);
		sendRequest(context,httpClient, httpContext, delete, null, responseHandler,
				context);
	}

	/**
	 * Perform a HTTP DELETE request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param headers
	 *            set one-time headers for this request
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 *            执行HTTP DELETE请求�?
	 * 
	 * @param上下�? Android发起请求的上下文�?
	 * @param url
	 *            发�?�请求的URL�?
	 * @param�? 设置�?次�?�为这个请求�?
	 * @param responseHandler
	 *            应该处理响应的响应处理程序实例�??
	 * 
	 */
	public void delete(Context context, String url, Header[] headers,
			LXH_HttpResponseHandler responseHandler) {
		final HttpDelete delete = new HttpDelete(url);
		if (headers != null)
			delete.setHeaders(headers);
		sendRequest(context,httpClient, httpContext, delete, null, responseHandler,
				context);
	}

	// Private stuff
	protected void sendRequest(Context mContext,DefaultHttpClient client,
			HttpContext httpContext, HttpUriRequest uriRequest,
			String contentType, LXH_HttpResponseHandler responseHandler,
			Context context) {
		if (contentType != null) {
			uriRequest.addHeader("Content-Type", contentType);
		}

		Future<?> request = threadPool.submit(new LXH_HttpRequest(context,client,
				httpContext, uriRequest, responseHandler));
		if (context != null) {
			// Add request to request map
			List<WeakReference<Future<?>>> requestList = requestMap
					.get(context);
			if (requestList == null) {
				requestList = new LinkedList<WeakReference<Future<?>>>();
				requestMap.put(context, requestList);
			}
			requestList.add(new WeakReference<Future<?>>(request));
			// TODO: Remove dead weakrefs from requestLists?
		}

	}

	public static String getUrlWithQueryString(String url, RequestParams params) {
		if (params != null) {
			String paramString = params.getParamString();
			if (url.indexOf("?") == -1) {
				url += "?" + paramString;
			} else {
				url += "&" + paramString;
			}
		}

		return url;
	}

	private HttpEntity paramsToEntity(RequestParams params) {
		HttpEntity entity = null;

		if (params != null) {
			entity = params.getEntity();
		}

		return entity;
	}

	private HttpEntityEnclosingRequestBase addEntityToRequestBase(
			HttpEntityEnclosingRequestBase requestBase, HttpEntity entity) {
		if (entity != null) {
			requestBase.setEntity(entity);
		}

		return requestBase;
	}

	private static class InflatingEntity extends HttpEntityWrapper {
		public InflatingEntity(HttpEntity wrapped) {
			super(wrapped);
		}

		@Override
		public InputStream getContent() throws IOException {
			return new GZIPInputStream(wrappedEntity.getContent());
		}

		@Override
		public long getContentLength() {
			return -1;
		}
	}
	/**
	 * 直接通过HTTP协议提交数据到服务器,实现如下面表单提交功能: <FORM METHOD=POST
	 * ACTION="http://192.168.1.101:8083/upload/servlet/UploadServlet"
	 * enctype="multipart/form-data"> <INPUT TYPE="text" NAME="name"> <INPUT
	 * TYPE="text" NAME="id"> <input type="file" name="imagefile"/> <input
	 * type="file" name="zip"/> </FORM>
	 * 
	 * @param path
	 *            上传路径(注：避免使用localhost或127.0.0.1这样的路径测试，因为它会指向手机模拟器，你可以使用http://
	 *            www.iteye.cn或http://192.168.1.101:8083这样的路径测试)
	 * @param params
	 *            请求参数 key为参数名,value为参数值
	 * @param file
	 *            上传文件
	 * @throws Exception 
	 * 
	 * @url 请求地址
	 * @map 请求的参数，一键值对出现
	 * @formfileArr 文件数据封装类
	 * @ 使用方法可以仿照Demo参考
	 */
	public String SetFilesPost(String url,HashMap<String, String> map,FormFile formfileArr[]) throws Exception{
		HttpFilePost hf = new HttpFilePost();
		hf.setHttFileUploadUrl(url);
		return hf.post(url, map, formfileArr);
	}
}
