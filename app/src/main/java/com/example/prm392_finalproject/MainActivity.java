package com.example.prm392_finalproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prm392_finalproject.db.ConnectionClass;

import java.sql.Connection;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Test database connection when app starts
        testDatabaseConnection();
    }

    private void testDatabaseConnection() {
        // Run database connection on background thread
        new Thread(() -> {
            try {
                Log.d(TAG, "Attempting to connect to database...");
                
                // Create instance of ConnectionClass and call CONN() method
                ConnectionClass connectionClass = new ConnectionClass();
                Connection connection = connectionClass.CONN();
                
                if (connection != null && !connection.isClosed()) {
                    Log.d(TAG, "✅ DATABASE CONNECTION SUCCESSFUL!");
                    
                    // Show success message on UI thread
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, 
                            "Database connected successfully!", 
                            Toast.LENGTH_LONG).show();
                    });
                    
                    // Close the connection
                    connection.close();
                } else {
                    Log.e(TAG, "❌ DATABASE CONNECTION FAILED: Connection is null or closed");
                    
                    // Show error message on UI thread
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, 
                            "Database connection failed!", 
                            Toast.LENGTH_LONG).show();
                    });
                }
                
            } catch (Exception e) {
                Log.e(TAG, "❌ DATABASE CONNECTION ERROR: " + e.getMessage());
                e.printStackTrace();
                
                // Show error message on UI thread
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, 
                        "Database error: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}