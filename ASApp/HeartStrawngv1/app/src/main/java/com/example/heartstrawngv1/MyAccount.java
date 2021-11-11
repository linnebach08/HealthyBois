package com.example.heartstrawngv1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MyAccount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        Intent intent = getIntent();
        String firstName = intent.getStringExtra("firstName");
        String lastName = intent.getStringExtra("lastName");
        String username = intent.getStringExtra("username");
        int userID = intent.getIntExtra("userID", -1);

        EditText fNameText = findViewById(R.id.firstname_text);
        EditText lNameText = findViewById(R.id.lastname_text);
        EditText usernameText = findViewById(R.id.username_text);
        ImageButton fNameBtn = findViewById(R.id.firstname_edit_btn);
        ImageButton lNameBtn = findViewById(R.id.lastname_edit_btn);
        ImageButton usernameBtn = findViewById(R.id.username_edit_btn);
        Button changePasswordBtn = findViewById(R.id.change_password_btn);
        Button finishedBtn = findViewById(R.id.my_account_finished_btn);
        Button deleteAccountBtn = findViewById(R.id.delete_account_btn);

        fNameText.setText(firstName);
        lNameText.setText(lastName);
        usernameText.setText(username);

        fNameText.setInputType(InputType.TYPE_NULL);
        lNameText.setInputType(InputType.TYPE_NULL);
        usernameText.setInputType(InputType.TYPE_NULL);

        fNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fNameText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                fNameText.setEnabled(true);
                fNameText.requestFocus();
                fNameText.setSelection(fNameText.getText().length());
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(fNameText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        lNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lNameText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                lNameText.setEnabled(true);
                lNameText.requestFocus();
                lNameText.setSelection(lNameText.getText().length());
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(lNameText, InputMethodManager.SHOW_IMPLICIT);            }
        });

        usernameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usernameText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                usernameText.setEnabled(true);
                usernameText.requestFocus();
                usernameText.setSelection(usernameText.getText().length());
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(usernameText, InputMethodManager.SHOW_IMPLICIT);            }
        });

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent changePasswordIntent = new Intent(view.getContext(), ChangePasswordActivity.class);
                changePasswordIntent.putExtra("userID", userID);
                startActivity(changePasswordIntent);
            }
        });

        deleteAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Delete Workout?");

                LayoutInflater popupInflater = getLayoutInflater();
                View dialogLayout = popupInflater.inflate(R.layout.delete_workout_popup, null);
                builder.setView(dialogLayout);

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Instantiate the RequestQueue.
                        RequestQueue queue = Volley.newRequestQueue(view.getContext());
                        String url = "https://heartstrawng.azurewebsites.net/user/" + userID;

                        StringRequest deleteUser = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                SharedPreferences settings = getApplicationContext().getSharedPreferences("SHARED_PREFS", 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.clear();
                                editor.apply();

                                //TODO: pass data to main activity that tells it to return to login screen
                                //TODO: maybe popup saying account deleted

                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("deletedAccount", true);

                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("ERROR", error.toString());
                            }
                        });

                        deleteUser.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                        queue.add(deleteUser);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.show();
            }
        });

        finishedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(view.getContext());
                String url = "https://heartstrawng.azurewebsites.net/user/" + userID;

                JSONObject postData = new JSONObject();
                try {
                    if (!usernameText.getText().toString().equals(username)) {
                        postData.put("username", usernameText.getText().toString());
                    }
                    if (!fNameText.getText().toString().equals(firstName)) {
                        postData.put("firstName", fNameText.getText().toString());
                    }
                    if (!lNameText.getText().toString().equals(lastName)) {
                        postData.put("lastName", lNameText.getText().toString());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (postData.length() == 0) {
                    finish();
                }

                final String requestBody = postData.toString();

                /************************/
                StringRequest sr = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("firstName", fNameText.getText().toString());
                        resultIntent.putExtra("lastName", lNameText.getText().toString());
                        resultIntent.putExtra("username", usernameText.getText().toString());
                        resultIntent.putExtra("deletedAccount", false);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", error.toString());
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return requestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            Log.d("ERROR", uee.toString());
                            return null;
                        }
                    }
                };
                /***********************/

                sr.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                // Add the request to the RequestQueue.
                queue.add(sr);
            }
        });


    }
}