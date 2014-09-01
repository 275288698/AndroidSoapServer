package edu.agh.wsserver.settings;

import android.util.Log;

public class ServerSettings {
    public static final String LOG_TAG = ServerSettings.class.getSimpleName();

    public static final int THREADS_MIN = 1;
    public static final int THREADS_DEF = 10;
    public static final int THREADS_MAX = 50;
    public static final int PORT_MIN = 1024;
    public static final int PORT_DEF = 8080;
    public static final int PORT_MAX = 65535;

    private int serverPortNumber;
    private int numberOfThreads;
 
    private static volatile ServerSettings instance = null;
    
    private ServerSettings() {
    	/* default values */
    	setServerPortNumber(PORT_DEF);
    	setNumberOfThreads(THREADS_DEF);
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
