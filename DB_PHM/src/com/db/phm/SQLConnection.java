package com.db.phm;

import java.sql.*;

public class SQLConnection {
	
    static final String jdbcURL 
	= "jdbc:oracle:thin:@orca.csc.ncsu.edu:1521:orcl01";
    
    String user = "dgupta9";	
    String passwd = "200151449";	
            
    public static Connection conn = null;
    public static Statement stmt = null;
    public static ResultSet rs = null;
    
    public SQLConnection(){
    	
		try{
			try{
				Class.forName("oracle.jdbc.driver.OracleDriver");
			}catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			}
			conn = DriverManager.getConnection(jdbcURL, user, passwd);
			//conn.setAutoCommit(false);
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}   	
    }
    
    public int executeCommand(String query) {
        try {
                stmt.executeUpdate(query);
                return 0;
            } catch(Throwable oops) {
                oops.printStackTrace();
                return 1;
                }
    }
    
    public ResultSet queryCommand(String query) {
        try {
            
            rs = stmt.executeQuery(query);
            
            return rs;
            
        } catch(Throwable oops) {
                oops.printStackTrace();
                return null;
            }
    }
    public void terminateSQLconnection()
    {
        close(rs);
        close(stmt);
        close(conn);
    }
    static void close(Connection conn) {
        if(conn != null) {
            try { conn.close(); } catch(Throwable whatever) {}
        }
    }
    
    static void close(Statement st) {
        if(st != null) {
            try { st.close(); } catch(Throwable whatever) {}
        }
    }

    static void close(ResultSet rs) {
        if(rs != null) {
            try { rs.close(); } catch(Throwable whatever) {}
        }
    }   
}	

