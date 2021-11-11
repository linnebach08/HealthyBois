package com.example.heartstrawngv1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChangePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        FloatingActionButton fab = findViewById(R.id.change_password_back_btn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent intent = getIntent();
        int userID = intent.getIntExtra("userID", -1);

        EditText oldPasswordText = findViewById(R.id.old_password_edit);
        EditText newPasswordText = findViewById(R.id.new_password_edit);
        EditText confirmPasswordText = findViewById(R.id.confirm_password_edit);
        Button changePasswordBtn = findViewById(R.id.change_password_confirm_btn);

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                oldPasswordText.clearFocus();
                newPasswordText.clearFocus();
                confirmPasswordText.clearFocus();
                String oldPassword = oldPasswordText.getText().toString();
                String newPassword = newPasswordText.getText().toString();
                String confirmPassword = confirmPasswordText.getText().toString();

                if (newPassword.length() < 5) {
                    newPasswordText.setText("");
                    newPasswordText.setHint("Must be at least 5 characters");
                    return;
                }
                if (newPassword.equals(oldPassword)) {
                    Toast t = Toast.makeText(view.getContext(), "New password must not be the same as old password", Toast.LENGTH_LONG);
                    t.show();
                    return;
                }
                if (!newPassword.equals(confirmPassword)) {
                    Toast t = Toast.makeText(view.getContext(), "Confirmed password does not match", Toast.LENGTH_LONG);
                    t.show();
                    return;
                }

                RequestQueue queue = Volley.newRequestQueue(view.getContext());
                String url = "https://heartstrawng.azurewebsites.net/user/password/" + userID;

                JsonObjectRequest r = new JsonObjectRequest(Request.Method.PUT, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("RESPONSE", "Response: " + response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERROR", error.toString());
                    }
                }) {

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        String credentials = oldPassword + ":" + newPassword;
                        String auth = "Basic "
                                + Base64.encodeToString(credentials.getBytes(),
                                Base64.NO_WRAP);
                        params.put("Authorization", auth);
                        return params;
                    }
                };

                queue.add(r);
                finish();
            }
        });
    }
}