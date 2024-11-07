package edu.uiuc.cs427app;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * The CityList class provides a thread-safe collection of City objects.
 * The class provides methods to add and remove City objects.
 */
public class CityList {

    final private Map<String, City> cities;
    private static final String TAG = "CityList";

    /**
     * Constructs a new CityList instance.
     * Initializes the cities set as a synchronized HashSet.
     */
    public CityList() {
        this.cities = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Adds a city to the list.
     *
     * @param city The City object to be added to the list.
     */
    public void addCity(City city) {
        this.cities.put(city.getCity(), city);
    }

    /**
     * Adds a city to the list from a CSV line
     *
     * @param csvLine Line from the SimpleMaps CSV file
     */
    public void addCity(String csvLine) {
        // Attempting to create a city object
        City city;
        try {
            city = City.fromCsvLine(csvLine);
        }
        // If a city object can't be created from the string, just return without
        // performing any action
        catch (IllegalArgumentException e) {
            return;
        }
        this.cities.put(city.getCity(), city);
    }

    /**
     * Removes a city from the list.
     *
     * @param city The City object to be removed from the list.
     */
    public void removeCity(City city) {
        this.cities.remove(city.getCity());
    }

    /**
     * Returns an unmodifiable copy of the cities collection.
     *
     * @return An unmodifiable Map with city names as keys and City objects as values.
     *         The city names are in uppercase format.
     */
    public Map<String,City> getCities(){
        return Collections.unmodifiableMap(this.cities);
    }

    /**
     * Scans the "cities.csv" file in the assets directory and adds all cities to the CityList.
     *
     * @param context The application context to access assets.
     */
    public void scanAndAddCitiesFromCsv(Context context) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open("cities.csv")));
            String line;
            while ((line = reader.readLine()) != null) {
                addCity(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading cities.csv file", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing BufferedReader", e);
                }
            }
        }
    }
}
