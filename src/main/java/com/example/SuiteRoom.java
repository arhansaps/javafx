package com.example;

public class SuiteRoom extends Room {
    public SuiteRoom(Integer roomNumber, Double price, Boolean available) {
        super(roomNumber, RoomType.SUITE, price, available);
    }

    @Override
    public Double calculatePrice() {
        return getPrice() + 650.0;
    }

    @Override
    public String getAmenities() {
        return "WiFi, AC, Lounge Access, Mini Bar";
    }
}
