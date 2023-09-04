package com.bsmi.attendancesystem;

import  java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    public static Connection databaseLink;
    public static Connection connectDb(){
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
    }
}
