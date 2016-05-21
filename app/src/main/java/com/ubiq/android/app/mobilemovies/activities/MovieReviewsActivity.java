package com.ubiq.android.app.mobilemovies.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.ubiq.android.app.mobilemovies.R;
import com.ubiq.android.app.mobilemovies.model.Movie;
import com.ubiq.android.app.mobilemovies.model.MovieReview;
import com.ubiq.android.app.mobilemovies.model.PopularMovies;
import com.ubiq.android.app.mobilemovies.utils.MovieReviewsAdapter;
import com.ubiq.android.app.mobilemovies.utils.Utils;

import java.util.ArrayList;
import java.util.List;


public class MovieReviewsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_reviews_with_fab);

        int moviePosition = getIntent().getIntExtra(Utils.MOVIE_KEY,0);

        Movie movie = PopularMovies.getInstance().getMovie(moviePosition);
        String movieTitle         = movie.getTitle();
        List<MovieReview> reviews =  movie.getMovieReviews();

        MovieReviewsAdapter movieReviewsAdapter   = new MovieReviewsAdapter(new ArrayList<MovieReview>(),
                this);
        TextView textView = (TextView)findViewById(R.id.reviews_title_textview);
        textView.setText ("Reviews for " + movieTitle);
        setTitle("Reviews for " + movieTitle);
        ListView reviewView = (ListView)findViewById(R.id.reviews_list_view);
        reviewView.setAdapter(movieReviewsAdapter);
        movieReviewsAdapter.addAll(reviews);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_reviews, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
