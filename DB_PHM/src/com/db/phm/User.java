package com.db.phm;

public abstract class User {
	
	public  String username;
	
	//public String userType;
	
	public User(String username) {
		this.username = username;
	}
	
	public User(String userName, String firstName, String lastName) {
		this.username = userName;
		System.out.println("*******************Welcome*****************\n");
		System.out.println("\t\t" + firstName + " " + lastName + "!!!");
		System.out.println("\n*******************************************");
	}
	

}
