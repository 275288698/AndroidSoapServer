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

public class ParamsActivityFragment extends Fragment {
	public static final String LOG_TAG = ParamsActivityFragment.class.getSimpleName();

	public ParamsActivityFragment() {
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView START");
  
        final View rootView = inflater.inflate(R.layout.fragment_params, container, false);
          
        final Button applyChanges = (Button) rootView.findViewById(R.id.buttonApplySettings);
        final EditText portNumberTxt = (EditText) rootView.findViewById(R.id.portNumber);
        
        portNumberTxt.setText(String.valueOf(ServerSettings.getInstance().getServerPortNumber()));
        portNumberTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	applyChanges.setEnabled(false);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {
	            if (portNumberTxt.getText().toString().length() <= 0) {
	            	portNumberTxt.setError("Port number cannot be empty!");
	            } else {
	            	try {
	            		Integer portNumber = Integer.parseInt(portNumberTxt.getText().toString());
	            		if(portNumber > 1023 && portNumber <= 65535) {
	            			applyChanges.setEnabled(true);
	            		} else {
	    	            	portNumberTxt.setError("Enter an integer value from 1024 to 65535!");
	            		}
	            	} catch (Exception e) {
	            		Log.e(LOG_TAG, "Port number value must be an integer!", e);
	            		portNumberTxt.setError("Port number value must be an integer!");
	            	}
	            }
            }
         });
        
        applyChanges.setEnabled(false);
        applyChanges.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Integer portNumber = Integer.parseInt(portNumberTxt.getText().toString());
				ServerSettings.getInstance().setServerPortNumber(portNumber);
				applyChanges.setEnabled(false);
			}
		});

        Log.d(LOG_TAG, "onCreateView END");
        return rootView;
    }
}