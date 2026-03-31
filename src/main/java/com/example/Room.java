package com.example;

import java.io.Serializable;

public abstract class Room implements Serializable, Amenities {
    private static final long serialVersionUID = 1L;

    private Integer roomNumber;
    private RoomType roomType;
    private Double price;
    private Boolean available;

    protected Room(Integer roomNumber, RoomType roomType, Double price, Boolean available) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.price = price;
        this.available = available;
    }

    // Method overriding is demonstrated in child classes.
    public abstract Double calculatePrice();

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber
                + " | Type: " + roomType
                + " | Price: " + Pair.displayValue(calculatePrice())
                + " | Available: " + available
                + " | Amenities: " + getAmenities();
    }
}
