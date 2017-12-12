package com.example.kristie.myapplication;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Settings class in charge of changing user info and preferences
 */

public class SettingsFragment extends Fragment {

    private static Button logout;

    private TextView tv1;
    private EditText ed1;
    private EditText ed2;
    private ImageView iv1;
    private RadioButton rd1;
    private RadioButton rd2;
    private RadioButton rd3;
    private static Button updateSaveButton;


    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Load saved values from shared preferences
        SharedPreferences sharedPref = getActivity().getSharedPreferences("myPrefs", 0);
        String tx1 = sharedPref.getString("charName", "");
        String tx2 = sharedPref.getString("fullName", "");
        String tx3 = String.valueOf(sharedPref.getInt("Radius", PlayFragment.DEFAULT_RADIUS));
        String tx4 = sharedPref.getString("Tracking", "Vibrate");

        // Assign all views
        tv1 = view.findViewById(R.id.textView);
        ed1 = view.findViewById(R.id.displayname);
        ed2 = view.findViewById(R.id.radiusEditText);
        iv1 = view.findViewById(R.id.imageView);
        rd1 = view.findViewById(R.id.radioNone);
        rd2 = view.findViewById(R.id.radioVibrate);
        rd3 = view.findViewById(R.id.radioSound);


        // Set text values
        tv1.setText(String.format("Hello, %s!", tx1));
        ed1.setText(tx2);
        ed2.setText(tx3);


        switch (tx4) {
            case "None":
                rd1.setChecked(true);
                break;
            case "Vibrate":
                rd2.setChecked(true);
                break;
            case "Sound":
                rd3.setChecked(true);
                break;
        }


        return view;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }
}
