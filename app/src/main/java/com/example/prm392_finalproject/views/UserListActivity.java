package com.example.prm392_finalproject.views;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.UserRepository;
import com.example.prm392_finalproject.models.User;
import java.util.List;

public class UserListActivity extends AppCompatActivity {
    private RecyclerView rvUserList;
    private UserAdapter userAdapter;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        rvUserList = findViewById(R.id.rvUserList);
        rvUserList.setLayoutManager(new LinearLayoutManager(this));
        userRepository = new UserRepository();

        loadUserList();
    }

    private void loadUserList() {
        new Thread(() -> {
            List<User> userList = userRepository.getAllUsers();
            runOnUiThread(() -> {
                if (userList == null || userList.isEmpty()) {
                    Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show();
                }
                userAdapter = new UserAdapter(userList);
                rvUserList.setAdapter(userAdapter);
            });
        }).start();
    }
} 