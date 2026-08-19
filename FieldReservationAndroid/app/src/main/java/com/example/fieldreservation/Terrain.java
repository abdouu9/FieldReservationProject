package com.example.fieldreservation;

// TEST COMMENT - Si vous voyez ce commentaire dans Terrain.java, les changements sont appliqués
public class Terrain {
    private Long id;
    private String name;
    private String location;
    private double price;

    public Terrain() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Constructors, Getters, and Setters
}
