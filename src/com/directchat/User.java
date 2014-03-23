/*
 * File: User.java
 * Author: Alexander Meijer
 * Date: Sept 3, 2013
 * Class: ELEC 602 Mobile Computing
 * Version 1.0
 */

package com.directchat;

public class User implements Comparable<User>{
	private String name;
	private long last_seen, first_seen;
	private boolean is_online;
	private String ip;

	public User(String name, long lastSeen, long firstSeen, boolean isOnline, String IP){
		this.name = name;
		this.ip = IP;
		last_seen = lastSeen;
		first_seen = firstSeen;
		is_online = isOnline;
	}

	public User() {
		name = "null";
		last_seen = 0;
		first_seen = 0;
		is_online = false;
		ip = "255.255.255.255";
	}
	
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String IP){
		ip = IP;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getLast_seen() {
		return last_seen;
	}
	public void setLast_seen(long last_seen) {
		this.last_seen = last_seen;
	}
	public long getFirst_seen() {
		return first_seen;
	}
	public void setFirst_seen(long first_seen) {
		this.first_seen = first_seen;
	}
	public boolean is_online() {
		return is_online;
	}
	public void setIs_online(boolean is_online) {
		this.is_online = is_online;
	}

	@Override
	public int compareTo(User another) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;


		if(this.getLast_seen() == another.getLast_seen()){
			return EQUAL;
		}

		if(this.getLast_seen() > another.getLast_seen()){
			return BEFORE;
		}

		if(this.getLast_seen() < another.getLast_seen()){
			return AFTER;
		}


		//default
		return 0;
	}

}
