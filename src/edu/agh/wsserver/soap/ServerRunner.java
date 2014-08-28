package edu.agh.wsserver.soap;

import android.content.res.AssetManager;
import android.util.Log;

public class ServerRunner implements Runnable {
	private static final String LOG_TAG = ServerRunner.class.getSimpleName();

	/** Run GSoap server */
	private native int runServer();

	/** Stop GSoap server */
	public native boolean stopServer();

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
		runServer();
		Log.i(LOG_TAG, "Server stopped");
	}
}