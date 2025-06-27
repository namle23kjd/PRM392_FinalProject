package com.example.prm392_finalproject.models;

public class SupplierRequest {
    private String name;
    private String address;
    private String contact_info;
    private String phone;
    private String email;
    private boolean is_active;

    public SupplierRequest(String name, String address, String contact_info, String phone, String email, boolean is_active) {
        this.name = name;
        this.address = address;
        this.contact_info = contact_info;
        this.phone = phone;
        this.email = email;
        this.is_active = is_active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact_info() {
        return contact_info;
    }

    public void setContact_info(String contact_info) {
        this.contact_info = contact_info;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }
}
