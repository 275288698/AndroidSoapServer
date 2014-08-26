package edu.agh.wsserver.logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.util.Log;
import edu.agh.wsserver.data.LoggerItem;
import edu.agh.wsserver.data.LoggerListAdapter;

public class LoggerUpdater extends Thread {
	private static final String LOG_TAG = "SoapServer";

	private LoggerListAdapter loggerRef;
	private Activity mainActivity;
	private boolean run;

	public LoggerUpdater(LoggerListAdapter loggerAdapterToUpdate, Activity mainThreadActivity) {
		this.loggerRef = loggerAdapterToUpdate;
		this.mainActivity = mainThreadActivity;
		run = true;
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
			
			Process process = Runtime.getRuntime().exec("logcat -d HelloJni:D SoapServer:D *:S"); // TODO sparametryzowac
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			String line = "";
			
			while ((line = bufferedReader.readLine()) != null) {
				LoggerItem logItem = new LoggerItem("H", line);
				logItems.add(logItem);
			}

			/* uruchamiamy na watku macierzystym (dla tego Activity) */
			mainActivity.runOnUiThread(new Runnable() {
			     @Override
			     public void run() {
						loggerRef.setData(logItems); // TODO update zamiast nadpisywania, jak odczytac czesc logow?
						/* update listy */
						loggerRef.notifyDataSetChanged();
			     }
			});

		} catch(Exception e) {
			Log.e(LOG_TAG, "fatal error", e);
		}
	}
	
	public void stopLogger() {
		run = false;
	}
}
