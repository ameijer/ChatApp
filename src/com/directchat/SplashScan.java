/*
 * File: SplashScan.java
 * Author: Alexander Meijer
 * Date: Sept 3, 2013
 * Class: ELEC 602 Mobile Computing
 * Version 1.0
 */

package com.directchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class SplashScan extends Activity{
	public static final String TAG = "splash screen";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//boring init stuff
		super.onCreate(savedInstanceState);
		DirectChat app = ((DirectChat) getApplication());
		
		Log.d(TAG, "inflating layout");
		setContentView(R.layout.splash_scan);
		Log.d(TAG, "created, layout inflated");
		//check if the wifi is on
		if(!isWifiOn(this.getApplicationContext())){
			Log.e(TAG, "wifi determined not to be connected or connecting");
			Context context = getApplicationContext();

			//display a toast to the user, letting them know why the application failed to load
			//this method also preps the next intent
			failToast(getResources().getString(R.string.splashScreen_failToast_wifiFail), context);
			Log.d(TAG, "finish called from iswifion");

			//MUST RETURN FOR OS TO HANDLE QUIT INTENT
			return;
		}


		
		
		scan.start();
		
		//wait till thread is done
		try {
			scan.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		//by now, we have a good scan 
		//start listing activity screen
		
		Log.d(TAG, "starting main activity");
		Intent main = new Intent(SplashScan.this, MainActivity.class);
		startActivity(main);
		return;

	}//oncreate

	//run all networking stuff in separate threads
	//not strictly necessary for a splash screen but the compiler complains...
	Thread scan = new Thread() {
		public void run(){
			try{
				
				//perform network accesses here, load a db
				Log.d(TAG, "in scan thread, beginning network access");

				//simulate a network scan delay
				sleep(3000); //TODO - replace this with actual network calls!

			} catch (Exception e){ //TODO - better network exception handling
				Log.e(TAG, "unknown network scan error");
				//die gracefully on network error
				failToast(getResources().getString(R.string.splashScreen_failToast_networkFail), getApplicationContext());

				//MUST RETURN FOR OS TO HANDLE QUIT INTENT
				return;
			}
		}	
	};

	//make sure to return after this method to go back to home
	public void failToast(String message, Context context){
		// set toast message and launch toast
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();

		//die gracefully
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
		Log.d(TAG, "toast made, don't forget to end current method to quit");
		return;
	}

	//check the wifi
	protected static boolean isWifiOn(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo networkInfo = null;
		if (connectivityManager != null) {
			networkInfo =
					connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		}
		Log.d(TAG, "network info gotten");
		if(!networkInfo.isConnectedOrConnecting()){
			return false;//true for emulation
		} else {
			return true;
		}
	}
}
