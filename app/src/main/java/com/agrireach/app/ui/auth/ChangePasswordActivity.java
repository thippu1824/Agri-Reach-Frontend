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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText currentPasswordInput;
    private EditText newPasswordInput;
    private EditText confirmPasswordInput;
    private Button changePasswordButton;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        currentPasswordInput =
                findViewById(R.id.currentPasswordInput);
        newPasswordInput =
                findViewById(R.id.newPasswordInput);
        confirmPasswordInput =
                findViewById(R.id.confirmPasswordInput);
        changePasswordButton =
                findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(
                v -> changePassword()
        );
    }
    private void changePassword() {
        String currentPassword =
                currentPasswordInput.getText().toString();
        String newPassword =
                newPasswordInput.getText().toString();
        String confirmPassword =
                confirmPasswordInput.getText().toString();
        if (currentPassword.isEmpty()) {
            currentPasswordInput.setError(
                    "Current password is required"
            );
            currentPasswordInput.requestFocus();
            return;
        }
        if (newPassword.length() < 8) {
            newPasswordInput.setError(
                    "Password must contain at least 8 characters"
            );
            newPasswordInput.requestFocus();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordInput.setError(
                    "Passwords do not match"
            );
            confirmPasswordInput.requestFocus();
            return;
        }
        if (currentPassword.equals(newPassword)) {
            newPasswordInput.setError(
                    "New password must be different"
            );
            newPasswordInput.requestFocus();
            return;
        }
        String token = AuthSession.getAccessToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(
                    this,
                    "Session expired. Please log in again.",
                    Toast.LENGTH_LONG
            ).show();
            returnToLogin();
            return;
        }
        changePasswordButton.setEnabled(false);
        changePasswordButton.setText("Updating...");
        executor.execute(() -> {
            try {
                AuthApi authApi = new AuthApi();
                authApi.changePassword(
                        ApiConfig.BASE_URL,
                        token,
                        currentPassword,
                        newPassword
                );
                mainHandler.post(() -> {
                    Toast.makeText(
                            ChangePasswordActivity.this,
                            "Password changed successfully. Please log in again.",
                            Toast.LENGTH_LONG
                    ).show();
                    AuthSession.clear();
                    returnToLogin();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    changePasswordButton.setEnabled(true);
                    changePasswordButton.setText(
                            "Update Password"
                    );
                    Toast.makeText(
                            ChangePasswordActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }
    private void returnToLogin() {
        AuthSession.clear();
        Intent intent = new Intent(
                ChangePasswordActivity.this,
                MainActivity.class
        );
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}