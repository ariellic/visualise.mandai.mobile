package com.itpteam11.visualisemandai;

import java.util.Map;

/**
 * This class represents the User data model
 */
public class User {
    private String name;
    private String status;
    private String email;
    private String type;
    private Map<String, String> group;
    private Map<String, Boolean> service;

    public User() {}

    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getEmail() { return email; }
    public String getType() { return type; }
    public Map<String, String> getGroup() { return group; }
    public Map<String, Boolean> getService() { return service; }
}
