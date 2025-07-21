package com.example.prm392_finalproject.models.chat;

import com.example.prm392_finalproject.utils.MessageType;

public class ChatMessage {
    private String content;        // Nội dung tin nhắn
    private boolean isUser;        // Tin nhắn của người dùng hay bot
    private MessageType type;      // Loại tin nhắn: USER, BOT, ERROR, DELAY

    public ChatMessage(String content, boolean isUser, MessageType type) {
        this.content = content;
        this.isUser = isUser;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }


}
