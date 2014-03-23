/*
 * File: DBHelper.java
 * Author: Alexander Meijer
 * Date: Sept 5, 2013
 * Class: ELEC 602 Mobile Computing
 * Version 1.0
 */


package com.directchat;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//for the user database
public class DBHelper extends SQLiteOpenHelper{

	public static final String TAG = "DBhelper";

	//name
	public static final String DATABASE_NAME = "directchatDB.db";
	//db version, al upgrades must be monotonic 
	private static final int DATABASE_VERSION = 1;


	//User Table
	public static final String USER_TABLE_NAME = "user";
	public static final String USER_ID = "_id";
	
	//columns in user table
	public static final String COLUMN_USER = "user";
	public static final String COLUMN_FIRST_SEEN = "first_seen";
	public static final String COLUMN_LAST_SEEN = "last_seen";
	public static final String COLUMN_IS_ONLINE = "is_online";
	public static final String COLUMN_IP = "ip";
	
	//message table
	public static final String MESSAGE_TABLE_NAME = "message";
	public static final String MESSAGE_ID = "_id";
	
	//columns in message table 
	public static final String COLUMN_MESSAGE_TEXT = "message_text";
	public static final String COLUMN_MESSAGE_TIME_RECEIVED = "message_time_received";
	public static final String COLUMN_MESSAGE_FROM = "message_from";
	public static final String COLUMN_MESSAGE_TO = "message_to";
	public static final String COLUMN_MESSAGE_ISREAD = "is_read";

	private String CREATE_USER_TABLE = "create table "
            + USER_TABLE_NAME + "("
            + USER_ID + " integer primary key autoincrement, "
            + COLUMN_USER + " text not null, "
            + COLUMN_FIRST_SEEN + " text not null, "
            + COLUMN_LAST_SEEN + " text not null, "
            + COLUMN_IS_ONLINE + " INTEGER, "
            + COLUMN_IP + " text not null );";

		private String CREATE_MESSAGE_TABLE = "create table "
	            + MESSAGE_TABLE_NAME + "("
	            + MESSAGE_ID + " integer primary key autoincrement, "
	            + COLUMN_MESSAGE_TEXT + " text not null, "
	            + COLUMN_MESSAGE_TIME_RECEIVED + " text not null, "
	            + COLUMN_MESSAGE_FROM + " text not null, "
	            + COLUMN_MESSAGE_TO + " text not null, "
	            + COLUMN_MESSAGE_ISREAD + " INTEGER );";
		
		
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d(TAG, "DBhelper Construtor called");
	}

	//make tables here
	//TODO-Threading? This could take some time...
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		//use the sql from above
		db.execSQL(CREATE_MESSAGE_TABLE);
		db.execSQL(CREATE_USER_TABLE);
		
		Log.i(TAG, "Creating database...");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DBHelper.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", deleting old data");
		    db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
		    db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME);
		    onCreate(db);

	}

}
