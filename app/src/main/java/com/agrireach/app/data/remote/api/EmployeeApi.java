package com.agrireach.app.data.remote.api;

import com.agrireach.app.model.Employee;
import com.agrireach.app.core.network.ApiConfig;
import com.agrireach.app.core.session.AuthSession;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EmployeeApi {
    public void createEmployee(
            String employeeCode,
            String fullName,
            String phoneNumber,
            String email,
            String username,
            String password
    ) throws Exception {
        String token = AuthSession.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new Exception("Admin session expired. Please login again.");
        }
        URL url = new URL(
                ApiConfig.BASE_URL + "/api/v1/employees/create"
        );
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setDoOutput(true);
            connection.setRequestProperty(
                    "Content-Type", "application/json"
            );
            connection.setRequestProperty(
                    "Accept", "application/json"
            );
            connection.setRequestProperty(
                    "Authorization", "Bearer " + token
            );
            JSONObject requestBody = new JSONObject();
            requestBody.put("employeeCode", employeeCode);
            requestBody.put("fullName", fullName);
            requestBody.put("phoneNumber", phoneNumber);
            requestBody.put("email", email);
            requestBody.put("username", username);
            requestBody.put("password", password);
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(
                        requestBody.toString().getBytes(
                                StandardCharsets.UTF_8
                        )
                );
            }
            int responseCode = connection.getResponseCode();
            InputStream inputStream =
                    responseCode >= 200 && responseCode < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream();
            StringBuilder response = new StringBuilder();
            if (inputStream != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                inputStream,
                                StandardCharsets.UTF_8
                        ))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            }
            if (responseCode != HttpURLConnection.HTTP_CREATED) {
                throw new Exception(
                        "Employee creation failed (HTTP "
                                + responseCode + "): " + response
                );
            }
        } finally {
            connection.disconnect();
        }
    }

    public List<Employee> getAllEmployees() throws Exception {
        String token = AuthSession.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new Exception("Admin session expired. Please login again.");
        }
        URL url = new URL(
                ApiConfig.BASE_URL + "/api/v1/employees/getAll"
        );
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty(
                    "Accept", "application/json"
            );
            connection.setRequestProperty(
                    "Authorization", "Bearer " + token
            );
            int responseCode = connection.getResponseCode();
            InputStream inputStream =
                    responseCode >= 200 && responseCode < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream();
            StringBuilder response = new StringBuilder();
            if (inputStream != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                inputStream,
                                StandardCharsets.UTF_8
                        ))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            }
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception(
                        "Unable to fetch employees (HTTP "
                                + responseCode + "): " + response
                );
            }
            JSONArray jsonArray =
                    new JSONArray(response.toString());
            List<Employee> employees = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Employee employee = new Employee(
                        jsonObject.optString("employeeCode", ""),
                        jsonObject.optString("fullName", ""),
                        jsonObject.optString("phoneNumber", ""),
                        jsonObject.optString("email", ""),
                        jsonObject.optString("username", "")
                );
                employees.add(employee);
            }
            return employees;
        } finally {
            connection.disconnect();
        }
    }

}