package com.example.kristie.myapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

public class HistorySimpleArrayAdapter extends ArrayAdapter<HistoryInformation> {

    private ArrayList<HistoryInformation> HistoryInformations;
    private final List<List<HistoryInformation>> attributes;
    private final Context context;
    int resource;
    private RequestQueue queue;


    public HistorySimpleArrayAdapter(Context context, int resource, ArrayList<HistoryInformation> HistoryInformations, List<List<HistoryInformation>> attributes) {
        super(context, resource, HistoryInformations);

        this.context = context;
        this.HistoryInformations = HistoryInformations;
        this.resource = resource;
        this.attributes = attributes;
        this.queue = Volley.newRequestQueue(context);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final textVariable holder;


        if (convertView == null){

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();

            convertView = inflater.inflate(resource, parent, false);

            holder = new textVariable();
            holder.textView1 = convertView.findViewById(R.id.catName);
            holder.textView2 = convertView.findViewById(R.id.catLat);
            holder.textView3 = convertView.findViewById(R.id.catLong);
            holder.imageView1 = convertView.findViewById(R.id.catIcon);
            holder.imageView2 = convertView.findViewById(R.id.catPetted);

            convertView.setTag(holder);
        }
        else {
            holder = (textVariable) convertView.getTag();
        }

        holder.textView1.setText(HistoryInformations.get(position).getCatName());
        holder.textView2.setText("Latitude: " + HistoryInformations.get(position).getCatLat());
        holder.textView3.setText("Longitude: " + HistoryInformations.get(position).getCatLong());

        ImageRequest imageRequest = new ImageRequest(HistoryInformations.get(position).getImage(), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                holder.imageView1.setImageBitmap(response);
            }
        },0,0, ImageView.ScaleType.CENTER_CROP,null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("MY_TAG", "error connecting to server");
            }
        });
        queue.add(imageRequest);

        if (HistoryInformations.get(position).getCatPetted().equals("true")) {
            holder.imageView2.setImageResource(R.drawable.greenmarker);
        }
        else{
            holder.imageView2.setImageResource(R.drawable.greymarker);
        }

        return convertView;
    }


    static class textVariable {
        TextView textView1;
        TextView textView2;
        TextView textView3;
        ImageView imageView1;
        ImageView imageView2;
    }

}
