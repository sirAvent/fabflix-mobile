package edu.uci.ics.fabflixmobile.ui.movielist;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.google.gson.Gson;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.sql.DriverManager.println;

public class Search extends AppCompatActivity{
    EditText query_input;
    Button searchButton;
    String queryStr;

    /*
      In Android, localhost is the address of the device or the emulator.
      To connect to your machine, you need to use the below IP address
     */
    private final String host = "192.168.254.137";
    private final String port = "8080";
    private final String domain = "2023_fall_cs122b_sus_war";
    private final String serverEndpoint = "/api/Search?";
    private final String baseURL = "http://" + host + ":" + port + "/" + domain + serverEndpoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        query_input = findViewById(R.id.query_input);
        searchButton = findViewById(R.id.searchBtn);

        searchButton.setOnClickListener(view -> search(query_input));
    }

    @SuppressLint("SetTextI18n")
    public void search(EditText query) {
        queryStr = String.valueOf(query.getText());
        String parameters = "title=" + query.getText();
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + parameters,
                response -> {
                    try {
                        JSONArray jsonArr = new JSONArray(response);
                        final ArrayList<Movie> movies = new ArrayList<>();

                        for (int i = 0; i < jsonArr.length(); ++i) {
                            JSONObject jsonObj = jsonArr.getJSONObject(i);
                            movies.add(new Movie(
                                    jsonObj.getString("title"),
                                    jsonObj.getString("movieId"),
                                    jsonObj.optString("year", ""), // If "year" is not present, it will default to an empty string
                                    jsonObj.optString("director", ""), // If "director" is not present, it will default to an empty string
                                    jsonObj.optString("genres", ""), // If "genres" is not present, it will default to an empty string
                                    jsonObj.optString("star_names", ""),
                                    jsonObj.optString("rating", "")
                            ));
                        }
                        Gson gson = new Gson();
                        String moviesJsonStr = gson.toJson(movies);
                        Intent MovieListPage = new Intent(Search.this, MovieListActivity.class);
                        MovieListPage.putExtra("movies", moviesJsonStr);
                        MovieListPage.putExtra("offset", 0);
                        MovieListPage.putExtra("query", queryStr);

                        startActivity(MovieListPage);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    // error
                    Log.d("search.error", error.toString());
                }) {
        };
        queue.add(searchRequest);
    }
}