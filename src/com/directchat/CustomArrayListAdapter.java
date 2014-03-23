package com.directchat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;
//import android.*;

public class CustomArrayListAdapter extends ArrayAdapter<User> {

	private ArrayList<User> users = null;
	Context context;
	int layoutResourceId;
	DirectChat app;


	public CustomArrayListAdapter(Context context, int resource,
			ArrayList<User> objects) {
		super(context, resource, objects);
		layoutResourceId = resource;
		this.context = context;
		this.users = objects;
		this.app = ((DirectChat)context.getApplicationContext());
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View row = convertView;
		UserHolder holder = null;

		if(row==null){
			Activity thisactivity = (Activity) context;
			LayoutInflater inflater = thisactivity.getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new UserHolder();
			
			//bind all the parts of the holder object to the appropriate view
			holder.txt = (TextView)row.findViewById(R.id.userName_lvRow_mainActivity);
			holder.last_seen = (TextView)row.findViewById(R.id.userLastView_lvRow_mainActivity);
			holder.user_online_indicator = (ImageView) row.findViewById(R.id.userStatus_lvRow_mainActivity);
			holder.totmsgs = (TextView) row.findViewById(R.id.messagesAmount_lvRow_mainActivity);

			row.setTag(holder);
		} else {
			holder = (UserHolder)row.getTag();
		}
		
		//now set all the values of the elements for each user in the list
		User curUsr = users.get(position);

		//check if user is online
		if(curUsr.is_online()){ //userOnline
			holder.user_online_indicator.setImageResource(R.drawable.user_online_circle);
		}else{
			holder.user_online_indicator.setImageResource(R.drawable.user_offline_circle);
		}
		
		//Set the last seen date
		String dateasString= DateFormat.format("MM/dd/yyyy HH:MM", new Date(curUsr.getLast_seen())).toString();
		holder.last_seen.setText(dateasString);
		
		//set the name
		holder.txt.setText(curUsr.getName());
		//set the total number of messages in the conversation
				String totmessages = "Messages in conversation: " + app.db.getAllMessagesTo_FromUser(curUsr).size();
				holder.totmsgs.setText(totmessages);
		
		return row;
	}

	static class UserHolder {
		TextView txt;
		TextView last_seen;
		ImageView user_online_indicator;
		TextView totmsgs;

	}
}
