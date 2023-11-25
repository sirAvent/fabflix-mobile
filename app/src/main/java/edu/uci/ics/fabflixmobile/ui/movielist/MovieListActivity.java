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
    int maxRecords;
    int offset;
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
        numResultsView = findViewById(R.id.numResults);
        // TODO: this should be retrieved from the backend server
        Bundle extras = getIntent().getExtras();
        offset = extras.getInt("offset");
        int pageNum = offset + 1;
        query = extras.getString("query");
        pageView.setText("Page " + Integer.toString(pageNum));
        String jsonStr = extras.getString("movies");
        Gson gson = new Gson();
        final ArrayList<Movie> movies = gson.fromJson(jsonStr, new TypeToken<ArrayList<Movie>>(){}.getType());
        if(movies.size() > 0) {
            maxRecords = extras.getInt("maxRecords");
            if(maxRecords == 1) {
                numResultsView.setText("Found " + maxRecords + " Result for '" + query + "'" );
            }
            else {
                numResultsView.setText("Found " + maxRecords + " Results for '" + query + "'" );
            }
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
            maxRecords = 0;
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
        if(offset >= 0) {
            // use the same network queue across our application
            final RequestQueue queue = NetworkManager.sharedManager(this).queue;
            String parameters = "title=" + query + "&limit=10&page=" + offset;

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
                            MovieListPage.putExtra("offset", offset);
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

    }
    public void next() {
        if(offset + 10 < maxRecords) {
            // use the same network queue across our application
            final RequestQueue queue = NetworkManager.sharedManager(this).queue;
            String parameters = "query=" + query + "&sortBy=title+ASC+rating+ASC&numRecords=10&firstRecord=" + Integer.toString(offset + 10);

            final StringRequest loginRequest = new StringRequest(
                    Request.Method.GET,
                    baseURL + parameters,
                    response -> {
                        try {
                            JSONArray jsonArr = new JSONArray(response);
                            final ArrayList<Movie> movies = new ArrayList<>();
                            int maxRecords = jsonArr.getJSONObject(0).getInt("max_records");
                            for ( int i = 0; i < jsonArr.length(); ++i ) {
                                JSONObject jsonObj = jsonArr.getJSONObject(i);
                                String rating = "";
                                if(jsonObj.getString("movie_rating").equals("null")) {
                                    rating = "0.0";
                                }
                                else if(jsonObj.getString("movie_rating") == null) {
                                    rating = "0.0";
                                }
                                else {
                                    rating = jsonObj.getString("movie_rating");
                                }
                                movies.add(new Movie(jsonObj.getString("movie_title"), jsonObj.getString("movie_id"), jsonObj.getString("movie_year"),
                                        jsonObj.getString("movie_director"), jsonObj.getString("movie_genres"), jsonObj.getString("movie_stars"), rating));
                            }
                            Gson gson = new Gson();
                            String moviesJsonStr = gson.toJson(movies);
                            Intent MovieListPage = new Intent(MovieListActivity.this, MovieListActivity.class);
                            MovieListPage.putExtra("movies", moviesJsonStr);
                            MovieListPage.putExtra("offset", offset+10);
                            MovieListPage.putExtra("maxRecords", maxRecords);
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
        }
        // important: queue.add is where the login request is actually sent

    }
}
