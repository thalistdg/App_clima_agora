package com.example.climaagora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private TextView txtLocation;
    private TextView txtInfo;
    private ProgressBar progressBar;

    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLocation = findViewById(R.id.txtLocation);
        txtInfo = findViewById(R.id.txtInfo);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnBuscar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION
                    );
                } else {
                    buscar();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                buscar();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void buscar() {
        progressBar.setVisibility(View.VISIBLE);

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_CODE_LOCATION_PERMISSION
            );

        }
        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {

                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            latitude =
                                    locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            longitude =
                                    locationResult.getLocations().get(latestLocationIndex).getLongitude();


                            //consulta API
                            String url = String.format(
                                    "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=6bdc1c2fc9eab6921407eae03bb43b71&units=metric&lang=pt_br",
                                    latitude,
                                    longitude
                            );
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    //Log.d("response", response);
                                    String output;
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                                        JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                                        String description = jsonObjectWeather.getString("description");
                                        JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                                        double temp = jsonObjectMain.getDouble("temp");
                                        double temp_min = jsonObjectMain.getDouble("temp_min");
                                        double temp_max = jsonObjectMain.getDouble("temp_max");
                                        double feelslike = jsonObjectMain.getDouble("feels_like");
                                        int humidity = jsonObjectMain.getInt("humidity");
                                        String local = jsonResponse.getString("name");
                                        JSONObject jsonObjectClouds = jsonResponse.getJSONObject("clouds");
                                        int clouds = jsonObjectClouds.getInt("all");

                                        txtLocation.setText(local);

                                        output = temp + "°C\n"
                                                + description + " - " + clouds + "% de nebulosidade\n"
                                                + "Sensação térmica: " + feelslike + "°C\n"
                                                + "Máxima: " + temp_max + "°C\n"
                                                + "Mínima: " + temp_min + "°C\n"
                                                + "Umidade: " + humidity + "%\n";

                                        txtInfo.setText(output);
                                        progressBar.setVisibility(View.GONE);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                            requestQueue.add(stringRequest);

                        }

                    }

                }, Looper.getMainLooper());

    }
}