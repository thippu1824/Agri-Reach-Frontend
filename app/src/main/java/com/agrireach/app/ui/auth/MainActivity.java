package com.agrireach.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.agrireach.app.R;
import com.agrireach.app.core.network.ApiConfig;
import com.agrireach.app.data.remote.api.AuthApi;
import com.agrireach.app.core.session.AuthSession;
import com.agrireach.app.data.remote.dto.LoginResponse;
import com.agrireach.app.ui.admin.AdminDashboardActivity;
import com.agrireach.app.ui.employee.EmployeeDashboardActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> loginUser());
    }
    private void loginUser() {
        String username =
                usernameInput.getText().toString().trim();
        String password =
                passwordInput.getText().toString();
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }
        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");
        executor.execute(() -> {
            try {
                AuthApi authApi = new AuthApi();
                LoginResponse response = authApi.login(
                        ApiConfig.BASE_URL,
                        username,
                        password
                );
                String role = response.getRole();
                if (!"ADMIN".equalsIgnoreCase(role)
                        && !"EMPLOYEE".equalsIgnoreCase(role)) {
                    throw new Exception("Invalid user role");
                }
                AuthSession.save(response);
                mainHandler.post(() -> {
                    Toast.makeText(
                            MainActivity.this,
                            "Welcome " + response.getFullName(),
                            Toast.LENGTH_SHORT
                    ).show();
                    Intent intent;
                    if ("EMPLOYEE".equalsIgnoreCase(role)
                            && response.isMustChangePassword()) {
                        intent = new Intent(
                                MainActivity.this,
                                ChangePasswordActivity.class
                        );
                    } else if ("ADMIN".equalsIgnoreCase(role)) {
                        intent = new Intent(
                                MainActivity.this,
                                AdminDashboardActivity.class
                        );
                    } else {
                        intent = new Intent(
                                MainActivity.this,
                                EmployeeDashboardActivity.class
                        );
                    }
                    startActivity(intent);
                    finish();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                    Toast.makeText(
                            MainActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}