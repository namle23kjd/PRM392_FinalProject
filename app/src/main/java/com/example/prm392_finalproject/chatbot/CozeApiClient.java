package com.example.prm392_finalproject.chatbot;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class CozeApiClient {

    public void sendMessage(String userInput, ResponseCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(CozeConstants.API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + CozeConstants.API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("bot_id", CozeConstants.BOT_ID);
                jsonBody.put("user", "android_user");
                JSONArray inputArray = new JSONArray();
                inputArray.put(userInput);
                jsonBody.put("query", userInput);
                jsonBody.put("stream", false);

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Scanner in = new Scanner(conn.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (in.hasNextLine()) {
                        response.append(in.nextLine());
                    }
                    in.close();

                    JSONObject responseJson = new JSONObject(response.toString());
                    JSONArray messages = responseJson.getJSONArray("messages");
                    String content = messages.getJSONObject(0).getString("content");

                    callback.onSuccess(content);
                } else {
                    callback.onError(new Exception("API Error: " + responseCode));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
