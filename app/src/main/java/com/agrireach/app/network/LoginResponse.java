package com.agrireach.app.network;

import org.json.JSONObject;

public class LoginResponse {
    private String accessToken;
    private long expiresIn;
    private String username;
    private String fullName;
    private String role;
    private boolean mustChangePassword;
    private String tokenType;
    public LoginResponse(JSONObject json) throws Exception {
        accessToken = json.getString("accessToken");
        expiresIn = json.getLong("expiresIn");
        username = json.getString("username");
        fullName = json.getString("fullName");
        role = json.getString("role");
        mustChangePassword = json.getBoolean("mustChangePassword");
        tokenType = json.getString("tokenType");
    }
    public String getAccessToken() {
        return accessToken;
    }
    public long getExpiresIn() {
        return expiresIn;
    }
    public String getUsername() {
        return username;
    }
    public String getFullName() {
        return fullName;
    }
    public String getRole() {
        return role;
    }
    public boolean isMustChangePassword() {
        return mustChangePassword;
    }
    public String getTokenType() {
        return tokenType;
    }
}