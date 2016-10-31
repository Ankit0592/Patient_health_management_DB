package com.db.phm;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import oracle.jdbc.OracleTypes;

public class Patient extends User{
	
	public String username = null;
	public String firstname = null;
	public String lastname = null;
	public String isSick = null;
	public String hasSplReco = null;
	
	public Patient(String username) {
		super(username);
		// TODO Auto-generated constructor stub
		this.username = username;
		
	}

	public Patient(String username, String firstname, String lastname, String isSick, String hasSplreco) throws Exception  {
		super(username, firstname, lastname);
		// TODO Auto-generated constructor stub
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.isSick = isSick;
		this.hasSplReco = hasSplreco;
		showMenuItems();
	}
	
	public  void showMenuItems() throws Exception { 
		
		healthSupporterCheck();
		System.out.println("Please select from the below options: ");
		System.out.println("1. Profile \n2. Diagnoses");
		System.out.println("3. Health Indicator \n4. Alerts");
		System.out.println("5. Health Supporters\n6. Logout");
		
		selectAnAction();
	}
	
	/* if a patient added a disease in last login but does not have health supporter
	 * patient needs to add health supporter before any other tasks are possible
	 * and this health supporter has to be primary as it is first
	 */
	public void healthSupporterCheck() throws Exception  {  
		// TODO Auto-generated method stub
		int hasNoHS = hasNoHealthSupporter();
		if(Integer.parseInt(isSick) == 1 && hasNoHS == 1)
		{
			System.out.println("You are sick. Add a health supporter before proceeding or logout ");
			Helper.printMessage("1.Choose a Health supporter id from below list to add: \t\t 2. Logout");
			try{ 
				boolean flag = true;
				while(flag)
				{
					Scanner scanner = new Scanner(System.in);
					String value = scanner.nextLine();
					int choice = Integer.parseInt(value);
					switch(choice){
					
					case 1:
						addHealthSupporter("Primary");
						flag = false;
						break;
					case 2:
						Welcome wc = new Welcome();
						wc.logout();
						flag = false;
						break;
					default:
						System.out.println("Invalid choice: Please enter again.");					
					}
				}
				
			
			}
			catch(Exception e){
				System.out.println("Something bad happened!!! Please try again...");
				showMenuItems();
			}		
		}
		else
			return;
	}	
	
	public int hasNoHealthSupporter()  throws Exception{ 
		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		String hs_id = null;
		try
		{
				String selectSQL = "select HS_UserId from supports where P_UserId = ?";
				sqlcon = new SQLConnection();
				preparedStatement = sqlcon.conn.prepareCall(selectSQL);
				preparedStatement.setString(1, username);
				ResultSet rs = preparedStatement.executeQuery();
				while(rs.next())
				{
					hs_id = rs.getString("HS_UserId");
				}
				if(hs_id == null) // has no health supporter
					return 1;
				return 0;				
		}
		catch(SQLException se)
		{
			se.printStackTrace();
			System.out.println("Something went wrong.");
		    System.out.println("Try again!");	
	    	showMenuItems();
		}
		finally{
			if(preparedStatement != null)
				preparedStatement.close();
			if(sqlcon != null)
				sqlcon.terminateSQLconnection();
		}
				
		return -1;
	}

	/*Insert in to the supports table*/
	public void addHealthSupporter(String healthSupporterType) throws Exception{ 
		// TODO Auto-generated method stub
		List<String> availableHSList = getAvailableHSList();		
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		int i =0;
		try
		{
			if(availableHSList != null)
			{
				if(availableHSList.isEmpty())
				{
					Helper.printMessage("No users exist for adding as Health supporter");// This will happen only when there is only one patient in the phm system and he is sick
				}
			else{
				while(i < availableHSList.size())
				{
					System.out.print(availableHSList.get(i));
					System.out.print("\t\t");					
					i++;
				}
				Helper.printMessage("Enter the health supported id");
				Scanner scanner = new Scanner(System.in);
				String hsid = scanner.nextLine();
				Helper.printMessage("Enter the authorization date");
				String authorizedDate = scanner.nextLine();
				String selectSQL = "INSERT INTO supports "+"(P_UserId, HS_UserId, dateAuthorised, HStype) VALUES" + "(?,?,?,?)";
				sqlcon = new SQLConnection();
				sqlcon.conn.setAutoCommit(false);
				preparedStatement = sqlcon.conn.prepareCall(selectSQL);
				preparedStatement.setString(1, username);
				preparedStatement.setString(2, hsid);
				preparedStatement.setDate(3, java.sql.Date.valueOf(authorizedDate));
				preparedStatement.setString(4, healthSupporterType);
				int result = preparedStatement.executeUpdate();
				System.out.println("Result of add health supporter is: "+result);
				System.out.println("Health supporter added !");
			    System.out.println("Commiting data here....");
			    sqlcon.conn.commit();				
			}
		  }
		}
		catch(SQLException se)
		{
			se.printStackTrace();
			System.out.println("Something went wrong.");
		    System.out.println("Rolling back data here....");
		    System.out.println("Try again!");	
	    	showMenuItems();
		}
		finally{
			if(preparedStatement != null)
				preparedStatement.close();
			if(sqlcon != null)
				sqlcon.terminateSQLconnection();
		}
		
	}

	public List<String> getAvailableHSList() throws Exception{ 
		// TODO Auto-generated method stub
		List<String> availableHSList = new ArrayList<String>();
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		try{			
			String selectSQL = "SELECT UserId FROM Users WHERE UserId != ?";
			sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, username);
			ResultSet rs = preparedStatement.executeQuery();
			while(rs.next()){
				availableHSList.add(rs.getString("UserId"));
			}
			return availableHSList;
		}
		catch(SQLException se){
			
			se.printStackTrace();
			Helper.printMessage("Something went wrong. Try again");
			showMenuItems();			
		}
		finally{
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlcon != null){
				sqlcon.terminateSQLconnection();
			}						
		}
		
		return null;
	}

	public  void selectAnAction() throws Exception {
		//Diagnoses resource = new Diagnoses(this.userName, this.userType);
		try{
			boolean flag = true;
			while(flag){
					//@SuppressWarnings("Diagnoses")
					Scanner scanner = new Scanner(System.in);
					String value = scanner.nextLine();
					int choice = Integer.parseInt(value);
					switch(choice){
					case 1:
						//Profile
						showProfile();
						flag=false;
						break;
					case 2:
						//Diagnoses
						Diagnoses dd = new Diagnoses(username,firstname,lastname, isSick, hasSplReco);
						dd.showDiagnoseMenuItems();
						flag = false;
						break;
					case 3:
						//Health Indicators. Show observation table for each health indicator Ex-weight, blood pressure, etc applicable to a person
						HealthIndicator obs = new HealthIndicator(username,firstname,lastname, isSick, hasSplReco);
						obs.showObservationReading();
						flag = false;
						break;
					case 4:
						//Alerts
						Alerts al = new Alerts(username, firstname, lastname, isSick, hasSplReco);
						al.showAlertsMenuItems();
						flag = false;
						break;
					case 5:
						//Health Supporters
						PatientSupporter ps = new PatientSupporter(username, firstname, lastname, isSick, hasSplReco);
						ps.showHealthSupporter();
						flag = false;
						break;
					case 6:
						Welcome wc = new Welcome();
						wc.logout();
						flag = false;
						break;
					default:
						System.out.println("Invalid choice: Please enter again.");
							
					}
				}
			}
			catch(SQLException se){
				se.printStackTrace();
				System.out.println("Something bad happened!!! Please try again...");
				showMenuItems();

			}
		
		}
	private void showHealthSupporter() {
		// TODO Auto-generated method stub
		
	}

	protected void showProfile() throws Exception{  
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		String gender = null;
		//System.out.println("In show profile");
		try{
			String selectSQL = "SELECT Gender, Apartment, Street, City, State, zipcode, DOB, isHS, isSick FROM Users WHERE UserId = ?";
			sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, username);
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				 gender = rs.getString("Gender");
				String apartment = rs.getString("Apartment");
				String street = rs.getString("Street");
				String city = rs.getString("City");
				String state = rs.getString("State");
				String zipcode = rs.getString("zipcode");
				java.sql.Date dob  = rs.getDate("DOB");
				String isHS = rs.getString("isHS");
				String isSick = rs.getString("isSick");
				String patientCategory = null;
				String userType= null;
				if(isSick.equals("1"))
					patientCategory = "sick";
				else
					patientCategory = "well";
				
				if(isHS.equals("1"))
					userType = "patient and Health Supporter";
				else
					userType = "patient";
				

	            System.out.println("User ID: "+username +"\n" + 
	            					"Name: "+firstname+"\t\t"+lastname+"\n" + 
	            					"Gender: "+gender +"\n"+
	            					"Date Of Birth: "+dob +"\n"+
	            					"Address: "+street +", "+apartment+", "+city+", "+state+"- "+zipcode +"\n"+
	            					"Patient Category: "+patientCategory +"\n" +
	            					"User Type: "+userType);

	            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
	            				
			 }
				if(gender == null){
					System.out.println("Not a valid user id.");
					return;
				}
			  System.out.println("Do you want to update profile?");
			  System.out.println("Please select from the below options: ");
			  System.out.println("\n1. Update Profile \t\t\t\t 2. Back to patient Menu");			  

				Scanner scanner = new Scanner(System.in);
				int value = Integer.parseInt(scanner.nextLine());
				switch(value)
				{
				case 1: 
					updateProfileMenu();
					break;
				case 2:
					showMenuItems();
					break; 
				}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong.");
		    System.out.println("Try again!");
			showMenuItems();
			}			
			finally {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if(sqlcon != null)
					sqlcon.terminateSQLconnection();
				}	

}

	private void updateProfileMenu() throws Exception { 
		// TODO Auto-generated method stub
		System.out.println("Please select the field, you want to modify: ");
        System.out.println("1. First Name" +"\t" + "2. Last Name"+"\n" 
		+"3. Apartment" + "\t\t" + "4. Street" +"\t\t"  + "5. City" +"\t\t" + "6. State" +"\t\t" +"7. zipcode"+ "\n" 
        + "8. Date Of Birth\n"
		+"9. Password\n"+"10. Gender\n"
        +"0. Go to the Patient Menu.");       
        modifyProfileData();
		
	}

	private void modifyProfileData() throws Exception  { 
		// TODO Auto-generated method stub
		int enteredValue = Integer.parseInt(Helper.enteredConsoleString());
		System.out.println("Entered value is: "+enteredValue);
		if(enteredValue==8){
			System.out.println("Enter your date of birth in yyyy-mm-dd format.");
			dateOfBirthValidation();
		}
		else if(enteredValue==1){
			//user_id
			String entereddata = Helper.enteredProfileData();
			System.out.println("Entered data is : "+entereddata);
			updateProfileData("FirstName", entereddata);
		}
		else if(enteredValue==2){
			//first_name
			updateProfileData("LastName", Helper.enteredProfileData());
		}else if(enteredValue==3){
			//last_name
			updateProfileData("Apartment", Helper.enteredProfileData());
		}else if(enteredValue==4){
			//Phone_no
			updateProfileData("Street", Helper.enteredProfileData());
		}else if(enteredValue==5){
			//alt_phone
			updateProfileData("City", Helper.enteredProfileData());
		}else if(enteredValue==6){
			//Address
			updateProfileData("State", Helper.enteredProfileData());
		}else if(enteredValue==7){
			//Nationality
			updateProfileData("zipcode", Helper.enteredProfileData());
		}else if(enteredValue==9){
			//sex
			updateProfileData("password", Helper.enteredProfileData());
		}
		else if(enteredValue==10){
			//password
			updateProfileData("Gender", Helper.enteredProfileData());
		}		
		else if(enteredValue==0){
			showMenuItems();
		}		
	}

	private void dateOfBirthValidation() throws Exception {  
		// TODO Auto-generated method stub
		String enteredValue = Helper.enteredProfileData();
		String validFormat = "yyyy-mm-dd";
		if(Helper.validateDateFormat(enteredValue, validFormat))
		{
			//call save functionality
			updateProfileData("DOB", enteredValue);
		}else{
			System.out.println("Date format is invalid.");
			dateOfBirthValidation();
			
		}		
	}

//	private String enteredProfileData() {
//		System.out.println("Please enter value:");
//		return Helper.enteredConsoleString();
//	}

	private void updateProfileData(String columnName, String newColumnValue)throws Exception{ 
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		  try {
				String selectSQL = "UPDATE Users SET "+columnName+" = '"+newColumnValue +"' WHERE UserId = ?";
			  	//String selectSQL = "UPDATE Users SET "+columnName+" = '"+newColumnValue+"' WHERE UserId = '"+username+"' and password =  '"+pwd+"'";
//				Helper.printMessage("selectSQL : " + selectSQL); 
//				Helper.printMessage("columnName : " + columnName);
//				Helper.printMessage("newColumnValue : " + newColumnValue);
//				Helper.printMessage("username : " + username);
//				Helper.printMessage("password : " + pwd);
				sqlcon = new SQLConnection();
				sqlcon.conn.setAutoCommit(false);
				preparedStatement = sqlcon.conn.prepareCall(selectSQL);
				preparedStatement.setString(1, username);
				//preparedStatement.setString(2, pwd);
				//int result = sqlcon.stmt.executeUpdate(selectSQL);// return no of rows updated
				int result = preparedStatement.executeUpdate();
				System.out.println("result is " + result);
	        	if(result == 0)
	        	{
	        		System.out.println("No row updated");
	        	}else
	        		System.out.println("\nUpdate Successful!!!\n");
			    System.out.println("Commiting data here....");
			    sqlcon.conn.commit();
			    showMenuItems();// if update successful, send the patient to patient menu and if he wants he can choose to see profile or logout
			} catch (SQLException se) {
				// TODO Auto-generated catch block
				se.printStackTrace();			
				System.out.println("Something went wrong.");
			    System.out.println("Rolling back data here....");
			    System.out.println("Try again!");
		    	try{
				 if(sqlcon.conn!=null)
					 sqlcon.conn.rollback();
		    	}catch(SQLException se2){
		         se2.printStackTrace();
		    	}
		    	showMenuItems();
			}
			finally {
				if (preparedStatement != null) {
						preparedStatement.close();
				}
				if(sqlcon != null)
					sqlcon.terminateSQLconnection();
			}
//		  System.out.println("Reached till end of function");
	}
}
