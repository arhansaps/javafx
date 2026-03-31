package com.example;

public class DeluxeRoom extends Room {
    public DeluxeRoom(Integer roomNumber, Double price, Boolean available) {
        super(roomNumber, RoomType.DELUXE, price, available);
    }

    @Override
    public Double calculatePrice() {
        return getPrice() + 300.0;
    }

    @Override
    public String getAmenities() {
        return "WiFi, AC, TV";
    }
}
