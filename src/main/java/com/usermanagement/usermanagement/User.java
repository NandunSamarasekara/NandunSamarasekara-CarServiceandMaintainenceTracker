package com.usermanagement.usermanagement;

public class User {
    private String NIC;
    private String fist_name;
    private String last_name;
    private String email;
    private String phone;
    private String password;

    public User() {
    }

    public User(String NIC, String fist_name, String last_name, String email, String phone, String password) {
        this.NIC = NIC;
        this.fist_name = fist_name;
        this.last_name = last_name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public String getNIC() {
        return NIC;
    }

    public void setNIC(String NIC) {
        this.NIC = NIC;
    }

    public String getFist_name() {
        return fist_name;
    }

    public void setFist_name(String fist_name) {
        this.fist_name = fist_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
