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
        final EditText threadsNumberTxt = (EditText) rootView.findViewById(R.id.threadsNumber);
        
        /* Port number section */
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
            	applyChanges.setEnabled(validateFields(rootView));
            }
         });
        
        /* Number of threads section */
        threadsNumberTxt.setText(String.valueOf(ServerSettings.getInstance().getNumberOfThreads()));
        threadsNumberTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	applyChanges.setEnabled(false);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

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
				Integer portNumber = Integer.parseInt(portNumberTxt.getText().toString());
				if(ServerSettings.getInstance().getServerPortNumber() != portNumber) {
					ServerSettings.getInstance().setServerPortNumber(portNumber);
				}
				
				Integer threadsNumber = Integer.parseInt(threadsNumberTxt.getText().toString());
				if(ServerSettings.getInstance().getNumberOfThreads() != threadsNumber) {
					ServerSettings.getInstance().setNumberOfThreads(threadsNumber);
				}
				
				applyChanges.setEnabled(false);
			}
		});

        Log.d(LOG_TAG, "onCreateView END");
        return rootView;
    }
	
	private boolean validateFields(View rootView) {
        final EditText portNumberTxt = (EditText) rootView.findViewById(R.id.portNumber);
        final EditText threadsNumberTxt = (EditText) rootView.findViewById(R.id.threadsNumber);
		
        boolean returnFlag = true;

        if(! validateNumberField(threadsNumberTxt, ServerSettings.THREADS_MIN, ServerSettings.THREADS_MAX)) {
        	returnFlag = false;
        }
		if(! validateNumberField(portNumberTxt, ServerSettings.PORT_MIN, ServerSettings.PORT_MAX)) {
			returnFlag = false;
		}
		
		return returnFlag;
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