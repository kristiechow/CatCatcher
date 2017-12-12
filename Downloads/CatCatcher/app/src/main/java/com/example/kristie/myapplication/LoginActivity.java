package com.example.kristie.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A login screen that offers login via username/password.
 */

public class LoginActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            SharedPreferences sp = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            if (sp.contains("charName") && sp.contains("password")) { // if user has already logged in
                Intent intent = new Intent(getApplicationContext(), tabbedActivity.class);
                startActivity(intent);
                finish();
            }

            Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
            mEmailSignInButton.setOnClickListener(new View.OnClickListener() { // login button
                @Override
                public void onClick(View view) {
                    userLogin(view);
                }
            });

            Button mybutton = (Button) findViewById(R.id.newAcccount);
            mybutton.setOnClickListener(new View.OnClickListener() { // create new account button
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                    LoginActivity.this.startActivity(myIntent);

                }
            });
        }

        // Login method to submit get request to cs65 server
        public void userLogin(View view) {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            EditText et1 = findViewById(R.id.email);
            EditText et2 = findViewById(R.id.password);
            String charName = et1.getText().toString();
            String password = et2.getText().toString();

            // Make sure there is content in both fields
            if (charName.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Invalid Username", Toast.LENGTH_SHORT).show();
                return;
            }
            else if (password.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Invalid password", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = String.format("http://cs65.cs.dartmouth.edu/profile.pl?name=%s&password=%s", charName, password); // fill url with user input

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject obj = new JSONObject(response);
                                if (!obj.has("error")) {
                                    SharedPreferences.Editor editor = getSharedPreferences("myPrefs", Context.MODE_PRIVATE).edit();
                                    editor.putString("charName", obj.getString("name")); // shared preferences get request
                                    editor.putString("password", obj.getString("password"));
                                    if (obj.has("fullName")) {
                                        editor.putString("Name", obj.getString("fullName"));
                                    }
                                    if (obj.has("radius")) {
                                        editor.putInt("Radius", obj.getInt("radius"));
                                    }
                                    if (obj.has("tracking")) {
                                        editor.putString("Tracking", obj.getString("tracking"));
                                    }
                                    editor.apply();

                                    // Starts tabs activity if successful
                                    Intent intent = new Intent(getApplicationContext(), tabbedActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Incorrect credentials", Toast.LENGTH_SHORT).show();
                                }
                            }
                            catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },

                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "Connection error", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            queue.add(stringRequest);
        }

}

