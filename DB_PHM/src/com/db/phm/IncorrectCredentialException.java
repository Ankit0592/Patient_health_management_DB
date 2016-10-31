package com.db.phm;

public class IncorrectCredentialException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String errorMsg;
	  
	    public IncorrectCredentialException () {
	            this.errorMsg = "Invalid Credential!!!";
	    }
	  
	    public IncorrectCredentialException (String errorMsg) {
	            this.errorMsg = errorMsg;

	    }
	    
	    public String toString() {
	            return this.errorMsg;
	    }
	
}
