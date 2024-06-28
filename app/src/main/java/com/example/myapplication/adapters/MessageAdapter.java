package com.example.myapplication.adapters;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ItemContainerMessageBinding;
import com.example.myapplication.models.Message;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private List<Message> messages;
    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemContainerMessageBinding itemContainerMessageBinding = DataBindingUtil.inflate(layoutInflater, R.layout.item_container_message, parent, false);
        return new MessageViewHolder(itemContainerMessageBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        private ItemContainerMessageBinding itemContainerMessageBinding;

        public MessageViewHolder(ItemContainerMessageBinding itemContainerMessageBinding) {
            super(itemContainerMessageBinding.getRoot());
            this.itemContainerMessageBinding = itemContainerMessageBinding;
        }

        public void bind(Message message) {
            if (message.getSentBy().equals(Message.SENT_BY_USER)) {
                itemContainerMessageBinding.layoutLeft.setVisibility(View.GONE);
                itemContainerMessageBinding.layoutRight.setVisibility(View.VISIBLE);
                itemContainerMessageBinding.textRight.setText(message.getMessage());
            } else if (message.getSentBy().equals(Message.SENT_BY_BOT)){
                itemContainerMessageBinding.layoutRight.setVisibility(View.GONE);
                itemContainerMessageBinding.layoutLeft.setVisibility(View.VISIBLE);
//                display message like GPT
                startBotMessageAnimation(message);
            }
        }
        private void startBotMessageAnimation(Message message) {
                Handler handler = new Handler();
                for (int i = 0; i < message.getMessage().length(); i++) {
                    int index = i;
                    handler.postDelayed(() -> {
                        String appendedChar = String.valueOf(message.getMessage().charAt(index));
                        itemContainerMessageBinding.textLeft.append(appendedChar);
                    }, 10L * i);
                }
        }
    }
}