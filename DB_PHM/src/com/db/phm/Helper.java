package com.db.phm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Helper {
	
	public static void printMessage(String message){
		System.out.println(message);
	}
	public static String getConsoleValue() {
	
		//printMessage("Enter the new value");
		Scanner scanner = new Scanner(System.in);
		String str = scanner.nextLine();
		if (str.isEmpty()) {
			printMessage("\n No value entered. Please try again..\n");
			return getConsoleValue();
		}
		return str;		
	}
	
	public static void printErrorMessage(){
		System.out.println("Something went wrong!!! Please try again...");
	}
	
	public static String enteredConsoleString() {
		//@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		String value = scanner.nextLine();
		if (value.isEmpty()) {
			printMessage("\nYou have not entered anything. Please try again..\n");
			return enteredConsoleString();
		}
		return value;
		
	}
	
	public static boolean validateDateFormat(String dateToValdate, String validFormat) {

	    SimpleDateFormat formatter = new SimpleDateFormat(validFormat);
	    //To make strict date format validation
	    formatter.setLenient(false);
	    Date parsedDate = null;
	    try {
	        parsedDate = formatter.parse(dateToValdate);
	    } catch (ParseException e) {
	        //Handle exception
	    	System.out.println("Date Exception!!");
	    }
	    if(parsedDate==null)return false;
	    else return true;
	}
	
	public static String enteredProfileData() {
		System.out.println("Please enter new value for this field:");
		return Helper.enteredConsoleString();
	}
	
	public static Integer getConsoleIntValue() {
		Integer n = -1;
		try{
			n = Integer.parseInt(getConsoleValue());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return n;
	}
	
	private static boolean validateDate(String startDate) {
		if(startDate.matches("\\d{2}[/]\\d{2}[/]\\d{4}")){
			String[] dateComp = startDate.split("/");
			try{
				Integer month=Integer.parseInt(dateComp[0]);
				if(month<1||month>12)
					return false;
				Integer date=Integer.parseInt(dateComp[1]);
				if(date<1||date>31)
					return false;
				Integer year=Integer.parseInt(dateComp[2]);
				if(year<1000||year>2999){
					return false;
				}
			}catch(Exception e){
				return false;
			}
			return true;
		}
		return false;
	}

	private static String getMonth(String string) {
		if(string.equals("01")){
			return "Jan";
		}else if(string.equals("02")){
			return "Feb";
		}else if(string.equals("03")){
			return "Mar";
		}else if(string.equals("04")){
			return "Apr";
		}else if(string.equals("05")){
			return "May";
		}else if(string.equals("06")){
			return "Jun";
		}else if(string.equals("07")){
			return "Jul";
		}else if(string.equals("08")){
			return "Aug";
		}else if(string.equals("09")){
			return "Sep";
		}else if(string.equals("10")){
			return "Oct";
		}else if(string.equals("11")){
			return "Nov";
		}else if(string.equals("12")){
			return "Dec";
		}
		return null;
	}	
	
	public static String getConsoleDateValue() {
		String formattedDate = null;
		boolean flag=true;
		while(flag){
			String date =  getConsoleValue();
			if(validateDate(date)){
				String[] dateSplit = date.split("/");
				formattedDate = dateSplit[1]+"-"+getMonth(dateSplit[0])+"-"+dateSplit[2];
				break;
			}
			System.out.println("Invalid date format, try again ...");
		}
		return formattedDate;
	}
	
}
