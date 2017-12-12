package com.example.kristie.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.graphics.Bitmap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    Handler dl;
    RequestQueue queue;

    private EditText charName;
    private EditText fullName;
    private EditText password;
    private EditText radiusText;
    private TextView tvAvailable;
    private Button btnChangeToClear;
    private JSONObject json;
    private String availability;
    private static Button saveButton;
    private static Button updateSaveButton;

    private Button takePictureButton;
    private ImageView imageView;
    private Uri file;
    private Bitmap imageBitmap;

    public static String SHARED_PREF = "shared_pref"; // for saving to external
    public static Editable pwd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        charName = (EditText) findViewById(R.id.editText);
        fullName = (EditText) findViewById(R.id.editText2);
        password = (EditText) findViewById(R.id.editText4);
        radiusText = (EditText) findViewById(R.id.radiusEditText);
        tvAvailable = findViewById(R.id.tvAvailable);
        btnChangeToClear = (Button) findViewById(R.id.button5); // clear button
        takePictureButton = (Button) findViewById(R.id.button_image);
        imageView = (ImageView) findViewById(R.id.imageview);
        saveButton = (Button) findViewById(R.id.button6);
        updateSaveButton = (Button) findViewById(R.id.saveButton);
        dl = new Handler();
        queue = Volley.newRequestQueue(this);

        int[] viewArray = {R.id.editText, R.id.editText2, R.id.editText4}; // all fields that trigger change
        int size = viewArray.length;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            takePictureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        for (int i = 0; i < size; i++) {
            final EditText edtvalue = (EditText) findViewById(viewArray[i]);

            edtvalue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) { // if clear, then already have an account
                    if (s.toString().trim().length() > 0) {
                        btnChangeToClear.setText("  Clear  ");
                    } else {
                        btnChangeToClear.setText("  I Already Have An Account  ");
                    }
                }

            });
        }

        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    if ((charName.getText().length() > 0) && (fullName.getText().length() > 0) && (password.getText().length() > 0) && (tvAvailable.getText() == "Available!")) {
                        onSaveClickedSP(view);

                        // Making a "toast" informing the user the data is saved
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.toast_save_text),
                                Toast.LENGTH_LONG).show();
                    } else {
                        // Making a "toast" informing the user that there are errors saving
                        Toast.makeText(getApplicationContext(),
                                "Save unsuccessful. Check name availability and incomplete fields.",
                                Toast.LENGTH_LONG).show();
                    }
                }
                catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error saving data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (!(SHARED_PREF.isEmpty())) {
            onLoadClickedSP();
        }

        updateSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    onSaveClicked(view);
                }
                catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error saving data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!(hasFocus)) {
                    pwd = password.getText();
                    showDialog(view);
                }
            }
        });

        charName.setOnFocusChangeListener( new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        checkNameAvailability();
                    }});
    }

    public void onSaveClickedSP(View v) throws JSONException { // save data when for when button is clicked
        EditText ed2 = findViewById(R.id.radiusEditText);

        try {
            SharedPreferences sp = getSharedPreferences("myPrefs", 0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("charName", charName.getText().toString());
            editor.putString("fullName", fullName.getText().toString());
            editor.putString("password", password.getText().toString());


            editor.apply();

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("name", charName.getText().toString());
            jsonRequest.put("fullName", fullName.getText().toString());
            jsonRequest.put("password", password.getText().toString());
            jsonRequest.put("radius", Integer.parseInt(ed2.getText().toString()));
            userDataPost(jsonRequest);

        }
        catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Error saving data", Toast.LENGTH_SHORT).show();
        }
    }


        public void onSaveClicked(View v) throws JSONException {

            final EditText ed1 = findViewById(R.id.displayname);

            RadioGroup radioGroup = findViewById(R.id.radioGroup);
            RadioButton radioButton = findViewById(radioGroup.getCheckedRadioButtonId());

            SharedPreferences sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String charName = sharedPrefs.getString("charName", "");
            String password = sharedPrefs.getString("password", "");

            JSONObject obj = new JSONObject();
            obj.put("name", charName);
            obj.put("password", password);
            obj.put("fullName", ed1.getText().toString());
            obj.put("radius", Integer.parseInt(radiusText.getText().toString()));
            obj.put("tracking", radioButton.getText().toString());

            if (!ed1.getText().toString().isEmpty() && !radiusText.getText().toString().isEmpty()) {
                updateUserInfo(obj, ed1.getText().toString(), Integer.parseInt(radiusText.getText().toString()), radioButton.getText().toString());
            }
        }

    private void updateUserInfo(JSONObject obj, final String fullName, final int radius, final String tracking) {
        String url = "http://cs65.cs.dartmouth.edu/profile.pl";
        JsonObjectRequest request = new JsonObjectRequest(url, obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("OK")) {
                        Toast.makeText(getApplicationContext(), "Info successfully updated", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit();
                        editor.putString("fullName", fullName);
                        editor.putInt("radius", radius);
                        editor.putString("tracking", tracking);
                        editor.apply();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "User info contains errors", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "error parsing server response", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "error connecting to server", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(request);
    }

        public void onLoadClickedSP() { // set on load
            SharedPreferences sp = getSharedPreferences("myPrefs", 0);
            charName.setText(sp.getString("charName",""));
            fullName.setText(sp.getString("fullName",""));
            password.setText(sp.getString("password",""));

        }

        protected void showDialog(View view) { // popup functionality for password confirmation
            PasswordDialog passworddlg = new PasswordDialog();
            passworddlg.show(getFragmentManager(), "pass");
        }


    public void clear(View view) { // clear all fields
        charName.setText("");
        fullName.setText("");
        password.setText("");
        imageView.setImageResource(android.R.color.transparent); // clear image
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takePictureButton.setEnabled(true);
            }
        }
    }

    public void takePicture(View view) { // image capture functionality

        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // intent to open camera
        file = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file); // intent to put image

        startActivityForResult(intent, 100);
    }

    private static File getOutputMediaFile(){ // put to external storage
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraDemo");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                imageView.setImageURI(file); // set profile picture
                beginCrop(file);
            }
            else if (requestCode == Crop.REQUEST_CROP) { //crop functionality
                handleCrop(resultCode, data);
                File imgfile = new File(file.getPath());
                if (imgfile.exists()) {
                    imgfile.delete();
                }
            }
        }
    }

    private void beginCrop(Uri source) { // start crop activity using library
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) { // set cropped version of image to UI
        if (resultCode == RESULT_OK) {
            imageView.setImageURI(Crop.getOutput(result));
            imageBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        }
        else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) { // change image to a new bitmap
        super.onRestoreInstanceState(savedInstanceState);
        imageBitmap = savedInstanceState.getParcelable("IMG");
        imageView.setImageBitmap(imageBitmap); // to recreate images destroyed by flips
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) { // supporting horizontal image functionality
        super.onSaveInstanceState(outState);
        outState.putParcelable("IMG", imageBitmap);
    }

    protected void checkNameAvailability() {

        new Thread(new Runnable() {

            String res = null; // closed over by the post()-ed run()

            @Override
            public void run() {
                try {
                    URL url = new URL("https://cs65.cs.dartmouth.edu/nametest.pl?name=" + charName.getText().toString());
                    URLConnection urlConnection = url.openConnection();
                    InputStream in = urlConnection.getInputStream();
                    res = copyInputStreamToString(in);
                    json = new JSONObject(res);
                    availability = json.getString("avail");
                }
                catch( Exception e){
                    Log.d("THREAD", e.toString());
                }
                dl.post(new Runnable() {
                    @SuppressLint("NewApi")
                    @Override
                    public void run() {
                        if( Objects.equals(availability, "false"))
                            tvAvailable.setText("Unavailable"); // display to UI that username already exists
                        else
                            tvAvailable.setText("Available!");
                    }
                });
                Log.d("NET", (res!=null) ? res : "null response");
            }
        }).start();
    }

    private String copyInputStreamToString(InputStream in) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }
        return total.toString();
    }

    public void userDataPost(JSONObject obj) { // post user data to cs65 server

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        String url = "http://cs65.cs.dartmouth.edu/profile.pl";
        JsonObjectRequest request = new JsonObjectRequest(url, obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("OK")) {
                        Intent myIntent = new Intent(MainActivity.this, tabbedActivity.class); // go to tabs
                        MainActivity.this.startActivity(myIntent);
                        Toast.makeText(getApplicationContext(), "Uploading your data", Toast.LENGTH_SHORT).show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1000);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Error in data", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Connection error", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(request);
    }


}