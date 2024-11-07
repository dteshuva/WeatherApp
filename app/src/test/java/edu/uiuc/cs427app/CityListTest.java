package edu.uiuc.cs427app;

import android.content.Context;
import android.content.res.AssetManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.*;

public class CityListTest {

    private CityList cityList;

    @Mock
    Context mockContext;

    @Mock
    AssetManager mockAssetManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        cityList = new CityList();
    }

   @Test
    public void testScanAndAddCitiesFromCsv() throws IOException {
        // Arrange
        String csvData = "00601,18.18027,-66.75266,Adjuntas,PR,Puerto Rico,TRUE,,16834,100.9,72001,Adjuntas,string,Adjuntas|Utuado,72001|72141,FALSE,FALSE,America/Puerto_Rico\n00601,18.18027,-66.75266,Adjuntas,NY,New York,TRUE,,16834,100.9,72001,Adjuntas,string,Adjuntas|Utuado,72001|72141,FALSE,FALSE,America/Puerto_Rico";
        InputStream inputStream = new ByteArrayInputStream(csvData.getBytes());

        Mockito.when(mockContext.getAssets()).thenReturn(mockAssetManager);
        Mockito.when(mockAssetManager.open("cities.csv")).thenReturn(inputStream);

        // Act
        cityList.scanAndAddCitiesFromCsv(mockContext);

        // Assert
        Map<String, City> cities = cityList.getCities();
        assertEquals(1, cities.size());
        assertTrue(cities.containsKey("Adjuntas".toUpperCase()));
        City city = cities.get("Adjuntas".toUpperCase());
        assertNotNull(city);
        assertEquals("00601", city.getZip());
        assertEquals(18.18027, city.getLat(), 0.0001);
        assertEquals(-66.75266, city.getLng(), 0.0001);
        assertEquals("Adjuntas".toUpperCase(), city.getCity());
        assertEquals("NY", city.getStateId());
        assertEquals("New York", city.getStateName());
        assertEquals(16834, city.getPopulation());
    }

}
