package edu.uiuc.cs427app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Manages city-specific information such as geographical location and weather.
 */
public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private String cityName;
    private City city;
    private ErrorService errorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        errorService = new ErrorService(this);

        // Get the city name from the intent
        cityName = getIntent().getStringExtra("city");
        String welcome = "Welcome to " + cityName;
        String cityWeatherInfo = "Detailed information about the weather of " + cityName;

        // Initialize the GUI elements
        TextView welcomeMessage = findViewById(R.id.welcomeText);
        TextView cityInfoMessage = findViewById(R.id.cityInfo);

        welcomeMessage.setText(welcome);
        cityInfoMessage.setText(cityWeatherInfo);

        Button buttonMap = findViewById(R.id.mapButton);
        buttonMap.setOnClickListener(this);

        // Retrieve the city object from CityList
        CityList cityList = new CityList();
        cityList.scanAndAddCitiesFromCsv(this);
        city = cityList.getCities().get(cityName.toUpperCase());

        if (city == null) {
            // Handle the error if the city is not found
            errorService.handleError("005", "City does not exist in the list.");
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.mapButton) {
            if (city != null) {
                double latitude = city.getLat();
                double longitude = city.getLng();
                openMap(latitude, longitude);
            } else {
                // Handle the error if city is null
                errorService.handleError("006", "City coordinates not found.");
            }
        }
    }

    // Method to open the MapActivity
    public void openMap(double latitude, double longitude) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivity(intent);
    }
}


//package edu.uiuc.cs427app;
//
//import android.os.Bundle;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
///**
// * Manages city-specific information such as geographical location and weather
// */
//public class DetailsActivity extends AppCompatActivity implements View.OnClickListener{
//
//    /**
//     * Initializes the activity, sets up UI components, and displays city information.
//     * This includes setting up the welcome message, city info text, and map button.
//     *
//     * @param savedInstanceState If the activity is being re-initialized after previously
//     *                          being shut down, this Bundle contains the most recent data.
//     */
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_details);
//
//        // Process the Intent payload that has opened this Activity and show the information accordingly
//        String cityName = getIntent().getStringExtra("city").toString();
//        String welcome = "Welcome to the "+cityName;
//        String cityWeatherInfo = "Detailed information about the weather of "+cityName;
//
//        // Initializing the GUI elements
//        TextView welcomeMessage = findViewById(R.id.welcomeText);
//        TextView cityInfoMessage = findViewById(R.id.cityInfo);
//
//        welcomeMessage.setText(welcome);
//        cityInfoMessage.setText(cityWeatherInfo);
//        // Get the weather information from a Service that connects to a weather server and show the results
//
//        Button buttonMap = findViewById(R.id.mapButton);
//        buttonMap.setOnClickListener(this);
//
//    }
//
//    /**
//     * Handles click events for the map button in the Details Activity.
//     * This will be implemented in Milestone 4 to show the city's location on a map.
//     *
//     * @param view The view that triggered the click event (map button)
//     */
//    @Override
//    public void onClick(View view) {
//        //Implement this (create an Intent that goes to a new Activity, which shows the map)
//    }
//}
//
