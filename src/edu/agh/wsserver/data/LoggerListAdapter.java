package edu.agh.wsserver.data;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import edu.agh.wsserver.activity.R;

public class LoggerListAdapter extends BaseAdapter {
	
	private Context context;
	private ArrayList<LoggerItem> loggerListItems;
	
	public LoggerListAdapter(Context context, ArrayList<LoggerItem> loggerListItems){
		this.context = context;
		this.loggerListItems = loggerListItems;
	}
	
	@Override
	public int getCount() {
		return loggerListItems == null ? 0 : loggerListItems.size();
	}

	@Override
	public Object getItem(int position) {
		return loggerListItems == null ? null : loggerListItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.logger_list_item, null);
        }

        TextView txtHeader = (TextView) convertView.findViewById(R.id.logHeader);
        TextView txtContent = (TextView) convertView.findViewById(R.id.logContent);
        
        txtHeader.setText(loggerListItems.get(position).getLogHeader());
        txtContent.setText(loggerListItems.get(position).getLogContent());
        
		return convertView;
	}
	
	public void setData(ArrayList<LoggerItem> items) {
		loggerListItems = items;
	}
}
