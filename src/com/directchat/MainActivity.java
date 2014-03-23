/*
 * File: MainActivity.java
 * Author: Mateus Aires Correa de Sa & Alexander Meijer
 * Date: Sept 5, 2013
 * Class: ELEC 602 Mobile Computing
 * Version 1.0
 */


package com.directchat;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.DashPathEffect;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Message;
import android.preference.PreferenceManager;

public class MainActivity extends Activity implements OnSharedPreferenceChangeListener{
	private DirectChat app;
	public static final String EXTRA_CONTACT_NAME = "com.directchat.CONTACT_NAME";
	private static String userName = "";

	Context context;
	ArrayList<User> onlineUsers;
	ArrayList<String> asString;
	CustomArrayListAdapter arrayAdapter;

	private ListView filledLV;
	private static boolean showOnlineOnly;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		SharedPreferences quickprefs = PreferenceManager.getDefaultSharedPreferences(this);
		showOnlineOnly = quickprefs.getBoolean("show_online_only", false);
		quickprefs.registerOnSharedPreferenceChangeListener(this);

		super.onCreate(savedInstanceState);
		context = MainActivity.this;
		app = (DirectChat) getApplication();
		setContentView(R.layout.activity_main);
		
		//populate users list from DB
		if(showOnlineOnly){
			onlineUsers = app.db.getAllOnlineUsers();
		}else{
			onlineUsers = app.db.getAllUsers();
		}
		Log.d("OnlineUserList", "size of online user list: " + onlineUsers.size());

		userName=PreferenceManager.getDefaultSharedPreferences(this).getString("username", getResources().getString(R.string.preferences_changeUserNameTitle));
		((TextView)findViewById(R.id.currentUserName)).setText(userName);
		filledLV = (ListView) findViewById(R.id.onlineUsersList);
		arrayAdapter = new CustomArrayListAdapter(MainActivity.this, R.layout.row_item__main_activity_user_list_lv, onlineUsers);
		filledLV.setAdapter(arrayAdapter);
		//Set a listItem click listener
		filledLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				onListItemClick((ListView) arg0,  arg1,  arg2,
						arg3);

			}
		});
		Thread update = new Thread() {
			public void run() {
				while(true){
					runOnUiThread(new Runnable() {
						@Override
						public void run() {

							arrayAdapter.clear();
				
							SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
							showOnlineOnly = pref.getBoolean("show_online_only", false);
							if(showOnlineOnly){
								onlineUsers = app.db.getAllUsers();

								for (int i = 0; i < onlineUsers.size(); i++){


									if(!onlineUsers.get(i).is_online()){
										onlineUsers.remove(i);
										i--;
									}
								}
							}else{
								onlineUsers = app.db.getAllUsers();
							}


							boolean allowSameAddress = false;
							for (int i = 0; i < onlineUsers.size(); i++){
								if(!allowSameAddress && onlineUsers.get(i).getName().equals(userName)) {
									onlineUsers.remove(i);
									break;
								}
							}

							for(User usr : onlineUsers){

								String tochg = usr.getName();
								arrayAdapter.add(usr);

								Log.d("asstring", "added to asString: " + tochg);

							}



						}
					});
					try {
						Thread.sleep(5000);
						
						//have another thread clean the db
						app.db.cleanDB();
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
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_settings:
			Intent i = new Intent(this, Preferences.class);
			startActivityForResult(i, 1);
			break;

		}

		return true;
	}

	//Handle clicks on the items of the listView
	protected void onListItemClick(ListView l, View v, int position, long id) {

		Intent intent = new Intent (this, MessagingActivity.class);

		String c = (String) (arrayAdapter.getItem(position).getName());
		String contactName = c;
		intent.putExtra(EXTRA_CONTACT_NAME, contactName);

		Log.d("MAIN_ACTIVITY", contactName);

		startActivity(intent);
	}

	public static String getUserName(){
		return userName;
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		//listen for show online users only
		//listen for uname change

		//listen for username change, change what is announced 
		if(key.equals("username")){
			userName = sharedPreferences.getString(key, getResources().getString(R.string.preferences_changeUserNameTitle));
			((TextView)findViewById(R.id.currentUserName)).setText(userName);
		} else if (key.equals("show_online_only")){
			//listen for the appear online network option change
			showOnlineOnly = sharedPreferences.getBoolean("show_online_only", false);
			if(showOnlineOnly){
				Log.d("MAIN_ACTIVITY","Showing Online Users Only   Showing Online Users Only   Showing Online Users Only   Showing Online Users Only   Showing Online Users Only   ");
			}else{
				Log.d("MAIN_ACTIVITY","Showing Offline Users Only   Showing Offline Users Only   Showing Offline Users Only   Showing Offline Users Only   Showing Offline Users Only   ");
			}
		}

	}








}
