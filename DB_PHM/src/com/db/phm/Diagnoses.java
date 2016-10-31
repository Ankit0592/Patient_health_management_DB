package com.db.phm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Diagnoses{
	
	public String username = null;
	public String firstname = null;
	public String lastname = null;
	public String isSick = null;
	public String hasSplReco = null;

	public Diagnoses(String username, String firstname, String lastname, String isSick, String hasSplReco) {
		// TODO Auto-generated constructor stub
		//
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.isSick = isSick;	
		this.hasSplReco = hasSplReco;
	}

	public  void showDiagnoseMenuItems() {
		System.out.println("Please select from the below options: ");
		System.out.println("1. Show Diagnoses \n2. Add Diagnoses \n3. Delete Diagnoses \n4. Back to patient Menu \n5. Logout");
		
		selectAnAction();
	}
	
	public  void selectAnAction() {
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
						//show list of diagnoses
						showDiagnoses();
						flag=false;
						break;
					case 2:
						addDiagnoses();
						flag = false;
						break;
					case 3:
						deleteDiagnoses();
						flag = false;
						break;
					case 4:
						Patient patient = new Patient(username, firstname, lastname, isSick, hasSplReco);
						patient.showMenuItems();
						flag = false;
						break;						
					case 5:
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
				showDiagnoseMenuItems(); // Back to Diagnosed menu. If user wants to logout hecan from this menu item

			}
		
		}
	public void deleteDiagnoses() throws SQLException{
		
		deleteDiagnosesHelper();
		showDiagnoseMenuItems();
		
	}
	public void deleteDiagnosesHelper() throws SQLException  {
		// TODO Auto-generated method stub
		System.out.print("Disease name to be deleted: "); 
		String diseaseName = Helper.enteredProfileData();
		SQLConnection sqlConn = null;
		PreparedStatement preparedStatement = null;
		int hasPatientThisDisease = -1;
		int patientCategory = -1;
		int hasPatientCategoryChanged = -1;
		//int hasPatientThisDisease = checkDiseaseInPatientDiagnoses(diseaseName);
		// add 
		try{
			String selectSQL = "select dname from Diagnosed where P_UserId = ? and dname = ?";
			sqlConn = new SQLConnection();
			preparedStatement = sqlConn.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, diseaseName);
			ResultSet rs = preparedStatement.executeQuery();
			if(!rs.isBeforeFirst())
			{
				Helper.printMessage("Disease not found in list. Add the disease " + diseaseName + " in list");
				hasPatientThisDisease = 0;
			}
			else
			{
				while (rs.next()) {
					System.out.println("Disease found in diagnosed for this user " + rs.getString("dname"));
					hasPatientThisDisease = 1;
				}
			}
			if(hasPatientThisDisease == 1)
			{
				String selectSQL1 = "delete from Diagnosed where P_UserId = ? and dname = ?";
				sqlConn.conn.setAutoCommit(false);
				preparedStatement = sqlConn.conn.prepareCall(selectSQL1);
				preparedStatement.setString(1, username);
				preparedStatement.setString(2, diseaseName);
				int result1 = preparedStatement.executeUpdate();
				System.out.println("Result of delete is: "+result1);//debug message			
				System.out.println("Disease deleted!");	
			    System.out.println("Commiting data here....");
			    sqlConn.conn.commit();
			  //updating the patient category(sick/well in same transaction)
			    
				String selectSQL2 = "select dname from Diagnosed where P_UserId = ?";
				preparedStatement = sqlConn.conn.prepareCall(selectSQL2);
				preparedStatement.setString(1, username);
				ResultSet rs2 = preparedStatement.executeQuery();
				if(!rs2.isBeforeFirst())
				{
					Helper.printMessage("patient not found in diagnosed table");
					patientCategory = 0;  //update the value of isSick
				}
				else
				{
					while (rs2.next()) {
						patientCategory = 1;
					}						
				}
				if(!isSick.equals(String.valueOf(patientCategory)))
				{
					System.out.println("patient category changed ");
					hasPatientCategoryChanged = 1;
				}	
				else
				{
					System.out.println("patient category not changed ");
					hasPatientCategoryChanged = 0;
				}			    
			    if(hasPatientCategoryChanged == 1)
			    {
					String selectSQL3 = "UPDATE Users SET isSick" +" = '0' WHERE UserId = ?"; //sick to well
					sqlConn.conn.setAutoCommit(false);
					preparedStatement = sqlConn.conn.prepareCall(selectSQL3);
					preparedStatement.setString(1, username);
					int result3 = preparedStatement.executeUpdate();
					System.out.println("Result of update patient category is: "+result3);//debug message
					System.out.println("patient category changed from sick to well!");
				    System.out.println("Commiting data here....");
				    sqlConn.conn.commit();				    	
			    }
			    
			    
			}
			else if(hasPatientThisDisease == 0)
			{
				Helper.printMessage("You can delete only the diseases you had. Try again!"); // first add disease to disease table 
				//updateDiagnoses();
			}		
		
	}
		catch(SQLException se)
		{
			se.printStackTrace();
			Helper.printMessage("Something went wrong. Try again!");
			  try{
				 if(sqlConn.conn!=null)
					 sqlConn.conn.rollback();
		      }catch(SQLException se2){
		         se2.printStackTrace();
		      }				
		}
		finally
		{
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlConn != null)
				sqlConn.terminateSQLconnection();
		}
	}
/* We wont be needing this method. Since patient category update should be done after add/delete diagnosis in same transaction */
	private void updatePatientCategoryInformation(String columnName, String newColumnValue)throws SQLException, Exception{
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
			} catch (SQLException se) {
				// TODO Auto-generated catch block
				System.out.println("Something went wrong.");
				System.out.println(" Try again!");
				sqlcon.conn.rollback();
				se.printStackTrace();
				updatePatientCategoryInformation(columnName, newColumnValue); // since diagnose has already been added, patient category needs to be updated. So if exception try to update agan till done
			}
				finally {
						if (preparedStatement != null) {
							preparedStatement.close();
						}
						if(sqlcon != null)
							sqlcon.terminateSQLconnection();
					}
//		  System.out.println("Reached till end of function");
		  //deleteDiagnoses();
	}

	public int patientCategoryChanged(String patientCategory) throws Exception{
		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		String retDiseaseName = null;
		String retpatientCategory = null;
		//System.out.println("In show profile");
		
		try{
			String selectSQL = "select dname, isSick from Diagnosed where P_UserId = ?";
			sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, username);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				retDiseaseName = rs.getString("dname");
				retpatientCategory = rs.getString("isSick");
				if(!patientCategory.equals(retpatientCategory))
				{
					System.out.println("patient category changed ");
					return 1;
				}	
				else
				{
					System.out.println("patient category not changed ");
					return 0;
				}	
				
			 }
				
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong. Try again!");
			//patientCategoryChanged(patientCategory);
			}			
			finally {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if(sqlcon != null)
					sqlcon.terminateSQLconnection();
				}	
	
				
		return -1;
	}
	public void deleteFromDiagnosedTable(String diseaseName) throws Exception  {
		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		int hasPatientCategoryChanged =-1;
		try{
			String selectSQL = "delete from Diagnosed where P_UserId = ? and dname = ?";
			sqlcon = new SQLConnection();
			sqlcon.conn.setAutoCommit(false);
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, diseaseName);
			int result = preparedStatement.executeUpdate();
			System.out.println("Result of delete is: "+result);//debug message			
			System.out.println("Disease deleted!");
			
			//updating the patient category(sick/well in same transaction)
			hasPatientCategoryChanged = patientCategoryChanged(isSick);
			if(hasPatientCategoryChanged == 1) // sick to well
			{
				selectSQL = "UPDATE Users SET isSick" +" = '0' WHERE UserId = ?";
				preparedStatement.setString(1, username);
				result = preparedStatement.executeUpdate();
				System.out.println("Result of update patient category is: "+result);//debug message
				System.out.println("patient category changed from sick to well!");
				//updatePatientCategoryInformation("isSick", "1");
			}			
		    System.out.println("Commiting data here....");
		    sqlcon.conn.commit();

		}catch (SQLException se) {
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
			 deleteDiagnoses();
			}			
			finally {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if(sqlcon != null)
					sqlcon.terminateSQLconnection();
				}				
	}
	public int checkDiseaseInPatientDiagnoses(String diseaseName) throws Exception { 
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		String retDiseaseName = null;
		//System.out.println("In show profile");
		
		try{
			String selectSQL = "select dname from Diagnosed where P_UserId = ? and dname = ?";
			sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, diseaseName);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				retDiseaseName = rs.getString("dname");
				System.out.println("Disease found in diagnosed for this user" + retDiseaseName);
				//Helper.printMessage("Disease found in list.");
				return 1;
			 }
				
				if(retDiseaseName == null){
					Helper.printMessage("Disease not found in list. Add the disease " + diseaseName + " in list");
					return 0;
				}

		}catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong. Try again!");
			deleteDiagnoses();
			}			
			finally {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if(sqlcon != null)
					sqlcon.terminateSQLconnection();
				}	
	
				
		return -1;
	}
	
	public void addDiagnoses() throws SQLException
	{
		addDiagnosesHelper();
		showDiagnoseMenuItems();
	}
	
	public void addDiagnosesHelper() throws SQLException { // this should happen in one transaction so pass the connection
		// TODO Auto-generated method stub
		System.out.print("Disease name: "); 
		String newDisease = Helper.enteredProfileData();
		System.out.print("Sick date in format yyyy-mm-dd : "); 
		String sickDate = Helper.enteredProfileData();
		String validFormat = "yyyy-mm-dd";
		int isDiseaseInList = -1;
		int hasPatientCategoryChanged = -1;
		int patientCategory = -1;
		SQLConnection sqlConn = new SQLConnection();
		PreparedStatement preparedStatement = null;
		try{
			if(Helper.validateDateFormat(sickDate, validFormat))
			{
				//isDiseaseInList = checkDiseaseExists(newDisease, sqlConn );
				//copy code of checkDiseaseExists
				String selectSQL = "select dname from Disease where dname = ?";
				sqlConn = new SQLConnection();
				preparedStatement = sqlConn.conn.prepareCall(selectSQL);
				preparedStatement.setString(1, newDisease);
				ResultSet rs = preparedStatement.executeQuery();
				if(!rs.isBeforeFirst())
				{
					Helper.printMessage("Disease not found in list. Add the disease '" + newDisease + "' in list");
					isDiseaseInList = 0;
				}
				else
				{
					while (rs.next()) {
						String diseaseName = rs.getString("dname");
						System.out.println("Disease found " + diseaseName);
						Helper.printMessage("Disease found in list.");
						isDiseaseInList = 1;
					 }	
					
				}
				//copy code of checkDiseaseExists
				if(isDiseaseInList == 1)
				{
					//copy code of addToDiagnosedTable
					//addToDiagnosedTable(newDisease, sickDate);
					String selectSQL1 = "INSERT INTO Diagnosed "+"(P_UserId, dName, fromDate) VALUES" + "(?,?,?)";
					sqlConn.conn.setAutoCommit(false);
					preparedStatement = sqlConn.conn.prepareCall(selectSQL1);
					preparedStatement.setString(1, username);
					preparedStatement.setString(2, newDisease);
					preparedStatement.setDate(3, java.sql.Date.valueOf(sickDate));
					int result1 = preparedStatement.executeUpdate();
					System.out.println("Result of insert diagnose is: "+result1);//debug message			
					System.out.println("Diagnose inserted!");
					sqlConn.conn.commit();
					//copy code of addToDiagnosedTable
					
					//updating the patient category(sick/well in same transaction)
					//copy code of patientCategoryChanged
					//hasPatientCategoryChanged = patientCategoryChanged(isSick);
					String selectSQL2 = "select dname from Diagnosed where P_UserId = ?";
					preparedStatement = sqlConn.conn.prepareCall(selectSQL2);
					preparedStatement.setString(1, username);
					ResultSet rs2 = preparedStatement.executeQuery();
					if(!rs2.isBeforeFirst())
					{
						Helper.printMessage("patient not found in diagnosed table");
						patientCategory = 0;  //update the value of isSick
					}
					else
					{
						while (rs2.next()) {
							patientCategory = 1;
						}						
					}
					if(!isSick.equals(String.valueOf(patientCategory)))
					{
						System.out.println("patient category changed ");
						hasPatientCategoryChanged = 1;
					}	
					else
					{
						System.out.println("patient category not changed ");
						hasPatientCategoryChanged = 0;
					}					

					//copy code of patientCategoryChanged				
					if(hasPatientCategoryChanged == 1) // well to sick
					{
						String selectSQL3 = "UPDATE Users SET isSick" +" = '1' WHERE UserId = ?";
						sqlConn.conn.setAutoCommit(false);
						preparedStatement = sqlConn.conn.prepareCall(selectSQL3);
						preparedStatement.setString(1, username);
						int result3 = preparedStatement.executeUpdate();
						System.out.println("Result of update patient category is: "+result3);//debug message
						System.out.println("patient category changed from well to sick!");
					    System.out.println("Commiting data here....");
					    sqlConn.conn.commit();						
						//updatePatientCategoryInformation("isSick", "1");
					}			
					
				}
				else if(isDiseaseInList == 0)
				{
					//these two need not be in same transaction since even a new disease is added in disease table, its not impacting any patient information
					//addToDiseaseTable(newDisease); // first add disease to disease table 
					// copy code from addToDiseaseTable
					String selectSQL4 = "INSERT INTO disease "+"(dName, description) VALUES" + "(?,?)";
					sqlConn.conn.setAutoCommit(false);
					preparedStatement = sqlConn.conn.prepareCall(selectSQL4);
					preparedStatement.setString(1, newDisease);
					preparedStatement.setString(2, null);
					int result4 = preparedStatement.executeUpdate();
					System.out.println("Result of insert is: "+result4);//debug message			
					System.out.println("Disease inserted!");
				    System.out.println("Commiting data here....");
				    sqlConn.conn.commit();					
					// copy code from addToDiseaseTable
				    //copy code from addToDiagnosedTable
				    //addToDiagnosedTable(newDisease, sickDate); // Then add disease to diagnosed table
					String selectSQL5 = "INSERT INTO Diagnosed "+"(P_UserId, dName, fromDate) VALUES" + "(?,?,?)";
					sqlConn.conn.setAutoCommit(false);
					preparedStatement = sqlConn.conn.prepareCall(selectSQL5);
					preparedStatement.setString(1, username);
					preparedStatement.setString(2, newDisease);
					preparedStatement.setDate(3, java.sql.Date.valueOf(sickDate));
					int result5 = preparedStatement.executeUpdate();
					System.out.println("Result of insert diagnose is: "+result5);//debug message			
					System.out.println("Diagnose inserted!");
					sqlConn.conn.commit();
					//copy code from addToDiagnosedTable
					
					//checkif patient category changed
					String selectSQL6 = "select dname from Diagnosed where P_UserId = ?";
					preparedStatement = sqlConn.conn.prepareCall(selectSQL6);
					preparedStatement.setString(1, username);
					ResultSet rs6 = preparedStatement.executeQuery();
					if(!rs6.isBeforeFirst())
					{
						Helper.printMessage("patient not found in diagnosed");
						patientCategory = 0;
					}
					else
					{
						while (rs6.next()) {
							patientCategory =1;
						}						
					}
					if(!isSick.equals(String.valueOf(patientCategory))) 
					{
						System.out.println("patient category changed ");
						hasPatientCategoryChanged = 1;
					}	
					else
					{
						System.out.println("patient category not changed ");
						hasPatientCategoryChanged = 0;
					}					
					//if patient category changed updatein users table
					if(hasPatientCategoryChanged == 1) // well to sick
					{
						String selectSQL7 = "UPDATE Users SET isSick" +" = '1' WHERE UserId = ?";
						sqlConn.conn.setAutoCommit(false);
						preparedStatement = sqlConn.conn.prepareCall(selectSQL7);
						preparedStatement.setString(1, username);
						int result7 = preparedStatement.executeUpdate();
						System.out.println("Result of update patient category is: "+result7);//debug message
						System.out.println("patient category changed from well to sick!");
					    System.out.println("Commiting data here....");
					    sqlConn.conn.commit();						
						//updatePatientCategoryInformation("isSick", "1");
					}					
				}
			}else{
				System.out.println("Date format is invalid. Try again");								
			}
			isSick = String.valueOf(patientCategory); // update te value of isSick to new value
		}
		catch(SQLException se)
		{
			se.printStackTrace();
			Helper.printMessage("Something went wrong. Try again!");
			  try{
				 if(sqlConn.conn!=null)
					 sqlConn.conn.rollback();
		      }catch(SQLException se2){
		         se2.printStackTrace();
		      }				
		}
		finally
		{
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if(sqlConn != null)
				sqlConn.terminateSQLconnection();
		}			
	}

	public void addToDiagnosedTable(String newDisease, String sickDate) throws Exception  {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		String diseaseName = null;
		int hasPatientCategoryChanged = -1;
		int result = -1;
		//System.out.println("In show profile");
		
		try{
			String selectSQL = "INSERT INTO Diagnosed "+"(P_UserId, dName, fromDate) VALUES" + "(?,?,?)";
			sqlcon = new SQLConnection();
			sqlcon.conn.setAutoCommit(false);
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, newDisease);
			preparedStatement.setDate(3, java.sql.Date.valueOf(sickDate));
			result = preparedStatement.executeUpdate();
			System.out.println("Result of insert diagnose is: "+result);//debug message			
			System.out.println("Diagnose inserted!");
			
			//updating the patient category(sick/well in same transaction)
			hasPatientCategoryChanged = patientCategoryChanged(isSick);
			if(hasPatientCategoryChanged == 1) // well to sick
			{
				selectSQL = "UPDATE Users SET isSick" +" = '1' WHERE UserId = ?";
				preparedStatement.setString(1, username);
				result = preparedStatement.executeUpdate();
				System.out.println("Result of update patient category is: "+result);//debug message
				System.out.println("patient category changed from well to sick!");
				//updatePatientCategoryInformation("isSick", "1");
			}			
		    System.out.println("Commiting data here....");
		    sqlcon.conn.commit();
		    showDiagnoseMenuItems();
		}catch (SQLException se) {
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
			updateDiagnoses();
			}			
			finally {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if(sqlcon != null)
					sqlcon.terminateSQLconnection();
				}			
	}
	public void addToDiseaseTable(String newDisease) throws Exception {
		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		String diseaseName = null;
		//System.out.println("In show profile");
		
		try{
			String selectSQL = "INSERT INTO disease "+"(dName, description) VALUES" + "(?,?)";
			sqlcon = new SQLConnection();
			sqlcon.conn.setAutoCommit(false);
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, newDisease);
			preparedStatement.setString(2, null);
			int result = preparedStatement.executeUpdate();
			System.out.println("Result of insert is: "+result);//debug message			
			System.out.println("Disease inserted!");
		    System.out.println("Commiting data here....");
		    sqlcon.conn.commit();			
		}catch (SQLException se) {
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
			updateDiagnoses();
			}			
			finally {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if(sqlcon != null)
					sqlcon.terminateSQLconnection();
				}			
	}
	public int checkDiseaseExists(String newDisease)  throws Exception{
		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		String diseaseName = null;
		//System.out.println("In show profile");
		
		try{
			String selectSQL = "select dname from Disease where dname = ?";
			sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, newDisease);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				diseaseName = rs.getString("dname");
				System.out.println("Disease found " + diseaseName);
				Helper.printMessage("Disease found in list.");
				return 1;
			 }
				
				if(diseaseName == null){
					Helper.printMessage("Disease not found in list. Add the disease '" + newDisease + "' in list");
					return 0;
				}

		}catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong. Try again!");
			updateDiagnoses();
			}			
			finally {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if(sqlcon != null)
					sqlcon.terminateSQLconnection();
				}	
	
				
		return -1;
	}
	public void showDiagnoses() throws SQLException
	{
		displayDiagnoses();
		showDiagnoseMenuItems();
	}
	
	public void displayDiagnoses() throws SQLException{
		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = null;
		SQLConnection sqlcon = null;
		String diseaseName = null;
		//System.out.println("In show profile");
		
		try{
			String selectSQL = "select dname from Diagnosed where P_UserId = ?";
			sqlcon = new SQLConnection();
			preparedStatement = sqlcon.conn.prepareCall(selectSQL);
			preparedStatement.setString(1, username);
			ResultSet rs = preparedStatement.executeQuery();
			Helper.printMessage("Disease diagnosed for " +firstname + "\t"+lastname+": ");
			if(!rs.isBeforeFirst())
			{
				System.out.println("No disease! Patient is well");
				return;
			}
			else{
				while (rs.next()) {
					diseaseName = rs.getString("dname");
					//Helper.printMessage("Disease diagnosed for " +firstname + "\t"+lastname+": ");
		            System.out.println(diseaseName);	            				
				 }								
			}
			  //updateDiagnoses(); // if showdiagnose is succesful, can send to update directly.. no need to go to diagnose menu back
//			  System.out.println("Do you want to update diagnoses?");
//			  System.out.println("Please select from the below options: ");
//			  System.out.println("\n1. Update Diagnoses \t\t\t\t 2. Back to Diagnose Menu");			  
//
//				Scanner scanner = new Scanner(System.in);
//				int value = Integer.parseInt(scanner.nextLine());
//				switch(value)
//				{
//				case 1: 
//					updateDiagnoses();
//					break;
//				case 2:
//					showDiagnoseMenuItems();
//					break; 
							
			}catch (SQLException se) {
			se.printStackTrace();
			System.out.println("Something went wrong. Try again!");
			//showDiagnoseMenuItems();//if sql exception in executing menu functions, send to menu so that user can logout if needed and start again
			}			
			finally {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if(sqlcon != null)
					sqlcon.terminateSQLconnection();
				}		
		
	}
	public void updateDiagnoses() throws Exception {
		// TODO Auto-generated method stub
		  System.out.println("Please select from the below options: ");
		  System.out.println("\n1. Add diagnoses \t\t\t\t 2.Delete Diagnoses 3. Back to diagnose Menu");			  

			Scanner scanner = new Scanner(System.in);
			int value = Integer.parseInt(scanner.nextLine());
			switch(value)
			{
			case 1: 
				addDiagnoses();
				break;
			case 2: 
				deleteDiagnoses();
				break;					
			case 3:
				showDiagnoseMenuItems();
				break; 
			}		
		
	}
}
