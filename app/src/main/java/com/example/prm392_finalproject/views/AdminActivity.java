package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.example.prm392_finalproject.controllers.UserRepository;
import com.example.prm392_finalproject.models.User;
import com.example.prm392_finalproject.R;

public class AdminActivity extends AppCompatActivity {
    private RecyclerView rvUserList;
    private UserAdapter userAdapter;
    private UserRepository userRepository;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Giả sử user hiện tại được truyền qua Intent hoặc singleton
        User currentUser = (User) getIntent().getSerializableExtra("currentUser");
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            Toast.makeText(this, "Bạn không có quyền truy cập trang này!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        rvUserList = findViewById(R.id.rvUserList);
        rvUserList.setLayoutManager(new LinearLayoutManager(this));
        userRepository = new UserRepository();

        // Load user list from DB
        new Thread(() -> {
            List<User> userList = userRepository.getAllUsers();
            handler.post(() -> {
                userAdapter = new UserAdapter(userList);
                rvUserList.setAdapter(userAdapter);
            });
        }).start();
    }
} 