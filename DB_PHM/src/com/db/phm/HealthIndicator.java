package com.db.phm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class HealthIndicator {
	
	public String username = null;
	public String pwd = null;
	public String firstname = null;
	public String lastname = null;
	public String isSick = null;
	public String hasSplReco = null;
	
	
	public HealthIndicator(String username, String firstname, String lastname, String isSick, String hasSplReco) {
		this.username = username;
		this.pwd = pwd;
		this.firstname = firstname;
		this.lastname = lastname;
		this.isSick = isSick;	
		this.hasSplReco = hasSplReco;
	}
	
	public  void showObservationReading() throws Exception { 
		System.out.println("\n\n*****************************************************");
		System.out.println("*                                                   *");
		System.out.println("*               Recommendations Page                *");
		System.out.println("*                                                   *");
		System.out.println("*****************************************************");
		boolean flag=true;
		while(flag){
			System.out.println("\n\n1. My Recommendations");
			System.out.println("2. Add Observation");
			System.out.println("3. View Observations");
			System.out.println("4. Go back to patient menu");
			System.out.println("5. Logout");			
			System.out.println("Enter choice [1-5]:");
			int choiceInt = Helper.getConsoleIntValue();
			switch(choiceInt){
				case 1:
					showRecommendation();
					break;
				case 2:
					showAddObservationPage();
					break;
				case 3:
					showViewObservationPage();
					break;
				case 4:
					flag = false;
					Patient patient = new Patient(username, firstname, lastname, isSick, hasSplReco);
					patient.showMenuItems();
					break;						
				case 5:
					flag = false;
					Welcome wc = new Welcome();
					wc.logout();					
					break; 
				default:
					System.out.println("Invalid Input, try again ...");
			}
		}
		//new Patient(this.username, this.firstname, this.lastname, this.isSick).showMenuItems();
	}
	private void showAddObservationPage() throws Exception { 
		ArrayList<String> obsType = getObservationList();
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
		String hiname = null,value = null,odate = null,rdate = null;
		while(true){
			int choiceInt = Helper.getConsoleIntValue();
			if(choiceInt == 0){
				new Welcome().printBanner();
				new Patient(username, firstname, lastname, isSick, hasSplReco).showMenuItems();
				break; 
			}
			if(choiceInt<=i&&choiceInt>0){
				hiname = obsType.get(choiceInt-1);
				System.out.println("Enter Observation value:");
				value=Helper.getConsoleValue();
				System.out.println("Enter Observation date [MM/DD/YYYY]:");
				odate = Helper.getConsoleDateValue();
				System.out.println("Enter Record date [MM/DD/YYYY]:");
				rdate = Helper.getConsoleDateValue();
				break;
			}
			System.out.println("Invalid input,try again ... ");
			System.out.println("Enter choice["+choiceStr+"]:");
		}
		//insert into database
		insertUserObservation(this.username,hiname,value,odate,rdate);
		
	}
	
	private void insertUserObservation(String username2, String hiname, String value, String odate, String rdate) {
		PreparedStatement preparedStatement = null;
		String insertDiseaseQuery = "INSERT INTO Reading (P_UserId, HIName, observationTime, recordTime, observationValue) VALUES (?, ?, ?, ?, ?)";
		try{
			SQLConnection sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(insertDiseaseQuery);
			preparedStatement.setString(1, this.username);
			preparedStatement.setString(2, hiname);
			preparedStatement.setString(3, odate);
			preparedStatement.setString(4, rdate);
			preparedStatement.setString(5, value);
			preparedStatement.execute();
			System.out.println("\n -----  Observation Added -----\n");
		}catch (SQLException e) {
			System.out.println("Error occured in adding diagnosed disease");
		}
	}
	
	public void showRecommendation() throws SQLException{ 
		//show disease recommendation
		HashMap<String,ArrayList<ArrayList<String>>> recMap = getDiseaseRecommendation();
		System.out.println("_______________________________");
		System.out.println("      Disease Recommendation   ");
		System.out.println("_______________________________");
		for (String diseaseName : recMap.keySet()) {
			System.out.println("\n"+diseaseName+"\n---------------------");
			System.out.println("Observation Type\t\tLower Limit\t\tUpper Limit\t\tHas Limit\t\tFrequency(days)");
			System.out.println("________________\t\t___________\t\t___________\t\t_________\t\t_______________");
			for (ArrayList<String> list : recMap.get(diseaseName)) {
				String obsName= list.get(0);
				obsName = String.format("%-30s", obsName);
				System.out.println(obsName+"\t"+list.get(1)+"\t\t\t"+list.get(2)+"\t\t\t"+list.get(3)+"\t\t\t"+list.get(4));
			}
		}
		
		//show specific recommendation
		ArrayList<ArrayList<String>> list = getSpecificRecommendation();
		System.out.println("\n\n______________________________________");
		System.out.println("      User Specific Recommendations   ");
		System.out.println("______________________________________\n");
		System.out.println("Observation Type\t\tLower Limit\t\tUpper Limit\t\tHas Limit\t\tFrequency(days)\t\tHealth Supporter");
		System.out.println("________________\t\t___________\t\t___________\t\t_________\t\t_______________\t\t________________");
		for (ArrayList<String> row : list) {
			String obsName= row.get(0);
			obsName = String.format("%-30s", obsName);
			System.out.println(obsName+"\t"+row.get(1)+"\t\t\t"+row.get(2)+"\t\t\t"+row.get(3)+"\t\t\t"+row.get(4)+"\t\t\t\t"+row.get(5));
		}
	}
	
	private ArrayList<ArrayList<String>> getSpecificRecommendation() throws SQLException {
		PreparedStatement preparedStatement = null;
		String viewObservationQuery = "SELECT HIName, lowerLimit, UpperLimit, hasLimit, freq FROM specificRecommendations WHERE P_UserID=?";
		ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
		SQLConnection sqlcon = null;
		try {
			sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(viewObservationQuery);
			preparedStatement.setString(1, this.username);
			ResultSet rs = preparedStatement.executeQuery();
			while(rs.next()){
				ArrayList<String> row = new ArrayList<String>();
				row.add(rs.getString("HIName"));
				row.add(rs.getString("lowerLimit"));
				row.add(rs.getString("UpperLimit"));
				row.add(rs.getString("hasLimit"));
				row.add(rs.getString("freq"));
				//row.add(rs.getString("HS_UserId"));
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Helper.printMessage("Something went wrong. Try again"); 
		}
		finally{
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlcon != null)
				sqlcon.terminateSQLconnection();
			}		
		return list;
	}
	public HashMap<String,ArrayList<ArrayList<String>>> getDiseaseRecommendation() throws SQLException{ 
		HashMap<String,ArrayList<ArrayList<String>>> recMap = new HashMap<String,ArrayList<ArrayList<String>>>();
		SQLConnection sqlcon = null;
		String viewObservationQuery;
		if(this.isSick.equals("1"))
			viewObservationQuery="SELECT R.dName dName, R.HIName HIName,  R.lowerLimit lowerLimit, R.UpperLimit UpperLimit, R.hasLimit hasLimit, R.freq freq FROM GeneralRecommendationSick R WHERE dName IN (SELECT DISTINCT D.dName FROM Diagnosed D WHERE D.P_UserId=?)"; // for sick patients
		else
			viewObservationQuery="SELECT 'Healthy Recommendation' dName, R.HIName HIName,  R.lowerLimit lowerLimit, R.UpperLimit UpperLimit, R.hasLimit hasLimit, R.freq freq FROM GeneralRecommendationSick R"; // for well patients
		PreparedStatement preparedStatement = null;
		try {
			sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(viewObservationQuery);
			preparedStatement.setString(1, this.username);
			ResultSet rs = preparedStatement.executeQuery();
			while(rs.next()){
				ArrayList<String> row = new ArrayList<String>();
				//row.add(rs.getString("dName"));
				row.add(rs.getString("HIName"));
				row.add(rs.getString("lowerLimit"));
				row.add(rs.getString("UpperLimit"));
				row.add(rs.getString("hasLimit"));
				row.add(rs.getString("freq"));
				if(!recMap.containsKey(rs.getString("dName"))){
					ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
					list.add(row);
					recMap.put(rs.getString("dName"), list);
				}else{
					String dname =rs.getString("dName");
					ArrayList<ArrayList<String>> list = recMap.get(dname);
					list.add(row);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Helper.printMessage("Something went wrong. Try Again");
		}
		finally{
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlcon != null)
				sqlcon.terminateSQLconnection();
			}
		return recMap;
		}
	
	public ArrayList<String> getObservationList(){
		ArrayList<String> list=new ArrayList<String>();
		PreparedStatement preparedStatement = null;
		try{
			//"SELECT HIName FROM observation ORDER BY HIName";
			String diseaseNamesQuery = "SELECT DISTINCT HINAME FROM GENERALRECOMMENDATIONSICK WHERE DNAME IN( SELECT dName from Diagnosed WHERE P_USERID=?)";
			SQLConnection sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(diseaseNamesQuery);
			preparedStatement.setString(1,this.username);
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
	
	private void showViewObservationPage() throws SQLException {  
		System.out.println("_______________________________");
		System.out.println("          Observations         ");
		System.out.println("_______________________________\n\n");
		ArrayList<ArrayList<String>> list = getUserObservations();
		System.out.println("\t\tObservation Name\t\t\tObservation Value\t\tObservation Date\t\tReading Date");
		System.out.println("\t\t________________\t\t\t_________________\t\t________________\t\t____________");
		for (ArrayList<String> row : list) {
			int i=0;
			for (String field : row) {
				if(i<2){
					field = String.format("\t\t%-30s", field);
				}
				if(i>=2){
					field = "\t"+field+"\t\t";
				}
				System.out.print(field);
				i+=1;
			}
			System.out.println("  ");
		}
		System.out.println("\n");
	}
	private ArrayList<ArrayList<String>> getUserObservations() throws SQLException { 
		PreparedStatement preparedStatement = null;
		String viewObservationQuery = "SELECT HIName, observationValue, to_char(observationTime,'MM/DD/YYYY') observationTime, to_char(recordTime,'MM/DD/YYYY') recordTime FROM Reading WHERE P_UserId=? ORDER BY recordTime";
		ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
		SQLConnection sqlcon  = null;
		try {
			sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(viewObservationQuery);
			preparedStatement.setString(1, this.username);
			ResultSet rs = preparedStatement.executeQuery();
			while(rs.next()){
				ArrayList<String> row = new ArrayList<String>();
				row.add(rs.getString("HIName"));
				row.add(rs.getString("observationValue"));
				row.add(rs.getString("observationTime"));
				row.add(rs.getString("recordTime"));
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Helper.printMessage("Something went wrong. Try again"); 
		}
		finally{
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlcon != null)
				sqlcon.terminateSQLconnection();
			}		
		
		return list;
	}
}
