package edu.agh.wsserver.soap;

import android.content.res.AssetManager;
import android.util.Log;

public class ServerRunner implements Runnable {
	public static final String LOG_TAG = ServerRunner.class.getSimpleName();

	private int currentServerPort = 8080;
	private int threadsPoolSize = 10;

	/** Run GSoap server */
	private native int runServer();

	/** Stop GSoap server */
	public native boolean stopServer();

	private native int setServerPort(int port);

	private native int setServerThreadsPoolSize(int size);

	private native int setAssetManager(AssetManager assetManager);

	/** Allows native code to read files from 'assets' directory */
	private final AssetManager assetMgr;

	public ServerRunner(AssetManager assetMgr) {
		this.assetMgr = assetMgr;
	}

	@Override
	public void run() {
		setAssetManager(assetMgr);
		Log.i(LOG_TAG, "Running server");
		int res = runServer();
		if (res < 0) {
			Log.w(LOG_TAG, "runServer() returned error code: " + res + ". Probably server has not started properly.");
		} else {
			Log.i(LOG_TAG, "Server stopped properly.");
		}
	}

	public int getCurrentServerPort() {
		return this.currentServerPort;
	}

	public void setCurrentServerPort(int serverPort) {
		this.currentServerPort = serverPort;
		setServerPort(serverPort);
	}

	public int getThreadsPoolSize() {
		return threadsPoolSize;
	}

	public void setThreadsPoolSize(int threadsPoolSize) {
		this.threadsPoolSize = threadsPoolSize;
		setServerThreadsPoolSize(threadsPoolSize);
	}
}