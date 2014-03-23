/*
 * File: DBManager.java
 * Author: Alexander Meijer
 * Date: Sept 5, 2013
 * Class: ELEC 602 Mobile Computing
 * Version 1.0
 */

package com.directchat;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class DirectChat extends Application {
	
	//our global DB, abstracted through a manager
	public DBManager db;
	
	@Override
	public void onCreate() {
		super.onCreate();
		db = new DBManager(this.getApplicationContext());
		//open DB
		if(!db.open()){
			Log.e("DirectChat Application level" , "DB open failed");
			System.exit(1);
		}
		
		Log.d("DC APP" , "Starting application services");

		//start network services
		//Users service
		Log.d("Networking", "calling NetworkUserserivce");
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String username=sharedPreferences.getString("username", getResources().getString(R.string.preferences_changeUserNameTitle));
		Intent initialSetUp = new Intent(this, NetworkUserService.class);
		initialSetUp.putExtra(Preferences.EXTRA_NAME_CHANGE, username);
		initialSetUp.putExtra(Preferences.EXTRA_STATUS_CHANGE_INTENT_INDICATOR, true);
		initialSetUp.putExtra(Preferences.EXTRA_STATUS_CHANGE, sharedPreferences.getBoolean("toggle_offline", true));
		startService(initialSetUp);
		
		//message service
		startService(new Intent(this, NetworkMessageService.class));
		
	}
	
	
}
