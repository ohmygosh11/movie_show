package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.MotionEvent;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.example.myapplication.R;
import com.example.myapplication.adapters.MessageAdapter;
import com.example.myapplication.databinding.ActivityChatBotBinding;
import com.example.myapplication.models.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatBotActivity extends AppCompatActivity {
    ActivityChatBotBinding activityChatBotBinding;
    List<Message> messages;
    MessageAdapter messageAdapter;
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_API_KEY = "sk-proj-ZahEa4JlMtfCzixhcgD2T3BlbkFJ51Xh3JaEaBAcT7IodrgE";
    public static final MediaType JSON = MediaType.get("application/json");
    OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    private static final int RECOGNIZER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        activityChatBotBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat_bot);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        doInitialization();
    }

    private void doInitialization() {
//        set adapter to recycler view
        messages = new ArrayList<>();
        activityChatBotBinding.recyclerViewChat.setHasFixedSize(true);
        messageAdapter = new MessageAdapter(messages);
        activityChatBotBinding.recyclerViewChat.setAdapter(messageAdapter);
//        close the chat bot
        activityChatBotBinding.imageClose.setOnClickListener(v -> onBackPressed());
//        handle click send message
        activityChatBotBinding.imageSendChat.setOnClickListener(v -> {
            String message = activityChatBotBinding.inputChat.getText().toString().trim();
            handleClickSendMessage(message);
        });
//        handle click voice
        activityChatBotBinding.imageVoiceChat.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to text");
            startActivityForResult(intent, RECOGNIZER_REQUEST);
        });

    }

    private void handleClickSendMessage(String message) {
        addToChat(message, Message.SENT_BY_USER);
        activityChatBotBinding.inputChat.setText("");
        activityChatBotBinding.textWelcome.setVisibility(View.GONE);
        callApi(message);
    }

    private void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messages.add(new Message(message, sentBy));
            messageAdapter.notifyItemRangeInserted(messages.size(), 1);
            messageAdapter.notifyItemInserted(messages.size());
            activityChatBotBinding.recyclerViewChat.smoothScrollToPosition(messages.size() - 1);
        });
    }

    private void callApi(String question) {
        JSONObject jsonBody = new JSONObject();
        try {
            JSONArray messagesArr = new JSONArray();

            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a helpful assistant.");
            messagesArr.put(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", question);
            messagesArr.put(userMessage);

            jsonBody.put("model", "gpt-3.5-turbo");
            jsonBody.put("messages", messagesArr);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(OPENAI_URL)
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addToChat("Failed response due to: " + e.getMessage(), Message.SENT_BY_BOT);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try{
                    if (!response.isSuccessful()) {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String message = jsonResponse.getJSONObject("error").getString("message");
                        addToChat("Failed response due to: " + message, Message.SENT_BY_BOT);
                    } else {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String content = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                        addToChat(content.trim(), Message.SENT_BY_BOT);
                    }
                } catch (JSONException | IOException e) {
                    addToChat("Failed response due to: " + e.getMessage(), Message.SENT_BY_BOT);
                }
            }
        });
    }

//    handle returned result from voice
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECOGNIZER_REQUEST &&  resultCode == RESULT_OK) {
            if (data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null) {
                    handleClickSendMessage(result.get(0));
                }
            }

        }
    }
}