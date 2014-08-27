package edu.agh.wsserver.data;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
        TextView txtTag = (TextView) convertView.findViewById(R.id.logTag);
        TextView txtContent = (TextView) convertView.findViewById(R.id.logContent);
        
        LoggerItem loggerItem = loggerListItems.get(position);
        
        txtHeader.setText(loggerItem.getLogHeader());
        txtTag.setText(loggerItem.getLogTag());
        txtContent.setText(loggerItem.getLogContent());
        
        //kolorek
        convertView.setBackgroundColor(loggerItem.getLogColor());
        
		return convertView;
	}
	
	public void setData(ArrayList<LoggerItem> items) {
		loggerListItems = items;
	}
	
	public void addItem(LoggerItem item) {
		if(loggerListItems == null) {
			loggerListItems = new ArrayList<LoggerItem>();
		}
		loggerListItems.add(item);
	}
	
	public void addItems(ArrayList<LoggerItem> itemsList) {
		if(loggerListItems == null) {
			loggerListItems = new ArrayList<LoggerItem>();
		}
		loggerListItems.addAll(itemsList);
	}
}
