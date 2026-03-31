package com.example;

public class VillaRoom extends Room {
    public VillaRoom(Integer roomNumber, Double price, Boolean available) {
        super(roomNumber, RoomType.VILLA, price, available);
    }

    @Override
    public Double calculatePrice() {
        return getPrice() + 1200.0;
    }

    @Override
    public String getAmenities() {
        return "WiFi, Private Pool, Garden Deck, Butler Service";
    }
}
