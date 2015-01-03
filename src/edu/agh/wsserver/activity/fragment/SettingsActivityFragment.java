package edu.agh.wsserver.activity.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import edu.agh.wsserver.activity.R;
import edu.agh.wsserver.settings.ServerSettings;

public class SettingsActivityFragment extends Fragment {

	private final static String LOG_TAG = SecurityActivityFragment.class.getSimpleName();

	public SettingsActivityFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onCreateView START");

		final View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

		final Button applyChanges = (Button) rootView.findViewById(R.id.buttonApplyMainServerSettings);
		final EditText mainServerIpTxt = (EditText) rootView.findViewById(R.id.mainServerIp);
		final EditText mainServerPortTxt = (EditText) rootView.findViewById(R.id.mainServerPort);

		/* IP address */
		mainServerIpTxt.setText(ServerSettings.getInstance().getMainServerIpAddress());
		mainServerIpTxt.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				applyChanges.setEnabled(false);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				applyChanges.setEnabled(validateFields(rootView));
			}
		});

		/* Port number */
		mainServerPortTxt.setText(String.valueOf(ServerSettings.getInstance().getMainServerPortNumber()));
		mainServerPortTxt.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				applyChanges.setEnabled(false);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				applyChanges.setEnabled(validateFields(rootView));
			}
		});

		/* Apply button section */
		applyChanges.setEnabled(false);
		applyChanges.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mainServerIp = mainServerIpTxt.getText().toString();
				if (!mainServerIp.equals(ServerSettings.getInstance().getMainServerIpAddress())) {
					ServerSettings.getInstance().setMainServerIpAddress(mainServerIp);
				}

				Integer mainServerPortNr = Integer.parseInt(mainServerPortTxt.getText().toString());
				if (ServerSettings.getInstance().getMainServerPortNumber() != mainServerPortNr) {
					ServerSettings.getInstance().setMainServerPortNumber(mainServerPortNr);
				}

				applyChanges.setEnabled(false);
			}
		});

		Log.d(LOG_TAG, "onCreateView END");
		return rootView;
	}

	private boolean validateFields(View rootView) {
		final EditText mainServerIpTxt = (EditText) rootView.findViewById(R.id.mainServerIp);
		final EditText mainServerPortTxt = (EditText) rootView.findViewById(R.id.mainServerPort);

		boolean returnFlag = true;

		if (!validateIpAddress(mainServerIpTxt)) {
			returnFlag = false;
		}
		if (!validateNumberField(mainServerPortTxt, ServerSettings.PORT_MIN, ServerSettings.PORT_MAX)) {
			returnFlag = false;
		}

		return returnFlag;
	}

	private boolean validateIpAddress(EditText field) {
		if (field.getText().toString().length() <= 0) {
			field.setError("Field cannot be empty!");
			return false;
		} else {
			String[] ipParts = field.getText().toString().split("\\.");
			if (ipParts.length != 4 || field.getText().toString().endsWith(".")) {
				field.setError("IP address has invalid format.");
				return false;
			}
			for (String part : ipParts) {
				try {
					int value = Integer.parseInt(part);
					if (value < 0 || value > 255) {
						field.setError("IP address must contain numbers from range 0-255.");
						return false;
					}
				} catch (NumberFormatException e) {
					field.setError("IP address contains invalid characters.");
					return false;
				}
			}
		}
		field.setError(null);
		return true;
	}

	private boolean validateNumberField(EditText field, int minVal, int maxVal) {
		if (field.getText().toString().length() <= 0) {
			field.setError("Field cannot be empty!");
		} else {
			try {
				Integer threadsCnt = Integer.parseInt(field.getText().toString());
				if (threadsCnt >= minVal && threadsCnt <= maxVal) {
					field.setError(null);
					return true;
				} else {
					field.setError("Enter an integer value from " + minVal + " to " + maxVal + "!");
				}
			} catch (Exception e) {
				Log.e(LOG_TAG, "Field value must be an integer!", e);
				field.setError("Field value must be an integer!");
			}
		}
		return false;
	}
}