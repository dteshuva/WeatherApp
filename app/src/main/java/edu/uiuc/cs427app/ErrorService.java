package edu.uiuc.cs427app;

import android.widget.Toast;
import android.content.Context;
import android.util.Log;

/**
 * ErrorService class handles error management by logging errors and displaying them as Toast messages.
 * This helps to inform the user about issues while keeping a record for debugging purposes.
 */
public class ErrorService {
    // Variables to hold the error code and error message details
    private String errorCode;
    private String errorMessage;
    private Context context; // Context is required for displaying Toast messages in Android

    /**
     * Constructor to initialize the ErrorService with the application context.
     *
     * @param context - The application context needed to display the Toast message.
     */
    public ErrorService(Context context) {
        this.context = context;
    }

    /**
     * Handles an error by logging the error details and displaying a Toast message to the user.
     * This method centralizes error handling to keep the user informed without navigating them away.
     *
     * @param errorCode - A string representing a unique code for the error, useful for tracking.
     * @param errorMessage - A descriptive message detailing the error, displayed to the user in the Toast.
     */
    public void handleError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;

        // Log the error details for debugging purposes.
        logError();

        // Display a Toast message to the user with the error information.
        // The Toast is set to LENGTH_LONG to give users enough time to read the message.
        Toast.makeText(context, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Logs the error details to Logcat, a logging system in Android, using the error code and message.
     * This allows developers to check logs and troubleshoot issues based on these logs.
     */
    private void logError() {
        // The "e" level is used to log error-level messages in Logcat.
        Log.e("ErrorService", "Error Code: " + errorCode + ", Message: " + errorMessage);
    }
}
