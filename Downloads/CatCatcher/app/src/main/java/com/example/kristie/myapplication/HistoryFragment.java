package com.example.kristie.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * History fragment
 */

public class HistoryFragment extends Fragment{

    private SharedPreferences sp;
    private RequestQueue queue;
    ListView listView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history_listview, container, false);

        sp = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        queue = Volley.newRequestQueue(getActivity());

        listView = view.findViewById(R.id.listView);
        getInformation();

        return view;
    }

    private void getInformation(){
        String charName = sp.getString("charName", "");
        String password = sp.getString("password", "");

        String url = String.format("http://cs65.cs.dartmouth.edu/catlist.pl?name=%s&password=%s&mode=easy", charName, password);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray catArray = new JSONArray(response);
                            List<List<HistoryInformation>> catAttributes = new ArrayList<>();
                            List<HistoryInformation> cats = new ArrayList<>();

                            for (int i = 0; i < catArray.length(); i++) {
                                JSONObject obj = catArray.getJSONObject(i);
                                HistoryInformation historyInformation = new HistoryInformation("","","","","");
                                historyInformation.setCatName(obj.getString("name"));
                                historyInformation.setCatLat(obj.getString("lat"));
                                historyInformation.setCatLong(obj.getString("lng"));
                                historyInformation.setCatPetted(obj.getString("petted"));
                                historyInformation.setImage(obj.getString("picUrl"));
                                catAttributes.add(historyInformation);
                                cats.add(historyInformation);

                            }

                            listView.setAdapter(new HistorySimpleArrayAdapter(getActivity(), R.layout.fragment_history, (ArrayList<HistoryInformation>) cats, catAttributes));

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
}


