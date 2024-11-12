// File: Weather.java
package edu.uiuc.cs427app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Represents weather data for a specific city
 */
public class Weather implements Serializable {

    private final String cityName;
    private final String dateTime;
    private final double temperature;
    private final String weatherDescription;
    private final int humidity;
    private final double windSpeed;
    private final int windDeg;
    private final double windGust;
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     *
     * @param cityName           Name of the city
     * @param dateTime           Date and time of the weather data
     * @param temperature        Temperature in Celsius
     * @param weatherDescription Description of the weather
     * @param humidity           Humidity percentage
     * @param windSpeed          Wind speed in m/s
     */
    public Weather(String cityName, String dateTime, double temperature, String weatherDescription, int humidity,
                   double windSpeed, int windDeg, double windGust) {
        this.cityName = cityName;
        this.dateTime = dateTime;
        this.temperature = temperature;
        this.weatherDescription = weatherDescription;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.windDeg = windDeg;
        this.windGust = windGust;
    }

    /**
     * Static method to parse JSON and return a Weather object
     *
     * @param jsonObject JSON object containing weather data
     * @return Weather object
     * @throws Exception If parsing fails
     */
    public static Weather parseWeatherJson(JSONObject jsonObject, String cityName) throws Exception {
        try {
            // Parse current weather
            JSONObject current = jsonObject.getJSONObject("current");

            // Parse timestamp
            long timestamp = current.getLong("dt");
            String dateTime = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
                    .format(new java.util.Date(timestamp * 1000));

            // Basic weather data
            double temperature = current.getDouble("temp");
            int humidity = current.getInt("humidity");

            // Wind data
            double windSpeed = current.getDouble("wind_speed");
            int windDeg = current.getInt("wind_deg");
            double windGust = current.has("wind_gust") ? current.getDouble("wind_gust") : 0.0;

            // Weather description
            JSONArray weatherArray = current.getJSONArray("weather");
            String weatherDescription = "";
            if (weatherArray.length() > 0) {
                weatherDescription = weatherArray.getJSONObject(0).getString("description");
            }

            return new Weather(cityName, dateTime, temperature, weatherDescription,
                    humidity, windSpeed, windDeg, windGust);
        } catch (Exception e) {
            throw new Exception("Error parsing weather JSON", e);
        }
    }

    // Getters
    public String getCityName() {
        return cityName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public int getWindDeg() {
        return windDeg;
    }

    public double getWindGust() {
        return windGust;
    }

    @Override
    public String toString() {
        return "Weather{" +
                "city='" + cityName + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", temperature=" + temperature +
                "°C, description='" + weatherDescription + '\'' +
                ", humidity=" + humidity +
                "%, windSpeed=" + windSpeed +
                " m/s, windDirection=" + windDeg +
                "°, windGust=" + windGust +
                " m/s}";
    }

    // Optional validation method
    public boolean isValid() {
        // Implement validation logic if required
        return true;
    }
}
