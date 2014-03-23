/*
 * File: NetworkUserInterface.java
 * Author: Alexander Meijer
 * Date: Oct 1, 2013
 * Class: ELEC 602 Mobile Computing
 * Version 1.0
 */

package com.directchat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
//handles announcement of user online? if so then the pref listen interface will need to be fleshed out
public class NetworkUserService extends Service{
	public static final String TAG = "NetworkUserService";

	private static String userName;
	private static boolean appearOnline;
	private DatagramSocket broadcastSocket;
	public static final int OFFLINE_DELAY = 10;
	DirectChat app;
	@Override
	public IBinder onBind(Intent arg0) {
		
		return null;
	}
	
	@Override
	public void onCreate(){
		Log.d(TAG, "oncreate called for  networkuserservice");
		appearOnline = true;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d(TAG, "Starting user announce thread");
		
		if(intent!=null){
			
			String newUserName = intent.getStringExtra(Preferences.EXTRA_NAME_CHANGE);
			if(newUserName!=null){
				userName=newUserName;
			}
			
			if(intent.getBooleanExtra(Preferences.EXTRA_STATUS_CHANGE_INTENT_INDICATOR, false)){
				Boolean newOnlineStatus = intent.getBooleanExtra(Preferences.EXTRA_STATUS_CHANGE, false);
				appearOnline = newOnlineStatus;
			}
	}
		
		app = (DirectChat) getApplication();
		try {
			broadcastSocket = new DatagramSocket(4001);
		} catch (SocketException e) {
	
			e.printStackTrace();
		} 

		try {
			broadcastSocket.setBroadcast(true);
		} catch (SocketException e) {
	
			e.printStackTrace();
		} 
		String Ip = Utils.getIPAddress(true);
		Log.d(TAG, "Broadcast socket established on port: "  + broadcastSocket.getLocalPort() + ", at IP: " + Ip);
		if(uid_broadcast.getState()==Thread.State.NEW){
			uid_broadcast.start();
		}
		
		if(user_search.getState()==Thread.State.NEW){
			user_search.start();
		}
		
		if(update_online.getState()==Thread.State.NEW){
			update_online.start();
		}

		return Service.START_STICKY;
	}

	InetAddress getBroadcastAddress() throws IOException {
		Context mContext = this.getApplicationContext();
		WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		// handle null somehow

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		return InetAddress.getByAddress(quads);
	}

	Thread uid_broadcast = new Thread() {
		public void run(){

			try{
				Log.d(TAG, "in broadcast thread");


				//build the datagram 

				
				while(true){
					if(appearOnline){
						if(userName!=null){
						Log.d(TAG, "STRING TO BE BROADCAST: " + userName);
						byte[] buf = userName.getBytes();
						DatagramPacket packet = new DatagramPacket(buf, buf.length, getBroadcastAddress(), 4001);
						broadcastSocket.send(packet);
					}else{
						String nullUsername = "NULL_USERNAME";
						Log.d(TAG, "STRING TO BE BROADCAST: " + nullUsername);
						byte[] buf = nullUsername.getBytes();
						DatagramPacket packet = new DatagramPacket(buf, buf.length, getBroadcastAddress(), 4001);
						broadcastSocket.send(packet);
					}
						}
					Thread.sleep(5000); //sleep for 5 s before next broadcast (as defined in standards)
				}

			} catch (Exception e){ //TODO - better network exception handling
				Log.e(TAG, "unknown username broadcast error", e);

				//MUST RETURN FOR OS TO HANDLE QUIT INTENT
				return;
			}
		}

	};

	Thread update_online = new Thread() {
		String userHandle;
		public void run(){


			try {
				while (true) {

					Log.d(TAG, "updating DB");
					Log.d(TAG, "gettin new userListCursor");
				
						ArrayList<User> allUsers = app.db.getAllUsers();
						
						for(User current_user:allUsers){
							//User current_user = app.db.cursorToUser(updateCursor.g);
							Log.d("UPDATE_SERVICE", "current_user name: " + current_user.getName());
							Log.d("UPDATE_SERVICE", "current_user last_seen: " + current_user.getLast_seen());
							
							Log.d(TAG, "evaluating user: " + current_user.getName());
							//Log.d("TAG","time difference: "+(System.currentTimeMillis() - Long.parseLong(updateCursor.getString(updateCursor.getColumnIndex(DBHelper.COLUMN_LAST_SEEN)))));
							if((System.currentTimeMillis() - current_user.getLast_seen()) > 10000/*10 sec timeout delay*/){
								//the user is offline
								Log.d("UPDATE_SERVICE", "sending user:  " + current_user.getName() + " offline");
								//create offline user
								User replacement_offline_user = new User(current_user.getName(), current_user.getLast_seen(), current_user.getLast_seen(), false, current_user.getIp());
								
								//wipe online user from DB
								app.db.deleteUser(current_user);
								
								//replace user with offline version of user
								app.db.addUser(replacement_offline_user);
								
							}
						}
							
							
							
							
						
						
			
					Thread.sleep(5000); //sleep for 5 s before next
				}
			} catch (Exception e) {
				Log.e(TAG, "Online user detection error - thread interrupted", e);

				//MUST RETURN FOR OS TO HANDLE QUIT INTENT
				return;
			}



		}

	};

	Thread user_search = new Thread() {
		public void run(){
			//always search for new users

			//from http://www.java-forums.org/advanced-java/34258-udp-broadcast-receiving.html
			try {
				byte[] buffer = new byte[2048];

				DatagramPacket packet = new DatagramPacket(buffer,
						buffer.length);
				while (true) {
					broadcastSocket.receive(packet);
					String msg = new String(buffer, 0, packet.getLength());
					
					//we dont want packets from localhost, so we only add packets from others
					String Ip = Utils.getIPAddress(true);
					Log.d("UdpRecv","packet address: "+packet.getAddress().getHostAddress());
					Log.d("UdpRecv","IP: "+Ip);
					
						//parse data
						Scanner scan = new Scanner(msg);

						User remoteuser = new User(scan.next(), System.currentTimeMillis(), System.currentTimeMillis(), true, packet.getAddress().getHostName() );
						Log.d("UdpRecv","UDP packet from another user");
						app.db.addUser(remoteuser);
						scan.close();
				



					packet.setLength(buffer.length);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	};
	

}
