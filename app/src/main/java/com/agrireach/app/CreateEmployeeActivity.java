package com.agrireach.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.agrireach.app.network.EmployeeApi;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateEmployeeActivity extends AppCompatActivity {
    private TextInputEditText employeeCodeInput;
    private TextInputEditText employeeNameInput;
    private TextInputEditText employeePhoneInput;
    private TextInputEditText employeeEmailInput;
    private TextInputEditText employeeUsernameInput;
    private TextInputEditText employeePasswordInput;
    private Button saveEmployeeButton;
    private Button backButton;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_employee);
        employeeCodeInput = findViewById(R.id.employeeCodeInput);
        employeeNameInput = findViewById(R.id.employeeNameInput);
        employeePhoneInput = findViewById(R.id.employeePhoneInput);
        employeeEmailInput = findViewById(R.id.employeeEmailInput);
        employeeUsernameInput = findViewById(R.id.employeeUsernameInput);
        employeePasswordInput = findViewById(R.id.employeePasswordInput);
        saveEmployeeButton = findViewById(R.id.saveEmployeeButton);
        backButton = findViewById(R.id.backButton);
        saveEmployeeButton.setOnClickListener(v -> validateEmployee());
        backButton.setOnClickListener(v -> finish());
    }
    private void validateEmployee() {
        String employeeCode = getText(employeeCodeInput);
        String fullName = getText(employeeNameInput);
        String phoneNumber = getText(employeePhoneInput);
        String email = getText(employeeEmailInput);
        String username = getText(employeeUsernameInput);
        // Do not trim passwords.
        String password = employeePasswordInput.getText() == null
                ? ""
                : employeePasswordInput.getText().toString();
        if (employeeCode.isEmpty()) {
            employeeCodeInput.setError("Employee code is required");
            employeeCodeInput.requestFocus();
            return;
        }
        if (fullName.isEmpty()) {
            employeeNameInput.setError("Full name is required");
            employeeNameInput.requestFocus();
            return;
        }
        if (username.length() < 4) {
            employeeUsernameInput.setError(
                    "Username must contain at least 4 characters"
            );
            employeeUsernameInput.requestFocus();
            return;
        }
        if (password.length() < 8) {
            employeePasswordInput.setError(
                    "Password must contain at least 8 characters"
            );
            employeePasswordInput.requestFocus();
            return;
        }
        if (!email.isEmpty() &&
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            employeeEmailInput.setError("Enter a valid email address");
            employeeEmailInput.requestFocus();
            return;
        }
        if (phoneNumber.length() > 20) {
            employeePhoneInput.setError(
                    "Phone number cannot exceed 20 characters"
            );
            employeePhoneInput.requestFocus();
            return;
        }
        createEmployee(
                employeeCode,
                fullName,
                phoneNumber,
                email,
                username,
                password
        );
    }
    private void createEmployee(
            String employeeCode,
            String fullName,
            String phoneNumber,
            String email,
            String username,
            String password
    ) {
        saveEmployeeButton.setEnabled(false);
        saveEmployeeButton.setText("Saving...");
        executor.execute(() -> {
            try {
                EmployeeApi employeeApi = new EmployeeApi();
                employeeApi.createEmployee(
                        employeeCode,
                        fullName,
                        phoneNumber,
                        email,
                        username,
                        password
                );
                mainHandler.post(() -> {
                    Toast.makeText(
                            this,
                            "Employee created successfully!",
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    saveEmployeeButton.setEnabled(true);
                    saveEmployeeButton.setText("Save Employee");
                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }
    private String getText(TextInputEditText input) {
        return input.getText() == null
                ? ""
                : input.getText().toString().trim();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}