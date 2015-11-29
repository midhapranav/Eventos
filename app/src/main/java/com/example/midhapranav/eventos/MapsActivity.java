package com.example.midhapranav.eventos;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    static class markerHolder {
        String title;
        String id;
        Double latitude;
        Double longitude;
        Double radius;
        Marker marker;
        Circle circle;
        List<eventDetails> eventList;
    }

    static class eventDetails {
        Marker eventMarker;
        String name;
        String description;
        String startTime;
        String endTime;
        String URL;
        double latitude;
        double longitude;
    }
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private List<markerHolder> mMarkerList;
    private List<eventDetails> mEventsList;
    public static String mUserId;
    public static Location location;
    public static HashMap<String, eventDetails> eventDetailsMap;
    public static String geoFenceIdWhenNew;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMarkerList = new ArrayList<markerHolder>();
        eventDetailsMap = new HashMap<String,eventDetails>();
        mUserId = getIntent().getExtras().getString("userid");
        Log.d("USERID Debug->","The user id is "+ mUserId);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        setupCreateGeoFenceButton();
        setupExistingGeoFences();
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
        final String myGeoFenceName = "";
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            UiSettings ui = mMap.getUiSettings();
            ui.setCompassEnabled(true);
            ui.setZoomControlsEnabled(true);
            ui.setAllGesturesEnabled(true);
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    Log.d("Map Debugging","Location is changed");
                    for(final markerHolder m : mMarkerList) {
                        Log.d("Map Debugging", "finding marker");
                        Location loc = new Location("");
                        loc.setLongitude(m.longitude);
                        loc.setLatitude(m.latitude);
                        float result = location.distanceTo(loc);
                        Log.d("Map Debugging", "Distance is " + result*0.000621371);
                        Log.d("Map Debugging","Radius is "+ m.radius);
                        if ((result*0.000621371)<m.radius) {
                            Log.d("Map Debugging","found marker");
                            final String name = m.title;
                         MapsActivity.this.runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 Toast.makeText(getBaseContext(), ("You are in "+name),
                                         Toast.LENGTH_SHORT).show();
                                 sendNotification(name);
                             }
                         });

                            break;
                        }
                    }
                }
            });


            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    for (final markerHolder m : mMarkerList) {
                        if (Math.abs(m.latitude - latLng.latitude) < 0.0005 && Math.abs(m.longitude - latLng.longitude) < 0.0005) {
                            Log.d("MapClick debug->", "Marker found with title " + m.title);
                            LinearLayout alertBoxLayout = new LinearLayout(MapsActivity.this);
                            alertBoxLayout.setOrientation(LinearLayout.VERTICAL);
                            AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
                            alert.setTitle("Edit GeoFence"); //Set Alert dialog title here
                            // Set an EditText view to get user input
                            final TextView radiusText = new TextView(getApplicationContext());
                            final EditText radius = new EditText(getApplicationContext());
                            final EditText name = new EditText(getApplicationContext());
                            final TextView nameText = new TextView(getApplicationContext());

                            radiusText.setText("\t Update Radius here(in miles):");
                            nameText.setTextColor(Color.BLACK);
                            alertBoxLayout.addView(radiusText);
                            radius.setText(Double.toString(m.radius));
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
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                String params = "geofenceid=" + m.id + "&geofencename=" + name.getEditableText().toString() + "&radius=" + radius.getEditableText().toString() + "&long=" + m.longitude + "&lat=" + m.latitude;
                                                String url = "http://eventosdataapi-env.elasticbeanstalk.com/?selector=6&" + params;
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
                                                if ((json.getString("success").equals("True"))) {
                                                    m.radius = Double.parseDouble(radius.getEditableText().toString());
                                                    m.title = name.getEditableText().toString();
                                                    MapsActivity.this.runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            for (eventDetails e : m.eventList) {
                                                                e.eventMarker.remove();
                                                                Log.d("DDelete deubg->", "Geofence deleted");
                                                            }
                                                            m.eventList = getEvents(m.id);

                                                            m.circle.setRadius(Double.parseDouble(radius.getEditableText().toString()) * 1609);
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
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                String params = "geofenceid=" + m.id;
                                                String url = "http://eventosdataapi-env.elasticbeanstalk.com/?selector=7&" + params;
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
                                                if ((json.getString("success").equals("True"))) {
                                                    MapsActivity.this.runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Log.d("DDelete delete ->", Integer.toString(m.eventList.size()));
                                                            for (eventDetails e : m.eventList) {
                                                                e.eventMarker.remove();
                                                                Log.d("DDelete deubg->", "Geofence deleted");
                                                            }
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
        Log.d("GeoFence debug->", "You can create geoFences here");
        //TODO setup-map with existing geoFences
    }

    private void setupExistingGeoFences() {
        new Thread (new Runnable(){
            @Override
            public void run(){
                try {
                    String url = "http://eventosdataapi-env.elasticbeanstalk.com/?userid="+mUserId+"&selector=4";
                    URL urlObj = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(false);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept-Charset", "UTF-8");
                    conn.setConnectTimeout(15000);
                    conn.connect();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }
                    JSONObject json = new JSONObject(sb.toString());
                    Log.d("GeoFenceCreate debug->",json.toString());
                    JSONArray arr = json.getJSONArray("geofences");
                    for(int loop=0; loop<arr.length(); loop++) {
                        String obj = arr.get(loop).toString();
                        obj = obj.replaceFirst("\\[\\{", "{");
                        obj = obj.replaceAll("\\}\\]", "}");
                        JSONObject obj1 = new JSONObject(obj);
                        String coord = obj1.getJSONObject("center").getString("coordinates");
                        coord = coord.replaceAll("\\[", "");
                        coord = coord.replaceAll("\\]", "");
                        String[] latlng = coord.split(",");
                        Log.d("Latitude is ->", latlng[0]);
                        Log.d("Longitutde  is ->", latlng[1]);
                        LatLng coordinate = new LatLng(Double.parseDouble(latlng[1]), Double.parseDouble(latlng[0]));
                        String geoFenceid = obj1.getString("geofenceid");
                        String name = obj1.getString("geofencename");
                        double radius = Double.parseDouble(obj1.getString("radius"));
                        drawGeoFence(coordinate, name, geoFenceid, radius);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

                address.setTextColor(Color.BLACK);
                radius.setTextColor(Color.BLACK);
                name.setTextColor(Color.BLACK);
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
                        setLocationFromStringAddress(address.getEditableText().toString(), Double.parseDouble(radius.getEditableText().toString()), name.getEditableText().toString());
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
            createNewGeoFenceRecord(name, radius, coordinate, 1);
            return;
        }
        catch (Exception e){}
    }

    public void createNewGeoFenceRecord(final String geoFenceName, final double geoFenceRadius, final LatLng geoFenceCenter, final int selector) {
        new Thread (new Runnable(){
            @Override
            public void run(){
                try {
                    if(selector == 1) {
                        String params = "userid=" + mUserId + "&geofencename=" + geoFenceName + "&radius=" + (double)geoFenceRadius + "&long=" + geoFenceCenter.longitude + "&lat=" + geoFenceCenter.latitude;
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
                        geoFenceIdWhenNew = json.getString("geofenceId");
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
                            CircleOptions circleOptions = new CircleOptions().center(geoFenceCenter).radius(geoFenceRadius * 1609).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
                            Circle circle = mMap.addCircle(circleOptions);
                            Log.d("CircleDebug", "Creating circle");
                            Marker m = mMap.addMarker(new MarkerOptions().position(geoFenceCenter).title(geoFenceName));
                            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(geoFenceCenter, 11.0f);
                            mMap.animateCamera(yourLocation);
                            markerHolder mMarkerHolder = new markerHolder();
                            mMarkerHolder.id = geoFenceIdWhenNew;
                            mMarkerHolder.title = geoFenceName;
                            mMarkerHolder.latitude = geoFenceCenter.latitude;
                            mMarkerHolder.longitude = geoFenceCenter.longitude;
                            mMarkerHolder.title = geoFenceName;
                            mMarkerHolder.radius = geoFenceRadius;
                            mMarkerHolder.circle = circle;
                            mMarkerHolder.marker = m;
                            mMarkerHolder.eventList = new ArrayList<eventDetails>();
                            mMarkerHolder.eventList = getEvents(mMarkerHolder.id);
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
    }

    private List<eventDetails> getEvents(final String geoFenceId) {

        final List<eventDetails> eventsList = new ArrayList<eventDetails>();
        new Thread (new Runnable(){
            @Override
            public void run(){
                try {
                    String url = "http://eventosdataapi-env.elasticbeanstalk.com/?geofenceid="+geoFenceId+"&selector=5";
                    URL urlObj = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(false);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept-Charset", "UTF-8");
                    conn.setConnectTimeout(15000);
                    conn.connect();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }
                    final JSONObject json = new JSONObject(sb.toString());
                    Log.d("Events json->",json.toString());
                    final JSONArray array = json.getJSONArray("events");
                    final String events = array.getJSONObject(0).getString("eventid");

                    Log.d("Event debug", events);
                    Log.d("Event debug", "The number of events are " + array.length());
                    Log.d("Event debug", "The whole of json is " + json.toString());
                    MapsActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                for (int loop = 0; loop < array.length(); loop++) {
                                    final int finalLoop = loop;
                                    final LatLng coordinate = new LatLng(Double.parseDouble(array.getJSONObject(finalLoop).getString("lat")), Double.parseDouble(array.getJSONObject(finalLoop).getString("long")));
                                    Marker m1 = mMap.addMarker(new MarkerOptions().position(coordinate));
                                    Log.d("DDelete debug->", "Marker added for id " + geoFenceId);
                                    m1.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.event_marker));
                                    m1.setTitle(array.getJSONObject(finalLoop).getString("eventid"));

                                    eventDetails newEventDetails = new eventDetails();
                                    newEventDetails.eventMarker = m1;
                                    newEventDetails.name = array.getJSONObject(finalLoop).getString("eventname");
                                    eventsList.add(newEventDetails);
                                    Log.d("Printing final id ->", "The id is " + array.getJSONObject(finalLoop).getString("eventid") + " and location is " + coordinate.latitude + " , " + coordinate.longitude);
                                    newEventDetails.description = array.getJSONObject(finalLoop).getString("description");
                                    newEventDetails.startTime = array.getJSONObject(finalLoop).getString("start_time");
                                    newEventDetails.endTime = array.getJSONObject(finalLoop).getString("end_time");
                                    newEventDetails.URL = array.getJSONObject(finalLoop).getString("eventwebsiteurl");
                                    newEventDetails.latitude = coordinate.latitude;
                                    newEventDetails.longitude = coordinate.longitude;
                                    eventDetailsMap.put(m1.getTitle(), newEventDetails);
                                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                        @Override
                                        public boolean onMarkerClick(Marker marker) {
                                            if(eventDetailsMap.containsKey(marker.getTitle())) {
                                                final eventDetails clicked = eventDetailsMap.get(marker.getTitle());

                                            LinearLayout alertBoxLayout = new LinearLayout(MapsActivity.this);
                                            alertBoxLayout.setOrientation(LinearLayout.VERTICAL);
                                            AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
                                            final TextView eventDescTitle = new TextView(getApplicationContext());
                                            final TextView eventDesc = new TextView(getApplicationContext());
                                            final TextView startTime = new TextView(getApplicationContext());
                                            final TextView endTime = new TextView(getApplicationContext());
                                            final TextView startTimeTitle = new TextView(getApplicationContext());
                                            final TextView endTimeTitle = new TextView(getApplicationContext());
                                            try {
                                                alert.setTitle(clicked.name); //Set Alert dialog title here
                                                eventDesc.setText(clicked.description);
                                                eventDescTitle.setText("Description");
                                                eventDesc.setTextColor(Color.BLACK);
                                                eventDescTitle.setTextColor(Color.BLACK);
                                                endTimeTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
                                                endTimeTitle.setText("End Time");
                                                endTimeTitle.setTextColor(Color.BLACK);
                                                endTime.setTextColor(Color.BLACK);
                                                endTime.setText(clicked.endTime);
                                                startTimeTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
                                                startTimeTitle.setText("Start Time");
                                                startTime.setTextColor(Color.BLACK);
                                                startTimeTitle.setTextColor(Color.BLACK);
                                                startTime.setText(clicked.startTime);
                                                startTimeTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
                                                eventDesc.setPadding(15, 0, 0, 0);
                                                eventDescTitle.setPadding(15, 0, 0, 0);
                                                startTime.setPadding(15, 0, 0, 0);
                                                startTimeTitle.setPadding(15, 0, 0, 0);
                                                endTime.setPadding(15, 0, 0, 0);
                                                endTimeTitle.setPadding(15, 0, 0, 0);
                                                alertBoxLayout.addView(eventDescTitle);
                                                alertBoxLayout.addView(eventDesc);
                                                alertBoxLayout.addView(startTimeTitle);
                                                alertBoxLayout.addView(startTime);
                                                alertBoxLayout.addView(endTimeTitle);
                                                alertBoxLayout.addView(endTime);
                                                alert.setView(alertBoxLayout);
                                                alert.setPositiveButton("GO TO URL", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        try {
                                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(clicked.URL));
                                                            startActivity(browserIntent);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                                alert.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        dialog.cancel();
                                                    }
                                                });
                                                alert.setNegativeButton("NAVIGATE", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                                                Uri.parse("http://maps.google.com/maps?saddr="+mMap.getMyLocation().getLatitude()+","+mMap.getMyLocation().getLongitude()+"&daddr=" + clicked.latitude + "," + clicked.longitude));
                                                        startActivity(intent);
                                                    }
                                                });
                                                AlertDialog alertDialog = alert.create();
                                                alertDialog.show();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            alert.setView(alertBoxLayout);
                                            return true;
                                        }
                                            return false;
                                    }
                                });

                            }
                            } catch (Exception e) {e.printStackTrace();}
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Log.d("DDelete getEvents->", Integer.toString(eventsList.size()));
        return eventsList;
    }

    private void drawGeoFence(final LatLng coordinate, final String name, final String geoFenceid, final double radius) {
        markerHolder existingMarkerHolder = new markerHolder();
        existingMarkerHolder.title = name;
        existingMarkerHolder.id = geoFenceid;
        existingMarkerHolder.radius = radius;
        Log.d("Coord debug latitude",Double.toString(coordinate.latitude));
        Log.d("Coord debug longitude",Double.toString(coordinate.longitude));
        existingMarkerHolder.latitude = coordinate.latitude;
        existingMarkerHolder.longitude = coordinate.longitude;
        MapsActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                int strokeColor = 0xffff0000; //red outline
                int shadeColor = 0x44ff0000; //opaque red fill
                CircleOptions circleOptions = new CircleOptions().center(coordinate).radius(radius * 1609).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
                Circle circle = mMap.addCircle(circleOptions);
                Log.d("CircleDebug", "Creating circle");
                Marker m = mMap.addMarker(new MarkerOptions().position(coordinate).title(name));
                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 11.0f);
                mMap.animateCamera(yourLocation);
                markerHolder mMarkerHolder = new markerHolder();
                mMarkerHolder.id = geoFenceid;
                mMarkerHolder.latitude = coordinate.latitude;
                mMarkerHolder.longitude = coordinate.longitude;
                mMarkerHolder.title = name;
                mMarkerHolder.radius = radius;
                mMarkerHolder.circle = circle;
                mMarkerHolder.marker = m;
                mMarkerHolder.eventList = new ArrayList<eventDetails>();
                mMarkerHolder.eventList = getEvents(mMarkerHolder.id);
                mMarkerList.add(mMarkerHolder);
            }
        });
    }

    public void sendNotification(String myGeoFenceName) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.cast_ic_notification_on)
                        .setContentTitle("Eventos")
                        .setContentText("Hey you are in "+ myGeoFenceName);
        Intent resultIntent = new Intent(this, MapsActivity.class);
// Because clicking the notification opens a new ("special") activity, there's
// no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        // Sets an ID for the notification
        int mNotificationId = 001;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
