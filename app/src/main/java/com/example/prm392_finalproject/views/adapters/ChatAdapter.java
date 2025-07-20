package com.example.prm392_finalproject.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.chat.ChatMessage;
import com.example.prm392_finalproject.utils.MessageType;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> messageList;

    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;
    public static final int TYPE_ERROR = 2;
    public static final int TYPE_DELAY = 3;

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        MessageType type = messageList.get(position).getType();
        switch (type) {
            case USER: return TYPE_USER;
            case BOT: return TYPE_BOT;
            case ERROR: return TYPE_ERROR;
            case DELAY: return TYPE_DELAY;
            default: return -1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            View view = inflater.inflate(R.layout.activity_item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else if (viewType == TYPE_BOT) {
            View view = inflater.inflate(R.layout.activity_item_chat_bot, parent, false);
            return new BotViewHolder(view);
        } else if (viewType == TYPE_ERROR) {
            View view = inflater.inflate(R.layout.activity_item_error_message, parent, false);
            return new ErrorViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.activity_item_delay_message, parent, false);
            return new DelayViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        switch (getItemViewType(position)) {
            case TYPE_USER:
                ((UserViewHolder) holder).bind(message);
                break;
            case TYPE_BOT:
                ((BotViewHolder) holder).bind(message);
                break;
            case TYPE_ERROR:
                ((ErrorViewHolder) holder).bind(message);
                break;
            case TYPE_DELAY:
                // No binding needed
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userMessageText;
        UserViewHolder(View itemView) {
            super(itemView);
            userMessageText = itemView.findViewById(R.id.userMessageText);
        }

        void bind(ChatMessage message) {
            String content = message.getContent();
            if (content != null && !content.isEmpty()) {
                userMessageText.setText(content);
            } else {
                userMessageText.setText(" ");
            }
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView botMessageText;
        BotViewHolder(View itemView) {
            super(itemView);
            botMessageText = itemView.findViewById(R.id.botMessageText);
        }

        void bind(ChatMessage message) {
            String content = message.getContent();
            if (content != null && !content.isEmpty()) {
                botMessageText.setText(content);
            } else {
                botMessageText.setText(" ");
            }
        }
    }

    static class ErrorViewHolder extends RecyclerView.ViewHolder {
        TextView errorMessageText;
        ErrorViewHolder(View itemView) {
            super(itemView);
            errorMessageText = itemView.findViewById(R.id.errorMessageText);
        }

        void bind(ChatMessage message) {
            String content = message.getContent();
            if (content != null && !content.isEmpty()) {
                errorMessageText.setText(content);
            } else {
                errorMessageText.setText("⚠️ Đã xảy ra lỗi.");
            }
        }
    }

    static class DelayViewHolder extends RecyclerView.ViewHolder {
        DelayViewHolder(View itemView) {
            super(itemView);
        }
    }

    public void addMessage(ChatMessage message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    public void clearMessages() {
        messageList.clear();
        notifyDataSetChanged();
    }
}
