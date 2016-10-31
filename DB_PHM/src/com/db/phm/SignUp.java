package com.db.phm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;


public class SignUp {
	
	private SQLConnection sqlConn;
	
	public SignUp(SQLConnection sqlConn){
		
		this.sqlConn = sqlConn;
		// TODO Auto-generated constructor stub
		try{
			enterUserDetails();
		}
		catch(Exception e)
		{
			throw e;
		}
		
	}
	public SignUp(){
		
		enterUserDetails();
	}


	public void enterUserDetails(){
		// TODO Auto-generated method stub
		String username = null;
		String firstname = null;
		String lastname = null;
		String gender = null;
		String apartment = null;
		String street = null;
		String city = null;
		String state = null;
		String dob = null;
		String isPatient = null;
		String isHS = null;
		String isSick = null;
		String password = null;
		String zipcode = null;
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter a user id of your choice");
		username = scanner.nextLine();
		System.out.println("Enter your first name");
		firstname = scanner.nextLine();
		System.out.println("Enter your last name");
		lastname = scanner.nextLine();
		System.out.println("Enter your gender");
		gender = scanner.nextLine();		
		System.out.println("Enter your apartment");
		apartment = scanner.nextLine();
		System.out.println("Enter your street");
		street = scanner.nextLine();		
		System.out.println("Enter your city");
		city = scanner.nextLine();		
		System.out.println("Enter your state");
		state = scanner.nextLine();		
		System.out.println("Enter your date of birth in yyyy-mm-dd format");
		dob = scanner.nextLine();		
		System.out.println("Do you want to signup as patient?\n YES - Enter 1\n NO- Enter 0");
		isPatient = scanner.nextLine();		
		System.out.println("Do you want to signup as health supporter?\n YES - Enter 1\n NO- Enter 0");
		isHS = scanner.nextLine();
		System.out.println("Are you sick?\n YES - Enter 1\n NO- Enter 0");
		isSick = scanner.nextLine();
		System.out.println("Set your password");
		password = scanner.nextLine();	
		System.out.println("Enter your zipcode");
		zipcode = scanner.nextLine();
		
		try {
		insertInDB(username, firstname,lastname, gender,apartment,street,city, state,dob,isPatient,isHS, isSick, password,zipcode);
		
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void insertInDB(String username, String firstname, String lastname, String gender, String apartment,
			String street, String city, String state, String dob, String isPatient, String isHS, String isSick,
			String password, String zipcode) throws SQLException{ 
		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		try{
			String selectSQL = "INSERT INTO Users"+"(UserId, FirstName, LastName, Gender, Apartment, Street, City, State, DOB, isPatient, isHS, isSick, password, zipcode) VALUES" + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			Helper.printMessage("Insert query: \n"+ selectSQL);
			sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, firstname);
			preparedStatement.setString(3, lastname);
			preparedStatement.setString(4, gender);
			preparedStatement.setString(5, apartment);
			preparedStatement.setString(6, street);
			preparedStatement.setString(7, city);
			preparedStatement.setString(8, state);
			preparedStatement.setDate(9, java.sql.Date.valueOf(dob));
			preparedStatement.setInt(10, Integer.parseInt(isPatient));
			preparedStatement.setInt(11, Integer.parseInt(isHS));
			preparedStatement.setInt(12, Integer.parseInt(isSick));
			preparedStatement.setString(13, password);
			preparedStatement.setString(14, zipcode);
			int result = preparedStatement.executeUpdate();
			System.out.println("Result of insert is: "+result);
			
			System.out.println("Sign up successful! Please login to continue");
			Welcome wc = new Welcome();
			wc.homeScreen();
	}
	catch (SQLException e) {
		System.out.println("Something went wrong."+" Please try again.");
		//Welcome wc = new Welcome();
		//wc.homeScreen();
		throw e;

	}
		finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlcon != null){
				sqlcon.terminateSQLconnection();
			}
			}
	}

}
