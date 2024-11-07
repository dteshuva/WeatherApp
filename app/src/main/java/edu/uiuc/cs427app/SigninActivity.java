package edu.uiuc.cs427app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * The SigninActivity class provides a user interface for signing in and signing up.
 * It allows users to select a theme and manages user credentials.
 */
public class SigninActivity extends AppCompatActivity implements View.OnClickListener {

    private ErrorService errorService = new ErrorService(this);
    private RadioGroup radioGroupTheme;
    private RadioButton radioLoadTheme;
    private RadioButton radioLightTheme;
    private RadioButton radioDarkTheme;
    private String userId;

    /**
     * Initializes the activity, sets the theme based on user preferences,
     * and sets up UI components and their event listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Initialize UI components
        Button buttonSignin = findViewById(R.id.buttonSignin);
        Button buttonSignup = findViewById(R.id.buttonSignup);
        radioGroupTheme = findViewById(R.id.radioGroupTheme);

        // Initialize radio buttons for theme selection
        radioLoadTheme = findViewById(R.id.radioLoadTheme); // "Load Theme" option
        radioLightTheme = findViewById(R.id.radioLight);     // "Light Theme" option
        radioDarkTheme = findViewById(R.id.radioDark);       // "Dark Theme" option

        // Set click listeners for buttons
        buttonSignin.setOnClickListener(this);
        buttonSignup.setOnClickListener(this);

        // Set "Load Theme" as the default selected option
        radioLoadTheme.setChecked(true);

        // Listener to change theme mode based on user selection
        radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioLight) {
                setThemeMode(false);
            } else if (checkedId == R.id.radioDark) {
                setThemeMode(true);
            } else if (checkedId == R.id.radioLoadTheme) {
                applyUserSavedTheme();
            }
        });
    }

    /**
     * Sets the application theme mode (light or dark) for the specific user and saves the preference.
     *
     * @param nightMode True for night mode, false for light mode.
     */
    private void setThemeMode(boolean nightMode) {
        if (userId != null) {
            // Save theme preference in user-specific SharedPreferences
            SharedPreferences preferences = getSharedPreferences("user_prefs_" + userId, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isNightMode", nightMode);
            editor.apply();

            // Apply the theme
            AppCompatDelegate.setDefaultNightMode(nightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        }
    }

    /**
     * Applies the saved theme for the current user if it exists.
     */
    private void applyUserSavedTheme() {
        if (userId != null) {
            SharedPreferences preferences = getSharedPreferences("user_prefs_" + userId, MODE_PRIVATE);
            boolean isDarkMode = preferences.getBoolean("isNightMode", false); // Default to light mode if not set
            AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        }
    }

    /**
     * Handles click events for sign-in and sign-up buttons.
     *
     * @param view The view that was clicked.
     */
    @Override
    public void onClick(View view) {
        EditText EditText_username = findViewById(R.id.editTextUsername);
        EditText EditText_password = findViewById(R.id.editTextPassword);

        switch (view.getId()) {
            case R.id.buttonSignin:
                handleSignIn(EditText_username.getText().toString(), EditText_password.getText().toString());
                break;

            case R.id.buttonSignup:
                handleSignUp(EditText_username.getText().toString(), EditText_password.getText().toString());
                break;
        }
    }

    /**
     * Handles the sign-in process, authenticating the user and starting the MainActivity.
     *
     * @param username The user's entered username.
     * @param password The user's entered password.
     */
    private void handleSignIn(String username, String password) {
        try {
            User user = User.signIn(getApplicationContext(), username, password);
            if (user != null) {
                // Save the user ID in SharedPreferences
                userId = username; // Assign the userId to use in theme functions
                saveUserId(userId);

                // Apply theme based on user selection
                if (radioLoadTheme.isChecked()) {
                    applyUserSavedTheme();
                } else if (radioLightTheme.isChecked()) {
                    setThemeMode(false);
                } else if (radioDarkTheme.isChecked()) {
                    setThemeMode(true);
                }

                // Start the MainActivity upon successful sign-in
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            errorService.handleError("500", "Failed to open MainActivity: " + e.getMessage());
        }
    }

    /**
     * Handles the sign-up process, creating a new user account.
     *
     * @param username The user's entered username.
     * @param password The user's entered password.
     */
    private void handleSignUp(String username, String password) {
        try {
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (radioLoadTheme.isChecked()) {
                Toast.makeText(this, "Load Theme cannot be selected for Sign Up.", Toast.LENGTH_SHORT).show();
                return;
            }

            String theme = radioDarkTheme.isChecked() ? "Dark" : "Light";
            User user = new User(username, "", password, theme, "List");

            // Attempt to sign up the user
            if (user.signUp(getApplicationContext())) {
                Toast.makeText(this, "Sign up successful! Please sign in.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to sign up", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            errorService.handleError("500", "Failed to sign up: " + e.getMessage());
        }
    }

    /**
     * Saves the user ID to SharedPreferences for future reference.
     *
     * @param userId The user ID to be saved.
     */
    private void saveUserId(String userId) {
        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        preferences.edit().putString("user_id", userId).apply();
    }
}
