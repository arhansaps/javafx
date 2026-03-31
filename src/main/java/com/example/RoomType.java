package com.example;

public enum RoomType {
    STANDARD(1200.0),
    DELUXE(2200.0),
    SUITE(3400.0),
    VILLA(5200.0);

    private final Double basePrice;

    RoomType(Double basePrice) {
        this.basePrice = basePrice;
    }

    public Double getBasePrice() {
        return basePrice;
    }
}
