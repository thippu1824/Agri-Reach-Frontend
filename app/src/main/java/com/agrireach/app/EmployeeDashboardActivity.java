package com.agrireach.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.agrireach.app.network.AuthSession;

public class EmployeeDashboardActivity extends AppCompatActivity {
    private TextView welcomeNameText;
    private Button employeeLogoutButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);
        welcomeNameText = findViewById(R.id.welcomeNameText);
        employeeLogoutButton = findViewById(R.id.employeeLogoutButton);
        String employeeName = AuthSession.getFullName();
        if (employeeName == null || employeeName.isEmpty()) {
            employeeName = "Employee";
        }
        welcomeNameText.setText("Welcome, " + employeeName + "!");
        employeeLogoutButton.setOnClickListener(v -> logout());
    }
    private void logout() {
        AuthSession.clear();
        Intent intent = new Intent(
                EmployeeDashboardActivity.this,
                MainActivity.class
        );
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
        finish();
    }
}