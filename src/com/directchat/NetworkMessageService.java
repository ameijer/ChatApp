/*
 * File: NetworkMessageService.java
 * Author: Alexander Meijer
 * Date: Oct 1, 2013
 * Class: ELEC 602 Mobile Computing
 * Version 1.0
 */


package com.directchat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class NetworkMessageService extends Service{
	DirectChat app;
	public static final String TAG = "networkmessageservice";
	@Override
	public IBinder onBind(Intent intent) {
		Log.d("NetworkMessageService", "onBind called");
		return null;
	}

	@Override 
	public void onCreate() {
		Log.d("NetworkMessageService", "onCreate called");
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("NetworkMessageService", "onStartCommand called");
		app = ((DirectChat)this.getApplication());

		if (!message_search.isAlive()) {
			message_search.start();
		}

		//OLD TEST CODE
		/*//this contains test code that creates data similar to what we expect this method to return
		ArrayList<Message> TEST_msgs = TEST_getMessageList();

		//add each message to the DB
		for (int i = 0; i < TEST_msgs.size(); i++){
			app.db.addMessage(TEST_msgs.get(i));
			Log.d("TEST_network manager", "test message added to DB");
		}

		Log.d("TEST_network manager", "test messages added to DB");*/

		return Service.START_STICKY;
	}

	Thread message_search = new Thread() {
		public void run(){
			ServerSocket incoming = null;

			try {
				incoming = new ServerSocket(4444);
				Log.d("msg_search", "beginning message search thread");
				//accept incoming connections
				while(true){ //listen always
					Socket sockin = incoming.accept();
					Log.d(TAG, "incoming message accepted");
					InputStream instream = sockin.getInputStream();
					InputStreamReader instrr= new InputStreamReader(instream);
					String IP = sockin.getRemoteSocketAddress().toString();
					Log.d("msg_search","IP of incoming connection resolved to be:" + IP);

					BufferedReader bufferdin = new BufferedReader(instrr);
					String readstring = "";
					
		

					//now string is ready to be placed into a message + added to db
					String incomingtxt = bufferdin.readLine();
					Log.d("RECEIVED", "Received message: " + incomingtxt);
					bufferdin.close();

					String processedIP = IP.substring(1, IP.indexOf(":"));
					User fromUser = app.db.findUserbyIp(processedIP);
					Log.d("RECEIVED", "  Message received from user: " + fromUser.getName());
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(NetworkMessageService.this);
					String current_uname = prefs.getString("username", "@string/preferences_changeUserNameTitle");
					Message toAdd = new Message(incomingtxt, System.currentTimeMillis(), fromUser, app.db.findUserByName(current_uname), false);
					Message added = app.db.addMessage(toAdd);
					Log.d("RECEIVED", "Message with contents: " + added.getMessageText() + " from user: " + added.getMessageFrom().getName() + " added to db");

				
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	

	public static void send_msg(Message toSend, Context context){

		class Send_msg implements Runnable {
			private Message toSend;
			private Context context;
			public Send_msg(Message msg, Context context) {
				// store parameter for later user
				toSend = msg;
				this.context = context;
			}

			public void run() {
				//find & store target IP and strings
				String targetIp = toSend.getMessageTo().getIp();
				String messageContents = toSend.getMessageText() + '\n';

				//establish socket

				try {
					Socket sock = new Socket(targetIp, 4444); //server on 4444 according to our standards
					PrintWriter outstream = new PrintWriter(sock.getOutputStream());





					Log.d(TAG, "writing string: " + messageContents + " to socket");
					outstream.println(messageContents);
					//clean up the sockets
					outstream.flush();
					outstream.close();
					Log.d(TAG, "string witten to socket. socket flushed");
					sock.close();


				} catch (UnknownHostException e) {
					Log.e(TAG, "Unknown host exception in send message thread", e);

					//print a friendly message for our user
					NetworkMessageService.failToast("unable to connect to other user. try again later", context.getApplicationContext());
				} catch (IOException e) {
					Log.e(TAG, "Unknown IO exception in send message thread", e);

					//print a friendly message for our user
					NetworkMessageService.failToast("error sending message. try again later", context.getApplicationContext());
				}


			}
		}
		Thread t = new Thread(new Send_msg(toSend, context));
		t.start();

	}

	//make sure to return after this method to go back to home
	public static void failToast(String message, Context context){
		// set toast message and launch toast
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();

		Log.d(TAG, "toast made, don't forget to end current method to quit");
		return;
	}
	public ArrayList<Message> TEST_getMessageList(){
		ArrayList<Message> messagelist = new ArrayList<Message>();

		//test users -- maybe move this code to the network users service 
		User user1 = new User("User 1", 100, 90, false, "1");
		User user2 = new User("User 2", 200, 190, true, "2");
		User user3 = new User("User 3", 300, 290, true, "3");
		User user4 = new User("User 4", 400, 390, false, "4");
		User user5 = new User("User 5", 500, 490, false, "5");
		User user6 = new User("User 6", 600, 590, true, "6");
		User me = new User("ME", 999999, 999998, true, "7");

		Message m1 = new Message("The first message received from user 1", 91, user1, me, true);
		Message m2 = new Message("The second message received from user 1", 100, user1, me, false);
		Message m3 = new Message("The first message received from user 2", 191, user2, me, true);
		Message m4 = new Message("The second message received from user 2", 192, user2, me, true);
		Message m5 = new Message("The third message received from user 2", 193, user2, me, true);
		Message m6 = new Message("The first message sent to user 2", 201, me, user2, false); //is read is essentially irrelevant for sends
		Message m7 = new Message("The first message received from user 3", 291, user3, me, false);
		Message m8 = new Message("The first message received from user 4", 391, user4, me, false);
		Message m9 = new Message("The first message received from user 5", 491, user5, me, true);
		Message m10 = new Message("The first message received from user 6", 591, user6, me, false);
		Message m11 = new Message("The second message received from user 6", 592, user6, me, false);

		messagelist.add(m1);
		messagelist.add(m2);
		messagelist.add(m3);
		messagelist.add(m4);
		messagelist.add(m5);
		messagelist.add(m6);
		messagelist.add(m7);
		messagelist.add(m8);
		messagelist.add(m9);
		messagelist.add(m10);
		messagelist.add(m11);

		return messagelist;
	}

}
