package edu.agh.wsserver.activity.fragment;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import edu.agh.wsserver.activity.R;
import edu.agh.wsserver.mainserver.MainServerConnector;
import edu.agh.wsserver.settings.ServerSettings;
import edu.agh.wsserver.soap.ServerRunner;
import edu.agh.wsserver.utils.ServerUtils;

public class ServerActivityFragment extends Fragment {
	public static final String LOG_TAG = ServerActivityFragment.class.getSimpleName();

	private ServerRunner serverRunner = null;

	private final ExecutorService es = Executors.newSingleThreadExecutor();
	private Future<?> currentTask = null;
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
		final TextView portTextView = (TextView) rootView.findViewById(R.id.portTextView);
		
		if (serverRunner == null) {
			serverRunner = new ServerRunner(this.getActivity().getAssets());
		}

		stopServerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startServerButton.setEnabled(true);
				stopServerButton.setEnabled(false);

				String mainServerIp = ServerSettings.getInstance().getMainServerIpAddress();
				boolean canDeregisterFromMainServer = mainServerIp != null && !"".equals(mainServerIp);
				if (!"ERROR".equals(ipAddressText.getText()) && canDeregisterFromMainServer) {
					MainServerConnector.getInstance().stopConnectionWithMainServer();
				}

				ipAddressText.setText(v.getResources().getString(R.string.ip_number));
				portTextView.setText(v.getResources().getString(R.string.port_number));

				Log.i(LOG_TAG, "Trying to STOP server.");
				serverRunner.stopServer();
				isRunning = false;
			}
		});

		startServerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentTask == null || currentTask.isDone()) {
					stopServerButton.setEnabled(true);
					startServerButton.setEnabled(false);
					String localIp = ServerUtils.getLocalIP();
					ipAddressText.setText(localIp);
					portTextView.setText(String.valueOf(ServerSettings.getInstance().getServerPortNumber()));

					String mainServerIp = ServerSettings.getInstance().getMainServerIpAddress();
					boolean canRegisterToMainServer = mainServerIp != null && !"".equals(mainServerIp);
					if (!"ERROR".equals(localIp) && canRegisterToMainServer) {
						MainServerConnector.getInstance().establishConnectionWithServer(localIp,
								String.valueOf(ServerSettings.getInstance().getServerPortNumber()));
					}

					Log.i(LOG_TAG, "Trying to RUN server.");
					serverRunner.setCurrentServerPort(ServerSettings.getInstance().getServerPortNumber());
					serverRunner.setThreadsPoolSize(ServerSettings.getInstance().getNumberOfThreads());
					currentTask = es.submit(serverRunner);
					isRunning = true;
				} else {
					Log.w(LOG_TAG, "Current server instance have not stopped yet. Try again after few seconds.");
				}
			}
		});

		if (isRunning) {
			stopServerButton.setEnabled(true);
			startServerButton.setEnabled(false);
			ipAddressText.setText(ServerUtils.getLocalIP());
			portTextView.setText(String.valueOf(serverRunner.getCurrentServerPort()));
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

	public ServerRunner getServerRunner() {
		return this.serverRunner;
	}

	@Override
	public void onDestroy() {
		if (serverRunner != null) {
			MainServerConnector.getInstance().stopConnectionWithMainServer();
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
