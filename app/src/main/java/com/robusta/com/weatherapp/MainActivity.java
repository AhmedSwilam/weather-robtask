package com.robusta.com.weatherapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 101;
    private static final int REQUEST_IMAGE = 12;
    private LocationRequest mLocationRequest;
    private PendingResult<LocationSettingsResult> result;
    private GoogleApiClient client;
    private Unbinder unbinder;
    private LocationManager locationManager;
    private String provider;
    private MyLocationListener mylistener;
    private Criteria criteria;
    private static final Integer GPS_SETTINGS = 0x7;
    @BindView(R.id.imageView1)
    ImageView imageView;
    RequestQueue requestQueue;
    TextView textView;
    String data = "";

    private String jsonurl = "http://samples.openweathermap.org/data/2.5/weather?lat=37&lon=139&appid=1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
          textView= (TextView) findViewById(R.id.weather);

        requestQueue= Volley.newRequestQueue(this);


          JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, jsonurl, null, new Response.Listener<JSONObject>() {


              @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array=response.getJSONArray("coord");
                    for (int i=0;i<array.length();i++){
                        JSONObject coord=array.getJSONObject(i);
                        String weather=coord.getString("weather");
                        String temp=coord.getString("temp");
                        String pressure=coord.getString("pressure");
                        String humidity=coord.getString("humidity");
                        // textView.append(weather+""+temp+""+pressure+""+humidity+" \n ");
                        data += "weather: " + weather +
                                "temp : " + temp+"pressure: "+pressure +"humidity: "+humidity;
                        textView.setText(data);
                    }

                    } catch (JSONException e1) {
                    e1.printStackTrace();
                }

            }}, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            Log.e("VOLLEY","ERROR");
            }
        });
        requestQueue.add(request);
        unbinder = ButterKnife.bind(this);
        client = new GoogleApiClient.Builder(this)
                .addApi(AppIndex.API)
                .addApi(LocationServices.API)
                .build();
        askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, MY_PERMISSIONS_REQUEST_LOCATION);
        //askForPermission(Manifest.permission.CAMERA, MY_PERMISSIONS_REQUEST_CAMERA);
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_LOCATION:
                    openCamera();
                    break;
                //Camera
                case MY_PERMISSIONS_REQUEST_CAMERA:
                    openCamera();
                    break;
            }
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }

    }

//    private void askForGPS() {
//        mLocationRequest = LocationRequest.create();
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
//        builder.setAlwaysShow(true);
//        result = LocationServices.SettingsApi.checkLocationSettings(client, builder.build());
//        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//            @Override
//            public void onResult(LocationSettingsResult result) {
//                final Status status = result.getStatus();
//                switch (status.getStatusCode()) {
//                    case LocationSettingsStatusCodes.SUCCESS:
//                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//                        // Define the criteria how to select the location provider
//                        criteria = new Criteria();
//                        criteria.setAccuracy(Criteria.ACCURACY_COARSE);   //default
//
//                        // user defines the criteria
//
//                        criteria.setCostAllowed(false);
//                        // get the best provider depending on the criteria
//                        provider = locationManager.getBestProvider(criteria, false);
//
//                        // the last known location of this provider
//                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                            // TODO: Consider calling
//                            //    ActivityCompat#requestPermissions
//                            // here to request the missing permissions, and then overriding
//                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                            //                                          int[] grantResults)
//                            // to handle the case where the user grants the permission. See the documentation
//                            // for ActivityCompat#requestPermissions for more details.
//                            return;
//                        }
//                        Location location = locationManager.getLastKnownLocation(provider);
//
//                        mylistener = new MyLocationListener();
//
//                        if (location != null) {
//                            mylistener.onLocationChanged(location);
//                        } else {
//                            // leads to the settings because there is no last known location
//                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                            startActivity(intent);
//                        }
//                        // location updates: at least 1 meter and 200millsecs change
//                        String a = "" + location.getLatitude();
//                        double latitude = location.getLatitude();
//                        double longitude = location.getLongitude();
//
//                        break;
//                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                        try {
//                            status.startResolutionForResult(MainActivity.this, GPS_SETTINGS);
//                        } catch (IntentSender.SendIntentException e) {
//
//                        }
//                        break;
//                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                        break;
//                }
//            }
//        });
//    }


    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);

        }
        }


    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            // Initialize the location fields
            Toast.makeText(MainActivity.this, "" + location.getLatitude() + location.getLongitude(),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(MainActivity.this, provider + "'s status changed to " + status + "!",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(MainActivity.this, "Provider " + provider + " enabled!",
                    Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(MainActivity.this, "Provider " + provider + " disabled!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
