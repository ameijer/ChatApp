/*
 * File: DBManager.java
 * Author: Alexander Meijer
 * Date: Sept 5, 2013
 * Class: ELEC 602 Mobile Computing
 * Version 1.0
 */

package com.directchat;

import java.util.ArrayList;
import java.util.Collections;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class DBManager {
	private SQLiteDatabase database;
	private DBHelper dbhelper;
	public static final String TAG = "DBMANAGER";
	private Cursor userListCursor;
	private Cursor userListCursor_onlineOnly;
	private Cursor currentMessagesCursor;

	public DBManager(Context context){
		dbhelper = new DBHelper(context);
	}

	public boolean open(){
		try {
			database = dbhelper.getWritableDatabase();
		} catch (SQLiteException e){
			return false;
		}
		return true;
	}

	public void close(){
		dbhelper.close();
	}
	
	public Cursor generalQuery(String table_name){
		return database.rawQuery("select * from " + table_name,null);
	}

	public void deleteDB(Context context){
		close();
		ContextWrapper wrapper = new ContextWrapper(context);
		boolean result = wrapper.deleteDatabase(DBHelper.DATABASE_NAME);
		Log.d(TAG, "DB deleted, delete returned " + result);
	}

	//returns the message as it exists in the DB
	public synchronized Message addMessage(Message message){

		//add user to DB if they dont exist in it 
		if(findUserByName(message.getMessageFrom().getName()) == null){
			addUser(message.getMessageFrom());
			Log.d(TAG, "user not found. adding to DB");
		}


		//check for EXACT duplicate
		//ALL fields must be identical 
		Cursor cursorcheck = database.query(
				DBHelper.MESSAGE_TABLE_NAME,
				null,
				DBHelper.COLUMN_MESSAGE_TEXT + " = ? " + " AND " + DBHelper.COLUMN_MESSAGE_TIME_RECEIVED + " = ?" ,
				new String[] {message.getMessageText(), Long.toString(message.getTimeReceived())},
				null,
				null,
				null);
		cursorcheck.close();
		
		ContentValues values = new ContentValues();
		

		//load the values with the message data
		//message contents
		values.put(DBHelper.COLUMN_MESSAGE_TEXT, message.getMessageText());
		//time sent, cast to string 
		values.put(DBHelper.COLUMN_MESSAGE_TIME_RECEIVED, Long.toString(message.getTimeReceived()));
		//username of sender, stored as string
		values.put(DBHelper.COLUMN_MESSAGE_FROM, message.getMessageFrom().getName());
		//username of receiver, stored as string
		values.put(DBHelper.COLUMN_MESSAGE_TO, message.getMessageTo().getName());
		//put whether message is read or not
		if(message.isRead()){
			values.put(DBHelper.COLUMN_MESSAGE_ISREAD, 1);
		} else {
			values.put(DBHelper.COLUMN_MESSAGE_ISREAD, 0);
		}


		//insert the new message data
		long insert_id = database.insert(DBHelper.MESSAGE_TABLE_NAME, null, values);

		//query the message in the table and return it to make sure its there correctly
		Cursor cursor = database.query(DBHelper.MESSAGE_TABLE_NAME,
				null, DBHelper.MESSAGE_ID + " = " + insert_id, null,
				null, null, null);
		cursor.moveToFirst();
		Message dbMessage = cursorToMessage(cursor);
		cursor.close();

		return dbMessage;

	}

	public synchronized User addUser(User user){
		//find the user by name. if they already exist, then update the existing entry
		User existing = findUserByName(user.getName());
		ContentValues values = new ContentValues();

		if(existing == null){
			//there is no existing user

			//load the values with user data
			//username
			values.put(DBHelper.COLUMN_USER, user.getName());
			//last seen, cast to string 
			values.put(DBHelper.COLUMN_LAST_SEEN, Long.toString(user.getLast_seen()));
			//first seen, cast to string 
			values.put(DBHelper.COLUMN_FIRST_SEEN, Long.toString(user.getFirst_seen()));
			//whether user is online, as int
			if(user.is_online()){
				values.put(DBHelper.COLUMN_IS_ONLINE, 1);
			}else{
				values.put(DBHelper.COLUMN_IS_ONLINE, 0);
			}
			
			//store the IP of this user for TCP
			values.put(DBHelper.COLUMN_IP, user.getIp());

			//insert the new user data
			long insert_id = database.insert(DBHelper.USER_TABLE_NAME, null, values);

			//query the user in the table and return it to make sure its there correctly
			Cursor cursor = database.query(DBHelper.USER_TABLE_NAME,
					null, DBHelper.USER_ID + " = " + insert_id, null,
					null, null, null);
			cursor.moveToFirst();
			User result = cursorToUser(cursor);
			cursor.close();

			return result;

		} else {
			//user exists, must be modded

			//delete existing user in the table
			deleteUser(existing);

			//update online status
			if (user.is_online()){
				values.put(DBHelper.COLUMN_IS_ONLINE, 1);
			} else {
				values.put(DBHelper.COLUMN_IS_ONLINE, 0);
			}

			//update last seen 
			values.put(DBHelper.COLUMN_LAST_SEEN, Long.toString(user.getLast_seen()));

			//update first seen... the existing user will have the earlier seen time
			values.put(DBHelper.COLUMN_FIRST_SEEN, Long.toString(existing.getFirst_seen()));
			
			//store the IP of this user for TCP
			values.put(DBHelper.COLUMN_IP, user.getIp());
			//name doesnt need to be updated
			values.put(DBHelper.COLUMN_USER, user.getName());


			//insert as above
			//insert the new user data
			long insert_id = database.insert(DBHelper.USER_TABLE_NAME, null, values);

			//query the user in the table and return it to make sure its there correctly
			Cursor cursor = database.query(DBHelper.USER_TABLE_NAME,
					null, DBHelper.USER_ID + " = " + insert_id, null,
					null, null, null);
			cursor.moveToFirst();
			User result = cursorToUser(cursor);
			cursor.close();

			return result;
		}

	}

	//should strip whitespaces before/after for accurate search
	public User findUserByName(String name){
		//query user table
		Log.d("DBMANAGER", "querying db for name = " + name);
		Cursor userCursor = database.query(DBHelper.USER_TABLE_NAME, null, DBHelper.COLUMN_USER + "=?", new String[]{name}, null, null, null);
		//should only be one user, the first one...
		userCursor.moveToFirst();
		User target = cursorToUser(userCursor);
		userCursor.close();

		return target;
	}


	public synchronized Message deleteMessage(Message message){
		//make sure we dont delete the first row by accident
		if(message.getId() < 1)
			return null;

		database.delete(DBHelper.MESSAGE_TABLE_NAME, DBHelper.MESSAGE_ID + " = " + message.getId(), null);
		return message;
	}

	//null if no message
	public Message findMessageById(long id){
		Log.d("DBMANAGER", "querying db for message by id = " + id);

		//sql query
		Cursor messageCursor = database.query(DBHelper.MESSAGE_TABLE_NAME, null, DBHelper.MESSAGE_ID + " = " + id, null, null, null, null);
		if(messageCursor.getCount() < 1){
			return null;
		} else {
			Message result = cursorToMessage(messageCursor);
			return result;
		}

	}
	//returns null if user cant be found or error
	public synchronized User deleteUser(User user){
		User existing = findUserByName(user.getName());
		if(existing != null){
		if(database.delete(DBHelper.USER_TABLE_NAME, DBHelper.COLUMN_USER + " = '" + existing.getName() + "'", null) < 0) {
			return null; //error
		}
		}
		return existing;

	}

	public synchronized void cleanDB(){
		
		Thread clean_db = new Thread() {
			public void run(){
			
				ArrayList<User> userlist = new ArrayList<User>();
				Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.USER_TABLE_NAME, null);

				if(cursor.moveToFirst()){
					userlist.add(cursorToUser(cursor));
					Log.d(TAG, "added user: " + cursorToUser(cursor).getName() + " to user list from DB");
					Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
					Log.d(TAG, "Moving Cursor Pos...");
					while(cursor.moveToNext()){
						userlist.add(cursorToUser(cursor));
						Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
						Log.d(TAG, "added user: " + cursorToUser(cursor).getName() + " to user list from DB");
					}

				}
				
				
				for(int i = 0; i < userlist.size(); i++){
					User userToCompare = userlist.get(i);
					
					
					Cursor userCursor = database.query(DBHelper.USER_TABLE_NAME, null, DBHelper.COLUMN_IP + "=?", new String[]{userToCompare.getIp()}, null, null, null);
					//should only be one user, the first one...
					
					
					
					if(userCursor.getCount() > 1){
						//then there are multiple users with the ip
						Log.d("clean", "Duplicates found. Num: " + userCursor.getCount());
						userCursor.moveToFirst();
						User target = cursorToUser(userCursor);
						
						if(target.getLast_seen() > userToCompare.getLast_seen()){
							database.delete(DBHelper.USER_TABLE_NAME, DBHelper.COLUMN_USER + " = '" + userToCompare.getName() + "'", null);
							Log.d("clean", "Removed user: " + userToCompare.getName() + " was same Ip as " + target.getName());
							userToCompare = target;
							
						}
						while(userCursor.moveToNext()){
							target = cursorToUser(userCursor);
							
							if(target.getLast_seen() > userToCompare.getLast_seen()){
								database.delete(DBHelper.USER_TABLE_NAME, DBHelper.COLUMN_USER + " = '" + userToCompare.getName() + "'", null);
								Log.d("clean", "Removed user: " + userToCompare.getName() + " was same Ip as " + target.getName());
								userToCompare = target;
								
							}
						}
					}	
				}
			}
	

		};
		clean_db.start();
	}

	//return ALL users, both online and not online, sorted by time last seen
	public synchronized ArrayList<User> getAllUsers(){
		//TODO - TEST
		Log.d(TAG, "get all users");
		ArrayList<User> userlist = new ArrayList<User>();
		Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.USER_TABLE_NAME, null);

		if(cursor.moveToFirst()){
			userlist.add(cursorToUser(cursor));
			Log.d(TAG, "added user: " + cursorToUser(cursor).getName() + " to user list from DB");
			Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
			Log.d(TAG, "Moving Cursor Pos...");
			while(cursor.moveToNext()){
				userlist.add(cursorToUser(cursor));
				Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
				Log.d(TAG, "added user: " + cursorToUser(cursor).getName() + " to user list from DB");
			}

		}else {
			return userlist;
		}
		
		cursor.close();

		//sort
		Collections.sort(userlist);
		return userlist;
	}

	//get ALL messages in the DB, sorted by receipt/sent time
	public synchronized ArrayList<Message> getAllMessages(){
		//TODO - TEST
		Log.d(TAG, "get all messages");
		ArrayList<Message> messagelist = new ArrayList<Message>();
		Cursor  cursor = database.rawQuery("SELECT * FROM " + DBHelper.MESSAGE_TABLE_NAME, null);

		if(cursor.moveToFirst()){
			messagelist.add(cursorToMessage(cursor));
			Log.d(TAG, "added message: " + cursorToMessage(cursor).getMessageText()+ " to message list from DB");

			while(cursor.moveToNext()){
				messagelist.add(cursorToMessage(cursor));
				Log.d(TAG, "added message: " + cursorToMessage(cursor).getMessageText()+ " to message list from DB");
			} 
		}else {
			return null;
		}
		
		cursor.close();


		//sort for convenience 
		Collections.sort(messagelist);
		return messagelist;

	}
	
	public User findUserbyIp(String Ip){
		//query user table
				Log.d("finduserbyip", "querying db for IP = " + Ip);
				Cursor userCursor = database.query(DBHelper.USER_TABLE_NAME, null, DBHelper.COLUMN_IP + "=?", new String[]{Ip}, null, null, null);
				//should only be one user, the first one...
				Log.d("finduserbyip", "number of rows found with that IP: " + userCursor.getCount());
				userCursor.moveToFirst();
				User target = cursorToUser(userCursor);
				userCursor.close();
				return target;
		
	}

	//get all messages that involve a certain user, sorted by receipt/sent time
	public synchronized ArrayList<Message> getAllMessagesTo_FromUser(User user){

		ArrayList<Message> messagelist = new ArrayList<Message>();

		//complex query
		Cursor cursor = database.query(DBHelper.MESSAGE_TABLE_NAME, null, DBHelper.COLUMN_MESSAGE_TO + "=? OR " + DBHelper.COLUMN_MESSAGE_FROM +"=?", new String[]{user.getName(), user.getName()}, null, null, null);

		if(cursor.moveToFirst()){
			messagelist.add(cursorToMessage(cursor));
			Log.d(TAG, "added message: " + cursorToMessage(cursor).getMessageText()+ " to message list from DB");

			while(cursor.moveToNext()){
				messagelist.add(cursorToMessage(cursor));
				Log.d(TAG, "added message: " + cursorToMessage(cursor).getMessageText()+ " to message list from DB");
			} 
		}else {
			return messagelist;
		}
		
		cursor.close();

		//sort for convenience 
		Collections.sort(messagelist);
		return messagelist;
	}


	//will return the first user that the cursor is pointing to
 public synchronized User cursorToUser(Cursor cursor){
		Log.d("DBMANAGER", "cursor.getcount = " + cursor.getCount());
		
		if(cursor.getCount() < 1 ){
			return null;
		}
		
		

		User user = new User();
		user.setName(cursor.getString(1));
		user.setFirst_seen(Long.parseLong(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FIRST_SEEN))));
		user.setLast_seen(Long.parseLong(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_LAST_SEEN))));

		//since java insists upon typing the boolean here...
		if(cursor.getInt(4) != 0){
			user.setIs_online(true);
		}else {
			user.setIs_online(false);
		}
		
		user.setIp(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IP)));
		
		return user;
	}

	//will return the first message in the cursor list 
	private synchronized Message cursorToMessage(Cursor cursor){
		Message message = new Message();
		User sender;
		User receiver;
		if(cursor.getCount() < 1 ){
			return null;
		}
		
		//we know the from user's name in column 3
		String from_user_name = cursor.getString(3);

		//we know the to user's name is in column 4
		String to_user_name = cursor.getString(4);

		//now query user table to get user who sent message
		Cursor userCursor = database.query(DBHelper.USER_TABLE_NAME, null, DBHelper.COLUMN_USER + "=?", new String[]{from_user_name}, null, null, null);
		//should only be one user, the first one...
		userCursor.moveToFirst();
		sender = cursorToUser(userCursor);
		userCursor.close();

		//now query user table to get user who received message
		Cursor userCursor2 = database.query(DBHelper.USER_TABLE_NAME, null, DBHelper.COLUMN_USER + "=?", new String[]{to_user_name}, null, null, null);
		//should only be one user, the first one...
		userCursor2.moveToFirst();
		receiver = cursorToUser(userCursor2);
		userCursor2.close();


		//now we can build the message
		message.setId(cursor.getLong(0));
		message.setMessageFrom(sender);
		message.setMessageTo(receiver);
		message.setMessageText(cursor.getString(1));
		message.setTimeReceived(Long.parseLong(cursor.getString(2)));
		if(cursor.getInt(5) == 0){
			message.setRead(false);
		} else {
			message.setRead(true);
		}
		return message;
	}
	
	
	public Cursor getUserListCursor(){
			
			return database.query(
					DBHelper.USER_TABLE_NAME,
					new String[] {DBHelper.USER_ID, DBHelper.COLUMN_IS_ONLINE, DBHelper.COLUMN_USER, DBHelper.COLUMN_LAST_SEEN,DBHelper.COLUMN_FIRST_SEEN,DBHelper.COLUMN_IP},
					null,
					null,
					null,
					null,
					DBHelper.COLUMN_IS_ONLINE + " DESC, " + DBHelper.COLUMN_USER);
	}
	
	public ArrayList<User> getAllOnlineUsers(){
				Log.d(TAG, "get all users");
				ArrayList<User> userlist = new ArrayList<User>();
				Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.USER_TABLE_NAME + " WHERE "+DBHelper.COLUMN_IS_ONLINE+" =1", null);

				if(cursor.moveToFirst()){
					userlist.add(cursorToUser(cursor));
					Log.d(TAG, "added user: " + cursorToUser(cursor).getName() + " to user list from DB");
					Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
					Log.d(TAG, "Moving Cursor Pos...");
					while(cursor.moveToNext()){
						userlist.add(cursorToUser(cursor));
						Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
						Log.d(TAG, "added user: " + cursorToUser(cursor).getName() + " to user list from DB");
					}

				}else {
					return null;
				}
				
				cursor.close();

				//sort
				Collections.sort(userlist);
				return userlist;
	}

	public Cursor getMessagesToFromUserCursor(String userHandle) {
		currentMessagesCursor = database.query(DBHelper.MESSAGE_TABLE_NAME,
				null,
				DBHelper.COLUMN_MESSAGE_FROM+" = '"+userHandle+"' OR "+DBHelper.COLUMN_MESSAGE_TO+" = '"+userHandle+"'",
				null,
				null,
				null,
				DBHelper.COLUMN_MESSAGE_TIME_RECEIVED);
		return currentMessagesCursor;
	}
	
	public void setOffline(String userHandle){
		
		ContentValues args = new ContentValues();
		args.put(DBHelper.COLUMN_IS_ONLINE, 0);
		database.update(DBHelper.USER_TABLE_NAME, args, DBHelper.COLUMN_USER+" = '"+userHandle+"'", null);
		
	}

}
