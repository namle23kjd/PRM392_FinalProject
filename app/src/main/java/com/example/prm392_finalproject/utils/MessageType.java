package com.example.prm392_finalproject.utils;

public enum MessageType {
    USER(0),
    BOT(1),
    ERROR(2),
    DELAY(3);

    private final int value;

    MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
