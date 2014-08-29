package edu.agh.wsserver.utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import dalvik.system.DexFile;

public class ServerUtils {
	public static final String LOG_TAG = ServerUtils.class.getSimpleName();

	private ServerUtils(){}
	
	/**
	 * Wyszukuje wszystkie wartosci zmiennych statycznych o podanej nazwie, zdefiniowane w klasach z okreslonego pakietu.
	 * @param context - kontekst aplikacji, zeby jakos dobrac sie do DEX
	 * @param logTagFieldName - nazwa zmiennej statycznej (publicznej!!)
	 * @param logPackageName - nazwa pakietu po ktorym szukamy
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static List<String> getApplicationLogTags(Context context, String logTagFieldName, String logPackageName) {
		List<String> tags = new ArrayList<String>();
		List<Class> clazzes = null;
		
		try {
			clazzes = findClasses(context, logPackageName);
		} catch (Exception e) {
			Log.e(LOG_TAG, e.toString());
		}
		if(clazzes == null) {
			return null;
		}
		
		for(Class clazz : clazzes) {
			Field fld = null;
			try {
				fld = clazz.getField(logTagFieldName);
			} catch (NoSuchFieldException e) {
				continue;
			}

			Object staticObjValue = null;
			try {
				staticObjValue = fld.get(null);
			} catch (Exception e) {
				continue;
			}

			if (staticObjValue != null && staticObjValue instanceof String) {
				tags.add((String) staticObjValue);
			}
		}
		return tags;
	}

	@SuppressWarnings("rawtypes")
	private static List<Class> findClasses(Context context, String logPackageName) {
		List<Class> classes = new ArrayList<Class>();
		try {
	        DexFile df = new DexFile(context.getPackageCodePath());
	        for (Enumeration<String> iter = df.entries(); iter.hasMoreElements();) {
	            String s = iter.nextElement();
	            if(s.startsWith(logPackageName)) {
	            	Class clazz = null;
	            	try {
	            		clazz = Class.forName(s);
	            	} catch (Exception e) {
		            	continue;
	            	}
	            	
	            	if(clazz != null) {
	            		classes.add(clazz);
	            	}
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		return classes;
	}
	
	/**
	 * Usuwa logi LogCata.
	 */
	public static void clearLogCat() {
		try {
			Runtime.getRuntime().exec("logcat -c");
		} catch (Exception e) {
			Log.e(LOG_TAG, e.toString());
		}
	}
	
	/**
	 * Code adapted from http://stackoverflow.com/a/12854981
	 * @return Device external IP address or ERROR statement.
	 */
	public static String getExternalIP() {
		try {
			HttpClient httpclient = new DefaultHttpClient();
	        HttpGet httpget = new HttpGet("http://ip2country.sourceforge.net/ip2c.php?format=JSON");
	        // HttpGet httpget = new HttpGet("http://whatismyip.com.au/");
	        // HttpGet httpget = new HttpGet("http://www.whatismyip.org/");
	        HttpResponse response = httpclient.execute(httpget);
	        HttpEntity entity = response.getEntity();
	        if (entity != null && entity.getContentLength() > 0) {
	        	JSONObject json_data = new JSONObject(EntityUtils.toString(entity));
	        	return json_data.getString("ip");
	        } else {
	        	Log.e(LOG_TAG, "Response error: " + response.getStatusLine().toString());
	        }
		} catch (Exception e) {
			Log.e(LOG_TAG, e.toString());
		}
		return "ERROR";
	}

	/**
	 * Zwraca lokalny adres IP urzadzenia
	 */
	public static String getLocalIP() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
	                    return inetAddress.getHostAddress();
	                }
	            }
	        }
		} catch (SocketException ex) {
			Log.e(LOG_TAG, ex.toString());
		}
		return "ERROR";
	}
}