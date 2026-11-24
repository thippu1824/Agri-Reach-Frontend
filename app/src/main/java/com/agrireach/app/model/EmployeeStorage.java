package com.agrireach.app.model;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
public class EmployeeStorage {
    private static final String PREF_NAME = "AgriReachEmployees";
    private static final String EMPLOYEE_LIST = "employee_list";
    public static void saveEmployee(Context context, Employee employee) {
        SharedPreferences preferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String existingData = preferences.getString(EMPLOYEE_LIST, "[]");
        try {
            JSONArray employees = new JSONArray(existingData);
            JSONObject employeeObject = new JSONObject();
            employeeObject.put("employeeCode", employee.getEmployeeCode());
            employeeObject.put("fullName", employee.getFullName());
            employeeObject.put("phoneNumber", employee.getPhoneNumber());
            employeeObject.put("email", employee.getEmail());
            employeeObject.put("username", employee.getUsername());
            employees.put(employeeObject);
            preferences.edit()
                    .putString(EMPLOYEE_LIST, employees.toString())
                    .apply();
        } catch (Exception e) {
            throw new RuntimeException("Unable to save employee", e);
        }
    }
    public static List<Employee> getAllEmployees(Context context) {
        List<Employee> employeeList = new ArrayList<>();
        SharedPreferences preferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String existingData = preferences.getString(EMPLOYEE_LIST, "[]");
        try {
            JSONArray employees = new JSONArray(existingData);
            for (int i = 0; i < employees.length(); i++) {
                JSONObject object = employees.getJSONObject(i);
                Employee employee = new Employee(
                        object.getString("employeeCode"),
                        object.getString("fullName"),
                        object.getString("phoneNumber"),
                        object.getString("email"),
                        object.getString("username")
                );
                employeeList.add(employee);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to retrieve employees", e);
        }
        return employeeList;
    }
}