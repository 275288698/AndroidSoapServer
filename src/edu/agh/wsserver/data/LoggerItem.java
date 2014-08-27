package edu.agh.wsserver.data;

import android.content.res.Resources;
import edu.agh.wsserver.activity.R;

/*
 * 	D — Debug
 *	I — Info
 *	W — Warning
 *	E — Error
 *	F — Fatal
 */
public class LoggerItem {
	private String logHeader;
	private String logTag;
	private String logContent;
	private int color;
	
	public LoggerItem(String logH, String logT, String logC, Resources resources) {
		this.logHeader = logH;
		this.logTag = logT;
		this.logContent = logC;
		
		if(logHeader.equals("F") || logHeader.equals("E")) {
			this.color = resources.getColor(R.color.logger_error);
		} else if(logHeader.equals("W")) {
			this.color = resources.getColor(R.color.logger_warn);
		} else if(logHeader.equals("I")) {
			this.color = resources.getColor(R.color.logger_info);
		} else {
			this.color = resources.getColor(R.color.logger_debug);
		}
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
	
	public void setColor(int color) {
		this.color = color;
	}
	
	public int getLogColor() {
		return this.color;
	}

	public String getLogTag() {
		return logTag;
	}

	public void setLogTag(String logTag) {
		this.logTag = logTag;
	}
}
