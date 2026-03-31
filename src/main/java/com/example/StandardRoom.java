package com.example;

public class StandardRoom extends Room {
    public StandardRoom(Integer roomNumber, Double price, Boolean available) {
        super(roomNumber, RoomType.STANDARD, price, available);
    }

    @Override
    public Double calculatePrice() {
        return getPrice();
    }

    @Override
    public String getAmenities() {
        return "WiFi, Fan";
    }
}
