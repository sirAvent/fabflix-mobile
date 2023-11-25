package edu.uci.ics.fabflixmobile.ui.movielist;

import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SingleMovieActivity extends AppCompatActivity {
    TextView titleView;
    TextView ratingView;
    TextView directorView;
    TextView genresView;
    TextView starsView;

    String movieId;

    private final String host = "192.168.254.137";
    private final String port = "8080";
    private final String domain = "2023_fall_cs122b_sus_war";
    private final String serverEndpoint = "/api/single-movie";
    private final String baseURL = "http://" + host + ":" + port + "/" + domain + serverEndpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_movie);
        titleView = findViewById(R.id.single_title);
        ratingView = findViewById(R.id.single_rating);
        directorView = findViewById(R.id.single_director);
        genresView = findViewById(R.id.single_genres);
        starsView = findViewById(R.id.single_stars);

        Bundle extras = getIntent().getExtras();
        movieId = extras.getString("movieId");

        String parameters = "?id=" + movieId;

        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest ajaxRequest = new StringRequest(
                Request.Method.GET,
                baseURL + parameters,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObj = jsonArray.getJSONObject(0);
                        titleView.setText(jsonObj.getString("title") + " (" + jsonObj.getString("year") + ")");
                        ratingView.setText(jsonObj.getString("rating"));
                        directorView.setText("Director: " + jsonObj.getString("director"));
                        String[] parsed = jsonObj.getString("genres").split(", ");
                        genresView.setText("Genres: " + (jsonObj.getString("genres")));
                        starsView.setText("Stars: " + (jsonObj.getString("star_names")));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    // error
                    Log.d("singlemovie.error", error.toString());
                }) {
        };
        queue.add(ajaxRequest);
    }
}