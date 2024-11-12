package edu.uiuc.cs427app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Manages city-specific information such as geographical location and weather.
 */
public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private String cityName;
    private City city;
    private TextView welcomeMessage;
    private Button buttonMap, buttonWeatherInsights;
    private ErrorService errorService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Initialize views
        welcomeMessage = findViewById(R.id.welcomeText);

        buttonMap = findViewById(R.id.mapButton);
        buttonWeatherInsights = findViewById(R.id.weatherButton);

        // Set the error service
        errorService = new ErrorService(this);

        // Get the city name from the intent
        cityName = getIntent().getStringExtra("city");
        String welcome = "Discover the weather and locations of " + cityName;

        // Retrieve the city object from CityList
        CityList cityList = new CityList();
        cityList.scanAndAddCitiesFromCsv(this);
        city = cityList.getCities().get(cityName.toUpperCase());
        if (city == null) {
            // Handle the error if the city is not found
             errorService.handleError("005", "City does not exist in the list.");
        }
        // Update the UI with the city information
        updateDetailsUI(welcome);

    }

    /**
     * Updates the details screen with new weather/map options.
     */
    private void updateDetailsUI(String welcome) {
        // Update welcome message and city info message
        welcomeMessage.setText(welcome);

        // Add weather information button and map button
        addWeatherButton();
        addMapButton();
    }

    /**
     * Adds the weather information access button.
     */
    private void addWeatherButton() {
        // Make the weather information button visible and enable its click functionality
        buttonWeatherInsights.setVisibility(View.VISIBLE);
        buttonWeatherInsights.setOnClickListener(this);
    }

    /**
     * Adds the map view access button.
     */
    private void addMapButton() {
        // Make the map button visible and enable its click functionality
        buttonMap.setVisibility(View.VISIBLE);
        buttonMap.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.mapButton) {
            if (city != null) {
                String cityName = city.getCity();
                double latitude = city.getLat();
                double longitude = city.getLng();
                openMap(cityName, latitude, longitude);
            } else {
                // Handle the error if city is null
                errorService.handleError("006", "City coordinates not found.");
            }
        } else if (view.getId() == R.id.weatherButton) {
            // Handle the Weather Insights button click
            if (city != null) {
                // You can implement detailed weather insights logic here
                String cityName = city.getCity();
                showWeatherInsights(cityName);
            } else {
                // Handle the error if city data is not available
                errorService.handleError("007", "Weather data not available.");
            }
        }
    }

    // Method to open the MapActivity
    public void openMap(String cityName, double latitude, double longitude) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("cityName", cityName);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivity(intent);
    }

    // Method to open the WeatherActivity
    private void showWeatherInsights(String cityName) {
        Intent intent = new Intent(this, WeatherActivity.class);
        intent.putExtra("cityName", cityName);
        startActivity(intent);
    }
}
