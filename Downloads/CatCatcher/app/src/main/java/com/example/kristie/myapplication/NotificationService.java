package com.example.kristie.myapplication;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationService extends Service {

    private LocationManager locationManager = null;
    private SharedPreferences sp;
    private RequestQueue queue;
    private int catId;
    private String catName;

    private class LocationListener implements android.location.LocationListener {

        Location lastLocation;

        private LocationListener(String provider) {
            lastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            getTrackingInfo(location.getLatitude(), location.getLongitude());

            String trackingPref = sp.getString("Tracking", "Vibrate");
            if (trackingPref.equals("Vibrate")) {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    vibrator.vibrate(400);
                }
            }
            else if (trackingPref.equals("Sound")) {
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.look);
                mp.start();
            }

            lastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        private void getTrackingInfo(double latitude, double longitude) { // track info from server and send to cats
            String charName = sp.getString("charName", "");
            String password = sp.getString("password", "");

            String url = String.format("http://cs65.cs.dartmouth.edu/track.pl?name=%s&password=%s&catid=%s&lat=%s&lng=%s",
                    charName, password, catId, latitude, longitude);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject obj = new JSONObject(response);
                                if (obj.getString("status").equals("OK")) {
                                    double dist = Double.valueOf(obj.getString("distance"));
                                    double roundedDist = ((int) dist * 100)/100.0;
                                    String message = catName + " is " + roundedDist + " meters away";
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            }
                            catch (JSONException e) {
                                Log.d("MY_TAG", "error parsing server response");
                            }
                        }
                    },

                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("MY_TAG", "error connecting to server");
                        }
                    }
            );
            queue.add(stringRequest);
        }
    }

    LocationListener locationListener = new LocationListener(LocationManager.NETWORK_PROVIDER);

    enum Status { RUNNING, IDLE };
    Status st = Status.IDLE;
    IBinder binder = new MyBinder();

    public class MyBinder extends Binder { // bind to service
        public NotificationService getService() {
            return NotificationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null && intent.getAction().equals("STOP")) {
            stopSelf();
        }
        else if( st == Status.RUNNING ){
            Log.d("MY_TAG", "service already running");
            return START_NOT_STICKY;
        }
        else {
            st = Status.RUNNING;
            if (intent.hasExtra("CAT_ID")) { // start notification action
                catId = intent.getIntExtra("CAT_ID", 0);
                catName = intent.getStringExtra("CAT_NAME");

                Intent stopIntent = new Intent(getApplicationContext(), NotificationService.class);
                stopIntent.setAction("STOP");
                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, stopIntent, 0);
                Notification.Action action = new Notification.Action.Builder(null, "Stop", pendingIntent).build();

                NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("Cat Tracking Service Running")
                        .setContentText("Currently tracking " + catName)
                        .setSmallIcon(R.drawable.greenmarker)
                        .addAction(action)
                        .build();

                nm.notify(1, notification);
            } else {
                catId = 0;
            }
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        initializeLocationManager();
        queue = Volley.newRequestQueue(getApplication());
        sp = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5f, locationListener);
        }
        catch (java.lang.SecurityException e) {
            Log.d("MY_TAG", "failed to initialize location manager");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MY_TAG", "service is being destroyed");

        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(1);

        if (locationManager != null) {
            try {
                locationManager.removeUpdates(locationListener);
            }
            catch (Exception e) {
                Log.d("MY_TAG", "failed to remove location listener");
            }
        }
    }

    private void initializeLocationManager() {
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public String getCatName() {
        return catName;
    }
}
