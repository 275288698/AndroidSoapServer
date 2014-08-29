package edu.agh.wsserver.activity.fragment;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import edu.agh.wsserver.activity.R;
import edu.agh.wsserver.settings.ServerSettings;
import edu.agh.wsserver.soap.ServerRunner;
import edu.agh.wsserver.utils.ServerUtils;

public class ServerActivityFragment extends Fragment {
	public static final String LOG_TAG = "Server-screen";

	private ServerRunner serverRunner = null;

	private final ExecutorService es = Executors.newSingleThreadExecutor();
	private boolean isRunning = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onCreateView START");

		final View rootView = inflater.inflate(R.layout.fragment_server, container, false);
		// Set up Start Server Buttons
		final Button startServerButton = (Button) rootView.findViewById(R.id.startServerButton);
		final Button stopServerButton = (Button) rootView.findViewById(R.id.stopServer);
		final TextView ipAddressText = (TextView) rootView.findViewById(R.id.ipTextView);
		
		if (serverRunner == null) {
			serverRunner = new ServerRunner(this.getActivity().getAssets());
		}

		stopServerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startServerButton.setEnabled(true);
				stopServerButton.setEnabled(false);
				ipAddressText.setText(v.getResources().getString(R.string.ip_number));
				
				Log.i(LOG_TAG, "Trying to STOP server.");
				serverRunner.stopServer();
				isRunning = false;
			}
		});

		startServerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopServerButton.setEnabled(true);
				startServerButton.setEnabled(false);
				new GetLocalIpTask(ipAddressText).execute();

				Log.i(LOG_TAG, "Trying to RUN server.");
				serverRunner.setServerPort(ServerSettings.getInstance().getServerPortNumber());
				es.execute(serverRunner);
				isRunning = true;
			}
		});

		if (isRunning) {
			stopServerButton.setEnabled(true);
			startServerButton.setEnabled(false);
			new GetLocalIpTask((TextView) rootView.findViewById(R.id.ipTextView)).execute();
		} else {
			stopServerButton.setEnabled(false);
		}

		Log.d(LOG_TAG, "onCreateView END");
		return rootView;
	}

	static void setFinalStatic(Field field, Object newValue) throws Exception {
		field.setAccessible(true);

		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.set(null, newValue);
	}

	public static class GetLocalIpTask extends AsyncTask<Void, Void, String> {
		private TextView textView;

		public GetLocalIpTask(TextView textViewToUpdate) {
			this.textView = textViewToUpdate;
		}

		@Override
		protected String doInBackground(Void... params) {
			return ServerUtils.getLocalIP() + ":" + ServerSettings.getInstance().getServerPortNumber();
		}

		@Override
		protected void onPostExecute(String result) {
			textView.setText(result);
		}
	}

	public ServerRunner getServerRunner() {
		return this.serverRunner;
	}

	@Override
	public void onDestroy() {
		if (serverRunner != null) {
			serverRunner.stopServer();
			es.shutdown();
			try {
				es.awaitTermination(3, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Log.e(LOG_TAG, "Fragment destroying interrupted.", e);
			}
		}
		super.onDestroy();
	}
}
