/*
 * File: MessagingActivity.java
 * Author: Mateus Aires Correa de Sa
 * Date: Sept 8, 2013
 * Class: ELEC 602 Mobile Computing
 * Version 1.0
 */

package com.directchat;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MessagingActivity extends Activity {

	DirectChat app;
	Context context;
	private ArrayAdapter<Message> arrayAdapter;
	private ListView filledLV;
	private String contactName;
	ArrayList<Message> msgs;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (DirectChat)getApplication();
		setContentView(R.layout.activity_messaging);
		//asString = new ArrayList<String>();

		Intent intent = getIntent();
		contactName = intent.getStringExtra(MainActivity.EXTRA_CONTACT_NAME);

		Log.d("MESSAGING_ACTIVITY", contactName);
		User tempUser = app.db.findUserByName(contactName);
		Log.d("MESSAGING_ACTIVITY", "User: " + tempUser.getName() + " at IP: " + tempUser.getIp() + " found in DB");

		msgs = app.db.getAllMessagesTo_FromUser(tempUser);

		((TextView)findViewById(R.id.userTalkingWith)).setText(contactName);
		filledLV = (ListView) findViewById(R.id.userConversation);
		arrayAdapter = new MessageCustomArrayListAdapter(MessagingActivity.this, R.layout.row_item__message_activity_message_list_lv, msgs);
		filledLV.setAdapter(arrayAdapter);

		Thread update = new Thread() {
			public void run() {
				while(true){
					runOnUiThread(new Runnable() {
						@Override
						public void run() {

							arrayAdapter.clear();
							//asString.clear();
							msgs = app.db.getAllMessagesTo_FromUser(app.db.findUserByName(contactName));
							Log.d("MESSAGE_ACTIVITY", "Size of messages to/from user: " + msgs.size());
							for (Message msg:msgs){
								Log.d("MESSAGE_ACTIVITY", "Message in list: " + msg.getMessageText() + "from: " + msg.getMessageFrom().getName());
							}
							if(msgs != null){
							for(int i = msgs.size()-1; i >= 0; i --){

							
								arrayAdapter.add(msgs.get(i));

								
							}
						
							}

						}
					});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
				
						e.printStackTrace();
					}
				}
			}
		};

		update.start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.messaging, menu);
		return true;
	}


	public void sendMessage(View view){

		EditText editText = (EditText)findViewById(R.id.userMessageInput);
		String message = editText.getText().toString();
		Log.d("sendmessage", "in sendmessage, string to send: " + message);

		if(message.trim().length()>0){

			SharedPreferences quickprefs = PreferenceManager.getDefaultSharedPreferences(this);
			String current_uname =  quickprefs.getString("username", "DEFAULT - CHANGE UNAME");

			Log.d("sendmessage", "The username returned from default preferences as the sender is: " + current_uname);
			User tosend = app.db.findUserByName(contactName); //get from DB
			Log.d("sendmessage", "sending message to: " + tosend.getName()+ " at ip: " + tosend.getIp());
			
			User from = app.db.findUserByName(current_uname);
			if(from != null){
			Log.d("sendmessage", "User from: " + from.getName());
			} else {
				Log.d("sendmessage", "from user IS NULL");
			}
			Message msg = new Message(message, System.currentTimeMillis(), from, tosend, true);

			Log.d("sendmessage", "sending message from: " + from.getName()+ " at ip: " + from.getIp());
			//insert new message on the db and actually send it
			Message check = app.db.addMessage(msg);
			Log.d("sendmessage", "Message in DB: " + check.getMessageText());
			NetworkMessageService.send_msg(msg, getApplicationContext());

			editText.setText("");

		}
	}


}
