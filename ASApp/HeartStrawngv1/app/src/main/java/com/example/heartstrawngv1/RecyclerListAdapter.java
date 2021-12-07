package com.example.heartstrawngv1;

import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Paul Burke (ipaulpro)
 */
public class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerListAdapter.ItemViewHolder>
        implements ItemTouchHelperAdapter {

    private LayoutInflater mInflater;
    private Context c;
    private ItemViewHolder h;
    private final OnStartDragListener mDragStartListener;
    private boolean editWorkout;
    private int workoutID;

    private List<android.text.Spanned> mItems = new ArrayList<android.text.Spanned>() {
    };

    public RecyclerListAdapter(Context context, List<android.text.Spanned> data, OnStartDragListener dragStartListener, boolean editWorkout, int workoutID) {
        this.mInflater = LayoutInflater.from(context);
        this.mItems = data;
        c = context;
        mDragStartListener = dragStartListener;
        this.editWorkout = editWorkout;
        this.workoutID = workoutID;
    }

    public List<android.text.Spanned> getmItems() {
        return this.mItems;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.workout_details_row, parent, false);
        h = new ItemViewHolder(view, editWorkout);
        return h;
    }


    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        holder.textView.setText(mItems.get(position));
        int pos = position;
        holder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });

        if (editWorkout) {
            holder.editView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // A dumb, overly complicated piece of code that extracts the name and set info
                    // from the draggable item's HTML, and passes it to an instance of setWorkoutDetails
                    // to be pre-filled there

                    String text = holder.textView.getText().toString();
                    String sp = Html.toHtml(mItems.get(pos));
                    String[] spSplit = (sp.split("<p dir=\"ltr\"><i><b>"));
                    ArrayList<String> vals = new ArrayList<>();
                    String[] splitText = text.split(" ");
                    String name;
                    if (spSplit.length <= 2) {
                        vals.add(((spSplit[1].split("&#8195;"))[1].trim()).replace("</b></i></p>", ""));
                        name = (spSplit[1].split("&#8195;")[0].trim()).replace("</b></i></p>", "");

                    }
                    else {
                        String[] spspSplit = spSplit[1].split(" &#8195; ");
                        name = spspSplit[0];
                        vals.add(spspSplit[1].replace("</b></i></p>", ""));
                        for (int i = 2; i < spSplit.length; i++) {
                            String replaced = ((spSplit[i].replace("&#8195;", "")).trim()).replace("</b></i></p>", "");
                            vals.add(replaced);
                        }
                    }

                    // TODO: find a way to pass repsBased/weightBased/distanceBased/timeBased info to set
                    // This sucks
                    // Instantiate the RequestQueue.
                    RequestQueue queue = Volley.newRequestQueue(view.getContext());
                    String url = "https://heartstrawng.azurewebsites.net/exercises";

                    // Request a string response from the provided URL.
                    JsonArrayRequest stringRequest = new JsonArrayRequest(Request.Method.GET, url,
                            null,
                            response -> {
                                // Do something with response
                                //mTextView.setText(response.toString());

                                // Process the JSON
                                try{

                                    boolean repsBased = false;
                                    boolean timeBased = false;
                                    boolean distanceBased = false;
                                    boolean weightUsed = false;
                                    int id = -1;
                                    // Loop through the array elements
                                    for(int i = 0; i < response.length(); i++){
                                        // Get current json object
                                        JSONObject exercise = response.getJSONObject(i);

                                        // Get the current exercise (json object) data
                                        String retrievedName = exercise.getString("name");
                                        if (retrievedName.equals(name)) {
                                            repsBased = exercise.getBoolean("repsBased");
                                            timeBased = exercise.getBoolean("timeBased");
                                            distanceBased = exercise.getBoolean("distanceBased");
                                            weightUsed = exercise.getBoolean("weightUsed");
                                            id = exercise.getInt("exerciseID");
                                            break;
                                        }
                                    }

                                    Exercise e = new Exercise(name, repsBased, timeBased, distanceBased, weightUsed, id);

                                    Intent setDetailsIntent = new Intent(view.getContext(), setWorkoutDetails.class);
                                    setDetailsIntent.putExtra("name", name);
                                    setDetailsIntent.putExtra("setInfo", vals);
                                    setDetailsIntent.putExtra("fullDetails", e);
                                    setDetailsIntent.putExtra("workoutID", workoutID);

                                    view.getContext().startActivity(setDetailsIntent);
                                }catch (JSONException e){
                                    e.printStackTrace();
                                }
                            },
                            error -> {
                                Log.d("ERROR", error.toString());
                            });

                    stringRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                    // Add the request to the RequestQueue.
                    queue.add(stringRequest);

                }
            });
        }

    }

    @Override
    public void onItemDismiss(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Log.d("RecyclerListAdapter", "ItemMoved: " + fromPosition + " " + toPosition);
        android.text.Spanned prev = mItems.remove(fromPosition);
        mItems.add(toPosition > fromPosition ? toPosition - 1 : toPosition, prev);
        Log.d("RecyclerListAdapter", "mItems: " + mItems);
        notifyItemMoved(fromPosition, toPosition);


    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        public final TextView textView;
        public final ImageView handleView;
        public final ImageView editView;

        public ItemViewHolder(View itemView, boolean editWorkout) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.workout_details_row);
            handleView = (ImageView) itemView.findViewById(R.id.handle);

            if (editWorkout) {
                editView = (ImageView) itemView.findViewById(R.id.edit_exercise);
            }
            else {
                editView = null;
                FrameLayout parent = (FrameLayout) itemView.findViewById(R.id.item);
                parent.removeView(itemView.findViewById(R.id.edit_exercise));
            }
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
            /*android.text.Spanned s = (android.text.Spanned)textView.getText();
            String sh = Html.toHtml(s);
            String[] ss = sh.split(":");
            textView.setText(ss[0]);*/
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }
}
