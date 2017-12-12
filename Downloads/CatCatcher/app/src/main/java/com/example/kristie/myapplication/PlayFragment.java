package com.example.kristie.myapplication;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.varunmishra.catcameraoverlay.CameraViewActivity;
import com.varunmishra.catcameraoverlay.Config;
import com.varunmishra.catcameraoverlay.OnCatPetListener;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayFragment extends Fragment implements OnMapReadyCallback, LocationListener, OnCatPetListener {

    private GoogleMap mMap;
    private MapView mapView;
    private Location myLocation;
    private LocationManager locationManager;
    private Marker selectedMarker;
    private RequestQueue queue;
    private View view;
    private SharedPreferences sp;

    private TextView catPrompt;
    private TextView petStatus;
    private TextView userTextView;
    private TextView catNumber;
    private Button petButton;
    private Button playButton;
    private Button trackButton;
    private Button stopButton;
    private ImageButton resetButton;
    private ImageView catPic;

    private boolean serviceBound = false;
    private boolean firstClick = false;
    private int radius;
    private NotificationService trackingService;
    public static final int DEFAULT_RADIUS = 500;
    private boolean gameStarted;
    private Map<String, List<String>> cats;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_play, container, false);

        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        else {
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5f, this);
        }

        queue = Volley.newRequestQueue(getActivity());
        selectedMarker = null;
        cats = new HashMap<>();

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        catPrompt = view.findViewById(R.id.catPrompt);
        petStatus = view.findViewById(R.id.petStatus);
        userTextView = view.findViewById(R.id.userTextView);
        catNumber = view.findViewById(R.id.catNumber);
        petButton = view.findViewById(R.id.petButton);
        playButton = view.findViewById(R.id.playButton);
        trackButton = view.findViewById(R.id.trackingButton);
        stopButton = view.findViewById(R.id.stopButton);
        resetButton = view.findViewById(R.id.resetButton);
        catPic = view.findViewById(R.id.catPic);

        sp = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        userTextView.setText(String.format("Hi, %s!", sp.getString("charName", "")));
        radius = sp.getInt("radius", DEFAULT_RADIUS);

        if (gameStarted) {
            updatePlayView();
        }

        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ((NotificationService.class).getName().equals(service.service.getClassName())) {
                Log.d("TAG", "service is already running");
                updatePlayView();
                gameStarted = true;
                trackButton.setEnabled(true);
                trackButton.setVisibility(View.GONE);
                stopButton.setVisibility(View.VISIBLE);

                Intent intent = new Intent(getActivity(), NotificationService.class);
                getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }
        }

        petButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                petCats();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePlayView();
                gameStarted = true;
            }
        });

        trackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedMarker != null) {
                    trackButton.setVisibility(View.GONE);
                    stopButton.setVisibility(View.VISIBLE);

                    Intent serviceIntent = new Intent(getActivity(), NotificationService.class);
                    int catId = Integer.valueOf(cats.get(selectedMarker.getTitle()).get(2));
                    serviceIntent.putExtra("CAT_ID", catId);
                    serviceIntent.putExtra("CAT_NAME", selectedMarker.getTitle());
                    getActivity().startService(serviceIntent);
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trackButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.GONE);
                getActivity().stopService(new Intent(getActivity(), NotificationService.class));
                if (serviceBound) {
                    getActivity().unbindService(mConnection);
                }
                serviceBound = false;
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartButtonClick();
                getActivity().stopService(new Intent(getActivity(), NotificationService.class));
                if (serviceBound) {
                    getActivity().unbindService(mConnection);
                }
                serviceBound = false;
            }
        });

        return view;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        if (myLocation != null) {
            LatLng myCoordinates = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myCoordinates));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15)); // set zoom to current location
        }
        else {
            LatLng dartmouth = new LatLng(43.7054, -72.2887); // default to zoom in on Dartmouth location
            mMap.moveCamera(CameraUpdateFactory.newLatLng(dartmouth));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        }

        drawMarkers();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                trackButton.setEnabled(true);
                if (gameStarted) {
                    if (selectedMarker == null || !selectedMarker.getTitle().equals(marker.getTitle())) {
                        getActivity().stopService(new Intent(getActivity(), NotificationService.class));
                        if (serviceBound) {
                            getActivity().unbindService(mConnection);
                        }
                        serviceBound = false;
                        trackButton.setVisibility(View.VISIBLE);
                        stopButton.setVisibility(View.GONE);
                        selectedCat(marker);
                    }
                }
                return true;
            }
        });
    }

    private void drawMarkers() {     // Get request to retrieve cat lat and lng and give markers
        String charName = sp.getString("charName", "");
        String password = sp.getString("password", "");

        String url = String.format("http://cs65.cs.dartmouth.edu/catlist.pl?name=%s&password=%s&mode=easy", charName, password);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            int catCount = 0;
                            cats.clear();
                            JSONArray catArray = new JSONArray(response);
                            for (int i = 0; i < catArray.length(); i++) { // if cat is within radius
                                JSONObject obj = catArray.getJSONObject(i);
                                double latitude = Double.valueOf(obj.getString("lat"));
                                double longitude = Double.valueOf(obj.getString("lng"));
                                if (distanceToCoordinate(latitude, longitude) < radius) { // get new marker
                                    LatLng catLocation = new LatLng(latitude, longitude);
                                    int color = R.drawable.greymarker;
                                    if (selectedMarker != null && obj.getString("name").equals(selectedMarker.getTitle())) {
                                        color = R.drawable.greenmarker;
                                    }
                                    Marker marker = mMap.addMarker(new MarkerOptions().position(catLocation)
                                            .title(obj.getString("name"))
                                            .icon(BitmapDescriptorFactory.fromResource(color)));
                                    List<String> data = new ArrayList<>(); // store the data from cs65 server
                                    data.add(obj.getString("petted"));
                                    data.add(obj.getString("picUrl"));
                                    data.add(obj.getString("catId"));
                                    cats.put(marker.getTitle(), data);

                                    // Restore app state based on tracking service info
                                    if (!firstClick && trackingService != null && trackingService.getCatName().equals(obj.getString("name"))) {
                                        firstClick = true;
                                        gameStarted = true;
                                        selectedCat(marker);
                                    }
                                }

                                if (obj.getString("petted").equals("true")) {
                                    catCount++;
                                }
                            }
                            if (catCount == catArray.length()) {
                                Intent intent = new Intent(getActivity(), SuccessActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                            catNumber.setText(String.format("You have petted %s cats out of %s", catCount, catArray.length()));
                        }
                        catch (JSONException e) {
                            Log.d("MY_TAG", "Error parsing response");
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        stringRequest.setShouldCache(false);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    private void petCats() {    // post to pet cats
        String charName = sp.getString("charName", "");
        String password = sp.getString("password", "");

        String url = String.format("http://cs65.cs.dartmouth.edu/pat.pl?name=%s&password=%s&catid=%s&lat=%s&lng=%s",
                charName, password, cats.get(selectedMarker.getTitle()).get(2),
                myLocation.getLatitude(), myLocation.getLongitude());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getString("status").equals("OK")) {
                                Toast.makeText(getActivity(), "You pet the cat!", Toast.LENGTH_SHORT).show();
                                petButton.setEnabled(false);
                                cameraPetActivity();
                                checkCatCount();
                            }
                            else if (obj.getString("status").equals("ERROR")) {
                                Toast.makeText(getActivity(), "You're too far away!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (JSONException e) {
                            Log.d("MY_TAG", "Error parsing response");
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        queue.add(stringRequest);
    }


    private void checkCatCount() {     // check to see if user has pet all available cats
        String charName = sp.getString("charName", "");
        String password = sp.getString("password", "");

        String url = String.format("http://cs65.cs.dartmouth.edu/catlist.pl?name=%s&password=%s&mode=easy", charName, password);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            int catCount = 0;
                            JSONArray catArray = new JSONArray(response);
                            for (int i = 0; i < catArray.length(); i++) {
                                JSONObject obj = catArray.getJSONObject(i);
                                if (obj.getString("petted").equals("false")) {
                                    catCount++;
                                }
                            }
                            if (catCount == 0) {
                                Intent intent = new Intent(getActivity(), SuccessActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        }
                        catch (JSONException e) {
                            Log.d("MY_TAG", "Error parsing server response");
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "Error connecting to server", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        queue.add(stringRequest);
    }

    private void selectedCat(Marker marker) {    // change panel when user clicks on a cat
        Marker newMarker = mMap.addMarker(new MarkerOptions().position(marker.getPosition())
                .title(marker.getTitle())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.greenmarker)));
        if (selectedMarker != null) { // check to display color
            mMap.addMarker(new MarkerOptions().position(selectedMarker.getPosition())
                    .title(selectedMarker.getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.greymarker)));
            selectedMarker.remove();
        }
        marker.remove();
        selectedMarker = newMarker;

        catPrompt.setText(marker.getTitle()); // change panel content
        petButton.setEnabled(!Boolean.valueOf(cats.get(marker.getTitle()).get(0)));

        double latitude = selectedMarker.getPosition().latitude;
        double longitude = selectedMarker.getPosition().longitude;
        int distance = distanceToCoordinate(latitude, longitude);
        petStatus.setVisibility(View.VISIBLE);
        if (distance != Integer.MAX_VALUE) {
            petStatus.setText(String.format("%s meters away", distance));
        }

        ImageRequest imageRequest = new ImageRequest(cats.get(marker.getTitle()).get(1), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                catPic.setImageBitmap(response);
            }
        },0,0, ImageView.ScaleType.CENTER_CROP,null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(imageRequest);
    }

    @Override
    public void onLocationChanged(Location location) {     // updates camera for continuous distance update
        myLocation = location;
        LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(newPoint));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

            try {
                mMap.clear();
                mMap.setMyLocationEnabled(true);
                drawMarkers();
            }
            catch (SecurityException e) {
            }
        }

        if (selectedMarker != null) {
            double latitude = selectedMarker.getPosition().latitude;
            double longitude = selectedMarker.getPosition().longitude;
            int distance = distanceToCoordinate(latitude, longitude);
            if (distance < radius) {
                petStatus.setVisibility(View.VISIBLE);
                if (distance != Integer.MAX_VALUE) {
                    petStatus.setText(String.format("%s meters away", distance));
                }
                else {
                    petStatus.setText("Distance unreachable");
                }
            }
            else {
                catPrompt.setText("Try to click the cats");
                petStatus.setVisibility(View.GONE);
                catPic.setImageResource(R.drawable.clickicon);
                petButton.setEnabled(false);
                selectedMarker = null;
            }
        }
    }

    private int distanceToCoordinate(double targetLatitude, double targetLongitude) {     // Retreive distance between two points
        if (myLocation == null) {
            return Integer.MAX_VALUE;
        }

        double myLatitude = myLocation.getLatitude();
        double myLongitude = myLocation.getLongitude();
        GeodeticCalculator calculator = new GeodeticCalculator();
        Ellipsoid reference = Ellipsoid.WGS84;
        GlobalPosition pointA = new GlobalPosition(myLatitude, myLongitude, 0.0);
        GlobalPosition userPos = new GlobalPosition(targetLatitude, targetLongitude, 0.0);
        double distance = calculator.calculateGeodeticCurve(reference, userPos, pointA).getEllipsoidalDistance();
        return (int) distance;
    }

    public void restartButtonClick() {
        String charName = sp.getString("charName", "");
        String password = sp.getString("password", "");

        String url = String.format("http://cs65.cs.dartmouth.edu/resetlist.pl?name=%s&password=%s", charName, password);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getString("status").equals("OK")) {
                                updatePlayView();
                            }
                            else if (obj.getString("status").equals("ERROR")) {
                                Toast.makeText(getActivity(), obj.getString("reason"), Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (JSONException e) {
                            Toast.makeText(getActivity(), "error parsing server response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "error connecting to server", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
    }

    private void updatePlayView() {     // Update play layout from welcome panel to actual game play panel
        LinearLayout welcomeLayout = view.findViewById(R.id.welcomeLayout);
        welcomeLayout.setVisibility(View.GONE);

        LinearLayout playLayout = view.findViewById(R.id.playLayout);
        playLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {     // Permissions for location
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.setMyLocationEnabled(true);
                locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5f, this);
            }
            catch (SecurityException e) {
            }
        }
    }

    public void cameraPetActivity() { // camera overlay call function
        Config.catName = selectedMarker.getTitle();
        Config.catLatitude = selectedMarker.getPosition().latitude;
        Config.catLongitude = selectedMarker.getPosition().longitude;
        Config.locDistanceRange = 5000;
        Intent intent = new Intent(getActivity(), CameraViewActivity.class);
        startActivity(intent);
    }

    private ServiceConnection mConnection = new ServiceConnection() { // bind service

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            NotificationService.MyBinder binder = (NotificationService.MyBinder) service;
            trackingService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onCatPet(String catName) {
        Toast.makeText(getActivity(), "You pet " + catName, Toast.LENGTH_SHORT).show();
        petButton.setEnabled(false);
        checkCatCount();

    }
}
