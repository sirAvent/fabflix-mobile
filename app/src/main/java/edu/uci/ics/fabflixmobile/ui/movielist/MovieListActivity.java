package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MovieListActivity extends AppCompatActivity {

    TextView pageView;
    TextView noResultsView;
    TextView numResultsView;
    int pageNumber;
    Button nextButton;
    Button prevButton;
    String query;

    private final String host = "192.168.254.137";
    private final String port = "8080";
    private final String domain = "2023_fall_cs122b_sus_war";
    private final String serverEndpoint = "/api/Search?";
    private final String baseURL = "http://" + host + ":" + port + "/" + domain + serverEndpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        pageView = findViewById(R.id.page);
        // TODO: this should be retrieved from the backend server
        Bundle extras = getIntent().getExtras();
        pageNumber = extras.getInt("pageNumber");
        query = extras.getString("query");
        pageView.setText("Page " + Integer.toString(pageNumber));
        String jsonStr = extras.getString("movies");
        Gson gson = new Gson();
        final ArrayList<Movie> movies = gson.fromJson(jsonStr, new TypeToken<ArrayList<Movie>>(){}.getType());
        if(!movies.isEmpty()) {
            MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
            ListView listView = findViewById(R.id.list);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                Movie movie = movies.get(position);
                Intent SingleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
                SingleMoviePage.putExtra("movieId", movie.getId());
                startActivity(SingleMoviePage);
            });
        }
        else {
            numResultsView.setVisibility(View.GONE);
            noResultsView = findViewById(R.id.noResults);
            noResultsView.setText("There are no results for the query '" + query + "'" );
        }
        nextButton = findViewById(R.id.next);
        prevButton = findViewById(R.id.prev);
        nextButton.setOnClickListener(view -> next());
        prevButton.setOnClickListener(view -> prev());
    }
    @SuppressLint("SetTextI18n")
    public void prev() {
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        String parameters = "title=" + query + "&sorting=titleRatingAA&limit=10&page=" + Integer.toString(pageNumber-1);

        // request type is GET
        final StringRequest movieRequest = new StringRequest(
                Request.Method.GET,
                baseURL + parameters,
                response -> {
                    try {
                        JSONArray jsonArr = new JSONArray(response);
                        final ArrayList<Movie> movies = new ArrayList<>();
                        for ( int i = 0; i < jsonArr.length(); ++i ) {
                            JSONObject jsonObj = jsonArr.getJSONObject(i);
                            movies.add(new Movie(jsonObj.getString("title"), jsonObj.getString("movieId"), jsonObj.getString("year"),
                                    jsonObj.getString("director"), jsonObj.getString("genres"), jsonObj.getString("star_names"), jsonObj.getString("rating")));
                        }
                        Gson gson = new Gson();
                        String moviesJsonStr = gson.toJson(movies);
                        Intent MovieListPage = new Intent(MovieListActivity.this, MovieListActivity.class);
                        MovieListPage.putExtra("movies", moviesJsonStr);
                        MovieListPage.putExtra("pageNumber", pageNumber-1);
                        MovieListPage.putExtra("query", query);
                        startActivity(MovieListPage);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    // error
                    Log.d("movie.error", error.toString());
                }) {
        };
        queue.add(movieRequest);

    }
    public void next() {
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        String parameters = "title=" + query + "&sorting=titleRatingAA&limit=10&page=" + Integer.toString(pageNumber+1);
        Log.d("param", parameters);

        final StringRequest loginRequest = new StringRequest(
                Request.Method.GET,
                baseURL + parameters,
                response -> {
                    try {
                        JSONArray jsonArr = new JSONArray(response);

                        final ArrayList<Movie> movies = new ArrayList<>();
                        for ( int i = 0; i < jsonArr.length(); ++i ) {
                            JSONObject jsonObj = jsonArr.getJSONObject(i);
                            movies.add(new Movie(jsonObj.getString("title"), jsonObj.getString("movieId"), jsonObj.getString("year"),
                                    jsonObj.getString("director"), jsonObj.getString("genres"), jsonObj.getString("star_names"), jsonObj.getString("rating")));
                        }
                        Gson gson = new Gson();
                        String moviesJsonStr = gson.toJson(movies);
                        Intent MovieListPage = new Intent(MovieListActivity.this, MovieListActivity.class);
                        MovieListPage.putExtra("movies", moviesJsonStr);
                        MovieListPage.putExtra("pageNumber", pageNumber+1);
                        MovieListPage.putExtra("query", query);
                        startActivity(MovieListPage);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                }) {
        };
        queue.add(loginRequest);
        // on login submit and next button click
        // important: queue.add is where the login request is actually sent

    }
}
