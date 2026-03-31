package com.example;

public enum TaskCategory {
    HOUSEKEEPING("Housekeeping"),
    MAINTENANCE("Maintenance");

    private final String displayName;

    TaskCategory(String displayName) {
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
