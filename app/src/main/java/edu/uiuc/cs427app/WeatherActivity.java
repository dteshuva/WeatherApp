// File: WeatherActivity.java
package edu.uiuc.cs427app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity to display weather information
 */
public class WeatherActivity extends AppCompatActivity implements WeatherService.WeatherServiceCallback {

    private static final String TAG = "WeatherActivity";

    private TextView cityNameTextView;
    private TextView dateTimeTextView;
    private TextView temperatureTextView;
    private TextView weatherDescriptionTextView;
    private TextView humidityTextView;
    private TextView windSpeedTextView;

    private String cityName;
    private WeatherService weatherService;
    private Weather weatherData; // Store weather data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);


        // Disable insights button until weather data is loaded
        findViewById(R.id.weatherButton).setEnabled(false);

        // Get city name from intent
        Intent intent = getIntent();
        cityName = intent.getStringExtra("cityName");

        // Initialize views
        cityNameTextView = findViewById(R.id.cityNameTextView);
        dateTimeTextView = findViewById(R.id.dateTimeTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        weatherDescriptionTextView = findViewById(R.id.weatherDescriptionTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        windSpeedTextView = findViewById(R.id.windSpeedTextView);

        // Set city name
        cityNameTextView.setText(cityName);

        // Initialize WeatherService
        weatherService = new WeatherService(this, this);

        // Fetch weather data
        weatherService.fetchWeather(cityName);

        // Set up "Weather Insights" button
        findViewById(R.id.weatherButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWeatherInsights();
            }
        });
    }

    // Callback when weather data is received
    @Override
    public void onWeatherDataReceived(Weather weather) {
        this.weatherData = weather; // Store weather data
        updateUI(weather);
        // Enable insights button after weather data is loaded
        findViewById(R.id.weatherButton).setEnabled(true);
    }

    // Callback when there is an error fetching weather data
    @Override
    public void onWeatherDataError(String error) {
        Log.e(TAG, "Error: " + error);
        // Show error message to the user (implement as needed)
        new ErrorService(this).handleError("WEATHER-001", "Failed to load weather data: " + error);
    }

    // Update the UI with weather data
    private void updateUI(Weather weather) {
        dateTimeTextView.setText(weather.getDateTime());
        temperatureTextView.setText(String.format("%.1f°C", weather.getTemperature()));
        weatherDescriptionTextView.setText(weather.getWeatherDescription());
        humidityTextView.setText("Humidity: " + weather.getHumidity() + "%");
        windSpeedTextView.setText("Wind Speed: " + weather.getWindSpeed() + " m/s");
    }

    // Navigate to Weather Insights Activity
    private void openWeatherInsights() {
        Log.d(TAG, "Opening Weather Insights for city: " + cityName);
        if (weatherData == null) {
            Log.e(TAG, "Weather data is null!");
            // Show error and return if weather data isn't loaded yet
            new ErrorService(this).handleError("WEATHER-002", "Please wait for weather data to load");
            return;
        }
        Intent intent = new Intent(WeatherActivity.this, WeatherInsightsActivity.class);
        intent.putExtra("cityName", cityName);
        intent.putExtra("weatherData", weatherData);
        startActivity(intent);
    }
}
