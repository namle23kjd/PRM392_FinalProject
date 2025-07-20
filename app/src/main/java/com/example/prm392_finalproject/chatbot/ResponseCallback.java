package com.example.prm392_finalproject.chatbot;

public interface ResponseCallback {
    void onSuccess(String reply);
    void onError(Exception e);
}
