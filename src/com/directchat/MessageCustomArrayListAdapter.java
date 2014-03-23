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

public class MessageCustomArrayListAdapter extends ArrayAdapter<Message> {

	private ArrayList<Message> messages = null;
	Context context;
	int layoutResourceId;
	DirectChat app;


	public MessageCustomArrayListAdapter(Context context, int resource,
			ArrayList<Message> objects) {
		super(context, resource, objects);
		layoutResourceId = resource;
		this.context = context;
		this.messages = objects;
		this.app = ((DirectChat)context.getApplicationContext());
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View row = convertView;
		MessageHolder holder = null;

		if(row==null){
			Activity thisactivity = (Activity) context;
			LayoutInflater inflater = thisactivity.getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new MessageHolder();
			
			//bind all the parts of the holder object to the appropriate view
			holder.touser = (TextView)row.findViewById(R.id.touser_lvRow_messageActivity);
			holder.fromuser = (TextView) row.findViewById(R.id.fromUser_lvRow_messageActivity);
			holder.messagetxt = (TextView) row.findViewById(R.id.messagetext_lvRow_messageActivity);
			holder.messagesent = (TextView) row.findViewById(R.id.messagesent_lvRow_messageActivity);

			row.setTag(holder);
		} else {
			holder = (MessageHolder)row.getTag();
		}
		
		//now set all the values of the elements for each message in the list
		Message curMsg = messages.get(position);

		//Set the sending date
		String dateasString= DateFormat.format("MM/dd/yyyy HH:MM", new Date(curMsg.getTimeReceived())).toString();
		holder.messagesent.setText("Message sent at: " + dateasString);
		
		//set the name of the receiver
		holder.touser.setText("To: " + curMsg.getMessageTo().getName());
		
		//set name of the sender 
		holder.touser.setText("From: " + curMsg.getMessageFrom().getName());
		
		//set the message contents
		holder.messagetxt.setText(curMsg.getMessageText());
		
		return row;
	}

	static class MessageHolder {
		TextView touser, fromuser, messagetxt, messagesent;
		

	}
}
