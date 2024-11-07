package edu.uiuc.cs427app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * Manages user-specific preferences such as UI themes and layouts.
 */
public class UserPreferences {
    private String theme;  // "Light" or "Dark"
    private String layout; // "List" or "Grid"

    // Constructor
    /**
     * Constructs a new UserPreferences with specified theme and layout.
     *
     * @param theme The user's preferred theme ("Light" or "Dark")
     * @param layout The user's preferred layout ("List" or "Grid")
     */
    public UserPreferences(String theme, String layout) {
        this.theme = theme;
        this.layout = layout;
    }

    // Getters and Setters
    /**
     * Gets the current theme preference
     * @return The current theme ("Light" or "Dark")
     */
    public String getTheme() {
        return theme;
    }
    /**
     * Sets the theme preference
     * @param theme The new theme to set ("Light" or "Dark")
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }
    /**
     * Gets the current layout preference
     * @return The current layout ("List" or "Grid")
     */
    public String getLayout() {
        return layout;
    }
    /**
     * Sets the layout preference
     * @param layout The new layout to set ("List" or "Grid")
     */
    public void setLayout(String layout) {
        this.layout = layout;
    }

    /**
     * Applies the user's preferred theme to the activity.
     *
     * @param activity The activity where the theme will be applied.
     */
    public void applyTheme(Activity activity) {
        if ("Light".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if ("Dark".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        activity.recreate(); // Needed to apply the theme change
    }

    /**
     * Applies the user's preferred layout to the RecyclerView.
     *
     * @param recyclerView The RecyclerView to which the layout will be applied.
     */
    public void applyLayout(RecyclerView recyclerView) {
        Context context = recyclerView.getContext();
        if ("List".equals(layout)) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else if ("Grid".equals(layout)) {
            recyclerView.setLayoutManager(new GridLayoutManager(context, 2)); // 2 columns for grid
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(context)); // Default to list
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * Saves the user preferences locally.
     *
     * @param context The context used to access SharedPreferences.
     */
    public void savePreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("theme", theme);
        editor.putString("layout", layout);
        editor.apply();
    }

    /**
     * Loads the user preferences from local storage.
     *
     * @param context The context used to access SharedPreferences.
     */
    public void loadPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        theme = sharedPreferences.getString("theme", "Light"); // Default to Light theme
        layout = sharedPreferences.getString("layout", "List"); // Default to List layout
    }
}
