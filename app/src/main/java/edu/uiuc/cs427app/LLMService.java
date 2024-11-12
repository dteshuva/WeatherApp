package edu.uiuc.cs427app;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.Content;
import com.google.common.util.concurrent.ListenableFuture;
import android.content.Context;
import android.util.Log;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Service class that handles interactions with Google's Gemini LLM API
 * for generating weather-related insights and questions.
 */
public class LLMService {
    private static final String TAG = "LLMService";
    private static final String MODEL_NAME = "gemini-pro";
    private GenerativeModelFutures model;
    private static LLMService instance;
    private ErrorService errorService;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private static final String GEMINI_API_KEY = "AIzaSyDmeVY9nDUTU0sK-kkAqPvms3maZBPAwAo";

    /**
     * Private constructor for singleton pattern
     * @param context Application context for error handling
     */
    private LLMService(Context context) {
        try {
            GenerativeModel baseModel = new GenerativeModel(MODEL_NAME, GEMINI_API_KEY);
            model = GenerativeModelFutures.from(baseModel);
            errorService = new ErrorService(context);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing LLMService: " + e.getMessage());
            errorService.handleError("LLM-001", "Failed to initialize LLM service");
        }
    }

    /**
     * Gets singleton instance of LLMService
     * @param context Application context
     * @return LLMService instance
     */
    public static synchronized LLMService getInstance(Context context) {
        if (instance == null) {
            instance = new LLMService(context);
        }
        return instance;
    }

    /**
     * Generates weather-related questions based on current weather data
     * @param weatherData Current weather conditions
     * @return ListenableFuture containing the generate content response
     */
    public ListenableFuture<GenerateContentResponse> generateQuestions(String weatherData) {
        String prompt = String.format(
                "Today's weather is: %s\n" +
                        "Please generate exactly 3 context-specific questions based on this weather data " +
                        "that users might ask to help them make decisions about their day. " +
                        "Format the response as a simple list with one question per line. " +
                        "Focus on practical questions about clothing, activities, or preparations needed. " +
                        "Don't include any explanatory text, just the questions.",
                weatherData
        );

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        return model.generateContent(content);
    }

    /**
     * Generates insights for a specific weather-related question
     * @param weatherData Current weather conditions
     * @param question User's weather-related question
     * @return ListenableFuture containing the generate content response
     */
    public ListenableFuture<GenerateContentResponse> generateInsight(String weatherData, String question) {
        String prompt = String.format(
                "Based on this weather data: %s\n" +
                        "Please answer this question: %s\n" +
                        "Provide a concise, practical response focused on helping the user make a decision. " +
                        "Keep the response under 3 sentences.",
                weatherData, question
        );

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        return model.generateContent(content);
    }

    /**
     * Shuts down the executor service
     */
    public void shutdown() {
        if (executor instanceof java.util.concurrent.ExecutorService) {
            ((java.util.concurrent.ExecutorService) executor).shutdown();
        }
    }
}
