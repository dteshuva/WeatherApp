// File: WeatherService.java
package edu.uiuc.cs427app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;

/**
 * WeatherService class to fetch weather data from API
 */
public class WeatherService {
    private static final String TAG = "WeatherService";
    private static final String API_KEY = "aa85c48169af5b26e1c8763f7326c000";
    // Use correct API endpoint for current weather data
    private static final String BASE_URL = "https://api.openweathermap.org/data/3.0/onecall";

    private WeatherServiceCallback callback;
    private Context context;
    private ErrorService errorService;

    public WeatherService(Context context, WeatherServiceCallback callback) {
        this.context = context;
        this.callback = callback;
        this.errorService = new ErrorService(context);
        setupSSL();
    }

    private void setupSSL() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            Log.d(TAG, "SSL setup completed. All certificates are trusted temporarily.");
        } catch (Exception e) {
            Log.e(TAG, "SSL setup error", e);
        }
    }

    public interface WeatherServiceCallback {
        void onWeatherDataReceived(Weather weather);
        void onWeatherDataError(String error);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void fetchWeather(String cityName) {
        // Check network availability first
        if (!isNetworkAvailable()) {
            Log.e(TAG, "No internet connection available");
            callback.onWeatherDataError("No internet connection");
            errorService.handleError("WEATHER-003", "No internet connection available");
            return;
        }

        // Get city data
        CityList cityList = new CityList();
        cityList.scanAndAddCitiesFromCsv(context);
        City city = cityList.getCities().get(cityName.toUpperCase());

        if (city != null) {
            Log.d(TAG, "Fetching weather data for city: " + city.getCity());
            new FetchWeatherTask().execute(city);
        } else {
            Log.e(TAG, "City not found in database: " + cityName);
            callback.onWeatherDataError("City not found");
            errorService.handleError("WEATHER-002", "City not found in database");
        }
    }

    private class FetchWeatherTask extends AsyncTask<City, Void, Weather> {
        private String errorMessage = null;

        @Override
        protected Weather doInBackground(City... params) {
            City city = params[0];
            try {
                if (!isNetworkAvailable()) {
                    throw new IOException("No internet connection");
                }
                return weatherApiCall(city);
            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e(TAG, "Error fetching weather data: " + e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Weather weather) {
            if (weather != null) {
                callback.onWeatherDataReceived(weather);
            } else {
                callback.onWeatherDataError(errorMessage != null ? errorMessage : "Unknown error");
                errorService.handleError("WEATHER-001", errorMessage);
            }
        }
    }

    private Weather weatherApiCall(City city) throws Exception {
        String urlString = String.format(Locale.US, "%s?lat=%.6f&lon=%.6f&units=metric&appid=%s",
                BASE_URL,
                city.getLat(),
                city.getLng(),
                API_KEY
        );

        Log.d(TAG, "Attempting to fetch weather data from: " + urlString);

        URL url = new URL(urlString);
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000); // 15 seconds timeout
            connection.setReadTimeout(15000);    // 15 seconds timeout
            connection.setDoInput(true);
            connection.connect();

            int responseCode = connection.getResponseCode();
            Log.d(TAG, "Weather API Response Code: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                String errorResponse = readStream(connection.getErrorStream());
                Log.e(TAG, "Error response: " + errorResponse);
                throw new Exception("Server returned code: " + responseCode);
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            String jsonResponse = response.toString();
            Log.d(TAG, "Weather API Response: " + jsonResponse);

            JSONObject jsonObject = new JSONObject(jsonResponse);
            jsonObject.put("name", city.getCity());
            return Weather.parseWeatherJson(jsonObject, city.getCity());

        } catch (IOException e) {
            Log.e(TAG, "Network error: " + e.getMessage(), e);
            throw new Exception("Network error: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing reader", e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }
}
