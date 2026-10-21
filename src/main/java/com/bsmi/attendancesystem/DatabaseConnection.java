package com.bsmi.attendancesystem;

import  java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
     public static Connection databaseLink;
    // Method to connect to H2 embedded database
    public static Connection connectDb (){
        String databaseName = "attendance_system";
        String url ="jdbc:h2:./data/" + databaseName;
        String databaseUser = "admin";
        String databasePassword = "admin";
        try {
            databaseLink = DriverManager.getConnection(url, databaseUser, databasePassword);
            return databaseLink;
        } catch (SQLException e) {e.printStackTrace();}
        return null;
    }
}
