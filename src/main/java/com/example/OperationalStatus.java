package com.example;

public enum OperationalStatus {
    VACANT_CLEAN("Vacant Clean", true),
    OCCUPIED("Occupied", false),
    DIRTY("Dirty", false),
    CLEANING("Cleaning", false),
    OUT_OF_ORDER("Out of Order", false),
    MAINTENANCE("Maintenance", false);

    private final String displayName;
    private final boolean sellable;

    OperationalStatus(String displayName, boolean sellable) {
        this.displayName = displayName;
        this.sellable = sellable;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSellable() {
        return sellable;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
