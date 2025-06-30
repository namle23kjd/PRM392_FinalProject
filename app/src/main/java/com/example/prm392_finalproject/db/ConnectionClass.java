package com.example.prm392_finalproject.db;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;

public class ConnectionClass {
    protected static String db = "technology_device_store";
    protected static String ip = "160.191.50.42";
    protected static String port = "3306";
    protected static String username = "binh";
    protected  static String password = "1234";


    public Connection CONN(){
        Connection conn = null;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            String connectionString = "jdbc:mysql://"+ip + ":" +port +"/" + db;
            conn = DriverManager.getConnection(connectionString,username,password);

        }catch (Exception e)
        {
            Log.e("Error", Objects.requireNonNull(e.getMessage()));
        }
        return conn;

    }
}
