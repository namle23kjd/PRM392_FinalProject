package com.example.prm392_finalproject.models;

import java.util.Date;

public class User {
    private int customer_id;
    private String gender;
    private String first_name;
    private String last_name;
    private String email;
    private String phone;
    private String address;
    private Date dob;
    private Date created_at;
    private String role;
    private String password;

    // Default constructor
    public User() {
        this.role = "customer"; // Default role for new registrations
        this.created_at = new Date(); // Set current date
    }

    // Constructor for registration
    public User(String gender, String first_name, String last_name, String email, 
                String phone, String address, Date dob, String password) {
        this();
        this.gender = gender;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.dob = dob;
        this.password = password;
    }

    // Getters and Setters
    public int getCustomerId() {
        return customer_id;
    }

    public void setCustomerId(int customer_id) {
        this.customer_id = customer_id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFirstName() {
        return first_name;
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public void setLastName(String last_name) {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Helper method to get full name
    public String getFullName() {
        return first_name + " " + last_name;
    }

    @Override
    public String toString() {
        return "User{" +
                "customer_id=" + customer_id +
                ", gender='" + gender + '\'' +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", dob=" + dob +
                ", created_at=" + created_at +
                ", role='" + role + '\'' +
                ", password='" + (password != null ? "***" : "null") + '\'' +
                '}';
    }
} 