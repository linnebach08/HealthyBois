package com.example.heartstrawng;

import android.os.AsyncTask;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

class getWorkouts extends AsyncTask<Void, Void, Void>{

    /* The code that executes the work to be done on a separate thread */
    @Override
    protected Void doInBackground(Void... voids) {
        URL serverURL = null;
        HttpsURLConnection conn = null;
        try {
            serverURL = new URL("http", "www.google.com", "/get_workoutss/");
            conn = (HttpsURLConnection)serverURL.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "heart-strawng-v0.1");

            // Server responded OK, do whatever with datas
            if (conn.getResponseCode() == 200) {
                InputStream responseBody = conn.getInputStream();
                InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
            }
            else {

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
