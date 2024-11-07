package edu.uiuc.cs427app;

import static org.junit.Assert.*;
import org.junit.Test;

public class CityTest {

    @Test
    public void testFromCsvLine_ValidInput() {
        // Arrange
        String validCsvLine = "90011,34.00714,-118.25874,Los Angeles,CA,California,TRUE,,109414,0.0,06037," +
                "Los Angeles County,1.0,Los Angeles County,06037,false,false,America/Los_Angeles";

        // Act
        City city = City.fromCsvLine(validCsvLine);

        // Assert
        assertEquals("90011", city.getZip());
        assertEquals(34.00714, city.getLat(), 0.0001);
        assertEquals(-118.25874, city.getLng(), 0.0001);
        assertEquals("Los Angeles", city.getCity());
        assertEquals("CA", city.getStateId());
        assertEquals("California", city.getStateName());
        assertTrue(city.isZcta());
        assertEquals("", city.getParentZcta());
        assertEquals(109414, city.getPopulation());
        assertEquals(0.0, city.getDensity(), 0.0001);
        assertEquals("06037", city.getCountyFips());
        assertEquals("Los Angeles County", city.getCountyName());
        assertEquals("1.0", city.getCountyWeights());
        assertEquals("Los Angeles County", city.getCountyNamesAll());
        assertEquals("06037", city.getCountyFipsAll());
        assertFalse(city.isImprecise());
        assertFalse(city.isMilitary());
        assertEquals("America/Los_Angeles", city.getTimezone());
    }

    @Test
    public void testFromCsvLine_MissingFields() {
        // Arrange
        String invalidCsvLine = "90011,34.00714,-118.25874,Los Angeles,CA";

        // Act & Assert
        try {
            City.fromCsvLine(invalidCsvLine);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("insufficient fields"));
        }
    }

    @Test
    public void testFromCsvLine_InvalidNumber() {
        // Arrange
        String invalidCsvLine = "90011,invalid,-118.25874,Los Angeles,CA,California,TRUE,,109414,0.0,06037," +
                "Los Angeles County,1.0,Los Angeles County,06037,false,false,America/Los_Angeles";

        // Act & Assert
        try {
            City.fromCsvLine(invalidCsvLine);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid CSV line format"));
        }
    }

    @Test
    public void testFromCsvLine_InvalidBoolean() {
        // Arrange
        String invalidCsvLine = "90011,34.00714,-118.25874,Los Angeles,CA,California,NotBoolean,,109414,0.0,06037," +
                "Los Angeles County,1.0,Los Angeles County,06037,false,false,America/Los_Angeles";

        // Act & Assert
        try {
            City.fromCsvLine(invalidCsvLine);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid boolean value"));
        }
    }

    @Test
    public void testFromCsvLine_NullInput() {
        // Act & Assert
        try {
            City.fromCsvLine(null);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("CSV line cannot be null"));
        }
    }

    @Test
    public void testFromCsvLine_EmptyInput() {
        // Act & Assert
        try {
            City.fromCsvLine("");
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("insufficient fields"));
        }
    }

    @Test
    public void testFromCsvLine_WithWhitespace() {
        // Arrange
        String whitespaceLine = " 90011 , 34.00714 , -118.25874 , Los Angeles , CA , California , TRUE , , " +
                "109414 , 0.0 , 06037 , Los Angeles County , 1.0 , Los Angeles County , 06037 , " +
                "false , false , America/Los_Angeles ";

        // Act
        City city = City.fromCsvLine(whitespaceLine);

        // Assert
        assertEquals("90011", city.getZip());
        assertEquals("Los Angeles", city.getCity());
        assertEquals("CA", city.getStateId());
    }
}
