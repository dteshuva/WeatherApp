// File: WeatherInsightsActivity.java
package edu.uiuc.cs427app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Activity to display LLM-generated weather insights
 */
public class WeatherInsightsActivity extends AppCompatActivity {

    private static final String TAG = "WeatherInsightsActivity";
    private String cityName;
    private Weather weatherData;
    private LinearLayout questionsContainer;
    private TextView responseTextView;
    private ProgressBar loadingSpinner;
    private LLMService llmService;
    private ErrorService errorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_insights);

        // Initialize views
        questionsContainer = findViewById(R.id.questionsContainer);
        responseTextView = findViewById(R.id.responseTextView);
        loadingSpinner = findViewById(R.id.loadingSpinner);

        // Get data from intent with validation
        Intent intent = getIntent();
        if (intent == null) {
            errorService.handleError("INSIGHTS-001", "Invalid navigation to insights");
            finish();
            return;
        }

        cityName = intent.getStringExtra("cityName");
        weatherData = (Weather) intent.getSerializableExtra("weatherData");

        if (cityName == null || weatherData == null) {
            errorService.handleError("INSIGHTS-002", "Missing required weather data");
            finish();
            return;
        }

        // Continue with normal initialization
        llmService = LLMService.getInstance(this);
        fetchQuestions();
    }

    private void fetchQuestions() {
        loadingSpinner.setVisibility(View.VISIBLE);
        questionsContainer.removeAllViews(); // Clear existing questions

        String weatherDescription = formatWeatherData();

        try {
            ListenableFuture<GenerateContentResponse> future = llmService.generateQuestions(weatherDescription);
            future.addListener(() -> {
                try {
                    String response = future.get().getText();
                    runOnUiThread(() -> {
                        loadingSpinner.setVisibility(View.GONE);
                        displayQuestions(response);
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error getting LLM response", e);
                    runOnUiThread(() -> {
                        loadingSpinner.setVisibility(View.GONE);
                        errorService.handleError("INSIGHTS-002", "Failed to generate questions: " + e.getMessage());
                    });
                }
            }, MoreExecutors.directExecutor());
        } catch (Exception e) {
            Log.e(TAG, "Error fetching questions", e);
            loadingSpinner.setVisibility(View.GONE);
            errorService.handleError("INSIGHTS-003", "Error starting question generation: " + e.getMessage());
        }
    }

    private void displayQuestions(String questionsText) {
        String[] questions = questionsText.split("\n");
        for (final String question : questions) {
            if (!question.trim().isEmpty()) {
                Button questionButton = new Button(this);
                questionButton.setText(question.trim());
                questionButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                questionButton.setOnClickListener(v -> fetchInsight(question.trim()));
                questionsContainer.addView(questionButton);
            }
        }
    }

    private void fetchInsight(String question) {
        loadingSpinner.setVisibility(View.VISIBLE);
        responseTextView.setVisibility(View.GONE);

        String weatherDescription = formatWeatherData();

        try {
            ListenableFuture<GenerateContentResponse> future = llmService.generateInsight(weatherDescription, question);
            future.addListener(() -> {
                try {
                    String response = future.get().getText();
                    runOnUiThread(() -> {
                        loadingSpinner.setVisibility(View.GONE);
                        responseTextView.setText(response);
                        responseTextView.setVisibility(View.VISIBLE);
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error getting insight response", e);
                    runOnUiThread(() -> {
                        loadingSpinner.setVisibility(View.GONE);
                        errorService.handleError("INSIGHTS-004", "Failed to generate insight: " + e.getMessage());
                    });
                }
            }, MoreExecutors.directExecutor());
        } catch (Exception e) {
            Log.e(TAG, "Error fetching insight", e);
            loadingSpinner.setVisibility(View.GONE);
            errorService.handleError("INSIGHTS-005", "Error starting insight generation: " + e.getMessage());
        }
    }

    private String formatWeatherData() {
        return String.format(
                "City: %s, Weather: %s, Temperature: %.1f°C, Humidity: %d%%, Wind Speed: %.1f m/s",
                weatherData.getCityName(),
                weatherData.getWeatherDescription(),
                weatherData.getTemperature(),
                weatherData.getHumidity(),
                weatherData.getWindSpeed()
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (llmService != null) {
            llmService.shutdown();
        }
    }
}
