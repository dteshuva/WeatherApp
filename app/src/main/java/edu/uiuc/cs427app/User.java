package edu.uiuc.cs427app;

import android.content.Context;
import android.util.Log;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;

/**
 * Represents a user in the application.
 * Handles user attributes, authentication, and preference management.
 */
public class User {
    private String username;
    private String email;
    private String password;
    private String theme;
    private String layout;

    /**
     * Constructs a new User with specified attributes.
     *
     * @param username The user's username
     * @param email The user's email address
     * @param password The user's password
     * @param theme The user's preferred theme
     * @param layout The user's preferred layout
     */
    public User(String username, String email, String password, String theme, String layout) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.theme = theme;
        this.layout = layout;
    }

    /**
     * Signs up a new user by adding them to the database.
     * @param context   The context of the application.
     * @return True if sign-up is successful, false otherwise.
     */
    public boolean signUp(Context context) {
        try {
            BufferedReader reader;
            StringBuilder fileContent = new StringBuilder();

            // Check if user_info.tsv exists in internal storage
            if (fileExists(context, "user_info.tsv")) {
                // Read existing users from internal storage
                reader = new BufferedReader(
                        new InputStreamReader(context.openFileInput("user_info.tsv")));
            } else {
                // If not, copy it from assets
                copyAssetToInternalStorage(context, "user_info.tsv");
                reader = new BufferedReader(
                        new InputStreamReader(context.openFileInput("user_info.tsv")));
            }

            // Check for duplicate usernames
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    fileContent.append(line).append("\n");
                    String[] userData = line.split("\t");
                    if (userData.length >= 1 && userData[0].equals(this.username)) {
                        // Username already exists
                        reader.close();
                        return false;
                    }
                }
            }
            reader.close();

            // Append the new user
            String tsvLine = String.format("%s\t%s\t%s\t%s\t%s\n",
                    username, email, password, theme, layout);
            fileContent.append(tsvLine);

            // Write back to user_info.tsv in internal storage
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            context.openFileOutput("user_info.tsv", Context.MODE_PRIVATE)));
            writer.write(fileContent.toString());
            writer.close();
            return true;
        } catch (IOException e) {
            Log.e("User", "Error signing up user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Signs in an existing user.
     *
     * @param context   The context of the application.
     * @param username  The username of the user.
     * @param password  The password entered by the user.
     * @return The authenticated User object if successful, null otherwise.
     */
    public static User signIn(Context context, String username, String password) {
        try {
            BufferedReader reader;

            // Check if user_info.tsv exists in internal storage
            if (fileExists(context, "user_info.tsv")) {
                reader = new BufferedReader(
                        new InputStreamReader(
                                context.openFileInput("user_info.tsv")));
            } else {
                // If not, copy it from assets
                copyAssetToInternalStorage(context, "user_info.tsv");
                reader = new BufferedReader(
                        new InputStreamReader(
                                context.openFileInput("user_info.tsv")));
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5 &&
                        parts[0].equals(username) &&
                        parts[2].equals(password)) {
                    reader.close();
                    return new User(parts[0], parts[1], parts[2], parts[3], parts[4]);
                }
            }
            reader.close();
        } catch (IOException e) {
            Log.e("User", "Error signing in: " + e.getMessage());
        }
        return null;
    }

    // Utility methods
    /**
     * Copies files in the assets folder to internal storage
     * @param context   The context of the application.
     * @param filename  The file name of the asset.
     * @return true if the file exists in the stream path, false if it isn't
     */
    private static boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        return file.exists();
    }

    /**
     * Copies files in the assets folder to internal storage
     * @param context   The context of the application.
     * @param filename  The file name of the asset.
     */
    private static void copyAssetToInternalStorage(Context context, String filename) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream in = assetManager.open(filename);
            OutputStream out = context.openFileOutput(filename, Context.MODE_PRIVATE);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Getters
    /**
     * Gets the username of the user
     * @return The user's username
     */
    public String getUsername() { return username; }
    /**
     * Gets the email address of the user
     * @return The user's email address
     */
    public String getEmail() { return email; }
    /**
     * Gets the theme preference of the user
     * @return The user's preferred theme
     */
    public String getTheme() { return theme; }
    /**
     * Gets the layout preference of the user
     * @return The user's preferred layout
     */
    public String getLayout() { return layout; }
}
