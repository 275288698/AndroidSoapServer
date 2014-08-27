package edu.agh.wsserver.logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.util.Log;
import edu.agh.wsserver.data.LoggerItem;
import edu.agh.wsserver.data.LoggerListAdapter;
import edu.agh.wsserver.utils.ServerUtils;

/**
 * Kazda klasa z (pod)pakietu "edu.agh.wsserver" posiadajaca pole 
 * public static final String LOG_TAG bedzie dodawana do outputa loggera.
 * Jesli nie chcemy, aby informacje z logow byly wyswietlane wystarczy 
 * ustawic pole LOG_TAG w tej klasie jako prywatne (lub je usunac)!
 * 
 * @author krzysztof broncel
 *
 */
public class LoggerUpdater extends Thread {
	public static final String LOG_TAG = "Logger";
	private static final String LOGCAT_PATTERN = "[a-zA-Z]/.+:.+";
	private static final String LOGCAT_SPLIT1 = "/";
	private static final String LOGCAT_SPLIT2 = "\\(.+\\): ";
	
	
	private LoggerListAdapter loggerRef;
	private Activity mainActivity;
	private boolean run;
	private Pattern loggerPattern;
	private String logcatQuery;
	
	public LoggerUpdater(LoggerListAdapter loggerAdapterToUpdate, Activity mainThreadActivity) {
		this.loggerRef = loggerAdapterToUpdate;
		this.mainActivity = mainThreadActivity;
		
		run = true;
		loggerPattern = Pattern.compile(LOGCAT_PATTERN);
		logcatQuery = prepareLogcatQuery();
	}
	
	private String prepareLogcatQuery() {
		String returnStatement = "logcat -d";
		List<String> appTags = ServerUtils.getApplicationLogTags(this.mainActivity.getApplicationContext(), 
				"LOG_TAG", "edu.agh.wsserver");
		for(String appTag : appTags) {
			if(!appTag.equals("")) {
				returnStatement += " " + appTag + ":D";
			}
		}
		returnStatement += " *:S";
		
		Log.d(LOG_TAG, returnStatement);
		return returnStatement;
	}
	
	@Override
	public void run() {
		Log.i(LOG_TAG, "Logger thread has been started.");
		try {
			while(run) {
		        getAndUpdateLogs();
		        sleep(1000);
			}
		} catch(Exception e) {
			Log.e(LOG_TAG, "fatal error", e);
		}
		Log.i(LOG_TAG, "Logger thread has been stopped.");
	}
	
	private void getAndUpdateLogs() {
		try {
			final ArrayList<LoggerItem> logItems = new ArrayList<LoggerItem>();
			
			Process process = Runtime.getRuntime().exec(logcatQuery); 
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
					loggerRef.addItems(logItems); 
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
		
		String[] split = line.split(LOGCAT_SPLIT1);
		String[] secondSplit = split[1].split(LOGCAT_SPLIT2);
		return new LoggerItem(split[0], secondSplit[0], secondSplit[1], mainActivity.getResources());
	}
	
	public void stopLogger() {
		run = false;
	}
}
