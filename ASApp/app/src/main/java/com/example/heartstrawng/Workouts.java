package com.example.heartstrawng;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class Workouts extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button addWorkoutView;
    private Button showWorkoutView;

    //private OnFragmentInteractionListener mListener;

    public Workouts() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Workouts.
     */
    // TODO: Rename and change types and number of parameters
    public static Workouts newInstance(String param1, String param2) {
        Workouts fragment = new Workouts();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_workouts, container, false);
        addWorkoutView = (Button) view.findViewById(R.id.add_workout_button);
        showWorkoutView = (Button) view.findViewById(R.id.show_workout_button);
        addWorkoutView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Intent addWorkoutIntent = new Intent(v.getContext(), AddWorkoutActivity.class);
                startActivity(addWorkoutIntent);
                /*URL serverURL = null;
                HttpsURLConnection conn = null;
                try {
                    serverURL = new URL("http", "www.google.com", "/post_workout/");
                    conn = (HttpsURLConnection)serverURL.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("User-Agent", "heart-strawng-v0.1");

                    // Server responded OK, do whatever with datas
                    if (conn.getResponseCode() == 200) {
                        InputStream responseBody = conn.getInputStream();
                        InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                        JsonReader jsonReader = new JsonReader(responseBodyReader);

                    }
                    else {

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }*/

            }
        });

        showWorkoutView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                /*URL serverURL = null;
                HttpsURLConnection conn = null;
                try {
                    serverURL = new URL("http", "www.google.com", "/get_workout/");
                    conn = (HttpsURLConnection)serverURL.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("User-Agent", "heart-strawng-v0.1");

                    // Server responded OK, do whatever with datas
                    if (conn.getResponseCode() == 200) {
                        InputStream responseBody = conn.getInputStream();
                        InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                        JsonReader jsonReader = new JsonReader(responseBodyReader);

                    }
                    else {

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }*/

            }
        });

        return view;
    }



    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    /*@Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

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
    /*public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
