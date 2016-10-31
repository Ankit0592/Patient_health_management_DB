
package com.db.phm;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Scanner;

/* Changes Made to Other Files:
 * 
	Patient.java - added these lines to case 5 ofselectAnAction()
  
	PatientSupporter ps = new PatientSupporter(username, firstname, lastname, isSick);
	ps.showHealthSupporter();
	
	@Rashmi please add code into marked places to call back to patient page. I was loosing context of data .
 
 */

public class PatientSupporter {
	//public static Patient patient;
SQLConnection sqlcon;
Patient patient = null;

public String username = null;
public String firstname = null;
public String lastname = null;
public String isSick = null;
public String hasSplReco = null;

private ArrayList<String> healthSupporterIDList;
private ArrayList<String> dateAuthorisedList;
private ArrayList<String> supporterTypeList;

public PatientSupporter(String username, String firstname, String lastname, String isSick, String hasSplReco)
{
	this.username = username;
	this.firstname = firstname;
	this.lastname = lastname;
	this.isSick = isSick;
	this.hasSplReco = hasSplReco;
	
}


// VIEW HEALTH SUPPORTERS DETAILS
public void showHealthSupporter() throws SQLException{
	//this.patient = patient;
	System.out.println("\n\n*****************************************************");
	System.out.println("*                                                   *");
	System.out.println("*            Patient Health Supporter Page          *");
	System.out.println("*                                                   *");
	System.out.println("*****************************************************\n");
	
	healthSupporterIDList= new ArrayList<String>();
	dateAuthorisedList = new ArrayList<String>();
	supporterTypeList = new ArrayList<String>();
	
	PreparedStatement preparedStatement = null;
	sqlcon = null;
	// FETCHING HS_IDs and date Authorised
	try{
		String selectSQL = "SELECT HS_UserId, dateAuthorised,HStype FROM Supports WHERE P_UserId = ?";
		sqlcon = new SQLConnection();
		preparedStatement = sqlcon.conn.prepareCall(selectSQL);
		preparedStatement.setString(1, username);
		ResultSet rs = preparedStatement.executeQuery();
		int i=0;
		while(rs.next()){
			healthSupporterIDList.add(rs.getString("HS_UserId"));
			dateAuthorisedList.add(rs.getString("dateAuthorised"));
			supporterTypeList.add(rs.getString("HStype"));
			//--------DEBUG CODE---------------start
			//Helper.printMessage("HS_UserId: " +healthSupporterIDList[i]);
			//Helper.printMessage("dateAuthorised: " +dateAuthorisedList[i]);
			//--------DEBUG CODE---------------end
			i++;
		}
	}catch (SQLException e) {
		// TODO Auto-generated catch block
		System.out.println("Something went wrong. Try again!");
		e.printStackTrace();
		//updateProfileMenu();
	}			
	finally {
		if (preparedStatement != null) {
			preparedStatement.close();
		}
		if(sqlcon != null)
			sqlcon.terminateSQLconnection();
	}
	if(healthSupporterIDList.size()==0){
		System.out.println("------------------------------------No supporters currently Added---------------------------------");
	}else{
		// Fetching Health Supporters Details
		try{
			
			String selectSQL=null;
			if(healthSupporterIDList.size()==1){
				selectSQL = "SELECT UserId, firstName, lastName, Gender, Apartment, Street, City, State, zipcode, DOB, isHS, isSick FROM Users WHERE UserId = ?";
				sqlcon = new SQLConnection();
				preparedStatement = sqlcon.conn.prepareCall(selectSQL);
				preparedStatement.setString(1, healthSupporterIDList.get(0));
			}else if(healthSupporterIDList.size()==2){
				selectSQL = "SELECT UserId, firstName, lastName, Gender, Apartment, Street, City, State, zipcode, DOB, isHS, isSick FROM Users WHERE UserId = ? OR UserId = ?";
				sqlcon = new SQLConnection();
				preparedStatement = sqlcon.conn.prepareCall(selectSQL);
				preparedStatement.setString(1, healthSupporterIDList.get(0));
				preparedStatement.setString(2, healthSupporterIDList.get(1));
			}
			
			ResultSet rs = preparedStatement.executeQuery();
			System.out.println("Below are Health supporters\n\n");
			while(rs.next()){
				String UserID = rs.getString("UserId");
				String firstName = rs.getString("firstName");
				String lastName = rs.getString("lastName");
				String gender = rs.getString("Gender");
				String apartment = rs.getString("Apartment");
				String street = rs.getString("Street");
				String city = rs.getString("City");
				String state = rs.getString("State");
				String zipcode = rs.getString("zipcode");
				Date dob  = rs.getDate("DOB");
				String isHS = rs.getString("isHS");
				String isSick = rs.getString("isSick");
				String patientCategory = null;
				String userType= null;
				//HealthSupporters[i]= new Patient(UserID, firstName, lastName, gender, apartment, street, city, state, zipcode, dob, isHS, isSick);
				
				int hsUserIDIndex = healthSupporterIDList.indexOf(UserID);
				System.out.println("UID: "+UserID+"\t\tAuthorisedDate: "+dateAuthorisedList.get(hsUserIDIndex)+"\t\tType: "+supporterTypeList.get(hsUserIDIndex));
				System.out.println("Name: "+firstName+" "+lastName);
				System.out.println("Address: "+apartment+", "+street+", "+city+", " +state+", "+zipcode+"\n--------------------------\n");
			}//While loop ends
		
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong. Try again!");
			e.printStackTrace();
			//updateProfileMenu();
		}			
		finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlcon != null)
				sqlcon.terminateSQLconnection();
		}
	}//end of ELSE for fetching Health Supporters count
	// Generating UI for Health Supporters.
	// if list empty give option of only to add health supporters
	if(healthSupporterIDList.size()==0){
		boolean flag=true;
		while(flag){
			try{
				System.out.println("\nPatient Health supporter option\n1. Add Primary Health Supporter\n2. Go back to Patient Home Page \n2. Logout");
				System.out.println("Enter choice[1-3]:");
				int value = Integer.parseInt(Helper.enteredConsoleString());
				switch(value)
				{
				case 1: 
					addHealthSupporter("Primary");
					flag=false;
					showHealthSupporter();
					break;
				case 2:
					flag = false;
					new Welcome().printBanner();
					new Patient(username, firstname, lastname, isSick, hasSplReco).showMenuItems();				
					break;
				case 3:
					flag = false;
					Welcome wc = new Welcome();
					wc.logout();					
					break; 
				default:
					System.out.println("Invalid Option, Please Try Again");
					break;
				}
			}catch(Exception e){
				System.out.println("Invalid Text entered on Health Supporter Main Page, Please try again");
			}
			
		}
		
		
	}else if( healthSupporterIDList.size()==2){
		boolean flag = true;
		while(flag){
			try{
				System.out.println("\nPatient Health supporter option\n1. Delete Secondary Health Supporter\n2. Update Health Supporter\n3. Go back to Patient Home Page \n4. Logout");
				System.out.println("Enter choice[1-4]:");
				int value = Integer.parseInt(Helper.enteredConsoleString());
				switch(value)
				{
				case 1: 
					DeleteHealthSupporter("Secondary");
					flag = false;
					showHealthSupporter();
					break;
				case 2:
					UpdateHealthSupporter();
					flag = false;
					showHealthSupporter();
					break;
				case 3:
					new Welcome().printBanner();
					flag = false;
					new Patient(username, firstname, lastname, isSick, hasSplReco).showMenuItems();				
					break;
				case 4:
					flag = false;
					Welcome wc = new Welcome();
					wc.logout();					
					break; 
				default:
					System.out.println("Invalid Option, Please Try Again");
					break;
				}
			}catch(Exception e){
				System.out.println("Invalid Text entered on Health Supporter Main Page, Please try again");
			}
		}
		
		
	}else if( healthSupporterIDList.size()==1){
		boolean flag = true;
		while(flag){
			try{
				System.out.println("Choose option, 1:Add Secondary Health Supporter \t\t\t 2:Update Health Supporter ");
				System.out.println("Choose option, 3:Delete Primary Health Supporter \t\t\t 4:Go back to Patient Home Page \t\t\t 4: Logout");
				System.out.println("\nPatient Health supporter option\n1. Add Secondary Health Supporter\n2. Update Health Supporter\n3. Delete Primary Health Supporter\n4. Go back to Patient Home Page\n5. Logout");
				System.out.println("Enter choice[1-5]:");
				int value = Integer.parseInt(Helper.enteredConsoleString());
				switch(value)
				{
				case 1: 
					addHealthSupporter("Secondary");
					flag = false;
					showHealthSupporter();
					break;
				case 2:
					UpdateHealthSupporter();
					flag = false;
					showHealthSupporter();
					break;
				case 3:
					DeleteHealthSupporter("Primary");
					flag = false;
					showHealthSupporter();
					break;
				case 4:
					flag = false;
					new Welcome().printBanner();
					new Patient(username, firstname, lastname, isSick, hasSplReco).showMenuItems();
					break;
				case 5:
					flag = false;
					Welcome wc = new Welcome();
					wc.logout();					
					break; 					
				default:
					System.out.println("Invalid Option, Please Try Again");
					break;
				}
			}catch(Exception e){
				System.out.println("Invalid Text entered on Health Supporter Main Page, Please try again");
			}
		}
	}
}	

// ADD HEALTH SUPPORTER
private void addHealthSupporter(String type) throws Exception{
	//this.patient = patient;
	
	// Display list of all available UserIds
	ArrayList<String> userIDList = new ArrayList<String>();
	PreparedStatement preparedStatement = null;
	try{
		String selectSQL = "SELECT UserId FROM Users";
		sqlcon = new SQLConnection();
		preparedStatement = sqlcon.conn.prepareCall(selectSQL);
		ResultSet rs = preparedStatement.executeQuery();
		while(rs.next()){
			String userId = rs.getString("UserId");
			userIDList.add(userId);
		}
		// remove self id and current HS ids, and display remaining ones.
		userIDList.remove(username);
		for(int i=0;i<healthSupporterIDList.size();i++){
			userIDList.remove(healthSupporterIDList.get(i));
		}
		// display remaining ids.
		System.out.println("Available User Ids");
		Iterator<String> userIdListIterator = userIDList.iterator();
		while(userIdListIterator.hasNext()){
			System.out.print(userIdListIterator.next()+", ");
		}
		System.out.println(" ");
		
		//enter ids to add and do so after a check
		System.out.println("\n Please enter the available userId to add or 'back' to go to Health Supporter Main Page");
		boolean flag=true;
		while(flag){
			String input = Helper.enteredConsoleString();
			if(userIDList.contains(input)){
				// code to enter into database
				System.out.println("Adding Health Supporter for Patient "+username+" -----------");
				selectSQL = "INSERT into Supports values(?,?,?,?)";
				sqlcon = new SQLConnection();
				preparedStatement = sqlcon.conn.prepareCall(selectSQL);
				preparedStatement.setString(1, username);
				preparedStatement.setString(2, input);
				java.sql.Date currentDate = new Date(Calendar.getInstance().getTimeInMillis()); //getting current date
				preparedStatement.setDate(3, currentDate);
				preparedStatement.setString(4, type);
				int rowUpdateCount = preparedStatement.executeUpdate();
				System.out.println(rowUpdateCount+" Row(s) Updated");
				sqlcon.conn.commit();
				// go back to displaying updated health supporters
				//showHealthSupporter();
				flag=false;
			}else if(input.equalsIgnoreCase("back")){			
				flag=false;
			}else{
				System.out.println("invalid ID/ input option, Please try again");
			}
		}
		
		
	}catch (SQLException e) {
		// TODO Auto-generated catch block
		System.out.println("Error: Fetching IDs from the user's table!  Try again!");
		e.printStackTrace();
		
	}			
	finally {
		if (preparedStatement != null) {
			preparedStatement.close();
		}
		if(sqlcon != null)
			sqlcon.terminateSQLconnection();
	}
}

// UPDATE HEALTH SUPPORTER
private void UpdateHealthSupporter() throws Exception{
	boolean flag = true;
	while(flag){
		System.out.println("Enter the UserId of the health supporter you want to update");
		System.out.println("Enter 'back' to go to Patient Health Supporter Screen");
		String hsIdInput = Helper.enteredConsoleString();
		if(hsIdInput.equalsIgnoreCase("back")){
			flag=false;
		}else if(healthSupporterIDList.contains(hsIdInput)){
			// Update HS
			System.out.println("Please enter a number for the parameter you want to update about the Selected Health Supporter");
			System.out.println("1.FirstName \t 2.LastName \t 3.Apartment \t 4.Street");
			System.out.println("5.City \t 6.State \t 7.Zipcode \t 8.Authorization Date \t 9.GO BACK TO PATIENT HEALTH SUPPORTER SCREEN" );
			boolean innerFlag = true;
			while(innerFlag){
				try{
					//Helper.printMessage("Enter the new value for this field");
					Scanner in2 = new Scanner(System.in);
					int parameterInput = Integer.parseInt(in2.nextLine());
					Helper.printMessage("Enter the new value");
					switch (parameterInput) {
					case 1:
						updateUserProfileData(hsIdInput, "FirstName", Helper.getConsoleValue());
						innerFlag=false;
						break;
					case 2:
						updateUserProfileData(hsIdInput, "LastName", Helper.getConsoleValue());
						innerFlag=false;
						break;
					case 3:
						updateUserProfileData(hsIdInput, "Apartment", Helper.getConsoleValue());
						innerFlag=false;
						break;
					case 4:
						updateUserProfileData(hsIdInput, "Street", Helper.getConsoleValue());
						innerFlag=false;
						break;
					case 5:
						updateUserProfileData(hsIdInput, "City", Helper.getConsoleValue());
						innerFlag=false;
						break;
					case 6:
						updateUserProfileData(hsIdInput, "State", Helper.getConsoleValue());
						innerFlag=false;
						break;
					case 7:
						updateUserProfileData(hsIdInput, "Zipcode", Helper.getConsoleValue());
						innerFlag=false;
						break;
					case 8:
						updateAutorizationData(hsIdInput, "DATEAUTHORISED", Helper.getConsoleValue());
						innerFlag=false;
						break;						
					case 9:
						innerFlag=false;
						break;
					default:
						innerFlag=false;
						break;
					}//switch ends
				}catch(Exception e){
					System.out.println("invalid selection , please try again");
				}//try catch ends					
			}
			flag=false;
		}else{
			System.out.println("Entered invalid ID or the concerned user is not your current health Supporter, Please try again ! ");
		}
	}// while ends
}

private void updateAutorizationData(String hsIdInput,String columnName, String newColumnValue) throws SQLException { 
	// TODO Auto-generated method stub
	PreparedStatement preparedStatement = null;
	SQLConnection sqlcon = null;
	try {
		System.out.println("Updating-----------");
		String selectSQL = "UPDATE Supports SET "+columnName+" = ? WHERE P_USERID = ? and HS_USERID = ?";
		sqlcon = new SQLConnection();
		sqlcon.conn.setAutoCommit(false);
		preparedStatement = sqlcon.conn.prepareCall(selectSQL);
		preparedStatement.setDate(1,java.sql.Date.valueOf(newColumnValue));
		preparedStatement.setString(2,username);
		preparedStatement.setString(3, hsIdInput); 
		int updatedRowCount = preparedStatement.executeUpdate();
		System.out.println( updatedRowCount+" row updated");
    	sqlcon.conn.commit();
    }catch (SQLException e){
    	// TODO Auto-generated catch block
		System.out.println("Something went wrong. Try again!");
		sqlcon.conn.rollback();
		e.printStackTrace();			
	}
  	catch(Throwable e){
  		e.printStackTrace();
  	}
	finally{
		if (preparedStatement != null) {
			preparedStatement.close();
		}
		if(sqlcon != null)
			sqlcon.terminateSQLconnection();
	}
//		  System.out.println("Reached till end of function"
	}	



// DELETE HEALTH SUPPORTER
private void DeleteHealthSupporter(String hsType) throws Exception{
	if(hsType=="Primary" && isSick.equals("1")){
		System.out.println("Sick Patients Cannot Delete Their Only Primary Health Supporter !!");
	}else{
		PreparedStatement preparedStatement = null;
		try{
			System.out.println("Deleting Health Supporter-----------");
			
			String selectSQL = "DELETE FROM Supports WHERE P_UserId=? AND HStype=?";
			sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, hsType);
			int rowUpdateCount = preparedStatement.executeUpdate();
			System.out.println(rowUpdateCount+" Row(s) Updated");
			sqlcon.conn.commit();
			
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			//System.out.println("Error Fetching IDs from the user's table!");
			System.out.println("Error Deleting Supporter.Try again!");
			e.printStackTrace();
		}			
		finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlcon != null)
				sqlcon.terminateSQLconnection();
		}
		
	}
	
}

// UTILITY FUNCTION
private void updateUserProfileData(String UserID,String columnName, String newColumnValue)throws SQLException, Exception{
	PreparedStatement preparedStatement = null;
	SQLConnection sqlcon = null;
	try {
		System.out.println("Updating-----------");
		String selectSQL = "UPDATE Users SET "+columnName+" = '"+newColumnValue +"' WHERE UserId = ?";
		sqlcon = new SQLConnection();
		sqlcon.conn.setAutoCommit(false);
		preparedStatement = sqlcon.conn.prepareCall(selectSQL);
		preparedStatement.setString(1, UserID);
		int updatedRowCount = preparedStatement.executeUpdate();
		System.out.println( updatedRowCount+" row updated");
    	sqlcon.conn.commit();
    }catch (SQLException e){
    	// TODO Auto-generated catch block
		System.out.println("Something went wrong. Try again!");
		sqlcon.conn.rollback();
		e.printStackTrace();			
	}
  	catch(Throwable e){
  		e.printStackTrace();
  	}
	finally{
		if (preparedStatement != null) {
			preparedStatement.close();
		}
		if(sqlcon != null)
			sqlcon.terminateSQLconnection();
	}
//		  System.out.println("Reached till end of function"
	}
}
//------------------------------------------------------------------------------ CODE END	
