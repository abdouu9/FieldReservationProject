package com.example.FieldReservation.entities;

import jakarta.persistence.*;

@Entity
public class User {
    @Id
    private String uid;                // Firebase UID

    @Column(nullable = false, unique = true)
    private String email;

    private String name;               // optional
    
    private String role;               // user or manager

    public User() {
    }

    public User(String uid, String email, String name) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.role = "user"; // Default role
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}