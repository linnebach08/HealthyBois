package com.example.heartstrawngv1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ScheduleWorkout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_workout);
        createNotificationChannel();

        FloatingActionButton fab = findViewById(R.id.schedule_workout_back_btn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        CalendarView calendar = findViewById(R.id.schedule_workout_calendar);
        TextView scheduledText = findViewById(R.id.scheduled_workout_text);
        TimePicker timePicker = findViewById(R.id.schedule_time_picker);
        Button doneBtn = findViewById(R.id.schedule_workout_done_btn);

        Bundle extras = getIntent().getExtras();
        String workoutName;
        int workoutID;
        int userID;
        try {
            workoutName = extras.getString("workoutName");
            workoutID = extras.getInt("workoutID");
            userID = extras.getInt("userID");
        } catch (Exception e) {
            workoutName = "";
            workoutID = -1;
            userID = -1;
        }

        long date = calendar.getDate();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String strDate = dateFormat.format(new Date(date));
        String[] splitDate = strDate.split("-");
        String defaultString = workoutName + " scheduled for " + getMonth(Integer.parseInt(splitDate[1]) - 1) + " " + splitDate[2] + " at";
        scheduledText.setText(defaultString);

        String finalWorkoutName = workoutName;

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(Calendar.YEAR, year);
                calendar1.set(Calendar.MONTH, month);
                calendar1.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                long milliTime = calendar1.getTimeInMillis();

                calendar.setDate(milliTime);
                String s = finalWorkoutName + " scheduled for " + getMonth(month) + " " + dayOfMonth + " at";
                scheduledText.setText(s);
            }
        });

        int finalUserID = userID;
        int finalWorkoutID = workoutID;
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                long lSelectedDate = calendar.getDate();

                Log.d("HOUR", "hour is: " + timePicker.getHour());
                Log.d("MINUTE", "minute is: " + timePicker.getMinute());

                int hours = timePicker.getHour() * 60 * 60 * 1000;
                int minutes = timePicker.getMinute() * 60 * 1000;
                Log.d("CONVHOUR", "hour is: " + hours);
                Log.d("CONVMINUTE", "minute is: " + minutes);
                long finalDate = lSelectedDate + hours + minutes;
                Log.d("FINALDATE", "Its: " + lSelectedDate);
                Date dSelectedDate = new Date(lSelectedDate);
                Log.d("DATEIS", "Its: " + dSelectedDate);
                //String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
                String pattern = "yyyy-MM-dd HH:mm";

                //DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
                //LocalDateTime dSelectedDate = LocalDateTime.parse()

                SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.getDefault());
                String formattedDate = formatter.format(dSelectedDate);

                long currTime = System.currentTimeMillis();
                long timer = finalDate - currTime;
                Log.d("TIMER", "Its " + timer);
                if (timer <= 0) {
                    Toast.makeText(ScheduleWorkout.this, "Cannot schedule workout for the past", Toast.LENGTH_LONG).show();

                }

                RequestQueue queue = Volley.newRequestQueue(view.getContext());
                String postUrl = "https://heartstrawng.azurewebsites.net/workout/scheduled";
                JSONObject o = new JSONObject();
                try {
                    o.put("workoutID", finalWorkoutID);
                    o.put("date", formattedDate);

                } catch (JSONException e) {
                    Log.d("JSONERR", e.toString());
                }
                Log.d("BODY", "Body is " + o);
                // Request a string response from the provided URL.
                JsonObjectRequest scheduleWorkoutRequest = new JsonObjectRequest(Request.Method.POST, postUrl,
                        o,
                        response -> {
                            Intent intent = new Intent(ScheduleWorkout.this, ReminderBroadcast.class);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(ScheduleWorkout.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timer, pendingIntent);
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("scheduled", true);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }, error -> {
                    Log.d("ERROR", o.toString());

                });

                scheduleWorkoutRequest.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                Toast scheduling = Toast.makeText(view.getContext(), "Scheduling workout...", Toast.LENGTH_LONG);
                scheduling.show();
                // Add the request to the RequestQueue.
                queue.add(scheduleWorkoutRequest);
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "HeartStrawngReminderChannel";
            String description = "Channel for HeartStrawng Reminder";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyHeartStrawng", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String getMonth(int month) {
        switch(month) {
            case 0:
                return "January";
            case 1:
                return "February";
            case 2:
                return "March";
            case 3:
                return "April";
            case 4:
                return "May";
            case 5:
                return "June";
            case 6:
                return "July";
            case 7:
                return "August";
            case 8:
                return "September";
            case 9:
                return "October";
            case 10:
                return "November";
            case 11:
                return "December";

        }
        return "";
    }
}