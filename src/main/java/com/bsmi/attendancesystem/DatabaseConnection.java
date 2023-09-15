package com.bsmi.attendancesystem;

import  java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
     public static Connection databaseLink;
     // Method to connect to MySQL server database
     /* public static Connection connectDb(){
        String databaseName = "attendance_system";
        String url = "jdbc:mysql://localhost/" + databaseName;
        String databaseUser = "admin";
        String databasePassword = "admin";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            databaseLink = DriverManager.getConnection(url, databaseUser, databasePassword);
            return databaseLink;
        }catch (Exception e){e.printStackTrace();}
        return null;
    } */
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
