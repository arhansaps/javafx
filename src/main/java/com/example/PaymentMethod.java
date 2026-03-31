package com.example;

public enum PaymentMethod {
    CASH("Cash"),
    CARD("Card"),
    UPI("UPI");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
