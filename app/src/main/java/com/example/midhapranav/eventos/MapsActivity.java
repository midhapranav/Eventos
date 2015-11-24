package com.example.midhapranav.eventos;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements LocationListener {

    static class markerHolder {
        String title;
        String id;
        Double latitude;
        Double longitude;
        int radius;
        Marker marker;
        Circle circle;
    }

    static class events {
        String eventId;
        String name;
        String startTime;
        String endTime;
        String description;
        URL url;
    }
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private List<markerHolder> mMarkerList;
    private List<events> mEventsList;
    public static String mUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMarkerList = new ArrayList<markerHolder>();
        mUserId = getIntent().getExtras().getString("userid");
        Log.d("USERID Debug->","The user id is "+ mUserId);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        setupCreateGeoFenceButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            UiSettings ui = mMap.getUiSettings();
            ui.setCompassEnabled(true);
            ui.setZoomControlsEnabled(true);
            ui.setAllGesturesEnabled(true);
            mMap.setMyLocationEnabled(true);
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    for(final markerHolder m : mMarkerList) {
                        if(Math.abs(m.latitude - latLng.latitude)<0.0005 && Math.abs(m.longitude - latLng.longitude)<0.0005) {
                            Log.d("MapClick debug->","Marker found with title "+m.title);
                            LinearLayout alertBoxLayout = new LinearLayout(MapsActivity.this);
                            alertBoxLayout.setOrientation(LinearLayout.VERTICAL);
                            AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
                            alert.setTitle("Edit GeoFence"); //Set Alert dialog title here
                            // Set an EditText view to get user input
                            final TextView radiusText = new TextView(getApplicationContext());
                            final EditText radius = new EditText(getApplicationContext());
                            final EditText name = new EditText(getApplicationContext());
                            final TextView nameText = new TextView(getApplicationContext());

                            radiusText.setText("\t Update Radius here:");
                            nameText.setTextColor(Color.BLACK);
                            alertBoxLayout.addView(radiusText);
                            radius.setText(Integer.toString(m.radius));
                            radius.setTextColor(Color.BLACK);
                            radiusText.setTextColor(Color.BLACK);
                            alertBoxLayout.addView(radius);
                            nameText.setText("\t Update Name here:");
                            name.setText(m.title);
                            name.setTextColor(Color.BLACK);
                            alertBoxLayout.addView(nameText);
                            alertBoxLayout.addView(name);

                            alert.setView(alertBoxLayout);
                            alert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //You will get as string input data in this variable.
                                    // here we convert the input to a string and show in a toast.
                                    new Thread (new Runnable(){
                                        @Override
                                        public void run(){
                                            try {
                                                String params = "geofenceid="+m.id+"&geofencename="+name.getEditableText().toString()+"&radius="+radius.getEditableText().toString()+"&long="+m.longitude+"&lat="+m.latitude;
                                                String url = "http://eventosdataapi-env.elasticbeanstalk.com/?selector=6&"+params;
                                                URL urlObj = new URL(url);
                                                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                                                conn.setDoOutput(false);
                                                conn.setRequestMethod("PUT");
                                                conn.setRequestProperty("Accept-Charset", "UTF-8");
                                                conn.setConnectTimeout(15000);
                                                conn.getOutputStream().write(params.getBytes("UTF-8"));
                                                Log.d("UpdateDelete debug->", Integer.toString(conn.getResponseCode()));
                                                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                                StringBuilder sb = new StringBuilder();
                                                String output;
                                                while ((output = br.readLine()) != null) {
                                                    sb.append(output);
                                                }
                                                JSONObject json = new JSONObject(sb.toString());
                                                Log.d("UpdateDelete debug->", json.toString());
                                                if((json.getString("success").equals("True"))) {
                                                    m.radius = Integer.parseInt(radius.getEditableText().toString());
                                                    m.title = name.getEditableText().toString();
                                                    MapsActivity.this.runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            m.circle.setRadius(Double.parseDouble(radius.getEditableText().toString()));
                                                            m.marker.setTitle(name.getEditableText().toString());
                                                            Toast.makeText(getBaseContext(), ("GeoFence updated!"),
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else {
                                                    MapsActivity.this.runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getBaseContext(), ("Network Error! Please Try again!"),
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();
                                }
                            });
                            alert.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //You will get as string input data in this variable.
                                    // here we convert the input to a string and show in a toast.
                                    Log.d("UpdateDelete Debug - >", "Delete here");
                                    new Thread (new Runnable(){
                                        @Override
                                        public void run(){
                                            try {
                                                String params = "geofenceid="+m.id;
                                                String url = "http://eventosdataapi-env.elasticbeanstalk.com/?selector=7&"+params;
                                                URL urlObj = new URL(url);
                                                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                                                conn.setDoOutput(false);
                                                conn.setRequestMethod("PUT");
                                                conn.setRequestProperty("Accept-Charset", "UTF-8");
                                                conn.setConnectTimeout(15000);
                                                conn.getOutputStream().write(params.getBytes("UTF-8"));
                                                Log.d("UpdateDelete debug->", Integer.toString(conn.getResponseCode()));
                                                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                                StringBuilder sb = new StringBuilder();
                                                String output;
                                                while ((output = br.readLine()) != null) {
                                                    sb.append(output);
                                                }
                                                JSONObject json = new JSONObject(sb.toString());
                                                Log.d("UpdateDelete debug->", json.toString());
                                                if((json.getString("success").equals("True"))) {
                                                    MapsActivity.this.runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            m.circle.remove();
                                                            m.marker.remove();
                                                            mMarkerList.remove(m);
                                                            Toast.makeText(getBaseContext(), ("GeoFence deleted!"),
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else {
                                                    MapsActivity.this.runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getBaseContext(), ("Network Error! Please Try again!"),
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();

                                }
                            });
                            alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Canceled.
                                    dialog.cancel();
                                }
                            });
                            AlertDialog alertDialog = alert.create();
                            alertDialog.show();
                        }
                    }
                }
            });
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMapWithGeoFences();
            }


        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMapWithGeoFences() {
        //TODO setup-map with existing geoFences
    }

    private void setupCreateGeoFenceButton() {
        // Declare your builder here -
        Button mButton = (Button) findViewById(R.id.create_geo_fence_button);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout alertBoxLayout = new LinearLayout(MapsActivity.this);
                alertBoxLayout.setOrientation(LinearLayout.VERTICAL);

                AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
                alert.setTitle("Create GeoFence"); //Set Alert dialog title here
                // Set an EditText view to get user input
                final EditText address = new EditText(getApplicationContext());
                final TextView addressText = new TextView(getApplicationContext());
                final TextView radiusText = new TextView(getApplicationContext());
                final EditText radius = new EditText(getApplicationContext());
                final EditText name = new EditText(getApplicationContext());
                final TextView nameText = new TextView(getApplicationContext());

                addressText.setText("\t Enter Address here:");
                addressText.setTextColor(Color.BLACK);
                alertBoxLayout.addView(addressText);
                alertBoxLayout.addView(address);
                radiusText.setText("\t Enter Radius here:");
                nameText.setTextColor(Color.BLACK);
                alertBoxLayout.addView(radiusText);
                alertBoxLayout.addView(radius);
                nameText.setText("\t Enter Name here:");
                radiusText.setTextColor(Color.BLACK);
                alertBoxLayout.addView(nameText);
                alertBoxLayout.addView(name);

                alert.setView(alertBoxLayout);
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //You will get as string input data in this variable.
                        // here we convert the input to a string and show in a toast.
                        String srt = address.getEditableText().toString();
                        setLocationFromStringAddress(address.getEditableText().toString(), Double.parseDouble(radius.getEditableText().toString()),name.getEditableText().toString());
                    }
                });
                alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = alert.create();
                alertDialog.show();
            }
        });
    }

    public void setLocationFromStringAddress(String strAddress, double radius, String name){

        Geocoder coder = new Geocoder(getApplicationContext());
        List<Address> address;

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address == null) {
                return;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();
            LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());
            double radiusInMeters = radius;
            createNewGeoFenceRecord(name, (int) radius, coordinate, 1);
            return;
        }
        catch (Exception e){}
    }


    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        Log.d("Map debug->",latitude +","+longitude);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void createNewGeoFenceRecord(final String geoFenceName, final int geoFenceRadius, final LatLng geoFenceCenter, final int selector) {

        new Thread (new Runnable(){
            @Override
            public void run(){
                try {
                    if(selector == 1) {
                        String params = "userid=" + mUserId + "&geofencename=" + geoFenceName + "&radius=" + (double)geoFenceRadius*0.000621371 + "&long=" + geoFenceCenter.longitude + "&lat=" + geoFenceCenter.latitude;
                        String url = "http://eventosdataapi-env.elasticbeanstalk.com/?selector=3";
                        URL urlObj = new URL(url);
                        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                        conn.setDoOutput(false);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Accept-Charset", "UTF-8");
                        conn.setConnectTimeout(15000);
                        conn.getOutputStream().write(params.getBytes("UTF-8"));
                        Log.d("AddGeoFence debug->", Integer.toString(conn.getResponseCode()));
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String output;
                        while ((output = br.readLine()) != null) {
                            sb.append(output);
                        }
                        JSONObject json = new JSONObject(sb.toString());
                        Log.d("AddGeoFence debug->", json.toString());
                        if ((json.getString("success").equals("True"))) {
                            MapsActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getBaseContext(), ("GeoFence created!"),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            MapsActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getBaseContext(), ("Network Error! Please Try again!"),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    MapsActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            int strokeColor = 0xffff0000; //red outline
                            int shadeColor = 0x44ff0000; //opaque red fill
                            CircleOptions circleOptions = new CircleOptions().center(geoFenceCenter).radius(geoFenceRadius * 0.000621371).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
                            Circle circle = mMap.addCircle(circleOptions);
                            Marker m = mMap.addMarker(new MarkerOptions().position(geoFenceCenter).title(geoFenceName));
                            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(geoFenceCenter, 11.0f);
                            mMap.animateCamera(yourLocation);
                            markerHolder mMarkerHolder = new markerHolder();
                            mMarkerHolder.id = geoFenceName;
                            mMarkerHolder.latitude = geoFenceCenter.latitude;
                            mMarkerHolder.longitude = geoFenceCenter.longitude;
                            mMarkerHolder.title = geoFenceName;
                            mMarkerHolder.radius = geoFenceRadius;
                            mMarkerHolder.circle = circle;
                            mMarkerHolder.marker = m;
                            mMarkerList.add(mMarkerHolder);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        getEvents();
    }

    private void getEvents() {
        new Thread (new Runnable(){
            @Override
            public void run(){
                try {
                    String url = "http://eventosdataapi-env.elasticbeanstalk.com/?geofenceid=2&selector=5";
                    URL urlObj = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(false);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept-Charset", "UTF-8");
                    conn.setConnectTimeout(15000);
                    Log.d("Login debug->","Connecting");
                    conn.connect();
                    Log.d("Login debug->", Integer.toString(conn.getResponseCode()));
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }
                    JSONObject json = new JSONObject(sb.toString());
                    Log.d("GetEvents debug->",json.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
