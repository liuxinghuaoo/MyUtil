package com.lxh.util.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 上传文件到服务器
 * 
 * @author Administrator
 * 
 */
public class HttpFilePost {
	private static String sHttpFilePostUrl = "http://learningxrk.xicp.net:8081/interfaceapi";
	private String sUploadFileServerUrl = null;
	private String sUploadedReturnMsg = null;// 服务器上传文件返回信息

	/**
	 * 服务器上传文件返回信息
	 */
	public String getUploadedReturnMsg() {
		return sUploadedReturnMsg;
	}

	/**
	 * 设置后台上传路径
	 * 
	 * @param sUrl
	 */
	public static void setHttFileUploadUrl(String sUrl) {
		sHttpFilePostUrl = sUrl;
	}
	String lineEnd = System.getProperty("line.separator");	// The value is "\r\n" in Windows.
	String multipart_form_data = "multipart/form-data";
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
	 */
	public String post(String path, Map<String, String> params,
			FormFile[] files) throws Exception {
		final String BOUNDARY = "---------------------------7da2137580612"; // 数据分隔线
		final String endline = "--" + BOUNDARY + "--\r\n";// 数据结束标志

		int fileDataLength = 0;
		for (FormFile uploadFile : files) {// 得到文件类型数据的总长度
			StringBuilder fileExplain = new StringBuilder();
			fileExplain.append("--");
			fileExplain.append(BOUNDARY);
			fileExplain.append("\r\n");
			fileExplain.append("Content-Disposition: form-data;name=\""
					+ uploadFile.getParameterName() + "\";filename=\""
					+ uploadFile.getFilname() + "\"\r\n");
			fileExplain.append("Content-Type: " + uploadFile.getContentType()
					+ "\r\n\r\n");
			fileExplain.append("\r\n");
			fileDataLength += fileExplain.length();
			if (uploadFile.getInStream() != null) {
				fileDataLength += uploadFile.getFile().length();
			} else {
				fileDataLength += uploadFile.getData().length;
			}
		}
		StringBuilder textEntity = new StringBuilder();
		if (params != null)
			for (Map.Entry<String, String> entry : params.entrySet()) {// 构造文本类型参数的实体数据
				textEntity.append("--");
				textEntity.append(BOUNDARY);
				textEntity.append("\r\n");
				textEntity.append("Content-Disposition: form-data; name=\""
						+ entry.getKey() + "\"\r\n\r\n");
				textEntity.append(entry.getValue());
				textEntity.append("\r\n");
			}
		// 计算传输给服务器的实体数据总长度
		int dataLength = textEntity.toString().getBytes().length
				+ fileDataLength + endline.getBytes().length;

		URL url = new URL(path);
		int port = url.getPort() == -1 ? 80 : url.getPort();
//		Socket socket = new Socket(InetAddress.getByName(url.getHost()), port);
//		OutputStream outStream = socket.getOutputStream();
		HttpURLConnection conn = null;
		conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(120000);
		conn.setDoInput(true);		// 允许输入
		conn.setDoOutput(true);		// 允许输出
		conn.setUseCaches(false);	// 不使用Cache
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Content-Type", multipart_form_data + "; boundary=" + BOUNDARY);
		
		conn.connect();
		
		DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
		// 下面完成HTTP请求头的发送
		String requestmethod = "POST " + url.getPath() + " HTTP/1.1\r\n";
		outStream.write(requestmethod.getBytes());
		String accept = "Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*\r\n";
		outStream.write(accept.getBytes());
		String language = "Accept-Language: zh-CN\r\n";
		outStream.write(language.getBytes());
		String contenttype = "Content-Type: multipart/form-data; boundary="
				+ BOUNDARY + "\r\n";
		outStream.write(contenttype.getBytes());
		String contentlength = "Content-Length: " + dataLength + "\r\n";
		outStream.write(contentlength.getBytes());
		String alive = "Connection: Keep-Alive\r\n";
		outStream.write(alive.getBytes());
		String host = "Host: " + url.getHost() + ":" + port + "\r\n";
		outStream.write(host.getBytes());
		// 写完HTTP请求头后根据HTTP协议再写一个回车换行
		outStream.write("\r\n".getBytes());
		// 把所有文本类型的实体数据发送出来
		outStream.write(textEntity.toString().getBytes());
		// 把所有文件类型的实体数据发送出来
		for (FormFile uploadFile : files) {
			StringBuilder fileEntity = new StringBuilder();
			fileEntity.append("--");
			fileEntity.append(BOUNDARY);
			fileEntity.append("\r\n");
			fileEntity.append("Content-Disposition: form-data;name=\""
					+ uploadFile.getParameterName() + "\";filename=\""
					+ uploadFile.getFilname() + "\"\r\n");
			fileEntity.append("Content-Type: " + uploadFile.getContentType()
					+ "\r\n\r\n");
			outStream.write(fileEntity.toString().getBytes());
			if (uploadFile.getInStream() != null) {
				byte[] buffer = new byte[1024 * 3];
				int len = 0;
				while ((len = uploadFile.getInStream()
						.read(buffer, 0, 1024 * 3)) != -1) {
					outStream.write(buffer, 0, len);
				}
				uploadFile.getInStream().close();
			} else {
				outStream.write(uploadFile.getData(), 0,
						uploadFile.getData().length);
			}
			outStream.write("\r\n".getBytes());
		}
		// 下面发送数据结束标志，表示数据已经结束
		outStream.write(endline.getBytes());
		int code = conn.getResponseCode();
		if(code != 200) {
			return "操作失败";
		}
		
		BufferedReader	input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder response = new StringBuilder();
		String oneLine;
		while((oneLine = input.readLine()) != null) {
			response.append(oneLine + lineEnd);
		}
		System.out.println(response.toString());
//		input.close();
		
		
//		BufferedReader reader = new BufferedReader(new InputStreamReader(
//				socket.getInputStream()));
//		if (reader.readLine().indexOf("200") == -1) {// 读取web服务器返回的数据，判断请求码是否为200，如果不是200，代表请求失败
//			return "操作失败";
//		}
		
		
//		String line = null;
//		while ((line = reader.readLine()) != null) {
//			if (sUploadedReturnMsg == null)
//				sUploadedReturnMsg = line;
//			else
//				sUploadedReturnMsg += "\n" + line;
//			if (line.indexOf("$$") == 0 && sUploadFileServerUrl != null) {
//				sUploadFileServerUrl = line.substring(2);
//			}
//			// 获取地址
//			if (line.indexOf("$$") == 0) {
//				sUploadFileServerUrl = line.substring(2);
//			}
//		}
		outStream.flush();
		outStream.close();
//		reader.close();
//		socket.close();
		return response.toString();
	}

	/**
	 * 提交数据到服务器
	 * 
	 * @param path
	 *            上传路径(注：避免使用localhost或127.0.0.1这样的路径测试，因为它会指向手机模拟器，你可以使用http://
	 *            www.itcast.cn或http://192.168.1.10:8080这样的路径测试)
	 * @param params
	 *            请求参数 key为参数名,value为参数值
	 * @param file
	 *            上传文件
	 */
	public String post(String path, Map<String, String> params, FormFile file)
			throws Exception {
		return post(path, params, new FormFile[] { file });
	}

	public String post(FormFile file) throws Exception {
		return post(sHttpFilePostUrl, null, new FormFile[] { file });
	}

	public String post(String sUserId, FormFile file) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("user_id", sUserId);
		return post(sHttpFilePostUrl, params, new FormFile[] { file });
	}

	public String post(String path, Map<String, String> params)
			throws Exception {
		return post(path, params, new FormFile[] {});
	}

	/**
	 * 返回文件上传后的HTTP链接
	 * 
	 * @return
	 */
	public String getFileUploadServerUrl() {
		return this.sUploadFileServerUrl;
	}
}