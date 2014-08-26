package edu.agh.wsserver.data;

public class LoggerItem {
	private String logHeader;
	private String logContent;
	
	public LoggerItem(String logH, String logC) {
		logHeader = logH;
		logContent = logC;
	}
	
	public String getLogContent() {
		return logContent;
	}
	public void setLogContent(String logContent) {
		this.logContent = logContent;
	}
	public String getLogHeader() {
		return logHeader;
	}
	public void setLogHeader(String logHeader) {
		this.logHeader = logHeader;
	}
}
