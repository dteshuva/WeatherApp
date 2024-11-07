package edu.uiuc.cs427app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import edu.uiuc.cs427app.databinding.ActivityMainBinding;
import java.util.HashSet;
import java.util.Set;

/**
 * The MainActivity class handles the user interface for managing a list of cities.
 * It allows users to add and remove cities from their list and sign out of the application.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private Switch switchOrientation;

    private LinearLayout citiesLayout;
    private EditText editTextCity;
    private Button buttonAddLocation;
    private Button buttonSignout;
    private String userId;
    private ErrorService errorService;
    private CityList cityList; // CityList instance to manage city names


    /**
     * Initializes the activity, sets up UI components, and loads user data.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the orientation switch
        switchOrientation = findViewById(R.id.switchOrientation);

        // Retrieve user ID on startup
        userId = getUserId();
        if (userId == null) {
            errorService = new ErrorService(this);
            errorService.handleError("001", "Please log in again");
            navigateToSignIn();
            return;
        }

        // Load user-specific orientation preference
        boolean isLandscape = loadOrientationPreference(userId);
        setOrientation(isLandscape);
        switchOrientation.setChecked(isLandscape);

        // Set up listener for orientation switch
        switchOrientation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setOrientation(isChecked);
            saveOrientationPreference(userId, isChecked);
        });

        // Initialize UI components
        citiesLayout = findViewById(R.id.citiesLayout);
        editTextCity = findViewById(R.id.editTextCity);
        buttonAddLocation = findViewById(R.id.buttonAddLocation);
        buttonSignout = findViewById(R.id.buttonSignout);

        buttonAddLocation.setOnClickListener(this);
        buttonSignout.setOnClickListener(this);

        errorService = new ErrorService(this);

        // Initialize CityList and load cities from CSV
        cityList = new CityList();
        cityList.scanAndAddCitiesFromCsv(this);  // Load the cities from the CSV file


        String titleText = "TEAM 22: " + userId.toUpperCase();
        setTitle(titleText);
        loadCities();
    }

    // Method to set the orientation based on user choice
    /**
     * Sets the orientation of the activity into either landscape or portrait
     *
     * @param isLandscape true if landscape, false if portait
     */
    private void setOrientation(boolean isLandscape) {
        if (isLandscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * Saves the orientation preference of the user
     *
     * @param userId id of the user
     * @param isLandscape true if landscape, false if portait
     */
    private void saveOrientationPreference(String userId, boolean isLandscape) {
        SharedPreferences preferences = getSharedPreferences("user_prefs_" + userId, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLandscape", isLandscape);
        editor.apply();
    }

    /**
     * Loads the orientation preference of the user
     *
     * @param userId id of the user
     */
    private boolean loadOrientationPreference(String userId) {
        SharedPreferences preferences = getSharedPreferences("user_prefs_" + userId, MODE_PRIVATE);
        return preferences.getBoolean("isLandscape", false);
    }

    /**
     * Loads the user's cities from SharedPreferences and adds them to the UI.
     */
    private void loadCities() {
        try {
            SharedPreferences cityPreferences = getSharedPreferences("user_cities_" + userId, Context.MODE_PRIVATE);
            Set<String> citySet = cityPreferences.getStringSet("cities", new HashSet<>());

            for (String city : citySet) {
                addCityButton(city); // city is already in uppercase
            }
        } catch (Exception e) {
            errorService.handleError("002", "Failed to load cities: " + e.getMessage());
        }
    }

    /**
     * Adds a button for a city to the UI.
     *
     * @param cityName The name of the city to be displayed on the button.
     */
    private void addCityButton(String cityName) {
        try {
            LinearLayout cityLayout = new LinearLayout(this);
            cityLayout.setOrientation(LinearLayout.HORIZONTAL);
            cityLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView cityTextView = new TextView(this);
            cityTextView.setText(cityName);
            cityTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1));  // This takes remaining space

            Button detailsButton = new Button(this);
            detailsButton.setText("Show Details");
            detailsButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            detailsButton.setOnClickListener(view -> {
                try {
                    // Create an Intent to launch DetailsActivity with the city name
                    Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                    intent.putExtra("city", cityName);
                    startActivity(intent);
                } catch (Exception e) {
                    errorService.handleError("500", "Failed to open details for " + cityName + ": " + e.getMessage());
                }
            });

            Button removeButton = new Button(this);
            removeButton.setText("Remove");
            removeButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            removeButton.setBackgroundColor(getResources().getColor(R.color.button_color));

            removeButton.setOnClickListener(view -> {
                try {
                    removeCity(cityName); // cityName is in uppercase
                    citiesLayout.removeView(cityLayout);
                } catch (Exception e) {
                    errorService.handleError("500", "Failed to remove city: " + e.getMessage());
                }
            });

            cityLayout.addView(cityTextView);
            cityLayout.addView(detailsButton);
            cityLayout.addView(removeButton);
            citiesLayout.addView(cityLayout);
        } catch (Exception e) {
            errorService.handleError("500", "Failed to add city button: " + e.getMessage());
        }
    }

    /**
     * Removes a city from SharedPreferences.
     *
     * @param cityName The name of the city to be removed.
     */
    private void removeCity(String cityName) {
        try {
            SharedPreferences cityPreferences = getSharedPreferences("user_cities_" + userId, Context.MODE_PRIVATE);
            Set<String> citySet = new HashSet<>(cityPreferences.getStringSet("cities", new HashSet<>()));
            citySet.remove(cityName); // cityName is already in uppercase
            cityPreferences.edit().putStringSet("cities", citySet).apply();
        } catch (Exception e) {
            errorService.handleError("500", "Failed to remove city from preferences: " + e.getMessage());
        }
    }

    /**
     * Handles click events for buttons in the activity.
     *
     * @param view The view that was clicked.
     */
    @Override
    public void onClick(View view) {
        try {
            if (view.getId() == R.id.buttonAddLocation) {
                String cityName = editTextCity.getText().toString().trim();
                if (!cityName.isEmpty()) {
                    String upperCityName = cityName.toUpperCase();
                    // Check if the city exists in the loaded CityList from CSV
                    if (cityList.getCities().containsKey(upperCityName)) {
                        SharedPreferences cityPreferences = getSharedPreferences("user_cities_" + userId, Context.MODE_PRIVATE);
                        Set<String> citySet = new HashSet<>(cityPreferences.getStringSet("cities", new HashSet<>()));
                        if (citySet.contains(upperCityName)) {
                            errorService.handleError("004", "City name already exists");
                        } else {
                            addCityButton(upperCityName);
                            saveCity(upperCityName); // Save in uppercase
                            editTextCity.setText("");
                        }
                    } else {
                        // Show error if city does not exist in the loaded CityList
                        errorService.handleError("005", "City does not exist in the list.");
                    }


                } else {
                    errorService.handleError("003", "Please enter a city name");
                }
            } else if (view.getId() == R.id.buttonSignout) {
                signOut();
            }
        } catch (Exception e) {
            errorService.handleError("500", "Failed to handle button click: " + e.getMessage());
        }
    }

    /**
     * Saves a new city to SharedPreferences.
     *
     * @param cityName The name of the city to be saved.
     */
    private void saveCity(String cityName) {
        try {
            SharedPreferences cityPreferences = getSharedPreferences("user_cities_" + userId, Context.MODE_PRIVATE);
            Set<String> citySet = new HashSet<>(cityPreferences.getStringSet("cities", new HashSet<>()));
            citySet.add(cityName); // cityName is already in uppercase
            cityPreferences.edit().putStringSet("cities", citySet).apply();
        } catch (Exception e) {
            errorService.handleError("500", "Failed to save city: " + e.getMessage());
        }
    }

    /**
     * Signs out the user by clearing the user ID from SharedPreferences.
     */
    private void signOut() {
        try {
            // Clear only the user ID to log out the user
            SharedPreferences preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
            preferences.edit().remove("user_id").apply();

            navigateToSignIn();
        } catch (Exception e) {
            errorService.handleError("500", "Failed to sign out: " + e.getMessage());
        }
    }

    /**
     * Navigates to the sign-in activity.
     */
    private void navigateToSignIn() {
        Intent intent = new Intent(this, SigninActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Retrieves the user ID from SharedPreferences.
     *
     * @return The user ID as a string or null if not found.
     */
    private String getUserId() {
        SharedPreferences preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        return preferences.getString("user_id", null);
    }
}
