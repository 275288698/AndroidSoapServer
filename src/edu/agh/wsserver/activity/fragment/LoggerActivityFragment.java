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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import edu.agh.wsserver.activity.R;
import edu.agh.wsserver.data.LoggerItem;
import edu.agh.wsserver.data.LoggerListAdapter;
import edu.agh.wsserver.logger.LoggerUpdater;

public class LoggerActivityFragment extends Fragment {
	public static final String LOG_TAG = LoggerActivityFragment.class.getSimpleName();

	private LoggerListAdapter loggerAdapter;

	private ListView list;

	private LoggerUpdater loggerTask = null;
	private ExecutorService es = Executors.newSingleThreadExecutor();
	private boolean isRunning = false;
	private boolean forceStopFollow = true;

	public LoggerActivityFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onCreateView START");

		final View rootView = inflater.inflate(R.layout.fragment_logger,
				container, false);
		final Switch logScrollSwitch = (Switch) rootView
				.findViewById(R.id.logScrollSwitch);

		if (loggerTask == null && !isRunning) {
			Log.i(LOG_TAG, "Trying to RUN logger.");

			loggerAdapter = new LoggerListAdapter(getActivity()
					.getApplicationContext(), new ArrayList<LoggerItem>());

			/*
			 * Przekazanie biezacego obiektu Activity do LoggerUpdatera umozliwi
			 * wywolywanie metod aktualizujacych komponenty widoku z poziomu ich
			 * macierzystego watku (tego, w ktorym zostaly utworzone) Dzieki
			 * temu zapobiegamy CalledFromWrongThreadException przy update
			 * widoku
			 */
			loggerTask = new LoggerUpdater(loggerAdapter, getActivity());

			es.execute(loggerTask);
			isRunning = true;
		}

		list = (ListView) rootView.findViewById(R.id.logListView);
		list.setAdapter(loggerAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
			}
		});

		list.setOnScrollListener(new OnScrollListener() {
			public void onScroll(AbsListView view, int first, int visible, int total) {
				if (!forceStopFollow) {
					if ((first + visible == total)) {
						if (list.getTranscriptMode() != AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL) {
							list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
							logScrollSwitch.setChecked(true);
						}
					} else {
						if (list.getTranscriptMode() != AbsListView.TRANSCRIPT_MODE_DISABLED) {
							list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
							logScrollSwitch.setChecked(false);
						}
					}
				}
			}

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				forceStopFollow = false;
			}
		});

		logScrollSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
				} else {
					forceStopFollow = true;
					list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
				}
			}
		});

		logScrollSwitch.setChecked(true);
		
		Log.d(LOG_TAG, "onCreateView END");
		return rootView;
	}
}