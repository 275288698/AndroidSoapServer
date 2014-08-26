package edu.agh.wsserver.logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.util.Log;
import edu.agh.wsserver.data.LoggerItem;
import edu.agh.wsserver.data.LoggerListAdapter;
import edu.agh.wsserver.utils.ServerUtils;

public class LoggerUpdater extends Thread {
	private static final String LOG_TAG = "SoapServer";
	private static final String LOGCAT_PATTERN = "[a-zA-Z]/.+:.+";
	private static final String LOGCAT_SPLIT = "/.+: ";
	
	private LoggerListAdapter loggerRef;
	private Activity mainActivity;
	private boolean run;
	private Pattern loggerPattern;
	
	public LoggerUpdater(LoggerListAdapter loggerAdapterToUpdate, Activity mainThreadActivity) {
		this.loggerRef = loggerAdapterToUpdate;
		this.mainActivity = mainThreadActivity;
		
		run = true;
		loggerPattern = Pattern.compile(LOGCAT_PATTERN);
	}
	
	@Override
	public void run() {
		try {
			while(run) {
		        getAndUpdateLogs();
		        sleep(1000);
			}
		} catch(Exception e) {
			Log.e(LOG_TAG, "fatal error", e);
		}
	}
	
	private void getAndUpdateLogs() {
		try {
			final ArrayList<LoggerItem> logItems = new ArrayList<LoggerItem>();
			
			Process process = Runtime.getRuntime().exec("logcat -d HelloJni:D SoapServer:D dalvikvm:D *:S"); // TODO sparametryzowac
			ServerUtils.clearLogCat();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			String line = "";
			
			while ((line = bufferedReader.readLine()) != null) {
				LoggerItem logItem = processLogCatLine(line);
				if(logItem != null) {
					logItems.add(logItem);
				}
			}

			/* uruchamiamy na watku macierzystym (dla tego Activity) */
			mainActivity.runOnUiThread(new Runnable() {
			     @Override
			     public void run() {
					loggerRef.addItems(logItems); // TODO update zamiast nadpisywania, jak odczytac czesc logow?
					/* update listy */
					loggerRef.notifyDataSetChanged();
			     }
			});

		} catch(Exception e) {
			Log.e(LOG_TAG, "fatal error", e);
		}
	}
	
	private LoggerItem processLogCatLine(String line) {
		Matcher matcher = loggerPattern.matcher(line);
		if(! matcher.matches()) {
			return null;
		}
		
		String[] split = line.split(LOGCAT_SPLIT);
		return new LoggerItem(split[0], split[1]);
	}
	
	public void stopLogger() {
		run = false;
	}
}
