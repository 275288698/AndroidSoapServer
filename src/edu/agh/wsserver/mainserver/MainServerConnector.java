package edu.agh.wsserver.mainserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;
import edu.agh.wsserver.settings.ServerSettings;

public class MainServerConnector {
	private static final String LOG_TAG = MainServerConnector.class.getSimpleName();
	private static final String CONNECT_SERVLET = "/Server4AndroidSoapServer/register";
	private static final String PING_SERVLET = "/Server4AndroidSoapServer/ping";
	private static final String WSDL_FILE_NAME = "DeviceServices.wsdl";
	

	private volatile static MainServerConnector instance;
	private volatile static AssetManager assetManager;

	public static MainServerConnector getInstance() {
		if (instance == null) {
			synchronized (MainServerConnector.class) {
				if (instance == null) {
					instance = new MainServerConnector();
				}
			}
		}
		return instance;
	}

	public void establishConnectionWithServer() {
		new ConnectorTask().execute("");
	}

	public void setAssetManager(AssetManager am) {
		assetManager = am;
	}

	private static byte[] compress(String string) throws IOException {
	    ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
	    GZIPOutputStream gos = new GZIPOutputStream(os);
	    gos.write(string.getBytes());
	    gos.close();
	    byte[] compressed = os.toByteArray();
	    os.close();
	    return compressed;
	}

	private static String convertStreamToString(InputStream is) {
	    Scanner s = new Scanner(is);
	    s.useDelimiter("\\A");
	    String result = s.hasNext() ? s.next() : "";
	    s.close();
	    return result;
	}
	
	private static class ConnectorTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			if (assetManager == null) {
				Log.e(LOG_TAG, "Asset manager must not be null.");
				return null;
			}

			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("http://" + ServerSettings.getInstance().getMainServerIpAddress() + ":" + ServerSettings.getInstance().getMainServerPortNumber()
					+ CONNECT_SERVLET);
			try {
				String wsdl = convertStreamToString(assetManager.open(WSDL_FILE_NAME));
				httpPost.setEntity(new ByteArrayEntity(compress(wsdl)));
				HttpResponse response = httpClient.execute(httpPost);
				Log.i(LOG_TAG, response.toString());
				// TODO fetch token
				// TODO start ping
			} catch (ClientProtocolException e) {
				Log.e(LOG_TAG, "Error occured.", e);
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error occured.", e);
			}
			return null;
		}
	}
}