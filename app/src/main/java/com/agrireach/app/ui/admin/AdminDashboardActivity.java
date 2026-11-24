package com.agrireach.app.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.agrireach.app.R;
import com.agrireach.app.core.session.AuthSession;

public class AdminDashboardActivity extends AppCompatActivity {
    private Button createEmployeeButton;
    private Button viewEmployeesButton;
    private Button manageUsersButton;

    private Button uploadVideoButton;

    private Button logoutButton;

    private Button viewVideosButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        createEmployeeButton = findViewById(R.id.createEmployeeButton);
        viewEmployeesButton = findViewById(R.id.viewEmployeesButton);
        manageUsersButton = findViewById(R.id.manageUsersButton);
        uploadVideoButton = findViewById(R.id.uploadVideoButton);
        logoutButton = findViewById(R.id.logoutButton);
        viewVideosButton = findViewById(R.id.viewVideosButton);

        createEmployeeButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AdminDashboardActivity.this,
                    CreateEmployeeActivity.class
            );
            startActivity(intent);
        });

        viewEmployeesButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AdminDashboardActivity.this,
                    EmployeeDirectoryActivity.class
            );
            startActivity(intent);
        });

        manageUsersButton.setOnClickListener(v ->
                android.widget.Toast.makeText(this,
                        "User Management coming soon!",
                        android.widget.Toast.LENGTH_SHORT).show()
        );

        uploadVideoButton.setOnClickListener(v -> {
                    Intent intent = new Intent(
                            AdminDashboardActivity.this,
                            UploadVideoActivity.class
                    );
                    startActivity(intent);
                });

        viewVideosButton.setOnClickListener(view -> {
            Intent intent = new Intent(
                    AdminDashboardActivity.this,
                    AdminVideosActivity.class
            );
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            AuthSession.clear();
            finish();
        });
    }
}