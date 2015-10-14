package com.lxh.util.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;



import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;

public class FileUtil {
	private static String TAG = "FileUtil";

    /**
     * 描述：读取Raw目录的文件内容.
     *
     * @param context the context
     * @param id the id
     * @param encoding the encoding
     * @return the string
     */
    public static String readRawByName(Context context,int id,String encoding){
    	String text = null;
    	InputStreamReader inputReader = null;
    	BufferedReader bufReader = null;
        try {
			inputReader = new InputStreamReader(context.getResources().openRawResource(id));
			bufReader = new BufferedReader(inputReader);
			String line = null;
			StringBuffer buffer = new StringBuffer();
			while((line = bufReader.readLine()) != null){
				 buffer.append(line);
			}
            text = new String(buffer.toString().getBytes(),encoding);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(bufReader!=null){
					bufReader.close();
				}
				if(inputReader!=null){
					inputReader.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        return text;
    }

    /**
     * 描述：读取Assets目录的文件内容.
     *
     * @param context the context
     * @param name the name
     * @param encoding the encoding
     * @return the string
     */
    public static String readAssetsByName(Context context,String name,String encoding){
    	String text = null;
    	InputStreamReader inputReader = null;
    	BufferedReader bufReader = null;
    	try {  
    		 inputReader = new InputStreamReader(context.getAssets().open(name));
    		 bufReader = new BufferedReader(inputReader);
    		 String line = null;
    		 StringBuffer buffer = new StringBuffer();
    		 while((line = bufReader.readLine()) != null){
    			 buffer.append(line);
    		 }
    		 text = new String(buffer.toString().getBytes(), encoding);
         } catch (Exception e) {  
        	 e.printStackTrace();
         } finally{
			try {
				if(bufReader!=null){
					bufReader.close();
				}
				if(inputReader!=null){
					inputReader.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    	return text;
    }
    /**
	 * 描述：获取src中的图片资源.
	 *
	 * @param src 图片的src路径，如（“image/arrow.png”）
	 * @return Bitmap 图片
	 */
	public static Bitmap getBitmapFromSrc(String src){
		Bitmap bit = null;
		try {
			bit = BitmapFactory.decodeStream(FileUtil.class.getResourceAsStream(src));
	    } catch (Exception e) {
	    	MyLog.d("", "获取图片异常："+e.getMessage());
		}
		return bit;
	}
	
	/**
	 * 描述：获取Asset中的图片资源.
	 *
	 * @param  fileName 图片的名称
	 * @return Bitmap 图片
	 */
	public static Bitmap getBitmapFromAsset(Context context,String fileName){
		Bitmap bit = null;
		try {
			AssetManager assetManager = context.getAssets();
			InputStream is = assetManager.open(fileName);
			bit = BitmapFactory.decodeStream(is);
	    } catch (Exception e) {
	    	MyLog.d(TAG, "获取图片异常："+e.getMessage());
		}
		return bit;
	}
	
	/**
	 * 描述：获取Asset中的图片资源.
	 *
	 * @param  fileName 图片的名称
	 * @return Drawable 图片
	 */
	public static Drawable getDrawableFromAsset(Context context,String fileName){
		Drawable drawable = null;
		try {
			AssetManager assetManager = context.getAssets();
			InputStream is = assetManager.open(fileName);
			drawable = Drawable.createFromStream(is,null);
	    } catch (Exception e) {
	    	MyLog.d(TAG, "获取图片异常："+e.getMessage());
		}
		return drawable;
	}
	
	
	
	/**
	 * 描述：获取网络文件的大小.
	 *
	 * @param Url 图片的网络路径
	 * @return int 网络文件的大小
	 */
	public static int getContentLengthFromUrl(String Url){
		int mContentLength = 0;
		try {
			 URL url = new URL(Url);
			 HttpURLConnection mHttpURLConnection = (HttpURLConnection) url.openConnection();
			 mHttpURLConnection.setConnectTimeout(5 * 1000);
			 mHttpURLConnection.setRequestMethod("GET");
			 mHttpURLConnection.setRequestProperty("Accept","image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			 mHttpURLConnection.setRequestProperty("Accept-Language", "zh-CN");
			 mHttpURLConnection.setRequestProperty("Referer", Url);
			 mHttpURLConnection.setRequestProperty("Charset", "UTF-8");
			 mHttpURLConnection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			 mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive");
			 mHttpURLConnection.connect();
			 if (mHttpURLConnection.getResponseCode() == 200){
				 // 根据响应获取文件大小
				 mContentLength = mHttpURLConnection.getContentLength();
			 }
	    } catch (Exception e) {
	    	 e.printStackTrace();
	    	 MyLog.d(TAG, "获取长度异常："+e.getMessage());
		}
		return mContentLength;
	}
	
	/**
     * 获取文件名，通过网络获取.
     * @param url 文件地址
     * @return 文件名
     */
    public static String getRealFileNameFromUrl(String url){
        String name = null;
        try {
            if(StrUtil.isEmpty(url)){
                return name;
            }
            
            URL mUrl = new URL(url);
            HttpURLConnection mHttpURLConnection = (HttpURLConnection) mUrl.openConnection();
            mHttpURLConnection.setConnectTimeout(5 * 1000);
            mHttpURLConnection.setRequestMethod("GET");
            mHttpURLConnection.setRequestProperty("Accept","image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            mHttpURLConnection.setRequestProperty("Accept-Language", "zh-CN");
            mHttpURLConnection.setRequestProperty("Referer", url);
            mHttpURLConnection.setRequestProperty("Charset", "UTF-8");
            mHttpURLConnection.setRequestProperty("User-Agent","");
            mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            mHttpURLConnection.connect();
            if (mHttpURLConnection.getResponseCode() == 200){
                for (int i = 0;; i++) {
                        String mine = mHttpURLConnection.getHeaderField(i);
                        if (mine == null){
                            break;
                        }
                        if ("content-disposition".equals(mHttpURLConnection.getHeaderFieldKey(i).toLowerCase())) {
                            Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
                            if (m.find())
                                return m.group(1).replace("\"", "");
                        }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            MyLog.d(TAG, "网络上获取文件名失败");
        }
        return name;
    }
	
	 
	/**
	 * 获取真实文件名（xx.后缀），通过网络获取.
	 * @param connection 连接
	 * @return 文件名
	 */
	public static String getRealFileName(HttpURLConnection connection){
		String name = null;
		try {
			if(connection == null){
				return name;
			}
			if (connection.getResponseCode() == 200){
				for (int i = 0;; i++) {
						String mime = connection.getHeaderField(i);
						if (mime == null){
							break;
						}
						// "Content-Disposition","attachment; filename=1.txt"
						// Content-Length
						if ("content-disposition".equals(connection.getHeaderFieldKey(i).toLowerCase())) {
							Matcher m = Pattern.compile(".*filename=(.*)").matcher(mime.toLowerCase());
							if (m.find()){
								return m.group(1).replace("\"", "");
							}
						}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MyLog.d(TAG, "网络上获取文件名失败");
		}
		return name;
    }
	
	/**
	 * 获取真实文件名（xx.后缀），通过网络获取.
	 *
	 * @param response the response
	 * @return 文件名
	 */
    public static String getRealFileName(HttpResponse response){
        String name = null;
        try {
            if(response == null){
                return name;
            }
            //获取文件名
            Header[] headers = response.getHeaders("content-disposition");
            for(int i=0;i<headers.length;i++){
                 Matcher m = Pattern.compile(".*filename=(.*)").matcher(headers[i].getValue());
                 if (m.find()){
                     name =  m.group(1).replace("\"", "");
                 }
            }
        } catch (Exception e) {
            e.printStackTrace();
            MyLog.d(TAG, "网络上获取文件名失败");
        }
        return name;
    }
    
    /**
     * 获取文件名（不含后缀）.
     *
     * @param url 文件地址
     * @return 文件名
     */
    public static String getCacheFileNameFromUrl(String url){
        if(StrUtil.isEmpty(url)){
            return null;
        }
        String name = null;
        try {
            name = Md5.MD5(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }
    
	
	/**
	 * 获取文件名（.后缀），外链模式和通过网络获取.
	 *
	 * @param url 文件地址
	 * @param response the response
	 * @return 文件名
	 */
    public static String getCacheFileNameFromUrl(String url,HttpResponse response){
        if(StrUtil.isEmpty(url)){
            return null;
        }
        String name = null;
        try {
            //获取后缀
            String suffix = getMIMEFromUrl(url,response);
            if(StrUtil.isEmpty(suffix)){
                suffix = ".ab";
            }
            name = Md5.MD5(url)+suffix;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }
	
	
	/**
	 * 获取文件名（.后缀），外链模式和通过网络获取.
	 *
	 * @param url 文件地址
	 * @param connection the connection
	 * @return 文件名
	 */
	public static String getCacheFileNameFromUrl(String url,HttpURLConnection connection){
		if(StrUtil.isEmpty(url)){
			return null;
		}
		String name = null;
		try {
			//获取后缀
			String suffix = getMIMEFromUrl(url,connection);
			if(StrUtil.isEmpty(suffix)){
				suffix = ".ab";
			}
			name = Md5.MD5(url)+suffix;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
    }
	
	
	/**
	 * 获取文件后缀，本地.
	 *
	 * @param url 文件地址
	 * @param connection the connection
	 * @return 文件后缀
	 */
	public static String getMIMEFromUrl(String url,HttpURLConnection connection){
		
		if(StrUtil.isEmpty(url)){
			return null;
		}
		String suffix = null;
		try {
			//获取后缀
			if(url.lastIndexOf(".")!=-1){
				 suffix = url.substring(url.lastIndexOf("."));
				 if(suffix.indexOf("/")!=-1 || suffix.indexOf("?")!=-1 || suffix.indexOf("&")!=-1){
					 suffix = null;
				 }
			}
			if(StrUtil.isEmpty(suffix)){
				 //获取文件名  这个效率不高
				 String fileName = getRealFileName(connection);
				 if(fileName!=null && fileName.lastIndexOf(".")!=-1){
					 suffix = fileName.substring(fileName.lastIndexOf("."));
				 }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return suffix;
    }
	
	/**
	 * 获取文件后缀，本地和网络.
	 *
	 * @param url 文件地址
	 * @param response the response
	 * @return 文件后缀
	 */
    public static String getMIMEFromUrl(String url,HttpResponse response){
        
        if(StrUtil.isEmpty(url)){
            return null;
        }
        String mime = null;
        try {
            //获取后缀
            if(url.lastIndexOf(".")!=-1){
                mime = url.substring(url.lastIndexOf("."));
                 if(mime.indexOf("/")!=-1 || mime.indexOf("?")!=-1 || mime.indexOf("&")!=-1){
                     mime = null;
                 }
            }
            if(StrUtil.isEmpty(mime)){
                 //获取文件名  这个效率不高
                 String fileName = getRealFileName(response);
                 if(fileName!=null && fileName.lastIndexOf(".")!=-1){
                     mime = fileName.substring(fileName.lastIndexOf("."));
                 }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mime;
    }
    /**
	 * 描述：SD卡是否能用.
	 *
	 * @return true 可用,false不可用
	 */
	public static boolean isCanUseSD() { 
	    try { 
	        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); 
	    } catch (Exception e) { 
	        e.printStackTrace(); 
	    } 
	    return false; 
    } 
	/**
	 * 描述：从sd卡中的文件读取到byte[].
	 *
	 * @param path sd卡中文件路径
	 * @return byte[]
	 */
	public static byte[] getByteArrayFromSD(String path) {  
		byte[] bytes = null; 
		ByteArrayOutputStream out = null;
	    try {
	    	File file = new File(path);  
	    	//SD卡是否存在
			if(!isCanUseSD()){
				 return null;
		    }
			//文件是否存在
			if(!file.exists()){
				 return null;
			}
	    	
	    	long fileSize = file.length();  
	    	if (fileSize > Integer.MAX_VALUE) {  
	    	      return null;  
	    	}  

			FileInputStream in = new FileInputStream(path);  
		    out = new ByteArrayOutputStream(1024);  
			byte[] buffer = new byte[1024];  
			int size=0;  
			while((size=in.read(buffer))!=-1) {  
			   out.write(buffer,0,size);  
			}  
			in.close();  
            bytes = out.toByteArray();  
   
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(out!=null){
				try {
					out.close();
				} catch (Exception e) {
				}
			}
		}
	    return bytes;
    }  
	
	/**
	 * 描述：将byte数组写入文件.
	 *
	 * @param path the path
	 * @param content the content
	 * @param create the create
	 */
	 public static void writeByteArrayToSD(String path, byte[] content,boolean create){  
	    
		 FileOutputStream fos = null;
		 try {
	    	File file = new File(path);  
	    	//SD卡是否存在
			if(!isCanUseSD()){
				 return;
		    }
			//文件是否存在
			if(!file.exists()){
				if(create){
					File parent = file.getParentFile();
					if(!parent.exists()){
						parent.mkdirs();
						file.createNewFile();
					}
				}else{
				    return;
				}
			}
			fos = new FileOutputStream(path);  
			fos.write(content);  
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(fos!=null){
				try {
					fos.close();
				} catch (Exception e) {
				}
			}
		}
   }  
	 
}
