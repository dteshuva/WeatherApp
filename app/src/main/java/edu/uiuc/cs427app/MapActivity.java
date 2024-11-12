package edu.uiuc.cs427app;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;

import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Retrieve data from Intent
        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);
        cityName = getIntent().getStringExtra("cityName");

        Log.d(TAG, "Received city data - City: " + cityName + ", Latitude: " + latitude + ", Longitude: " + longitude);

        // Display latitude and longitude
        TextView cityNameTextView = findViewById(R.id.textViewCityName);
        TextView latitudeTextView = findViewById(R.id.textViewLatitude);
        TextView longitudeTextView = findViewById(R.id.textViewLongitude);
        cityNameTextView.setText(getString(R.string.city_name_label, cityName));
        latitudeTextView.setText(getString(R.string.latitude_label, String.valueOf(latitude)));
        longitudeTextView.setText(getString(R.string.longitude_label, String.valueOf(longitude)));

        // Setup map fragment
        setupMapFragment();
    }

    private void setupMapFragment() {
        Log.d(TAG, "Setting up map fragment");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Map fragment is null");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "Map is ready");
        mMap = googleMap;

        // Center the map on the selected city
        LatLng cityLocation = new LatLng(latitude, longitude);
        Log.d(TAG, "Setting marker at: Latitude = " + latitude + ", Longitude = " + longitude);

        mMap.addMarker(new MarkerOptions().position(cityLocation).title("Marker in " + cityName));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, 12)); // Adjust zoom level if needed
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}
