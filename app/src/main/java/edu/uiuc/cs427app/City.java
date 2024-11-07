package edu.uiuc.cs427app;

import java.util.Locale;

/**
 * Represents a city with data from SimpleMaps CSV dataset.
 * Handles city information and provides methods for weather and map data retrieval.
 */
public class City {
    // Core attributes matching CSV format
    private String zip;           // ZIP code (primary key)
    private double lat;          // Latitude
    private double lng;          // Longitude
    private String city;         // City name
    private String stateId;      // State ID (e.g., CA)
    private String stateName;    // Full state name
    private boolean zcta;        // ZCTA status
    private String parentZcta;   // Parent ZCTA
    private int population;      // Population count
    private double density;      // Population density
    private String countyFips;   // County FIPS code
    private String countyName;   // County name
    private String countyWeights; // County weights
    private String countyNamesAll; // All county names
    private String countyFipsAll; // All county FIPS codes
    private boolean imprecise;   // Imprecise flag
    private boolean military;    // Military flag
    private String timezone;     // Timezone

    /**
     * Constructs a new City object with all attributes from the SimpleMaps CSV data.
     *
     * @param zip Unique ZIP code identifier for the city (e.g., "90011")
     * @param lat Geographical latitude in decimal degrees (e.g., 34.00714)
     * @param lng Geographical longitude in decimal degrees (e.g., -118.25874)
     * @param city Name of the city (e.g., "Los Angeles")
     * @param stateId Two-letter state identifier (e.g., "CA")
     * @param stateName Full name of the state (e.g., "California")
     * @param zcta Flag indicating if this is a ZIP Code Tabulation Area
     * @param parentZcta Parent ZCTA code if applicable, empty string if none
     * @param population Total population count for the ZIP code area
     * @param density Population density (population per square mile)
     * @param countyFips FIPS code for the county (e.g., "06037")
     * @param countyName Name of the county (e.g., "Los Angeles County")
     * @param countyWeights Weight distribution if ZIP spans multiple counties
     * @param countyNamesAll All county names if ZIP spans multiple counties
     * @param countyFipsAll All county FIPS codes if ZIP spans multiple counties
     * @param imprecise Flag indicating if location data is imprecise
     * @param military Flag indicating if this is a military ZIP code
     * @param timezone Timezone identifier (e.g., "America/Los_Angeles")
     *
     * @throws IllegalArgumentException if any numeric parameters are invalid or
     *         if required string parameters are null or empty
     */
    public City(String zip, double lat, double lng, String city, String stateId,
                String stateName, boolean zcta, String parentZcta, int population,
                double density, String countyFips, String countyName, String countyWeights,
                String countyNamesAll, String countyFipsAll, boolean imprecise,
                boolean military, String timezone) {
        // Validate required string fields
        if (zip == null || zip.trim().isEmpty()) {
            throw new IllegalArgumentException("ZIP code cannot be null or empty");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be null or empty");
        }
        if (stateId == null || stateId.trim().isEmpty()) {
            throw new IllegalArgumentException("State ID cannot be null or empty");
        }
        if (stateName == null || stateName.trim().isEmpty()) {
            throw new IllegalArgumentException("State name cannot be null or empty");
        }

        // Validate numeric ranges
        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (lng < -180 || lng > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        if (population < 0) {
            throw new IllegalArgumentException("Population cannot be negative");
        }
        if (density < 0) {
            throw new IllegalArgumentException("Density cannot be negative");
        }

        // Assign values after validation
        this.zip = zip.trim();
        this.lat = lat;
        this.lng = lng;
        this.city = city.toUpperCase().trim(); // Store city name in uppercase
        this.stateId = stateId.trim();
        this.stateName = stateName.trim();
        this.zcta = zcta;
        this.parentZcta = parentZcta != null ? parentZcta.trim() : "";
        this.population = population;
        this.density = density;
        this.countyFips = countyFips != null ? countyFips.trim() : "";
        this.countyName = countyName != null ? countyName.trim() : "";
        this.countyWeights = countyWeights != null ? countyWeights.trim() : "";
        this.countyNamesAll = countyNamesAll != null ? countyNamesAll.trim() : "";
        this.countyFipsAll = countyFipsAll != null ? countyFipsAll.trim() : "";
        this.imprecise = imprecise;
        this.military = military;
        this.timezone = timezone != null ? timezone.trim() : "";
    }

    /**
     * Creates a City object from a CSV line
     * @param csvLine Line from the SimpleMaps CSV file
     * @return New City object
     * @throws IllegalArgumentException if CSV line is invalid
     */
    public static City fromCsvLine(String csvLine) {
        if (csvLine == null) {
            throw new IllegalArgumentException("CSV line cannot be null");
        }

        try {
            // Use a regular expression to split by commas, ignoring commas within quotes
            String[] fields = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            if (fields.length < 18) {
                throw new IllegalArgumentException("Invalid CSV line format: insufficient fields");
            }

            // Remove surrounding quotes and trim each field
            for (int i = 0; i < fields.length; i++) {
                fields[i] = fields[i].replaceAll("^\"|\"$", "").trim();
            }

            return new City(
                    fields[0],                     // zip
                    Double.parseDouble(fields[1]), // lat
                    Double.parseDouble(fields[2]), // lng
                    fields[3],                     // city
                    fields[4],                     // state_id
                    fields[5],                     // state_name
                    parseBoolean(fields[6]),       // zcta
                    fields[7],                     // parent_zcta
                    Integer.parseInt(fields[8]),   // population
                    Double.parseDouble(fields[9]), // density
                    fields[10],                    // county_fips
                    fields[11],                    // county_name
                    fields[12],                    // county_weights
                    fields[13],                    // county_names_all
                    fields[14],                    // county_fips_all
                    parseBoolean(fields[15]),      // imprecise
                    parseBoolean(fields[16]),      // military
                    fields[17]                     // timezone
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid CSV line format: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to parse boolean values
     * @param value String value to parse
     * @return boolean result
     */
    private static boolean parseBoolean(String value) {
        value = value.toLowerCase().trim();
        if (value.equals("true") || value.equals("1")) return true;
        if (value.equals("false") || value.equals("0")) return false;
        throw new IllegalArgumentException("Invalid boolean value: " + value);
    }

    // Getters for all fields
    /**
     * Gets the ZIP code for this city
     * @return The city's ZIP code (e.g., "90011")
     */
    public String getZip() { return zip; }
    /**
     * Gets the latitude coordinate of this city
     * @return The geographical latitude in decimal degrees
     */
    public double getLat() { return lat; }
    /**
     * Gets the longitude coordinate of this city
     * @return The geographical longitude in decimal degrees
     */
    public double getLng() { return lng; }
    /**
     * Gets the name of this city
     * @return The city name in uppercase
     */
    public String getCity() { return city; }
    /**
     * Gets the two-letter state identifier
     * @return The state ID (e.g., "CA" for California)
     */
    public String getStateId() { return stateId; }
    /**
     * Gets the full name of the state
     * @return The complete state name (e.g., "California")
     */
    public String getStateName() { return stateName; }
    /**
     * Checks if this is a ZIP Code Tabulation Area
     * @return true if this is a ZCTA, false otherwise
     */
    public boolean isZcta() { return zcta; }
    /**
     * Gets the parent ZCTA code if applicable
     * @return The parent ZCTA code, or empty string if none exists
     */
    public String getParentZcta() { return parentZcta; }
    /**
     * Gets the population count for this ZIP code area
     * @return The total population
     */
    public int getPopulation() { return population; }
    /**
     * Gets the population density
     * @return The population density (population per square mile)
     */
    public double getDensity() { return density; }
    /**
     * Gets the FIPS code for the county
     * @return The county FIPS code (e.g., "06037")
     */
    public String getCountyFips() { return countyFips; }
    /**
     * Gets the name of the county
     * @return The county name (e.g., "Los Angeles County")
     */
    public String getCountyName() { return countyName; }
    /**
     * Gets the weight distribution if ZIP spans multiple counties
     * @return The county weights string
     */
    public String getCountyWeights() { return countyWeights; }
    /**
     * Gets all county names if ZIP spans multiple counties
     * @return String containing all county names
     */
    public String getCountyNamesAll() { return countyNamesAll; }
    /**
     * Gets all county FIPS codes if ZIP spans multiple counties
     * @return String containing all county FIPS codes
     */
    public String getCountyFipsAll() { return countyFipsAll; }
    /**
     * Checks if the location data is imprecise
     * @return true if location data is imprecise, false otherwise
     */
    public boolean isImprecise() { return imprecise; }
    /**
     * Checks if this is a military ZIP code
     * @return true if this is a military ZIP code, false otherwise
     */
    public boolean isMilitary() { return military; }
    /**
     * Gets the timezone identifier for this city
     * @return The timezone identifier (e.g., "America/Los_Angeles")
     */
    public String getTimezone() { return timezone; }

    /**
     * Converts city information to a formatted string representation
     * @return A formatted string containing the city name, state ID, ZIP code,
     *         population count, county name, and timezone
     */
    @Override
    public String toString() {
        return String.format(Locale.US, "%s, %s, %s (Pop: %d) - County: %s, Timezone: %s",
                getCity(), getStateId(), getZip(), getPopulation(), getCountyName(), getTimezone());
    }
}
