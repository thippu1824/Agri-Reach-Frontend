package com.agrireach.app;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.agrireach.app.network.EmployeeApi;
import com.google.android.material.card.MaterialCardView;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmployeeDirectoryActivity extends AppCompatActivity {
    private TextView employeeCountText;
    private TextView emptyStateText;
    private LinearLayout employeeListContainer;
    private Button backButton;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_directory);
        employeeCountText = findViewById(R.id.employeeCountText);
        emptyStateText = findViewById(R.id.emptyStateText);
        employeeListContainer = findViewById(R.id.employeeListContainer);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadEmployees();
    }
    private void loadEmployees() {
        employeeListContainer.removeAllViews();
        employeeCountText.setText("Loading employees...");
        emptyStateText.setVisibility(View.GONE);
        executor.execute(() -> {
            try {
                EmployeeApi employeeApi = new EmployeeApi();
                List<Employee> employees =
                        employeeApi.getAllEmployees();
                mainHandler.post(() -> displayEmployees(employees));
            } catch (Exception e) {
                mainHandler.post(() -> {
                    employeeCountText.setText("Unable to load employees");
                    emptyStateText.setText(
                            "Could not connect to the server. Please try again."
                    );
                    emptyStateText.setVisibility(View.VISIBLE);
                    Toast.makeText(
                            EmployeeDirectoryActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }
    private void displayEmployees(List<Employee> employees) {
        employeeListContainer.removeAllViews();
        employeeCountText.setText(
                employees.size() +
                        (employees.size() == 1
                                ? " employee"
                                : " employees")
        );
        if (employees.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            return;
        }
        emptyStateText.setVisibility(View.GONE);
        for (Employee employee : employees) {
            addEmployeeCard(employee);
        }
    }
    private void addEmployeeCard(Employee employee) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        cardParams.bottomMargin = dp(14);
        card.setLayoutParams(cardParams);
        card.setRadius(dp(18));
        card.setCardElevation(dp(1));
        card.setCardBackgroundColor(Color.WHITE);
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(Color.rgb(229, 236, 230));
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.addView(content);
        addText(content, employee.getFullName(),
                19, "#174C35", true);
        addText(content,
                "Employee Code: " + employee.getEmployeeCode(),
                14, "#68776D", false);
        addText(content,
                "Username: " + employee.getUsername(),
                14, "#333333", false);
        if (employee.getEmail() != null &&
                !employee.getEmail().isEmpty()) {
            addText(content,
                    "Email: " + employee.getEmail(),
                    14, "#333333", false);
        }
        if (employee.getPhoneNumber() != null &&
                !employee.getPhoneNumber().isEmpty()) {
            addText(content,
                    "Phone: " + employee.getPhoneNumber(),
                    14, "#333333", false);
        }
        employeeListContainer.addView(card);
    }
    private void addText(
            LinearLayout parent,
            String value,
            int textSize,
            String color,
            boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextSize(textSize);
        textView.setTextColor(Color.parseColor(color));
        if (bold) {
            textView.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        params.bottomMargin = dp(8);
        textView.setLayoutParams(params);
        parent.addView(textView);
    }
    private int dp(int value) {
        return (int) (value *
                getResources().getDisplayMetrics().density);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}