package com.agrireach.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {
    private Button createEmployeeButton;
    private Button viewEmployeesButton;
    private Button manageUsersButton;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        createEmployeeButton = findViewById(R.id.createEmployeeButton);
        viewEmployeesButton = findViewById(R.id.viewEmployeesButton);
        manageUsersButton = findViewById(R.id.manageUsersButton);
        logoutButton = findViewById(R.id.logoutButton);

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

        logoutButton.setOnClickListener(v -> {
            com.agrireach.app.network.AuthSession.clear();
            finish();
        });
    }
}