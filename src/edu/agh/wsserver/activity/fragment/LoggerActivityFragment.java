package edu.agh.wsserver.activity.fragment;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import edu.agh.wsserver.activity.R;
import edu.agh.wsserver.data.LoggerItem;
import edu.agh.wsserver.data.LoggerListAdapter;
import edu.agh.wsserver.logger.LoggerUpdater;

public class LoggerActivityFragment extends Fragment {
	private static final String LOG_TAG = "SoapServer";
	
	private LoggerListAdapter loggerAdapter;
	
	private ListView list;
    
	private LoggerUpdater loggerTask = null;
	private ExecutorService es = Executors.newSingleThreadExecutor();
	private boolean isRunning = false;
	
	public LoggerActivityFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_logger,
				container, false);
	
		if(loggerTask == null && !isRunning) {
	        Log.d(LOG_TAG, "Trying to RUN logger.");

			loggerAdapter = new LoggerListAdapter(getActivity().getApplicationContext(), new ArrayList<LoggerItem>());
			
			/* troche spierdolone, ale dzieki temu watek moze uzyskac dostep do watku w ktorym odpalona zostala nasza Activity,
			 * a co za tym idzie zapobiec wypierdalaniu CalledFromWrongThreadException przy update widoku */
			loggerTask = new LoggerUpdater(loggerAdapter, getActivity());
			      
			es.execute(loggerTask);
			isRunning = true;
		}
		
		list = (ListView)rootView.findViewById(R.id.logListView);
        list.setAdapter(loggerAdapter);
        list.setOnItemClickListener(new OnItemClickListener()    {
               public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) { }
        });
        
		return rootView;
	}
}