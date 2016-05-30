package com.ubiq.android.app.mobilemovies.utils.decorators;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ubiq.android.app.mobilemovies.R;
import com.ubiq.android.app.mobilemovies.model.Movie;
import com.ubiq.android.app.mobilemovies.utils.MovieTrailerAdapter;

/**
 * Decorator that passes critical references and handles show/hide evens
 */
public class MovieDetailTaskDecorator implements Decorator {

    private Activity            mInvokingActivity;
    private View                mView;
    private Movie               mMovie;
    private MovieTrailerAdapter mTrailerAdapter;

    public MovieDetailTaskDecorator(Activity invokingActivity,
                                    View view, Movie movie,
                                    MovieTrailerAdapter trailerAdapter) {
        mInvokingActivity = invokingActivity;
        mView             = view;
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
        TextView             trailerHeader;
        FloatingActionButton reviewsButton;
        ImageButton           favoriteButton;

        if (null != mView) {
            trailerHeader = (TextView)mView.findViewById(R.id.trailer_heading);
            reviewsButton = (FloatingActionButton)mView.findViewById(R.id.reviews_button);
            favoriteButton = (ImageButton)mView.findViewById(R.id.button_favorite);
        } else {
            trailerHeader = (TextView)mInvokingActivity.findViewById(R.id.trailer_heading);
            reviewsButton =
                    (FloatingActionButton)mInvokingActivity.findViewById(R.id.reviews_button);
            favoriteButton = (ImageButton)mInvokingActivity.findViewById(R.id.button_favorite);
        }


        trailerHeader.setText ("Trailers");
        if (mMovie.getMovieTrailers().size() == 0) trailerHeader.setText ("No Trailers");

        reviewsButton.setVisibility(View.VISIBLE);
        if (mMovie.getMovieReviews().size() == 0) reviewsButton.setVisibility(View.INVISIBLE);

        // if this movie has already been "favorite-ed", hide the
        // Mark as Favorite button. This will also need to be checked
        // against the database, but we won't do that here

//        favoriteButton.setVisibility(View.VISIBLE);
//        if (mMovie.isFavorite()) {
//            favoriteButton.setVisibility(View.INVISIBLE);
//        }

 //       favoriteButton.setImageResource (R.drawable.ic_star_black_24dp);
        if (mMovie.isFavorite()) {
            favoriteButton.setImageResource(R.drawable.ic_star_border_black_24dp);
        }
    }
}
