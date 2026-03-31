package com.example;

public class Pair<T, U> {
    private final T first;
    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    // Generic method to display any value type.
    public static <V> String displayValue(V value) {
        return String.valueOf(value);
    }
}
