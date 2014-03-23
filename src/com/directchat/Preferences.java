/*
 * File: Preferences.java
 * Author: Alexander Meijer
 * Date: Sept 11, 2013
 * Class: ELEC 602 Mobile Computing
 * Version 1.0
 */

package com.directchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	public static final String preferenceFileName = "com.DirectChat.app.SharedPreferences";
	public static final String EXTRA_NAME_CHANGE = "com.DirectChat.app.IntentExtra_NameChange";
	public static final String EXTRA_STATUS_CHANGE = "com.DirectChat.app.IntentExtra_OnlineStatusChange";
	public static final String EXTRA_STATUS_CHANGE_INTENT_INDICATOR = "com.DirectChat.app.IntentExtra_OnlineStatusChange_IntentIndicator";
	
	DirectChat app;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (DirectChat) getApplication();
		addPreferencesFromResource(R.xml.prefs);
		Log.d("shared prefs", "prefs inflated");
		
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//registering all sharedPreferenceListeners
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		//default values
		String userName=prefs.getString("username", getResources().getString(R.string.preferences_changeUserNameTitle));
		boolean appearOnlineChecked = prefs.getBoolean("toggle_offline", true);
		boolean showOnlineUsersOnlyChecked = prefs.getBoolean("show_online_only", false);
		
		EditTextPreference userNameText = (EditTextPreference)getPreferenceScreen().findPreference("username");
		userNameText.setTitle(userName);
		
		CheckBoxPreference toggle_offline = (CheckBoxPreference) getPreferenceScreen().findPreference("toggle_offline");
		toggle_offline.setChecked(appearOnlineChecked);
		
		CheckBoxPreference show_online_only = (CheckBoxPreference) getPreferenceScreen().findPreference("show_online_only");
		show_online_only.setChecked(showOnlineUsersOnlyChecked);

		//this listens to the buttons for the db operations. This is pretty gross, shouldn't have to be implemented anywhere else but here..

		Preference wiper = (Preference) findPreference("wipe_db");
		wiper.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Context context = getApplicationContext();
				//toast to show db about to be wiped
				Toast toast = Toast.makeText(context, getResources().getString(R.string.preferences_toast_wipingDB), Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 0);
				toast.show();

				//wipe db here, call a DB manager to hose it
				app.db.deleteDB(getApplicationContext());

				//tell user it is wiped
				Toast toast2 = Toast.makeText(context, getResources().getString(R.string.preferences_toast_DBWiped), Toast.LENGTH_LONG);
				toast2.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 0);
				toast2.show();
				return true;
			}
		});

		Preference cleaner = (Preference) findPreference("clean_db");
		cleaner.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Context context = getApplicationContext();
				//toast to show db about to be cleaned

				Toast toast = Toast.makeText(context, getResources().getString(R.string.preferences_toast_cleaningDB), Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 0);
				toast.show();

				//clean DB here

				//tell user it is wiped
				Toast toast2 = Toast.makeText(context, getResources().getString(R.string.preferences_toast_dbCleaned), Toast.LENGTH_LONG);
				toast2.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 0);
				toast2.show();
				return true;
			}
		});
	}



	//this is mostly for testing, to make sure the preference changes stuck
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Context context = getApplicationContext();
		Log.d("shared prefs", "shared pref change detected");
		//if its a uname change, let's change it @ in_box_title
		if(key.equals("username")){
			//Send modification to service
			String username=sharedPreferences.getString("username", getResources().getString(R.string.preferences_changeUserNameTitle));
			Intent nameChanged = new Intent(this, NetworkUserService.class);
			nameChanged.putExtra(EXTRA_NAME_CHANGE, username);
			this.startService(nameChanged);
			
			//toast to indicate change
			// set toast message and launch toast
			Toast toast = Toast.makeText(context, this.getResources().getString(R.string.preferences_toast_changeName) + sharedPreferences.getString("username", null), Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 0);
			toast.show();

			//now update the in box title 
			EditTextPreference etext = (EditTextPreference) getPreferenceScreen().findPreference("username");
			etext.setTitle(username);
			return;
		}

		//if its a netops change, let's show a toast for testing purposes
		if(key.equals("toggle_offline")){
			
			//Send modification to service
			Boolean showOnline = sharedPreferences.getBoolean("toggle_offline", false);
			Intent visibilityChanged = new Intent(this, NetworkUserService.class);
			visibilityChanged.putExtra(EXTRA_STATUS_CHANGE, showOnline);
			visibilityChanged.putExtra(EXTRA_STATUS_CHANGE_INTENT_INDICATOR, true);
			this.startService(visibilityChanged);
			
			//toast to indicate change
			// set toast message and launch toast
			Toast toast = Toast.makeText(context, this.getResources().getString(R.string.preferences_toast_toggledOnlineStatus)+" "+sharedPreferences.getBoolean("toggle_offline", false), Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 0);
			toast.show();

			return;
		}

		if(key.equals("show_online_only")){
			//toast to indicate change
			// set toast message and launch toast
			Toast toast = Toast.makeText(context, this.getResources().getString(R.string.preferences_toast_userListOnlineOnly), Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 0);
			toast.show();

			return;
		}


	}

}
