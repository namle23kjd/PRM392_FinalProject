package com.example.prm392_finalproject.views;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.chatbot.CozeApiClient;
import com.example.prm392_finalproject.chatbot.ResponseCallback;
import com.example.prm392_finalproject.models.chat.ChatMessage;
import com.example.prm392_finalproject.utils.MessageType;
import com.example.prm392_finalproject.views.adapters.ChatAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatBotActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatList;

    private EditText messageInput;
    private ImageButton sendButton;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        sendButton.setOnClickListener(view -> {
            String userInput = messageInput.getText().toString().trim();
            if (!userInput.isEmpty()) {
                addUserMessage(userInput);
                messageInput.setText("");

                ChatMessage delayMessage = new ChatMessage("Đang trả lời...", false, MessageType.DELAY);
                chatList.add(delayMessage);
                mainHandler.post(() -> chatAdapter.notifyItemInserted(chatList.size()));
                scrollToBottom();

                new CozeApiClient().sendMessage(userInput, new ResponseCallback() {
                    @Override
                    public void onSuccess(String reply) {
                        removeDelayMessage();
                        addBotMessage(reply);
                    }

                    @Override
                    public void onError(Exception e) {
                        removeDelayMessage();
                        addErrorMessage();
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void addUserMessage(String text) {
        chatList.add(new ChatMessage(text, true, MessageType.USER));
        mainHandler.post(() -> {
            chatAdapter.notifyItemInserted(chatList.size());
            scrollToBottom();
        });
    }

    private void addBotMessage(String text) {
        chatList.add(new ChatMessage(text, false, MessageType.BOT));
        mainHandler.post(() -> {
            chatAdapter.notifyItemInserted(chatList.size());
            scrollToBottom();
        });
    }

    private void addErrorMessage() {
        chatList.add(new ChatMessage("⚠️ Đã xảy ra lỗi khi gửi tin nhắn.", false, MessageType.ERROR));
        mainHandler.post(() -> {
            chatAdapter.notifyItemInserted(chatList.size());
            scrollToBottom();
        });
    }

    private void removeDelayMessage() {
        if (!chatList.isEmpty()) {
            int lastIndex = chatList.size() - 1;
            ChatMessage lastMsg = chatList.get(lastIndex);
            if (lastMsg.getType() == MessageType.DELAY) {
                chatList.remove(lastIndex);
                mainHandler.post(() -> chatAdapter.notifyItemRemoved(lastIndex));
            }
        }
    }

    private void scrollToBottom() {
        chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
    }
}
