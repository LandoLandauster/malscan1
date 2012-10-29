package test1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class hello {
	   public static void main(String[] args) {
	        System.out.println("input username");
	        Scanner input = new Scanner( System.in );
	        String login = input.next();
	        System.out.println("input password");
	        String pass = input.next();
	        System.out.println("-------- PostgreSQL JDBC Connection Testing ------------");
	    	try {
	    		 
				Class.forName("org.postgresql.Driver");
	 
			} catch (ClassNotFoundException e) {
	 
				System.out.println("error");
				e.printStackTrace();
				return;	
			}
	    	System.out.println("PostgreSQL JDBC Driver Registered!");
	    	 
			Connection connection = null;
	 
			try {
	 
				connection = DriverManager.getConnection(
						"jdbc:postgresql://127.0.0.1:5432/postgres", login, pass);
	 
			} catch (SQLException e) {
	 
				System.out.println("Connection Failed! Check output console");
				e.printStackTrace();
				return;
	 
			}
			if (connection != null) {
				System.out.println("You made it, take control your database now!");
			} else {
				System.out.println("Failed to make connection!");
			}
			
			
	    }

	
}
