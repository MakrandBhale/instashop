package com.makarand.instashop.Models;

public class User {
    String name, email, id;
    int userType;

    public User() {
    }

    public User(String name, String email, String id, int userType) {
        this.name = name;
        this.email = email;
        this.userType = userType;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }
}
