package com.example.heartstrawngv1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private String firstName;
    private String lastName;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Gets the data sent from the previous view
        Bundle extras = getIntent().getExtras();

        int userID;
        if (extras != null) {
            firstName = extras.getString("firstName");
            lastName = extras.getString("lastName");
            username = extras.getString("username");
            userID = extras.getInt("userID");
        }
        else {
            firstName = "";
            lastName = "";
            username = "";
            userID = 1;
        }

        Toolbar toolbar = findViewById(R.id.title);
        toolbar.setTitle("");

        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        // to make the Navigation drawer icon always appear on the action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        NavigationView navView = findViewById(R.id.navigation_view);
        Context c = this.getApplicationContext();
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemID = item.getItemId();
                switch (itemID){
                    case R.id.nav_account:
                        Intent myAccountIntent = new Intent(c, MyAccount.class);
                        myAccountIntent.putExtra("firstName", firstName);
                        myAccountIntent.putExtra("lastName", lastName);
                        myAccountIntent.putExtra("username", username);
                        myAccountIntent.putExtra("userID", userID);
                        startActivityForResult(myAccountIntent, 1);
                        break;
                    case R.id.nav_logout:
                        finish();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        viewPager = findViewById(R.id.view_pager);

        // setting up the adapter
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        Fragment wFragment = new Workouts();
        wFragment.setArguments(extras);

        Fragment hFragment = new HeartRate();
        hFragment.setArguments(extras);

        // add the fragments
        viewPagerAdapter.add(wFragment, "Workouts");
        viewPagerAdapter.add(hFragment, "HeartRate");
        viewPagerAdapter.add(new WaterIntake(), "Water Intake");

        // Set the adapter
        viewPager.setAdapter(viewPagerAdapter);

        // The Page (fragment) titles will be displayed in the
        // tabLayout hence we need to  set the page viewer
        // we use the setupWithViewPager().
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    // override the onOptionsItemSelected()
    // function to implement
    // the item click listener callback
    // to open and close the navigation
    // drawer when the icon is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // add existing exercise
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getBooleanExtra("deletedAccount", false)) {
                    Intent deleted = new Intent();
                    deleted.putExtra("deletedAccount", true);
                    finish();
                    return;
                }
                firstName = data.getStringExtra("firstName");
                lastName = data.getStringExtra("lastName");

                if (!username.equals(data.getStringExtra("username"))) {
                    //Get saved username from shared preferences and update it to changed username
                    SharedPreferences settings = getApplicationContext().getSharedPreferences("SHARED_PREFS", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.remove("username");
                    editor.putString("username", data.getStringExtra("username"));
                    editor.apply();

                    username = data.getStringExtra("username");
                }
            }
        }
    }
}