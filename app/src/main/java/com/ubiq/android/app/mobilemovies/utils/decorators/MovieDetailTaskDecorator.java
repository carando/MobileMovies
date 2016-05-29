package com.ubiq.android.app.mobilemovies.utils.decorators;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ubiq.android.app.mobilemovies.R;
import com.ubiq.android.app.mobilemovies.data.MovieOToRMapper;
import com.ubiq.android.app.mobilemovies.model.Movie;
import com.ubiq.android.app.mobilemovies.model.MovieTrailer;
import com.ubiq.android.app.mobilemovies.utils.MovieTrailerAdapter;
import com.ubiq.android.app.mobilemovies.utils.decorators.Decorator;

/**
 * Created by T on 5/29/2016.
 */
public class MovieDetailTaskDecorator implements Decorator {


    private Activity            mInvokingActivity;
    private Movie               mMovie;
    private MovieTrailerAdapter mTrailerAdapter;

    public MovieDetailTaskDecorator (Activity invokingActivity,
                                     Movie movie,
                                     MovieTrailerAdapter trailerAdapter) {
        mInvokingActivity = invokingActivity;
        mMovie            = movie;
        mTrailerAdapter   = trailerAdapter;
    }

    public Activity getInvokingActivity() {
        return mInvokingActivity;
    }

    public MovieTrailerAdapter getTrailerAdapter() {
        return mTrailerAdapter;
    }


    public Movie getMovie() {
        return mMovie;
    }

    public void hideOrShowDetail () {

        TextView trailerHeader = (TextView)mInvokingActivity.findViewById(R.id.trailer_heading);
        trailerHeader.setText ("Trailers");
        if (mMovie.getMovieTrailers().size() == 0) trailerHeader.setText ("No Trailers");

        FloatingActionButton reviewsButton =
                (FloatingActionButton)mInvokingActivity.findViewById(R.id.reviews_button);
        reviewsButton.setVisibility(View.VISIBLE);
        if (mMovie.getMovieReviews().size() == 0) reviewsButton.setVisibility(View.INVISIBLE);

        // if this movie has already been "favorite-ed", hide the
        // Mark as Favorite button. This will also need to be checked
        // against the database, but we won't do that here

        Button mFavoriteButton = (Button)mInvokingActivity.findViewById(R.id.button_favorite);
        mFavoriteButton.setVisibility(View.VISIBLE);
        if (mMovie.getFavorite()) {
            mFavoriteButton.setVisibility(View.INVISIBLE);
        }
    }
}
