package edu.agh.wsserver.settings;

public class ServerSettings {
    private static volatile ServerSettings instance = null;
	
    private int serverPortNumber;
 
    private ServerSettings() {
    	/* default values */
    	setServerPortNumber(8080);
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
	}
}
