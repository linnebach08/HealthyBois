package com.example.heartstrawngv1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final String[] username = new String[1];
        final String[] password = new String[1];

        Button newAccountBtn = findViewById(R.id.create_new_account_btn);
        Button logInBtn = findViewById(R.id.log_in_btn);
        EditText firstNameInput = findViewById(R.id.first_name_input);
        EditText lastNameInput = findViewById(R.id.last_name_input);
        EditText usernameInput = findViewById(R.id.username_Input);
        EditText passwordInput = findViewById(R.id.password_input);
        CheckBox rememberMe = findViewById(R.id.remember_me_checkbox);
        final boolean[] createNewAccount = {false, false, false};

        SharedPreferences settings = getApplicationContext().getSharedPreferences("SHARED_PREFS", 0);

        if (settings.contains("rememberMe")) {
            username[0] = settings.getString("username", "");
            password[0] = settings.getString("password", "");

            usernameInput.setText(username[0]);
            passwordInput.setText(password[0]);
            rememberMe.setChecked(true);

            logInBtn.setClickable(true);
        }
        else {
            logInBtn.setBackgroundColor(Color.GRAY);
            logInBtn.setClickable(false);
        }



        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals("") && createNewAccount[2] && !createNewAccount[0]) {
                    logInBtn.setClickable(true);
                    logInBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    logInBtn.setEnabled(true);
                }
                else if (!charSequence.toString().equals("") && !createNewAccount[0]) {
                    logInBtn.setClickable(false);
                    createNewAccount[1] = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals("") && createNewAccount[1] && !createNewAccount[0]) {
                    logInBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    logInBtn.setClickable(true);
                    logInBtn.setEnabled(true);
                }
                else if (!charSequence.toString().equals("") && !createNewAccount[0]) {
                    logInBtn.setClickable(false);
                    createNewAccount[2] = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (settings.contains("username")) {
                    if (!settings.getString("username", "").equals(usernameInput.getText().toString()) && rememberMe.isChecked()) {
                        SharedPreferences.Editor e = settings.edit();
                        e.remove("username");
                        e.putString("username", usernameInput.getText().toString());
                        e.remove("password");
                        e.putString("password", passwordInput.getText().toString());

                        e.apply();
                    }
                }
                username[0] = usernameInput.getText().toString();
                password[0] = passwordInput.getText().toString();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(passwordInput.getWindowToken(), 0);
                passwordInput.clearFocus();
                usernameInput.clearFocus();

                if (rememberMe.isChecked()) {
                    SharedPreferences.Editor editor = settings.edit();

                    if (!settings.contains("rememberMe")) {
                        editor.putBoolean("rememberMe", true);
                        editor.putString("username", username[0]);
                        editor.putString("password", password[0]);

                        editor.apply();
                    }
                }

                logUserIn(view, username[0], password[0]);

            }
        });

        passwordInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    if (createNewAccount[0]) {
                        passwordInput.setHint("Must be at least 5 characters");
                    }
                }
            }
        });

        // Has to be declared like this or IDE complains
        final int[] numClicked = {0};
        newAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numClicked[0]++;
                createNewAccount[0] = true;

                if (numClicked[0] == 3) {
                    Intent mainIntent = new Intent(view.getContext(), MainActivity.class);
                    try {
                        mainIntent.putExtra("userID", 1);
                        mainIntent.putExtra("firstName", "John");
                        mainIntent.putExtra("lastName", "Doe");
                        mainIntent.putExtra("username", "JohnDoe");
                        mainIntent.putExtra("newUser", false);
                    } catch (Exception e) {
                        Log.d("ERROR", "Error retrieving response info");
                    }
                    startActivity(mainIntent);
                    return;
                }

                if (newAccountBtn.getText().toString().equals("Finished")) {
                    passwordInput.clearFocus();
                    firstNameInput.clearFocus();
                    usernameInput.clearFocus();
                    lastNameInput.clearFocus();
                    String username = usernameInput.getText().toString();
                    String password = passwordInput.getText().toString();
                    String fName = firstNameInput.getText().toString();
                    String lName = lastNameInput.getText().toString();

                    if (password.length() < 5) {
                        passwordInput.setText("");
                        passwordInput.setHint("Must be at least 5 characters");
                        return;
                    }
                    if (fName.equals("")) {
                        firstNameInput.setText("");
                        firstNameInput.setHint("Please enter your first name");
                        return;
                    }
                    if (lName.equals("")) {
                        lastNameInput.setText("");
                        lastNameInput.setHint("Please enter your last name");
                        return;
                    }
                    if (username.equals("")) {
                        usernameInput.setText("");
                        usernameInput.setHint("Please create a username");
                        return;
                    }

                    RequestQueue queue = Volley.newRequestQueue(view.getContext());
                    String url = "https://heartstrawng.azurewebsites.net/user";

                    JSONObject body = new JSONObject();
                    try {
                        body.put("username", username);
                        body.put("firstName", fName);
                        body.put("lastName", lName);
                    } catch (Exception e) {
                        Log.d("ERROR", e.toString());
                    }


                    JsonObjectRequest r = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // TODO: Send request to database to log user in so I can get user info, then finish this activity,
                            // TODO: sending user data to main screen
                            Log.d("INFO","Info: " + response);
                            Intent mainIntent = new Intent(view.getContext(), MainActivity.class);
                            try {
                                mainIntent.putExtra("userID", response.getInt("userID"));
                                mainIntent.putExtra("firstName", response.getString("firstName"));
                                mainIntent.putExtra("lastName", response.getString("lastName"));
                                mainIntent.putExtra("username", response.getString("username"));
                                mainIntent.putExtra("newUser", true);
                            } catch (Exception e) {
                                Log.d("ERROR", "Error retrieving response info");
                            }
                            startActivity(mainIntent);
                            return;
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
                            String credentials = username + ":" + password;
                            String auth = "Basic "
                                    + Base64.encodeToString(credentials.getBytes(),
                                    Base64.NO_WRAP);
                            params.put("Authorization", auth);
                            return params;
                        }
                    };

                    queue.add(r);
                }
                ObjectAnimator animation = ObjectAnimator.ofFloat(newAccountBtn, "translationY", 500f);
                animation.setDuration(500);
                animation.start();

                ObjectAnimator animationTwo = ObjectAnimator.ofFloat(logInBtn, "translationY", 500f);
                animationTwo.setDuration(500);
                animationTwo.start();


                ObjectAnimator firstNameAnimator = ObjectAnimator.ofFloat(firstNameInput, "translationY", 900f);
                firstNameAnimator.setDuration(500);
                firstNameAnimator.start();


                ObjectAnimator lastNameAnimator = ObjectAnimator.ofFloat(lastNameInput, "translationY", 250f);
                lastNameAnimator.setDuration(500);
                lastNameAnimator.start();

                firstNameInput.setVisibility(View.VISIBLE);
                lastNameInput.setVisibility(View.VISIBLE);

                logInBtn.setBackgroundColor(Color.GRAY);
                logInBtn.setClickable(false);

                newAccountBtn.setText("Finished");



            }
        });
    }

    private void logUserIn(View view, String username, String password) {
        RequestQueue queue = Volley.newRequestQueue(view.getContext());
        String url = "https://heartstrawng.azurewebsites.net/user";

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.progress_layout);
        Button logInButton = findViewById(R.id.log_in_btn);
        Button newAccount = findViewById(R.id.create_new_account_btn);

        layout.setBackgroundColor(Color.WHITE);
        logInButton.setVisibility(View.INVISIBLE);
        newAccount.setVisibility(View.INVISIBLE);

        ProgressBar progressBar = new ProgressBar(view.getContext());
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(650, 1100, 100, 200);

        TextView loadingText = new TextView(view.getContext());
        loadingText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        loadingText.setTextSize(24);
        loadingText.setText("Logging in...");
        loadingText.setPadding(525, 1350, 100, 200);
        layout.addView(progressBar);
        layout.addView(loadingText);

        layout.bringToFront();

        JsonObjectRequest r = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("JSON", "JSON: " + response);
                Intent mainIntent = new Intent(view.getContext(), MainActivity.class);
                try {
                    mainIntent.putExtra("userID", response.getInt("userID"));
                    mainIntent.putExtra("firstName", response.getString("firstName"));
                    mainIntent.putExtra("lastName", response.getString("lastName"));
                    mainIntent.putExtra("username", response.getString("username"));
                    mainIntent.putExtra("newUser", false);
                } catch (Exception e) {
                    Log.d("ERROR", "Error retrieving response info");
                }
                ((ViewGroup) layout.getParent()).removeView(layout);
                logInButton.setVisibility(View.VISIBLE);
                newAccount.setVisibility(View.VISIBLE);

                startActivityForResult(mainIntent, 1);
                return;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR", error.toString());
                Toast errorToast = Toast.makeText(view.getContext(), "Error, please try again", Toast.LENGTH_LONG);
                errorToast.show();

            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError { ;
                HashMap<String, String> params = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(),
                        Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

        };
        r.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(r);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // add existing exercise
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getBooleanExtra("deletedAccount", false)) {
                    Toast deleted = Toast.makeText(this.getApplicationContext(), "Account Deleted", Toast.LENGTH_SHORT);
                    deleted.show();
                }
            }
        }
    }
}