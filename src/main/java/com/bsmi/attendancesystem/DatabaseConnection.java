package com.bsmi.attendancesystem;

import  java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
     public static Connection databaseLink;
    public static Connection connectDb (){
        DatabaseSettings settings = DatabaseSettings.current();
        try {
            databaseLink = DriverManager.getConnection(settings.url(), settings.user(), settings.password());
            return databaseLink;
        } catch (SQLException e) {e.printStackTrace();}
        return null;
    }
}
