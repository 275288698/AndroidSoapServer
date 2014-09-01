package edu.agh.wsserver.settings;

import android.util.Log;

public class ServerSettings {
    public static final String LOG_TAG = ServerSettings.class.getSimpleName();
    
    private static volatile ServerSettings instance = null;
    
    private int serverPortNumber;
    private int numberOfThreads;
 
    private ServerSettings() {
    	/* default values */
    	setServerPortNumber(8080);
    	setNumberOfThreads(10);
    }
 
    public static ServerSettings getInstance() {
        if (instance == null) {
            synchronized (ServerSettings.class) {
                if (instance == null) {
                    instance = new ServerSettings();
                }
            }
        }
		return instance;
	}

	public int getServerPortNumber() {
		return serverPortNumber;
	}

	public void setServerPortNumber(int serverPortNumber) {
		this.serverPortNumber = serverPortNumber;
        Log.d(LOG_TAG, "Port number has been changed: " + serverPortNumber);
	}

	public int getNumberOfThreads() {
		return numberOfThreads;
	}

	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
        Log.d(LOG_TAG, "Number of threads has been changed: " + numberOfThreads);
	}
}
