package com.db.phm;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;


public class Login {
	
	private SQLConnection sqlConn;
	
	public Login(SQLConnection sqlConn) throws IncorrectCredentialException{
		
		this.sqlConn = sqlConn;
		// TODO Auto-generated constructor stub
		try{
			validateLogin();
		}
		catch(Exception e)
		{
			throw e;
		}
	}
	
	public Login()throws IncorrectCredentialException{
		try{
			validateLogin();
		}
		catch(Exception e)
		{
			throw e;
		}
		
	}
	private void validateLogin() throws IncorrectCredentialException{
		
		String username = null;
		String password = null;
		System.out.println("Enter user id:");
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		username = scanner.nextLine();
		System.out.println("Enter password:");
		password = scanner.nextLine();
		
		//System.out.println("Username is " + username + " and password is " + password);
		try {
		validateUserLogin(username, password);
		
		} 
		catch(IncorrectCredentialException e){
			throw e;
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void validateUserLogin(String username, String password) throws Exception { 
		// TODO Auto-generated method stub
		
		String pwd = null;
		String isPatient = null;
		String isHS = null;
		String isSick = null;
		String firstname = null;
		String lastname = null;
		String hasSplReco = null;
		SQLConnection sqlcon = null;
       
			PreparedStatement preparedStatement = null;
			try{
				String selectSQL = "SELECT FirstName, LastName, password, isPatient, isHS, isSick,HASSPLRECO FROM Users WHERE UserId = ? and password = ?";
				sqlcon = new SQLConnection();
				preparedStatement = sqlcon.conn.prepareCall(selectSQL);
				preparedStatement.setString(1, username);
				preparedStatement.setString(2, password);
				//System.out.println("Query executed is: \n"+ preparedStatement);
				ResultSet rs = preparedStatement.executeQuery();
				while (rs.next()) {
					
					 firstname = rs.getString("FirstName");
					 lastname = rs.getString("LastName");
					 pwd = rs.getString("password");
					 isPatient = rs.getString("isPatient");
					 isHS = rs.getString("isHS");
					 isSick = rs.getString("isSick");
					 hasSplReco = rs.getString("HASSPLRECO");
//					System.out.println("firstname : " + firstname);
//					System.out.println("lastname : " + lastname);					
//					System.out.println("firstname : " + firstname);
//					System.out.println("pwd : " + pwd);
//					System.out.println("ispatient : " + isPatient);
//					System.out.println("isHS : " + isHS);
//					System.out.println("isSick : " + isSick);
				}
				if(pwd == null)
					throw new IncorrectCredentialException();
				if(pwd != null && password.equals(pwd))
				{
					/*
					Helper.printMessage("Login successful");
					if(Integer.parseInt(isPatient) == 1)
					{
						//Helper.printMessage("Need to show patient view");
						patientScreen(username, password, firstname, lastname, isSick, hasSplReco);
					}
					else if(Integer.parseInt(isHS) == 1)
					{
						//Helper.printMessage("Need to show Health supporter view");
						healthSupporterScreen(username, firstname, lastname);
					}
					else
					{
						// if both patient and health supporter, show the patient view 
						Helper.printMessage("Need to show patient view");
					}	
					*/					
					Helper.printMessage("\n----- Login successful ----");
					if(Integer.parseInt(isPatient) == 1)
					{	if(Integer.parseInt(isHS) == 1){
							System.out.println("\n_______________________________");
							System.out.println("      Account selection   ");
							System.out.println("_______________________________");
							System.out.println("\n Account type\n");
							System.out.println("1. Patient\t\t 2. Health Supporter");
							System.out.println("Enter choice [1,2]:");
							Scanner scan = new Scanner(System.in);
							int choice = scan.nextInt();
							if(choice == 2) 
					{
								healthSupporterScreen(username, password, firstname, lastname, isSick, hasSplReco);
								}
							else{
								patientScreen(username, password, firstname, lastname, isSick, hasSplReco);
						
							}
						}	
						//Helper.printMessage("Need to show patient view");
						else
						patientScreen(username, password, firstname, lastname, isSick, hasSplReco);
					}
					else if(Integer.parseInt(isHS) == 1)
					{
						//Helper.printMessage("Need to show Health supporter view");
						healthSupporterScreen(username, password, firstname, lastname, isSick, hasSplReco);
					}
					else
					{
						// if both patient and health supporter, show the patient view 
						Helper.printMessage("Need to show patient view");
					}						
				}
		}
		catch (IncorrectCredentialException e){
			System.out.println(e.toString()+" Please try again.");
			Welcome wc = new Welcome();
			wc.homeScreen();
		}
		catch (SQLException e) {
			throw e;
		}
			finally {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if(sqlcon != null) {
					sqlcon.terminateSQLconnection();
				}
				}
}

	private void healthSupporterScreen(String username, String password, String firstname, String lastname, String isSick, String hasSplreco) {
		// TODO Auto-generated method stub
		//System.out.println("Welcome "+firstname +" " +lastname);
		HealthSupporter hs = new HealthSupporter(username, password, firstname, lastname, isSick, hasSplreco);
		//
	}

	private void patientScreen(String username, String password, String firstname, String lastname, String isSick, String hasSplreco) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("Welcome "+firstname +" " +lastname);
		Patient patient = new Patient(username, firstname, lastname, isSick, hasSplreco);
	}
}
		
