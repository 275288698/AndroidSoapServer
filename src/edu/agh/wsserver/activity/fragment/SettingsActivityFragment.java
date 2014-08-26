package edu.agh.wsserver.activity.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.agh.wsserver.activity.R;

public class SettingsActivityFragment extends Fragment {
	public SettingsActivityFragment(){}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
  
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
          
        return rootView;
    }
}