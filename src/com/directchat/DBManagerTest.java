/*
 * File: DBManagerTest.java
 * Author: Alexander Meijer
 * Date: Sept 7, 2013
 * Class: ELEC 602 Mobile Computing
 * Version 1.0
 */
package com.directchat;

import android.content.Context;
import android.test.AndroidTestCase;
import junit.framework.TestCase;

public class DBManagerTest extends AndroidTestCase {
	DBManager dbman;
	
	public void setUp(){
		Context context = this.getContext();
		dbman = new DBManager(context);
		dbman.open();
	}
	

	public void testOpen() {
		if(!dbman.open())
			fail("opening db failed");
		
	}

	public void testAddMessage() {
		User sender = new User("first user", 1, 2, false, "1");
		User receiver = new User("first receiver", 3, 4, true, "2");
		User test = new User("person not previously in db", 99, 98, false, "3");
		Message mes = new Message("message one text", 5, sender, receiver, true);
		User dbsender = dbman.addUser(sender);
		User dbreceiver = dbman.addUser(receiver);
		dbman.addUser(test);
		Message result = dbman.addMessage(mes);
		if(!result.getMessageFrom().getName().equals("first user"))
			fail("users dont match");
		if(!result.getMessageTo().getName().equals("first receiver"))
			fail("usersdont match");
		if(!result.getMessageText().equals("message one text"))
			fail("message text dont match");
		if(result.getTimeReceived() != 5)
			fail("time received dont match");
		if(!result.isRead())
			fail("isread");
		
	}

	public void testAddUser() {
		User sender = new User("first user", 1, 2, false, "4");
		User receiver = new User("first receiver", 3, 4, true, "5");
		
		User dbsender = dbman.addUser(sender);
		User dbreceiver = dbman.addUser(receiver);
		
		if(!dbsender.getName().equals("first user")){
			fail("user not added correctly");
		}
		if(!dbreceiver.getName().equals("first receiver")){
			fail("user not added correctly");
		}
		
		
		if(dbreceiver.getFirst_seen() != 4){
			fail("first seen not added correctly");
		}
		if(dbsender.getFirst_seen() != 2){
			fail("first seen not added correctly");
		}
		
		
		if(dbreceiver.getLast_seen() != 3){
			fail("last seen not added correctly");
		}
		if(dbsender.getLast_seen() != 1){
			fail("last seen not added correctly");
		}
		
		if(dbsender.is_online() != false){
			fail("is online not added correctly");
		}
		
		if(dbreceiver.is_online() != true){
			fail("is online not added correctly");
		}
		
	}

	public void testDeleteMessage() {
		fail("Not yet implemented");
	}

	public void testDeleteUser() {
		fail("Not yet implemented");
	}

	public void testCleanDB() {
		fail("Not yet implemented");
	}

	public void testGetAllUsers() {
		fail("Not yet implemented");
	}

	public void testGetAllMessages() {
		fail("Not yet implemented");
	}

	public void testGetAllMessagesTo_FromUser() {
		fail("Not yet implemented");
	}

}
