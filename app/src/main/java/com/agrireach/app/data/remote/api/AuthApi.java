package com.agrireach.app.data.remote.api;

import com.agrireach.app.data.remote.dto.LoginResponse;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AuthApi {
    public LoginResponse login(
            String baseUrl,
            String username,
            String password
    ) throws Exception {
        URL url = new URL(
                baseUrl + "/api/v1/auth/login"
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
            JSONObject requestBody = new JSONObject();
            requestBody.put("username", username);
            requestBody.put("password", password);
            try (OutputStream outputStream =
                         connection.getOutputStream()) {
                outputStream.write(
                        requestBody.toString().getBytes(
                                StandardCharsets.UTF_8
                        )
                );
            }
            int responseCode = connection.getResponseCode();
            String response = readResponse(connection, responseCode);
            if (responseCode >= 200 && responseCode < 300) {
                return new LoginResponse(
                        new JSONObject(response)
                );
            }
            throw new Exception(
                    "Login failed (HTTP " + responseCode + "): "
                            + response
            );
        } finally {
            connection.disconnect();
        }
    }
    public String changePassword(
            String baseUrl,
            String accessToken,
            String currentPassword,
            String newPassword
    ) throws Exception {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new Exception("Authentication token is missing");
        }
        URL url = new URL(
                baseUrl + "/api/v1/auth/change-password"
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
                    "Authorization", "Bearer " + accessToken
            );
            JSONObject requestBody = new JSONObject();
            requestBody.put("currentPassword", currentPassword);
            requestBody.put("newPassword", newPassword);
            try (OutputStream outputStream =
                         connection.getOutputStream()) {
                outputStream.write(
                        requestBody.toString().getBytes(
                                StandardCharsets.UTF_8
                        )
                );
            }
            int responseCode = connection.getResponseCode();
            String response = readResponse(connection, responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return response;
            }
            throw new Exception(
                    "Password change failed (HTTP "
                            + responseCode + "): " + response
            );
        } finally {
            connection.disconnect();
        }
    }
    private String readResponse(
            HttpURLConnection connection,
            int responseCode
    ) throws Exception {
        InputStream inputStream;
        if (responseCode >= 200 && responseCode < 300) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }
        StringBuilder response = new StringBuilder();
        if (inputStream != null) {
            try (BufferedReader reader =
                         new BufferedReader(
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
        return response.toString();
    }
}