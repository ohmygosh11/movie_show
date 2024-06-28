package com.example.myapplication.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.example.myapplication.R;
import com.example.myapplication.adapters.MessageAdapter;
import com.example.myapplication.databinding.ActivityChatBotBinding;
import com.example.myapplication.models.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
    String storageImgPath;
    String imageURL;
    List<Message> messages;
    MessageAdapter messageAdapter;
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String KEY = "sk-proj-HwsV8es09GYvvAGHiAcqT3BlbkFJ83hgUaPpMucGwzAP1F2n";
    public static final MediaType JSON = MediaType.get("application/json");
    OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    private static final int RECOGNIZER_REQUEST = 1;
    private static final int REQUEST_CODE = 2;
    private static final int PICK_IMAGE_REQUEST = 3;

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
            handleClickSendMessage(message, imageURL);
        });
//        handle click voice
        activityChatBotBinding.imageVoiceChat.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to text");
            startActivityForResult(intent, RECOGNIZER_REQUEST);
        });
//        check grant permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
//        handle click attach
        activityChatBotBinding.imageAttach.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
    }

    private void handleClickSendMessage(String message, String imageURL) {
        addToChat(message, Message.SENT_BY_USER);
        if (storageImgPath == null) {
            imageURL = null;
        }
        activityChatBotBinding.inputChat.setText("");
        activityChatBotBinding.textWelcome.setVisibility(View.GONE);
        callApi(message, imageURL);
        imageURL = null;
    }

    private void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messages.add(new Message(message, sentBy));
            messageAdapter.notifyItemRangeInserted(messages.size(), 1);
            messageAdapter.notifyItemInserted(messages.size());
            activityChatBotBinding.recyclerViewChat.smoothScrollToPosition(messages.size() - 1);
        });
    }

    private void callApi(String question, String imageURL) {
        JSONObject jsonBody = new JSONObject();
        try {
            if (imageURL != null) {
                    JSONArray contentArr = new JSONArray();
                    JSONArray messageArr = new JSONArray();
                    JSONObject messageObj = new JSONObject();
                    JSONObject text = new JSONObject();
                    JSONObject url = new JSONObject();
                    JSONObject image = new JSONObject();

                    text.put("type", "text");
                    text.put("text", question);
                    url.put("url", imageURL);
                    image.put("type", "image_url");
                    image.put("image_url", url);
                    contentArr.put(text);
                    contentArr.put(image);
                    messageObj.put("role", "user");
                    messageObj.put("content", contentArr);
                    messageArr.put(messageObj);
                    jsonBody.put("model", "gpt-4-turbo");
                    jsonBody.put("messages", messageArr);
                    jsonBody.put("max_tokens", 300);
            } else {
                    JSONArray messagesArr = new JSONArray();
                    JSONObject systemMessage = new JSONObject();
                    JSONObject userMessage = new JSONObject();

                    systemMessage.put("role", "system");
                    systemMessage.put("content", "You are a helpful assistant.");
                    userMessage.put("role", "user");
                    userMessage.put("content", question);
                    messagesArr.put(systemMessage);
                    messagesArr.put(userMessage);
                    jsonBody.put("model", "gpt-3.5-turbo");
                    jsonBody.put("messages", messagesArr);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(OPENAI_URL)
                .header("Authorization", "Bearer " + KEY)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addToChat("Failed response due to1: " + e.getMessage(), Message.SENT_BY_BOT);
                if (imageURL != null) {
                    deleteImageFromFirebase(storageImgPath);
                    storageImgPath = null;
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try{
                    if (!response.isSuccessful()) {
                        assert response.body() != null;
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String message = jsonResponse.getJSONObject("error").getString("message");
                        addToChat("Failed response due to2: " + message, Message.SENT_BY_BOT);
                    } else {
                        assert response.body() != null;
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String content = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                        addToChat(content.trim(), Message.SENT_BY_BOT);
                    }
                } catch (JSONException | IOException e) {
                    addToChat("Failed response due to3: " + e.getMessage(), Message.SENT_BY_BOT);
                }
                if (imageURL != null) {
                    deleteImageFromFirebase(storageImgPath);
                    storageImgPath = null;
                }
            }
        });
    }

//    convert uri to hosted online url
    private void uploadFirebase(Uri imageUri) {
        if (imageUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString() + ".jpg");

            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    storageImgPath = imageRef.getPath();
                    imageURL = uri.toString();
                    Toast.makeText(this, "Upload image successfully", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed upload image", Toast.LENGTH_SHORT).show();
            });
        }
    }

//  delete image from database
    private void deleteImageFromFirebase(String storageImgPath) {
        if (storageImgPath != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference imageRef = storage.getReference().child(storageImgPath);
            imageRef.delete();
        }
    }

//    handle returned result from voice, photo picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECOGNIZER_REQUEST &&  resultCode == RESULT_OK) {
            if (data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null) {
                    handleClickSendMessage(result.get(0), null);
                }
            }
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    Toast.makeText(this, "Please wait a moment to upload the photo", Toast.LENGTH_SHORT).show();
                    uploadFirebase(imageUri);
                }
            }
        }
    }

}