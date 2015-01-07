package edu.agh.wsserver.mainserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;

import org.apache.http.Header;
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
	public static final String LOG_TAG = MainServerConnector.class.getSimpleName();
	private static final String REGISTER_SERVLET = "/Server4AndroidSoapServer/rest/register";
	private static final String DEREGISTER_SERVLET = "/Server4AndroidSoapServer/rest/deregister";
	private static final String PING_SERVLET = "/Server4AndroidSoapServer/rest/ping";
	private static final String WSDL_FILE_NAME = "DeviceServices.wsdl";
	private static final String TOKEN_HEADER = "deviceToken";
	private static final String IP_HEADER = "deviceIp";
	private static final String PORT_HEADER = "devicePort";
	private static final long PING_INTERVAL = 10 * 1000;

	private volatile static MainServerConnector instance;
	private volatile static AssetManager assetManager;

	private volatile String deviceToken = "";
	private final ExecutorService pingExecutor = Executors.newSingleThreadExecutor();
	private volatile boolean sendPings = false;
	private volatile Future<?> runningPingTask;

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

	public void establishConnectionWithServer(String localServerIp, String serverPort) {
		new RegisterServletTask().execute(localServerIp, serverPort);
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

	public void stopConnectionWithMainServer() {
		sendPings = false;
		if (runningPingTask != null) {
			runningPingTask.cancel(true);
		}
		new DeregisterServletTask().execute();
	}

	private class RegisterServletTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			if (assetManager == null) {
				Log.e(LOG_TAG, "Asset manager must not be null.");
				return null;
			}
			if (params == null || params.length < 2) {
				Log.e(LOG_TAG, "Local ip and port must be provided");
				return null;
			}

			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("http://" + ServerSettings.getInstance().getMainServerIpAddress() + ":"
					+ ServerSettings.getInstance().getMainServerPortNumber() + REGISTER_SERVLET);
			try {
				String wsdl = convertStreamToString(assetManager.open(WSDL_FILE_NAME));
				httpPost.setEntity(new ByteArrayEntity(compress(wsdl)));
				httpPost.setHeader(IP_HEADER, params[0]);
				httpPost.setHeader(PORT_HEADER, params[1]);
				HttpResponse response = httpClient.execute(httpPost);
				if (response.getStatusLine().getStatusCode() == 200) {
					Header[] headers = response.getHeaders(TOKEN_HEADER);
					if (headers != null && headers.length > 0) {
						MainServerConnector.this.deviceToken = headers[0].getValue();
						Log.d(LOG_TAG, "Received device token: " + MainServerConnector.this.deviceToken);
						sendPings = true;
					} else {
						Log.w(LOG_TAG, "Main server did not send device token.");
					}
				} else {
					Log.e(LOG_TAG, "Main server response error. Code: " + response.getStatusLine().getStatusCode() + ", reason: "
							+ response.getStatusLine().getReasonPhrase());
				}
			} catch (ClientProtocolException e) {
				Log.e(LOG_TAG, "Error occured: " + e.getMessage());
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error occured: " + e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (runningPingTask != null) {
				runningPingTask.cancel(true);
			}
			if (!"".equals(deviceToken)) {
				runningPingTask = pingExecutor.submit(new PingServletTask());
			}
		}
	}

	private class PingServletTask implements Runnable {
		@Override
		public void run() {
			if ("".equals(MainServerConnector.this.deviceToken)) {
				Log.w(LOG_TAG, "Device token is empty. Cannot send ping.");
				return;
			}
			while (!Thread.currentThread().isInterrupted() && sendPings) {
				try {
					HttpClient httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://" + ServerSettings.getInstance().getMainServerIpAddress() + ":"
							+ ServerSettings.getInstance().getMainServerPortNumber() + PING_SERVLET);
					httpPost.setHeader(TOKEN_HEADER, deviceToken);
					HttpResponse response = httpClient.execute(httpPost);
					if (response.getStatusLine().getStatusCode() != 200) {
						Log.e(LOG_TAG, "Main server response error. Code: " + response.getStatusLine().getStatusCode() + ", reason: "
								+ response.getStatusLine().getReasonPhrase());
					} else {
						Log.d(LOG_TAG, "Ping successfully sent.");
					}
					Thread.sleep(PING_INTERVAL);
				} catch (ClientProtocolException e) {
					Log.e(LOG_TAG, "Error occured: " + e.getMessage());
				} catch (IOException e) {
					Log.e(LOG_TAG, "Error occured: " + e.getMessage());
				} catch (InterruptedException e) {
					Log.i(LOG_TAG, "Ping thread interrupted.");
					sendPings = false;
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	private class DeregisterServletTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			if ("".equals(MainServerConnector.this.deviceToken)) {
				Log.w(LOG_TAG, "Device token is empty. Cannot deregister server.");
				return null;
			}

			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("http://" + ServerSettings.getInstance().getMainServerIpAddress() + ":"
					+ ServerSettings.getInstance().getMainServerPortNumber() + DEREGISTER_SERVLET);
			try {
				httpPost.setHeader(TOKEN_HEADER, deviceToken);
				HttpResponse response = httpClient.execute(httpPost);
				if (response.getStatusLine().getStatusCode() != 200) {
					Log.e(LOG_TAG, "Main server response error. Code: " + response.getStatusLine().getStatusCode() + ", reason: "
							+ response.getStatusLine().getReasonPhrase());
				} else {
					Log.i(LOG_TAG, "Server successfully deregistered from main server.");
				}
			} catch (ClientProtocolException e) {
				Log.e(LOG_TAG, "Error occured: " + e.getMessage());
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error occured: " + e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			deviceToken = "";
		}
	}
}