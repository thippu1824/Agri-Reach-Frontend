package com.agrireach.app.core.session;

import com.agrireach.app.data.remote.dto.LoginResponse;

public class AuthSession {
    private static String accessToken;
    private static String username;
    private static String fullName;
    private static String role;
    private static boolean mustChangePassword;

    public static void save(LoginResponse response) {
        accessToken = response.getAccessToken();
        username = response.getUsername();
        fullName = response.getFullName();
        role = response.getRole();
        mustChangePassword = response.isMustChangePassword();
    }
    public static String getAccessToken() {
        return accessToken;
    }
    public static String getUsername() {
        return username;
    }
    public static String getFullName() {
        return fullName;
    }
    public static String getRole() {
        return role;
    }
    public static boolean isMustChangePassword() {
        return mustChangePassword;
    }
    public static boolean isLoggedIn() {
        return accessToken != null && !accessToken.isEmpty();
    }
    public static void clear() {
        accessToken = null;
        username = null;
        fullName = null;
        role = null;
        mustChangePassword = false;
    }
}