package com.db.phm;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import oracle.jdbc.OracleTypes;

public class HealthSupporter extends User{
	
	public String username = null;
	public String pwd = null;
	public String firstname = null;
	public String lastname = null;
	public String isSick = null;
	public String hasSplReco = null;
	
	public HealthSupporter(String username) {
		super(username);
		// TODO Auto-generated constructor stub
		this.username = username;	
	}

	public HealthSupporter(String username, String pwd, String firstname, String lastname, String isSick, String hasSplReco) {
		super(username, firstname, lastname);
		// TODO Auto-generated constructor stub
		this.username = username;
		this.pwd = pwd;
		this.firstname = firstname;
		this.lastname = lastname;
		this.isSick = isSick;
		this.hasSplReco = hasSplReco;
		showMenuItems();
	}
	
	public  void showMenuItems() {
		
		selectAnAction();
	}
	
	public  void selectAnAction() {
		//Health Supporter selects an action
		try{
			boolean flag = true;
			while(flag){	
					System.out.println("Health Supporter Menu: ");
					System.out.println("\n1. Profile\n2. Patients List\n3. Logout");
					System.out.println("Enter choice [1-3]:");
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
						//show patients listing
						showPatients();
						break;
					case 3:
						//Logout
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
				e.printStackTrace();
				showMenuItems();

			}
		
		}
	
	// To fetch and view list of patients for health supporter
	protected void showPatients() throws Exception{
		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		System.out.println("\n\n_______________________________");
		System.out.println("          Patients List        ");
		System.out.println("_______________________________\n");
		try{
			
				String selectSQL = "SELECT UserId, FirstName, LastName FROM Users WHERE UserId IN (SELECT P_UserId FROM Supports WHERE HS_UserId = ?)";
			
				sqlcon = new SQLConnection();
				preparedStatement = sqlcon.conn.prepareCall(selectSQL);
				preparedStatement.setString(1, username);
				ResultSet rs = preparedStatement.executeQuery();
				
				ArrayList<ArrayList<String>> patientlist = new ArrayList<ArrayList<String>>();
				while (rs.next()) {
				    ArrayList<String> row = new ArrayList<String>();
				    row.add(rs.getString("UserId"));
					row.add(rs.getString("FirstName"));
					row.add(rs.getString("LastName"));
					patientlist.add(row);
				    }
	
			
			showAllPatientsTable(patientlist);
			System.out.println("Enter UserId of patient to see details or enter 0 to go back to Main Menu");
			Scanner scanner = new Scanner(System.in);
			String user = scanner.nextLine();
			if(user.equals("0")){
				if(preparedStatement != null)
					preparedStatement.close();
				if(sqlcon != null)
					sqlcon.terminateSQLconnection();
				showMenuItems();
			}
			else{
				if(preparedStatement != null)
					preparedStatement.close();
				if(sqlcon != null)
					sqlcon.terminateSQLconnection();
			
				seePatientDetails(user);
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong. Try again!");
			if(preparedStatement != null)
				preparedStatement.close();
			if(sqlcon != null)
				sqlcon.terminateSQLconnection();
			showMenuItems();
			}
		finally{
			if(preparedStatement != null)
				preparedStatement.close();
			if(sqlcon != null)
				sqlcon.terminateSQLconnection();
		}
	}
	
	protected void seePatientDetails(String user) throws Exception{

		int isHSAuthorised = -1;		
		System.out.println("\n\n_______________________________");
		System.out.println("     Patients ["+user+"] Page        ");
		System.out.println("_______________________________\n");
		
		isHSAuthorised = checkHSAuthorised(user);
		
		if(isHSAuthorised == 1)
		{
			ArrayList<String> patientDetails = getPatientDetails(user);
			Alerts alerts = new Alerts(user, patientDetails.get(0), patientDetails.get(1), patientDetails.get(10), patientDetails.get(11));
			alerts.generateOutsideLimitAlerts();
			alerts.generateLowActivityAlerts();
			ArrayList<ArrayList<String>> alertlist = getAlerts(user); // by select query
			showAlerts(alertlist);
			showPatientdetails(patientDetails, user);//select query
			
			 System.out.println("Do you want to add/edit recommendations or clear alerts?");
			 System.out.println("Please select from the below options: ");
			 System.out.println("\n1. Edit patient profile \t 2. Add Recommendation \t 3. Clear Alerts\t 4. Back to patients List");			  

			Scanner scanner = new Scanner(System.in);
			int value = Integer.parseInt(scanner.nextLine());
			switch(value)
			{
			case 1: 
				editPatientProfile(user);
				break;
			case 2: 
				showPatientRecommendationAdd(user);
				break;
			case 3: 
				//Clear Alerts
				clearAlerts(user);
				break;
			case 4:
				// Show Patients List
				showPatients();
				break; 
			}
			
		}
		else if(isHSAuthorised == 0)
		{
			Helper.printMessage("You are not authorised to view details"); 
		}
		else
		{
			System.out.println("Invalid user id. Try Again!!");			
		}
		
	
	}

	private void editPatientProfile(String user) throws SQLException, Exception {
		// TODO Auto-generated method stub
		System.out.println("\nUpdate profile page: ");
        System.out.println("1. First Name\n2. Last Name\n3. Apartment");
        System.out.println("4. Street\n5. City\n6. State\n7. zipcode");
        System.out.println("8. Date Of Birth\n9. Gender\n0. Go to the Previous Menu.");
        System.out.println("\nEnter choice [0-9]");
        modifyProfileData(user);
		
	}
	
	// Modification of patient profile data by health supporter
	private void modifyProfileData(String user) throws SQLException, Exception  {
		// TODO Auto-generated method stub
		int enteredValue = Integer.parseInt(Helper.enteredConsoleString());
		//System.out.println("Entered value is: "+enteredValue);
		if(enteredValue==8){
			System.out.println("Enter your date of birth in yyyy-mm-dd format.");
			//dateOfBirthValidation();
			String ipValue = Helper.enteredProfileData();
			String validFormat = "yyyy-mm-dd";
			if(Helper.validateDateFormat(ipValue, validFormat))
			{
				//call save functionality
				updatedateTypeData("DOB", ipValue, user);
			}else{
				System.out.println("Date format is invalid.");
				//dateOfBirthValidation();
				
			}					
		}
		else if(enteredValue==1){
			//user_id
			String entereddata = Helper.enteredProfileData();
			System.out.println("Entered data is : "+entereddata);
			updateData("FirstName", entereddata, user);
		}
		else if(enteredValue==2){
			//first_name
			updateData("LastName", Helper.enteredProfileData(), user);
		}else if(enteredValue==3){
			//last_name
			updateData("Apartment", Helper.enteredProfileData(), user);
		}else if(enteredValue==4){
			//Phone_no
			updateData("Street", Helper.enteredProfileData(), user);
		}else if(enteredValue==5){
			//alt_phone
			updateData("City", Helper.enteredProfileData(), user);
		}else if(enteredValue==6){
			//Address
			updateData("State", Helper.enteredProfileData(), user);
		}else if(enteredValue==7){
			//Nationality
			updateData("zipcode", Helper.enteredProfileData(), user);
		}
		else if(enteredValue==9){
			//password
			updateData("Gender", Helper.enteredProfileData(), user);
		}		
		else if(enteredValue==0){
			seePatientDetails(user);
		}		
	}
	private void updatedateTypeData(String columnName, String newColumnValue,String user) throws SQLException { 
		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		try {
			System.out.println("Updating-----------");
			String selectSQL = "UPDATE Users SET "+columnName+" = ? WHERE UserId = ?";
			sqlcon = new SQLConnection();
			sqlcon.conn.setAutoCommit(false);
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setDate(1,java.sql.Date.valueOf(newColumnValue));
			preparedStatement.setString(2,user);
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
//			  System.out.println("Reached till end of function"
		}	
	// Update patient profile data
	private void updateData(String columnName, String newColumnValue, String user)throws SQLException, Exception{
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		  try {
				String selectSQL = "UPDATE Users SET "+columnName+" = '"+newColumnValue +"' WHERE UserId = ?";
			  	
				sqlcon = new SQLConnection();
				sqlcon.conn.setAutoCommit(false);
				preparedStatement = sqlcon.conn.prepareCall(selectSQL);
				preparedStatement.setString(1, user);
				//preparedStatement.setString(2, pwd);
				//int result = sqlcon.stmt.executeUpdate(selectSQL);// return no of rows updated
				int result = preparedStatement.executeUpdate();
				System.out.println("result is " + result);
	        	if(result == 0)
	        	{
	        		System.out.println("No row updated");
	        	}else
	        		System.out.println("\nUpdate Successful!!!\n");
	        	sqlcon.conn.commit();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("Something went wrong. Try again!");
				sqlcon.conn.rollback();
				e.printStackTrace();	
				}
		  		catch(Throwable e) 
		  		{
		  			e.printStackTrace();
		  		}
					finally {
						if (preparedStatement != null) {
							preparedStatement.close();
						}
						if(sqlcon != null)
							sqlcon.terminateSQLconnection();
					}
//		  System.out.println("Reached till end of function");
		  seePatientDetails(user);
	}

	private void showPatientdetails(ArrayList<String> patientDetails, String user) {
		// TODO Auto-generated method stub
        System.out.println("User ID: "+user +"\n" + 
				"Name: "+patientDetails.get(0)+"\t\t"+patientDetails.get(1)+"\n" + 
				"Gender: "+patientDetails.get(2) +"\n"+
				"Date Of Birth: "+patientDetails.get(8) +"\n"+
				"Address: "+patientDetails.get(4) +", "+patientDetails.get(3)+", "+patientDetails.get(5)+", "+patientDetails.get(6)+"- "+patientDetails.get(7) +"\n"
				);

        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
		
		
	}

	private ArrayList<ArrayList<String>> getAlerts(String user) throws SQLException { 
		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		ArrayList<ArrayList<String>> alertlist = new ArrayList<ArrayList<String>>();
		try{
			sqlcon = new SQLConnection();
			String alertSQL = "select ALERTID, ALERTTYPE, HINAME, MESSAGE, READSTATUS from alerts where P_USERID = ? and CLEARSTATUS = '0'";
			preparedStatement = sqlcon.conn.prepareCall(alertSQL);
			preparedStatement.setString(1, user);
			ResultSet rsAlert = preparedStatement.executeQuery();	
			while (rsAlert.next()) {
		    ArrayList<String> row = new ArrayList<String>();
		   
		    row.add(rsAlert.getString("alertId"));
			row.add(rsAlert.getString("alertType"));
			row.add(rsAlert.getString("HIName"));
			row.add(rsAlert.getString("message"));
			row.add(rsAlert.getString("readStatus"));
			alertlist.add(row);
		    }
			return alertlist;
		}
		catch(SQLException se)
		{
			se.printStackTrace();
		}
		finally{
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlcon != null)
				sqlcon.terminateSQLconnection();			
		}
		return null;
	}

	private ArrayList<String> getPatientDetails(String user) throws SQLException { 
		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		ArrayList<String> list = new ArrayList<String>();
	
		try{
			sqlcon = new SQLConnection();
			String selectSQL = "SELECT  FirstName,LastName,Gender, Apartment, Street, City, State, zipcode, DOB, isHS, isSick, HASSPLRECO FROM Users WHERE UserId = ?";
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, user);
			ResultSet rs = preparedStatement.executeQuery();
			while(rs.next())
			{
//				String pfname = rs.getString("FirstName");
//				String plname = rs.getString("LastName");
//				String gender = rs.getString("Gender");
//				String apartment = rs.getString("Apartment");
//				String street = rs.getString("Street");
//				String city = rs.getString("City");
//				String state = rs.getString("State");
//				String zipcode = rs.getString("zipcode");
//				java.sql.Date dob  = rs.getDate("DOB");
//				String isHS = rs.getString("isHS");
//				String isSick = rs.getString("isSick");
//				String patientCategory = null;
//				String userType= null;
				list.add(rs.getString("FirstName"));
				list.add(rs.getString("LastName"));
				list.add(rs.getString("Gender"));
				list.add(rs.getString("Apartment"));
				list.add(rs.getString("Street"));
				list.add(rs.getString("City"));
				list.add(rs.getString("State"));
				list.add(rs.getString("zipcode"));
				list.add(rs.getDate("DOB").toString());//may throw error
				list.add(rs.getString("isHS"));
				list.add(rs.getString("isSick"));
				list.add(rs.getString("HASSPLRECO"));
			}
			return list;
		}
		catch(SQLException se)
		{
			se.printStackTrace();
			Helper.printMessage("Something went wrong. Try again"); 
			
		}
		finally{
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlcon != null)
				sqlcon.terminateSQLconnection();
			}	
		return null;
	}

	private int checkHSAuthorised(String user) throws SQLException{		
		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		java.sql.Date hsdateauthorised = null;
		Calendar calendar = Calendar.getInstance();
		java.util.Date currentDate = calendar.getTime();
		java.sql.Date curdate = new java.sql.Date(currentDate.getTime());
		Helper.printMessage(curdate.toString());
		
		try{
			String checkSQL = "SELECT dateAuthorised FROM Supports WHERE P_UserId = ? and HS_UserId = ?";
			sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(checkSQL);
			preparedStatement.setString(1, user);
			preparedStatement.setString(2, username);
			ResultSet checkHS = preparedStatement.executeQuery();
			while(checkHS.next())
			{
				hsdateauthorised = checkHS.getDate("dateAuthorised");
				if(hsdateauthorised.before(curdate) || hsdateauthorised.equals(curdate))
				{
					Helper.printMessage("HS is authorisesd"); 
					return 1;
				}
				else
				{
					return 0;
				}
			}
			
		}
		catch(SQLException se)
		{
			se.printStackTrace();
			Helper.printMessage("Something went wrong. Try again"); 
			return -1;
		}
		finally{
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlcon != null)
				sqlcon.terminateSQLconnection();
			}	
		return -1;
		}
		

	// See Selected Patient details
//	protected void seePatientDetailsHelper(String user) throws Exception{
//		// TODO Auto-generated method stub
//		PreparedStatement preparedStatement = null;
//		SQLConnection sqlcon = null;
//		String gender = null;
//		/*Integration*/
//		String p_firstname = null;
//		String p_LastName = null;
//		String p_isSick = null;
//		String p_hasSplReco = null;	
//		java.sql.Date hsdateauthorised = null;
//		Calendar calendar = Calendar.getInstance();
//		java.util.Date currentDate = calendar.getTime();
//		java.sql.Date curdate = new java.sql.Date(currentDate.getTime());
//		Helper.printMessage(curdate.toString());
//		int isHSAuthorised = -1;
//		/*Integration*/
//		System.out.println("\n\n_______________________________");
//		System.out.println("     Patients ["+user+"] Page        ");
//		System.out.println("_______________________________\n");
//		//System.out.println("In show profile");
//		try{
//			// Check if patient id entered is the one to which HS has authorization
//			String checkSQL = "SELECT dateAuthorised FROM Supports WHERE P_UserId = ? and HS_UserId = ?";
//			sqlcon = new SQLConnection();
//			preparedStatement = sqlcon.conn.prepareCall(checkSQL);
//			preparedStatement.setString(1, user);
//			preparedStatement.setString(2, username);
//			ResultSet checkHS = preparedStatement.executeQuery();	
//			
//			while(checkHS.next())
//			{
//				hsdateauthorised = checkHS.getDate("dateAuthorised");
//				if(hsdateauthorised.before(curdate) || hsdateauthorised.equals(curdate))
//				{
//					Helper.printMessage("HS is authorisesd"); 
//					isHSAuthorised =1;
//				}
//				else
//				{
//					isHSAuthorised =0;
//				}
//			}	
//			if(isHSAuthorised ==1)
//			{
//				String selectSQL = "SELECT  FirstName,LastName,Gender, Apartment, Street, City, State, zipcode, DOB, isHS, isSick, HASSPLRECO FROM Users WHERE UserId = ?";
//				preparedStatement = sqlcon.conn.prepareCall(selectSQL);
//				preparedStatement.setString(1, user);
//				ResultSet rs = preparedStatement.executeQuery();
//				/*Integration*/
//				/*
//				 before showing alerts, alerts needs to be generated, need to pass details of patient not HS so be careful here
//				 so query to get patient details and then call generate alerts*/
////				String patientDetailSQL = "SELECT  FirstName,LastName,Gender, Apartment, Street, City, State, zipcode, DOB, isHS, isSick, hasSplReco FROM Users WHERE UserId = ?";
////				preparedStatement = sqlcon.conn.prepareCall(patientDetailSQL);
////				preparedStatement.setString(1, user);
////				ResultSet rspatient = preparedStatement.executeQuery();
//				while(rs.next())
//				{
//					gender = rs.getString("Gender");
//					String pfname = rs.getString("FirstName");
//					String plname = rs.getString("LastName");
//					String apartment = rs.getString("Apartment");
//					String street = rs.getString("Street");
//					String city = rs.getString("City");
//					String state = rs.getString("State");
//					String zipcode = rs.getString("zipcode");
//					java.sql.Date dob  = rs.getDate("DOB");
//					String isHS = rs.getString("isHS");
//					String isSick = rs.getString("isSick");
//					String patientCategory = null;
//					String userType= null;
//					
//					//generate the alerts first
//					Alerts alerts = new Alerts( user, p_firstname, p_LastName, p_isSick, p_hasSplReco);
////					alerts.generateOutsideLimitAlerts(sqlcon);
////					alerts.generateLowActivityAlerts();
//					/*Integration*/
//					//query to get alerts
////					String alertSQL = "SELECT alertId, alertType, HIName, message, readStatus FROM alerts WHERE P_UserId = ? and readStatus= ?";
////					preparedStatement = sqlcon.conn.prepareCall(alertSQL);
////					preparedStatement.setString(1, user);
////					preparedStatement.setString(2, "0"); //Fetch unread alerts
////					ResultSet rsAlert = preparedStatement.executeQuery();
//					
//					//ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
////					ArrayList<ArrayList<String>> list = alerts.displayAlerts();
//					
////					while (rsAlert.next()) {
////					    ArrayList<String> row = new ArrayList<String>();
////					   
////					    row.add(rsAlert.getString("alertId"));
////						row.add(rsAlert.getString("alertType"));
////						row.add(rsAlert.getString("HIName"));
////						row.add(rsAlert.getString("message"));
////						row.add(rsAlert.getString("readStatus"));
////						list.add(row);
////					    }					
//					
//					//showAlerts(list);
//		            System.out.println("User ID: "+user +"\n" + 
//		            					"Name: "+pfname+"\t\t"+pfname+"\n" + 
//		            					"Gender: "+gender +"\n"+
//		            					"Date Of Birth: "+dob +"\n"+
//		            					"Address: "+street +", "+apartment+", "+city+", "+state+"- "+zipcode +"\n"
//		            					);
//	
//		            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
//				}
//				
//				  System.out.println("Do you want to add/edit recommendations or clear alerts?");
//				  System.out.println("Please select from the below options: ");
//				  System.out.println("\n1. Add Recommendation \t 2. Clear Alerts\t 3. Back to patients List");			  
//	
//				Scanner scanner = new Scanner(System.in);
//				int value = Integer.parseInt(scanner.nextLine());
//				switch(value)
//				{
//				case 1: 
//					showPatientRecommendationAdd(user);
//					break;
//				case 2: 
//					//Clear Alerts
//					clearAlerts(user);
//					break;
//				case 3:
//					// Show Patients List
//					showPatients();
//					break; 
//				}
//				
//			}
//			else if(isHSAuthorised ==0)
//			{
//				System.out.println("You are not yet authorised to view the details");
//			}
//			else{
//				System.out.println("Invalid user id. Try Again!!");
//				showPatients();
//			}
//		}catch (SQLException e) {
//			System.out.println(e);
//			System.out.println("Something went wrong. Try again!");
//			//seePatientDetails(user);
//			}finally {
//				if (preparedStatement != null) {
//					preparedStatement.close();
//				}
//				if(sqlcon != null)
//					sqlcon.terminateSQLconnection();
//				}
//
//	}

	private void showPatientRecommendationAdd(String patientID) {
		System.out.println("\n\n_____________________________________________________");
		System.out.println("  Patients ["+patientID+"] Recommendation        ");
		System.out.println("_____________________________________________________\n");
		
		//show existing recommendations
		int i=1;
		ArrayList<ArrayList<String>> list = getSpecificRecommendation(patientID);
		System.out.println("\n\n______________________________________");
		System.out.println("      User Specific Recommendations   ");
		System.out.println("______________________________________\n");
		System.out.println("\tRecommendation S.No\t\t\tObservation Type\t\tLower Limit\t\tUpper Limit\t\tHas Limit\t\tFrequency(days)");
		System.out.println("\t___________________\t\t\t________________\t\t___________\t\t___________\t\t_________\t\t_______________");
		for (ArrayList<String> row : list) {
			String rowNum = String.format("%-16d", i);
			String obsName= row.get(0);
			obsName = String.format("%-30s", obsName);
			System.out.println("\t\t"+rowNum+"\t\t"+obsName+"\t\t"+row.get(1)+"\t\t\t"+row.get(2)+"\t\t\t"+row.get(3)+"\t\t\t"+row.get(4));
		i++;
		}
		
		//show menu to add or edit existing one
		System.out.println("\nEnter Recommendation number to edit or 'add' to add new recommendation or 0 to go back:");
		Scanner scanner = new Scanner(System.in);
		String recID = scanner.nextLine();
		if(recID.equals("0")){
			selectAnAction();
		}else if(recID.equals("add")){
			addNewRecommendation(patientID);
		}
		else{
			//int choiceInt = Helper.getConsoleIntValue();
			int choiceInt = Integer.parseInt(recID);
			updateNewRecommendation(patientID,list.get(choiceInt-1).get(0));
		}
		
	}

	private void addNewRecommendation(String patientID) {
		//ArrayList<String> obsType = getObservationList(patientID); // commenting this code
		/*get observation from observation table */
		ArrayList<String> obsType = getObservationsFromObservationTable(patientID);
		System.out.println("\nAdd Observation - below observation can be entered:");
		int i=1;
		StringBuilder choiceStr = new StringBuilder("");
		for (String item : obsType) {
			System.out.println(i+". "+item);
			choiceStr.append(i+",");
			i+=1;
		}
		System.out.println("0. Go back to patient menu");
		choiceStr.append("0");
		System.out.println("Enter choice["+choiceStr+"]:");
		String hiname = null,lvalue = null,hvalue = null,freq = null;
		int hasLimit=1;
		String nonnumericalval = null;
		while(true){
			int choiceInt = Helper.getConsoleIntValue();
			if(choiceInt == 0){
				selectAnAction();
				break; 
			}
			if(choiceInt<=i&&choiceInt>0){
				hiname = obsType.get(choiceInt-1);
				System.out.println("Values numbers [yes/no]?");
				Scanner scanner = new Scanner(System.in);
				nonnumericalval = scanner.nextLine();
				if(nonnumericalval.equals("yes")){
					System.out.println("Enter low value [no - for no value]:");
					scanner = new Scanner(System.in);
					lvalue = scanner.nextLine();
					System.out.println("Enter high value [no - for no value]:");
					hvalue = scanner.nextLine();
					nonnumericalval=null;
					if(lvalue.equals("no")&&hvalue.equals("no"))
						hasLimit=0;
				}else{
					nonnumericalval = scanner.nextLine();
				}
				System.out.println("Enter frequency(days) [no - for no frequency]:");
				freq = scanner.nextLine();
				if(freq.equals("no"))
					freq=null;
				break;
			}
			System.out.println("Invalid input,try again ... ");
			System.out.println("Enter choice["+choiceStr+"]:");
		}
		
			//insert into database
		insertUserRecommendation(patientID,hiname,lvalue,hvalue,hasLimit,freq,nonnumericalval);
	}

	private void insertUserRecommendation(String patientID, String hiname, String lvalue, String hvalue, int hasLimit,
			String freq, String nonnumericalval) {
		PreparedStatement preparedStatement = null;
		String insertDiseaseQuery = "INSERT INTO specificRecommendations(P_USERID,HINAME,HASLIMIT,LOWERLIMIT,UPPERLIMIT,FREQ)"
				+"VALUES (?,?,?,?,?,?)";
		try{
			SQLConnection sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(insertDiseaseQuery);
			preparedStatement.setString(1, patientID);
			preparedStatement.setString(2, hiname);
			preparedStatement.setInt(3, hasLimit);
			preparedStatement.setString(4, lvalue);
			preparedStatement.setString(5, hvalue);
			preparedStatement.setString(6, freq);
			//preparedStatement.setString(7, nonnumericalval);
			preparedStatement.execute();
			System.out.println("\n -----  Recommendation added -----\n");
			
			/*Integration*/
			/*Need to update the hasSplReco info in users*/
			String updateUserRecoFieldQuery = "Update Users set hasSplReco = '1' where USERID = ?";
			sqlcon.conn.setAutoCommit(false);
			preparedStatement = sqlcon.conn.prepareCall(updateUserRecoFieldQuery);
			//preparedStatement.setString(7, nonnumericalval);
			preparedStatement.setString(1, patientID); 
			preparedStatement.executeUpdate();	
			sqlcon.conn.commit();
			
		}catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error occured in adding diagnosed disease");
		}
	}

	public ArrayList<String> getObservationList(String userId){
		ArrayList<String> list=new ArrayList<String>();
		PreparedStatement preparedStatement = null;
		try{
			//"SELECT HIName FROM observation ORDER BY HIName";
			String diseaseNamesQuery = "SELECT DISTINCT HINAME FROM GENERALRECOMMENDATIONSICK WHERE DNAME IN( SELECT dName from Diagnosed WHERE P_USERID=?)";
			SQLConnection sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(diseaseNamesQuery);
			preparedStatement.setString(1,userId);
			ResultSet rs = preparedStatement.executeQuery();
			if (!rs.isBeforeFirst()) {
				System.out.println("No observation type found in database");
				return list;
			}else{
			  while(rs.next()){
				  list.add(rs.getString("HIName"));
			  }
			}
		}catch (SQLException e) {
			System.out.println("Something went wrong. Try again!");
		}
		return list;
	}
	/*
	 	Integration
	 */
	public ArrayList<String> getObservationsFromObservationTable(String userId){
		ArrayList<String> list=new ArrayList<String>();
		PreparedStatement preparedStatement = null;
		try{
			//"SELECT HIName FROM observation ORDER BY HIName";
			String diseaseNamesQuery = "select HINAME from OBSERVATION";
			SQLConnection sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(diseaseNamesQuery);
			//preparedStatement.setString(1,userId);
			ResultSet rs = preparedStatement.executeQuery();
			if (!rs.isBeforeFirst()) {
				System.out.println("No observation type found in database");
				return list;
			}else{
			  while(rs.next()){
				  list.add(rs.getString("HIName"));
			  }
			}
		}catch (SQLException e) {
			System.out.println("Something went wrong. Try again!");
		}
		return list;
	}
	private void updateNewRecommendation(String patientID, String hiname) {
		System.out.println("Operations:");
		System.out.println("1. Update\n2. Delete");
		System.out.println("Enter choice[1-2]:");
		Scanner scanner = new Scanner(System.in);
		String option = scanner.nextLine();
		if(option.equals("1")){
			//
			System.out.println("Enter lower limit ['no' - for no limit]: ");
			String lowLimit = scanner.nextLine();
			System.out.println("Enter upper limit ['no' - for no limit]: ");
			String highLimit = scanner.nextLine();
			System.out.println("Enter the frequency['no' - for no frequency]");
			String frequency = scanner.nextLine();
			int hasLimit=1;
			if(lowLimit.equals("no")&&highLimit.equals("no"))
				hasLimit=0;
			updateRecommendation(patientID, hiname,lowLimit,highLimit,hasLimit,frequency);
		}else{
			// delete the recommendation
			deleteRecommendation(patientID, hiname);
		}
		
	}
	
	private void updateRecommendation(String patientID, String hiname, String lowLimit, String highLimit, int hasLimit,
			String frequency) {
		PreparedStatement preparedStatement = null;
		String insertDiseaseQuery = "UPDATE specificRecommendations SET hasLimit=?,lowerLimit=?,upperLimit=?, freq=? WHERE P_UserId=? AND HIName=?";
		try{
			SQLConnection sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(insertDiseaseQuery);
			preparedStatement.setInt(1, hasLimit);
			preparedStatement.setString(2, lowLimit);
			preparedStatement.setString(3, highLimit);
			preparedStatement.setString(4, frequency);
			preparedStatement.setString(5, patientID);
			preparedStatement.setString(6, hiname);
			preparedStatement.execute();
			System.out.println("\n -----  Recommendation updated -----\n");
		}catch (SQLException e) {
			System.out.println("Error occured in adding diagnosed disease");
		}
	}

	private void deleteRecommendation(String patientID, String hiname){
		PreparedStatement preparedStatement = null;
		String insertDiseaseQuery = "DELETE FROM specificRecommendations WHERE P_UserId=? AND HIName=?";
		try{
			SQLConnection sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(insertDiseaseQuery);
			preparedStatement.setString(1, patientID);
			preparedStatement.setString(2, hiname);
			preparedStatement.execute();
			System.out.println("\n -----  Recommendation deleted -----\n");
		}catch (SQLException e) {
			System.out.println("Error occured in adding diagnosed disease");
		}
	}

	private ArrayList<ArrayList<String>> getSpecificRecommendation(String userId) {
		PreparedStatement preparedStatement = null;
		String viewObservationQuery = "SELECT HIName, lowerLimit, UpperLimit, hasLimit, freq FROM specificRecommendations WHERE P_UserID=?";
		ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
		try {
			SQLConnection sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(viewObservationQuery);
			preparedStatement.setString(1, userId);
			ResultSet rs = preparedStatement.executeQuery();
			while(rs.next()){
				ArrayList<String> row = new ArrayList<String>();
				row.add(rs.getString("HIName"));
				row.add(rs.getString("lowerLimit"));
				row.add(rs.getString("UpperLimit"));
				row.add(rs.getString("hasLimit"));
				row.add(rs.getString("freq"));
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	public void clearAlerts(String patientID) throws Exception { 
		ArrayList<ArrayList<String>> alertlist = getAlerts(patientID); // by select query
		showAlerts(alertlist);
		Helper.printMessage("Select the alert Id from above alerts to clear !"); 
		Scanner scanner = new Scanner(System.in);
		String alertid = scanner.nextLine();
		clearAlertsFromTable(alertid);
		showPatients();
	}
	public void clearAlertsFromTable(String alertid) throws SQLException {
		SQLConnection sqlConn = null;
		PreparedStatement preparedStatement = null;
		String readstatus = null;
		try{
			sqlConn = new SQLConnection();
			String selectSql = "select READSTATUS from Alerts where ALERTID = ?";
			preparedStatement = sqlConn.conn.prepareCall(selectSql);
			preparedStatement.setString(1, alertid);

			ResultSet rs = preparedStatement.executeQuery();
			
			if(!rs.isBeforeFirst())
			{
					Helper.printMessage("Invalid Alertid. Try again");
					return;
			}
			else
			{
				while(rs.next())
				{
					readstatus = rs.getString("READSTATUS");
					if(Integer.parseInt(readstatus) == 0)
					{
						Helper.printMessage("you have not viewed the alert. you can't clear");
						return;
					}
					else
					{
						String selectSql1 = "UPDATE Alerts SET CLEARSTATUS" +" = '1' WHERE alertid = ?";
						sqlConn.conn.setAutoCommit(false);
						preparedStatement = sqlConn.conn.prepareCall(selectSql1);
						preparedStatement.setString(1, alertid);
						int result = preparedStatement.executeUpdate();
						
						Helper.printMessage("Alert cleared"); //debug message
					    //System.out.println("Commiting data here....");//debug message
					    sqlConn.conn.commit();
					}
				}
			}
			
		}
		catch(SQLException se)
		{
			se.printStackTrace();
		}
		finally{
			if(preparedStatement != null)
				preparedStatement.close();
			if(sqlConn != null)
				sqlConn.terminateSQLconnection();
		}

	}	
//	// Working on this Right now
//	private void clearAlerts(String user) throws SQLException, Exception {
//		// TODO Auto-generated method stub
//		
//		System.out.println("Enter AlertId to clear");
//		Scanner scan = new Scanner(System.in);
//		int alert = Integer.parseInt(scan.nextLine()); // Alert Id
//		PreparedStatement preparedStatement = null;
//		SQLConnection sqlcon = null;
//		try{
//			String selectSQL = "UPDATE alerts SET readStatus = '1' WHERE alertId ="+alert+"and authorisedPersonId = ?";
//			sqlcon = new SQLConnection();
//			sqlcon.conn.setAutoCommit(false);
//			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
//			preparedStatement.setString(1, username);
//			
//			int result = preparedStatement.executeUpdate();
//			System.out.println("result is " + result);
//	    	if(result == 0)
//	    	{
//	    		System.out.println("No record deleted!!!");
//	    	}else
//	    		System.out.println("\nDeleted Successfully!!!\n");
//	    	sqlcon.conn.commit();
//	    	System.out.println("Do you want to delete more alerts? Choose option:");
//	    	System.out.println("1. Yes\t\t2. No");  
//	    	
//			try{
//				Scanner scanner = new Scanner(System.in);
//				int alertid = Integer.parseInt(scanner.nextLine());
//				if(alertid == 1)
//					clearAlerts(user);
//				else if(alertid==2)
//					seePatientDetails(user);
//				else
//					{
//					System.out.println("Invalid alert id. Try Again!!");
//					clearAlerts(user);
//					}
//			}
//			catch(NumberFormatException e){
//				System.out.println("Invalid input");
//				clearAlerts(user);
//			}
//	    	
//		}	
//		catch (SQLException e) {
//			// TODO Auto-generated catch block
//			System.out.println("Something went wrong. Try again!");
//			sqlcon.conn.rollback();
//			e.printStackTrace();			
//			}
//	  		catch(Throwable e) 
//	  		{
//	  			e.printStackTrace();
//	  		}
//				finally {
//					if (preparedStatement != null) {
//						preparedStatement.close();
//					}
//					if(sqlcon != null)
//						sqlcon.terminateSQLconnection();
//				}
//	}

	// Viewing Alerts in table.. Need to test view for long messages
	private void showAlerts(ArrayList<ArrayList<String>> list) {
		// TODO Auto-generated method stub
		System.out.println("\n");
		System.out.println("\t\tAlertId\t\tAlert Type\t\tHealth Indicator\t\tmessage\t\t\t\tReadStatus");
		System.out.println("\t\t______\t\t__________\t\t_________________\t\t_______\t\t\t\t__________");
		for (ArrayList<String> row : list) {
			int i=0;
			for (String field : row) {
				if(i<2){
					field = String.format("\t\t%s", field);
				}
				if(i>=2){
					field = "\t\t"+field+"\t\t";
				}
				
				System.out.print(field);
				i+=1;
			}
			System.out.println("  ");
		}
		System.out.println("\n");
		
	}

	protected void showProfile() throws Exception{
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		String gender = null;
		//System.out.println("In show profile");
		try{
			String selectSQL = "SELECT Gender, Apartment, Street, City, State, zipcode, DOB, isPatient, isSick FROM Users WHERE UserId = ? and password = ?";
			sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, pwd);
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				 gender = rs.getString("Gender");
				String apartment = rs.getString("Apartment");
				String street = rs.getString("Street");
				String city = rs.getString("City");
				String state = rs.getString("State");
				String zipcode = rs.getString("zipcode");
				java.sql.Date dob  = rs.getDate("DOB");
				String isPatient = rs.getString("isPatient");
				String isSick = rs.getString("isSick");
				String patientCategory = null;
				String userType= null;
				if(isSick.equals("1"))
					patientCategory = "sick";
				else
					patientCategory = "well";
				
				if(isPatient.equals("1"))
					userType = "Patient and Health Supporter";
				else
					userType = "Health Supporter";
				
				
				System.out.println("\n\n*****************************************************");
				System.out.println("*                                                   *");
				System.out.println("*                     Profile Page                  *");
				System.out.println("*                                                   *");
				System.out.println("*****************************************************");
	            System.out.println("\nUser ID: "+username +"\n" + 
	            					"Name: "+firstname+" "+lastname+"\n" + 
	            					"Gender: "+gender +"\n"+
	            					"Date Of Birth: "+dob +"\n"+
	            					"Address: "+street +", "+apartment+", "+city+", "+state+"- "+zipcode +"\n"+
	            					"Patient Category: "+patientCategory +"\n" +
	            					"User Type: "+userType);

	            System.out.println("-------------------------------------------------------");
	            				
			}

			System.out.println("Do you want to update profile?");
			System.out.println("\n1. Update Profile\n2. Back to patient Menu");
			System.out.println("Enter choice[1,2]: ");
			
			Scanner scanner = new Scanner(System.in);
			int value = Integer.parseInt(scanner.nextLine());
			switch(value)
			{
			case 1: 
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if(sqlcon != null)
					sqlcon.terminateSQLconnection();
				updateProfileMenu();
				break;
			case 2:
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if(sqlcon != null)
					sqlcon.terminateSQLconnection();
				new Welcome().printBanner();
				showMenuItems();
				break; 
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong. Try again!");
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlcon != null)
				sqlcon.terminateSQLconnection();
			updateProfileMenu();
			}			
			finally {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if(sqlcon != null)
					sqlcon.terminateSQLconnection();
				}	

	}

	
	// OLD version Viewing profile of Health Supporter
	protected void showProfile_old() throws Exception{
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		String gender = null;
		//System.out.println("In show profile");
		try{
			String selectSQL = "SELECT Gender, Apartment, Street, City, State, zipcode, DOB, isPatient, isSick FROM Users WHERE UserId = ? and password = ?";
			sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, pwd);
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				gender = rs.getString("Gender");
				String apartment = rs.getString("Apartment");
				String street = rs.getString("Street");
				String city = rs.getString("City");
				String state = rs.getString("State");
				String zipcode = rs.getString("zipcode");
				java.sql.Date dob  = rs.getDate("DOB");
				String isPatient = rs.getString("isPatient");
				String isSick = rs.getString("isSick");
				String patientCategory = null;
				String userType= null;
				
				if(isSick.equals("1"))
					patientCategory = "sick";
				else
					patientCategory = "well";
				
				if(isPatient.equals("1"))
					userType = "Patient and Health Supporter";
				else
					userType = "Health Supporter";
				

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
			  System.out.println("\n1. Update Profile \t\t\t\t 2. Back to Health Supporter Menu");			  

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
			System.out.println(e);
			System.out.println("Something went wrong. Try again!");
			updateProfileMenu();
			}			
			finally {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if(sqlcon != null)
					sqlcon.terminateSQLconnection();
				}	

	}
	
	private void updateProfileMenu() throws SQLException, Exception {
		// TODO Auto-generated method stub
		System.out.println("\nUpdate profile page: ");
        System.out.println("1. First Name\n2. Last Name\n3. Apartment");
        System.out.println("4. Street\n5. City\n6. State\n7. zipcode");
        System.out.println("8. Date Of Birth\n9. Password\n10. Gender\n0. Go to the Previous Menu.");
        System.out.println("\nEnter choice [0-10]");
        modifyProfileData();
		
	}

	//OLD version
	private void updateProfileMenu_old() throws SQLException, Exception {
		// TODO Auto-generated method stub
		System.out.println("Please select the field, you want to modify: ");
        System.out.println("1. First Name" +"\t" + "2. Last Name"+"\n" 
		+"3. Apartment" + "\t\t" + "4. Street" +"\t\t"  + "5. City" +"\t\t" + "6. State" +"\t\t" +"7. zipcode"+ "\n" 
        + "8. Date Of Birth\n"
		+"9. Password\n"+"10. Gender\n"
        +"0. Go to the Previous Menu.");       
        modifyProfileData();
		
	}
	
	// Update the data for Health supporter
	private void modifyProfileData() throws SQLException, Exception  {
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

	private void dateOfBirthValidation() throws SQLException, Exception { 
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

	private void updateProfileData(String columnName, String newColumnValue)throws SQLException, Exception{
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		  try {
				String selectSQL = "UPDATE Users SET "+columnName+" = '"+newColumnValue +"' WHERE UserId = ?";
				sqlcon = new SQLConnection();
				sqlcon.conn.setAutoCommit(false);
				preparedStatement = sqlcon.conn.prepareCall(selectSQL);
				preparedStatement.setString(1, username);
				
				int result = preparedStatement.executeUpdate();
				System.out.println("result is " + result);
	        	if(result == 0)
	        	{
	        		System.out.println("No row updated");
	        	}else
	        		System.out.println("\nUpdate Successful!!!\n");
	        	sqlcon.conn.commit();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("Something went wrong. Try again!");
				sqlcon.conn.rollback();
				e.printStackTrace();			
				}
		  		catch(Throwable e) 
		  		{
		  			e.printStackTrace();
		  		}
					finally {
						if (preparedStatement != null) {
							preparedStatement.close();
						}
						if(sqlcon != null)
							sqlcon.terminateSQLconnection();
					}
	        showProfile();
	}
	
	// Display List of Patients who are supported by Health Supporter
	private void showAllPatientsTable(ArrayList<ArrayList<String>> list){
		System.out.println("\n");
		System.out.println("\t\tUserId\t\tFirst Name\t\tLast Name");
		System.out.println("\t\t______\t\t__________\t\t_________");
		for (ArrayList<String> row : list) {
			int i=0;
			for (String field : row) {
				if(i<2){
					field = String.format("\t\t%s", field);
				}
				if(i>=2){
					field = "\t\t\t"+field+"\t\t";
				}
				
				System.out.print(field);
				i+=1;
			}
			System.out.println("  ");
		}
		System.out.println("\n");
	}	
}
