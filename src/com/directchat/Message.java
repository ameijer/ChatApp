/*
 * File: Message.java
 * Author: Alexander Meijer
 * Date: Sept 10, 2013
 * Class: ELEC 602 Mobile Computing
 * Version 1.0
 */

package com.directchat;

public class Message implements Comparable<Message>{
	private String messageText;
	private long timeReceived;
	private User messageFrom;
	private User messageTo;
	private boolean isRead;
	private long id;
	
	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}
	public Message(){
		messageText = "null";
		timeReceived = 0;
		messageFrom = null;
		messageTo = null;
		this.isRead = false;
		id = 0;
	}
	
	public Message(String MessageText, long timeRecieved, User messageFrom, User messageTo, boolean isRead){
		this.messageText = MessageText;
		this.timeReceived = timeRecieved;
		this.messageFrom = messageFrom;
		this.messageTo = messageTo;
		this.isRead = isRead;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	public String getMessageText() {
		return messageText;
	}
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	public long getTimeReceived() {
		return timeReceived;
	}
	public void setTimeReceived(long timeReceived) {
		this.timeReceived = timeReceived;
	}
	public User getMessageFrom() {
		return messageFrom;
	}
	public void setMessageFrom(User messageFrom) {
		this.messageFrom = messageFrom;
	}
	public User getMessageTo() {
		return messageTo;
	}
	public void setMessageTo(User messageTo) {
		this.messageTo = messageTo;
	}
	
	//we shall sort by time received
	@Override
	public int compareTo(Message another) {
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;
		
	    //this is very unlikely...
	    if(this.getTimeReceived() == another.getTimeReceived()){
	    	return EQUAL;
	    }
	    
	    if(this.getTimeReceived() > another.getTimeReceived()){
	    	return BEFORE;
	    }
	    
	    if(this.getTimeReceived() < another.getTimeReceived()){
	    	return AFTER;
	    }
	    
	    
	    //default
	    return 0;
	}
	
}
