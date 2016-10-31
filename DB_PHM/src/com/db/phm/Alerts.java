package com.db.phm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Alerts {
	
	public String username = null;
	public String firstname = null;
	public String lastname = null;
	public String isSick = null;
	public String hasSplReco = null;
	public HashMap<String, String> nonnumeric2numeric = new HashMap<String, String>();
	
	public Alerts(String username, String firstname, String lastname, String isSick, String hasSplReco)  {
		// TODO Auto-generated constructor stub
		//
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.isSick = isSick;
		this.hasSplReco = hasSplReco;
		nonnumeric2numeric.put("Happy", "1");
		nonnumeric2numeric.put("Neutral", "2");
		nonnumeric2numeric.put("Sad", "3");
		nonnumeric2numeric.put("Depressed", "4");
		nonnumeric2numeric.put("Angry", "5");
	}
	
	public  void showAlertsMenuItems() throws SQLException{
	
		System.out.println("Please select from the below options: ");
		System.out.println("1. Show Alerts \n2. Clear Alerts \n3. Logout");
		
		selectAnAction();		
	}
	
	public  void selectAnAction() throws SQLException {
		//Diagnoses resource = new Diagnoses(this.userName, this.userType);
//		try{
			boolean flag = true;
			while(flag){
					//@SuppressWarnings("Diagnoses")
					Scanner scanner = new Scanner(System.in);
					String value = scanner.nextLine();
					int choice = Integer.parseInt(value);
					switch(choice){
					case 1:
						//Profile
						showAlerts();
						flag=false;
						break;
					case 2:
						//Diagnoses
						clearAlerts();
						flag = false;
						break;
					case 3:
						Welcome wc = new Welcome();
						wc.logout();
						flag = false;
						break;
					default:
						System.out.println("Invalid choice: Please enter again.");
							
					}
				}
		
		}


	public void showAlerts() throws SQLException { 
		// TODO Auto-generated method stub
		/*Generate alerts based on current data */
		if(Integer.parseInt(isSick) == 0)
		{
			Helper.printMessage("You have no alerts"); // alerts only for sick patient
			return;
		}
		generateOutsideLimitAlerts();
		generateLowActivityAlerts();
		
		displayAlerts();
		showAlertsMenuItems();// should go back to main menu after displaying both kind of alerts. So need to give go back to alert menu fromthese alrtgenerating finctions
	}
	
	public void  displayAlerts() throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		//ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
		try{
			sqlcon = new SQLConnection();
			String selectSQL = "select ALERTID, ALERTTYPE, HINAME, MESSAGE, READSTATUS from alerts where P_USERID = ? and CLEARSTATUS = '0'";
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, username);
			String alertid = null;
			ResultSet rs = preparedStatement.executeQuery();
			int i =1;
			if(!rs.isBeforeFirst())
			{
				Helper.printMessage("No alerts found !");
			}
			else
			{
				while(rs.next()){
					//ArrayList<String> row = new ArrayList<String>();
					alertid = rs.getString("ALERTID");
					String alerttype = rs.getString("ALERTTYPE");
					String hiname = rs.getString("HINAME");
					String message = rs.getString("MESSAGE");
					String READSTATUS = rs.getString("READSTATUS");
					//for health supporter integration
//				    row.add(alertid);
//					row.add(alerttype);
//					row.add(hiname);
//					row.add(message);
//					row.add(READSTATUS);
					//row.add(rsAlert.getString("readStatus"));					
					//for health supporter integration
					Helper.printMessage(i+". Health Indicator: "+hiname+ ", Alert type: "+alerttype+", Detail: "+message);
					String sq1 = "UPDATE Alerts SET READSTATUS" +" = '1' WHERE alertid = ?";
					sqlcon.conn.setAutoCommit(false);
					preparedStatement = sqlcon.conn.prepareCall(sq1);
					preparedStatement.setString(1, alertid);
					int result = preparedStatement.executeUpdate();
					
					//Helper.printMessage("Read status updated");//debug message
				    //System.out.println("Commiting data here....");//debug message
				    sqlcon.conn.commit();
				}
			}
			//change the read status to 1
			
		}
		catch(SQLException se)
		{
			se.printStackTrace();
			  try{
				 if(sqlcon.conn!=null)
					 sqlcon.conn.rollback();
		      }catch(SQLException se2){
		         se2.printStackTrace();
		      }	
			Helper.printMessage("Something went wrong. Try again !");			
		}
		finally{
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlcon != null)
				sqlcon.terminateSQLconnection();
			}
		return;
	}

	public void generateOutsideLimitAlerts() throws SQLException {
		// TODO Auto-generated method stub
		//HealthIndiactor hi = new HealthIndiactor(username, firstname, lastname, isSick);
		Map<String, LimitsForHIName> limitHmap = new HashMap<String, LimitsForHIName>(); 
		Map<String, List<String>> obsValHmap = new HashMap<String, List<String>>();
		Map<String, List<java.sql.Date>> obsDateHmap = new HashMap<String, List<java.sql.Date>>();
		//ArrayList<String> hiList = hi.getObservationList();
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		try{
			sqlcon = new SQLConnection();
						
			//get the low limit and high limit for each of HI types	
			if(hasSpecialRecommendation() == 1) 
			{
				String selectSQL = "SELECT HINAME, lowerLimit, UpperLimit, hasLimit, freq, HS_UserId FROM specificRecommendations where P_UserId = ?";
				preparedStatement = sqlcon.conn.prepareCall(selectSQL);
				preparedStatement.setString(1, username);
				ResultSet rs = preparedStatement.executeQuery();
				while(rs.next()){
					LimitsForHIName limits = new LimitsForHIName();
					limits.lowLimit = rs.getString("lowerLimit");
					limits.highLimit = rs.getString("UpperLimit");
					limits.hasLimit = Integer.parseInt(rs.getString("hasLimit"));
					limitHmap.put(rs.getString("HINAME"), limits); 
				}
			}
			//get the low limit and high limit for each of HI types for this patients	
			else  		
				{
				String selectSQL1 = "SELECT HINAME, MAX(HASLIMIT) AS HL, MAX(LOWERLIMIT) AS MAX_LL, MIN(UPPERLIMIT) AS MIN_HL FROM GENERALRECOMMENDATIONSICK WHERE DNAME IN( SELECT dName from Diagnosed where P_USERID= ?) Group By HINAME";
				preparedStatement = sqlcon.conn.prepareCall(selectSQL1);
				preparedStatement.setString(1, username);						
				ResultSet rs1 = preparedStatement.executeQuery();
				while(rs1.next()){
					LimitsForHIName limits1 = new LimitsForHIName();
					limits1.hasLimit = Integer.parseInt(rs1.getString("HL"));
					limits1.lowLimit = rs1.getString("MAX_LL");
					limits1.highLimit = rs1.getString("MIN_HL");
					limitHmap.put(rs1.getString("HINAME"), limits1); 
				}						
			}	
			// get the observation value and store in obsValHmap
			for(String hIndicator : limitHmap.keySet())
			{
				String selectSQL2 = "select * from (select observationvalue, RECORDTIME from reading where P_USERID = ? and HINAME = ? ORDER BY HINAME, RECORDTIME DESC) where rownum <=3";
				preparedStatement = sqlcon.conn.prepareCall(selectSQL2);
				preparedStatement.setString(1, username);
				preparedStatement.setString(2, hIndicator);
				ResultSet rs2 = preparedStatement.executeQuery();
				List<String> obsVallist = new ArrayList<String>();
				List<java.sql.Date> recorddate_obsVallist = new ArrayList<java.sql.Date>();
				while(rs2.next()){
					if(hIndicator.equals("Mood"))
					{
						String moodtype = rs2.getString(1);
						java.sql.Date recordtime = rs2.getDate(2);
						String num_moodType = nonnumeric2numeric.get(moodtype); 
						obsVallist.add(num_moodType);
						recorddate_obsVallist.add(recordtime);
					}
					else
					{
						obsVallist.add(rs2.getString(1));
						recorddate_obsVallist.add(rs2.getDate(2));
					}
				}
				obsValHmap.put(hIndicator, obsVallist);
				obsDateHmap.put(hIndicator, recorddate_obsVallist);
			}

			// check if obsvalue from obsValhmap is outside limit
			   for (String hIndicator : limitHmap.keySet()) {				   				   
				   // check if alert already generated for this record
				   LimitsForHIName limits_hi = limitHmap.get(hIndicator);
				   List<String> obs_val = obsValHmap.get(hIndicator);
				   List<java.sql.Date> recorddate_obs_val = obsDateHmap.get(hIndicator);
				   String lLimit= null;
				   String hLimit =  null;
				   String msg = null;
				   if(obs_val.size() == 3)
				   {// there are 3 consecutive readings then only alert check. if < 3 no check
						// check if alert already generated for this record
						String sqlquery = "select * from(select DATEFORLASTRECORD from Alerts where P_USERID = ? and HINAME = ?  and ALERTTYPE = ? order by DATEFORLASTRECORD desc) where rownum <=1";
						preparedStatement = sqlcon.conn.prepareCall(sqlquery);
						preparedStatement.setString(1, username);
						preparedStatement.setString(2, hIndicator);
						preparedStatement.setString(3, "OUT_LIMIT");
						ResultSet rset = preparedStatement.executeQuery();
						java.sql.Date lastrecordtime = null;
						while(rset.next())
						{
							lastrecordtime = rset.getDate(1);
							
						}
						if(recorddate_obs_val.get(0).equals(lastrecordtime))
						{
							//Helper.printMessage("Alert has already been generated for this health indicator");//debug message
							continue; // alert has already been generated for this health indicator
						}
					   if(limits_hi.hasAnyLimit() == 1)
					   {
						   lLimit = limits_hi.lowLimit;
						   hLimit = limits_hi.highLimit;
						   
						   //alert generation
						   if(limitOutofRange(lLimit,hLimit,obs_val) == 1)
						   {
							   if(hIndicator.equals("Mood"))
									 msg = "Last 3 observation values are " + obs_val.get(0)+", "+obs_val.get(1)+" and "+obs_val.get(2)+" Desired range is between :  "+lLimit+" and "+hLimit +" where 1: Happy 2: Neutral 3: Sad 4.Very Sad 4. Depressed 5. Angry"; 
							   else
							   	 msg = "Last 3 observation values are " + obs_val.get(0)+", "+obs_val.get(1)+" and "+obs_val.get(2)+" Desired range is between :  "+lLimit+" and "+hLimit; 
								
							   String selectSQL3 = "INSERT INTO Alerts (ALERTID, P_UserId, ALERTTYPE, HINAME, MESSAGE, READSTATUS, GENERATEDATE, DATEFORLASTRECORD, CLEARSTATUS)VALUES ( alertID_seq.nextval, ?, 'OUT_LIMIT', ?, ?, '0', to_date(TRUNC(SYSDATE),'yyyy-mm-dd'), ?, '0')";
								sqlcon.conn.setAutoCommit(false);
								preparedStatement = sqlcon.conn.prepareCall(selectSQL3);
								preparedStatement.setString(1, username);
								preparedStatement.setString(2, hIndicator);	
								preparedStatement.setString(3, msg); 
								preparedStatement.setDate(4, recorddate_obs_val.get(0));
								int result = preparedStatement.executeUpdate();
								//Helper.printMessage("alert inserted in alerts table");//debug message
							    //System.out.println("Commiting data here....");//debug message
							    sqlcon.conn.commit();
						   }
					   }
				   }
			    }
		}
		catch(SQLException se){
			
			se.printStackTrace();
			Helper.printMessage("Something went wrong. Try again");
			showAlertsMenuItems();
			
		}
		finally{
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlcon != null)
				sqlcon.terminateSQLconnection();
			}			
			
		}

	public int limitOutofRange(String lLimit, String hLimit, List<String> obs_val) {
		// TODO Auto-generated method stub
		// all the three latest readings should be out of range.If any of these readings are within the range alert is not generated
		for (String value : obs_val) {
		    if (Integer.parseInt(value) >= Integer.parseInt(lLimit) && Integer.parseInt(value) <= Integer.parseInt(hLimit)) {
		    	return 0;
		    }
		}
		return 1;
	}

	
	
	private int hasSpecialRecommendation() {
		// TODO Auto-generated method stub
		if(hasSplReco != null)
		 return Integer.parseInt(hasSplReco);
		else
		return -1;
	}

	public void generateLowActivityAlerts() {
		// TODO Auto-generated method stub
		
	}
	public void clearAlerts() throws SQLException {
		generateOutsideLimitAlerts();
		generateLowActivityAlerts();
		clearAlertsFromTable();
		showAlertsMenuItems();
	}
	public void clearAlertsFromTable() throws SQLException {
		// TODO Auto-generated method stub
		System.out.println("Please do the following for clear the alert");
		System.out.println("Enter the health indicator name: ");
		String hi_name = Helper.enteredConsoleString();
		System.out.println("Enter the alert type: \n1: for out of limits \n2: for low activity");
		String hi_type = Helper.enteredConsoleString();
		
		String alertid = null;
		String readstatus = null;
		
		//get the read status. it should be 1 then only can clear alerts
		SQLConnection sqlConn = null;
		PreparedStatement preparedStatement = null;
		try{
			sqlConn = new SQLConnection();
			String selectSql = "select ALERTID, READSTATUS from Alerts where P_UserId = ? and ALERTTYPE = ? and HINAME =? and CLEARSTATUS = '0'";
			preparedStatement = sqlConn.conn.prepareCall(selectSql);
			preparedStatement.setString(1, username);
			
			if(Integer.parseInt(hi_type) == 1)
			preparedStatement.setString(2, "OUT_LIMIT");
			else if(Integer.parseInt(hi_type) == 2)
				preparedStatement.setString(2, "LOW_ACTIVITY");
			
			preparedStatement.setString(3, hi_name);
			ResultSet rs = preparedStatement.executeQuery();
			
			if(!rs.isBeforeFirst())
			{
					Helper.printMessage("Alert does not exist for this health indication and alert type. Try again");
					return;
			}
			else
			{
				while(rs.next())
				{
					alertid = rs.getString("ALERTID");
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
			

			//else can't clear alerts
		}
		catch(SQLException se){
			
			se.printStackTrace();
			Helper.printMessage("Something went wrong. Try again");
			//showAlertsMenuItems();
			
		}
		finally{
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlConn != null)
				sqlConn.terminateSQLconnection();
			}			
			
		}

}

class LimitsForHIName
  {
	String hiName;
	int hasLimit;
	String lowLimit;
	String highLimit;
	//String[] otherLimitVal; // for mood type health indicators
	
	public String getHIName(){
		return hiName;
	}
	
	public int hasAnyLimit()
	{
		return hasLimit;
	}
	public String getLowLimit()
	{
		return lowLimit;
	}
	public String getHighLimit()
	{
		return highLimit;
	}
//	public String[] getOtherlimits()
//	{
//		return otherLimitVal;
//	}
  }
