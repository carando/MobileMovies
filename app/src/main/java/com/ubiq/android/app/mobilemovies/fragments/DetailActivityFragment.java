package com.ubiq.android.app.mobilemovies.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.ubiq.android.app.mobilemovies.R;
import com.ubiq.android.app.mobilemovies.activities.MovieReviewsActivity;
import com.ubiq.android.app.mobilemovies.data.MovieDBHelper;
import com.ubiq.android.app.mobilemovies.data.MovieOToRMapper;
import com.ubiq.android.app.mobilemovies.model.Movie;
import com.ubiq.android.app.mobilemovies.model.MovieReview;
import com.ubiq.android.app.mobilemovies.model.MovieTrailer;
import com.ubiq.android.app.mobilemovies.model.PopularMovies;
import com.ubiq.android.app.mobilemovies.utils.FetchMovieDetailTask;
import com.ubiq.android.app.mobilemovies.utils.MovieReviewsAdapter;
import com.ubiq.android.app.mobilemovies.utils.MovieTrailerAdapter;
import com.ubiq.android.app.mobilemovies.utils.Utils;
import com.ubiq.android.app.mobilemovies.widget.ExpandableTextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * The fragment that displays the detailed movie view
 */
public class DetailActivityFragment extends Fragment {
    private final String LOG_TAG              = DetailActivityFragment.class.getSimpleName();


    private Movie mMovie;
    private int                        mMoviePosition = ListView.INVALID_POSITION;
    private ArrayAdapter<MovieTrailer> mMovieTrailerAdapter = null;
    private ArrayAdapter<MovieReview>  mMovieReviewAdapter  = null;


    private MovieDBHelper      mMovieDBHelper = null;

    private TextView             mMovieTitleLargeTextView;
    private TextView             mMovieYearTextView;
    private TextView             mRunningTimeTextView;
    private TextView             mMovieRatingTextView;
    private ExpandableTextView   mMovieDescriptionTextView;
    private ImageView            mMoviePosterImageView;
    private Button               mFavoriteButton;
    private TextView             mTrailerHeader;
    private ListView             mTrailerView;
    private FloatingActionButton mReviewsButton;

    private boolean viewsInitialized = false;


    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = null;
        try {
            rootView = inflater.inflate(R.layout.movie_detail_with_fab, container, false);
            initializeViews(rootView);

            Bundle arguments = getArguments();
            if (arguments != null) {
                // The index of the movie in the PopularMovies singleton holder
                // If not present, we will have problems
                mMoviePosition = arguments.getInt(Utils.MOVIE_KEY);
                // Log.v(LOG_TAG, "**onCreateView position = " + String.valueOf(moviePosition));

                mMovie = PopularMovies.getInstance().getMovie(mMoviePosition);
                // Log.v(LOG_TAG, "movie[" + String.valueOf(moviePosition) + "] = " + mMovie.toString());

                //** Initialize the detailed movie view
                //    set the movie title banner
                mMovieTitleLargeTextView.setText(mMovie.getTitle());
                //    set the release year
                mMovieYearTextView.setText(mMovie.getReleaseYear());
                //    set the running time (if available); if not, set in FetchMovieDetail.onPostExecute
                mRunningTimeTextView.setText(String.valueOf(mMovie.getRunningTime()) + " min");
                //    set the movie movie_rating
                mMovieRatingTextView.setText(mMovie.getRating() + "/" + Utils.MAXIMUM_MOVIE_RATING);
                //    set the (short) description
                mMovieDescriptionTextView.setText(mMovie.getOverview());

                // fill in the thumbnail picture of the movie poster
                try {
                    URL url = Utils.buildMovieImageURL(mMovie.getPosterPath());
                    Log.v(LOG_TAG, "Movie poster url " + url);
                    //Load the image from the URL into imageView
                    Picasso.with(getContext())
                            .load(url.toString())
                            .resize(Utils.IMAGE_SIZE_WIDTH, Utils.IMAGE_SIZE_HEIGHT)
                            .into(mMoviePosterImageView);
                }
                catch (MalformedURLException e) {
                    Log.e(LOG_TAG, e.toString());
                }

                //   create the favorite button
                mFavoriteButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        final String movieTitle = mMovie.getTitle();
                        long result = addToFavorites(mMovie);
                       // Log.v(LOG_TAG, "***adding favorite result " + String.valueOf(result));
                        String toastString;
                        if (result > 0) {
                            toastString = movieTitle + " Added to Favorites!";
                        }
                        else if (result == MovieOToRMapper.MOVIE_IN_FAVORITES) {
                            toastString = movieTitle + " Already in Favorites";
                        }
                        else {
                            toastString = movieTitle + " Couldn't be added to Favorites";
                        }
                        Toast toast = Toast.makeText(getActivity(),
                                                     toastString,
                                                     Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                // Reviews FloatingActionButton will display a message saying that you can go
                // to reviews
                mReviewsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"Displays Reviews",Toast.LENGTH_SHORT).show();
                    }
                });

                mReviewsButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Intent intent = new Intent(getActivity(), MovieReviewsActivity.class);
                        intent.putExtra (Utils.MOVIE_KEY, mMoviePosition);
                        startActivity(intent);
                        return true;
                    }
                });

                //     bind mMovieTrailerAdapter with empty trailers for now
                ArrayList < MovieTrailer > movieTrailers = new ArrayList<MovieTrailer>();
                mMovieTrailerAdapter   = new MovieTrailerAdapter(movieTrailers, getActivity());
                mTrailerView.setAdapter(mMovieTrailerAdapter);

                updateMovieDetail();
                showOrHide ();
            }
            return rootView;
        } catch (Exception e) {
            Log.e (LOG_TAG, "***ERROR: " + e.toString());
            e.printStackTrace();
        }
        return rootView;
    }

    @Override
    public void onPause() {
        Bundle arguments;
        super.onPause();
        arguments = new Bundle();
        arguments.putInt(Utils.MOVIE_KEY, mMoviePosition);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMoviePosition!= ListView.INVALID_POSITION) {
            outState.putInt (Utils.MOVIE_KEY, mMoviePosition);
        }
    }

    public MovieReviewsAdapter getMovieReviewAdapter() {
        return (MovieReviewsAdapter) mMovieReviewAdapter;
    }


    private long addToFavorites(Movie movie) {
        MovieOToRMapper mapper = MovieOToRMapper.getInstance(getContext());
        return mapper.insertMovie(movie);
    }


    private void initializeViews(View rootView) {
        if (viewsInitialized) return;
        mMovieTitleLargeTextView = (TextView) rootView.findViewById(R.id.movie_title_large);
        mMovieYearTextView = (TextView) rootView.findViewById(R.id.year);
        mRunningTimeTextView = (TextView) rootView.findViewById(R.id.runningtime);
        mMovieRatingTextView = (TextView) rootView.findViewById(R.id.rating);
        mMovieDescriptionTextView = (ExpandableTextView) rootView.findViewById(R.id.movie_description);
        mMoviePosterImageView = (ImageView) rootView.findViewById(R.id.detailImage);
        mFavoriteButton = (Button) rootView.findViewById(R.id.button_favorite);
        mTrailerHeader = (TextView)rootView.findViewById(R.id.trailer_heading);
        mTrailerView = (ListView) rootView.findViewById(R.id.listview_trailers);
        mReviewsButton = (FloatingActionButton)rootView.findViewById(R.id.reviews_button);

        viewsInitialized = true;
    }

    private void showOrHide () {

        // if there are no reviews, hide the reviews button
        mReviewsButton.setVisibility(View.VISIBLE);
        if (mMovie.getNumberOfReviews() == 0) mReviewsButton.setVisibility(View.INVISIBLE);

        // if this movie has already been "favorite-ed", hide the
        // Mark as Favorite button
        // Note that this needs to checked against the db
        mFavoriteButton.setVisibility(View.VISIBLE);
        if (mMovie.getFavorite() ||
                MovieOToRMapper.getInstance(getContext()).movieInFavorites(mMovie)) {
            mFavoriteButton.setVisibility(View.INVISIBLE);
            mMovie.setFavorite(true);
        }
        mTrailerHeader.setText ("Trailers");
        if (mMovie.getMovieTrailers().size() == 0) mTrailerHeader.setText ("No Trailers");
    }

    /**
     * Determines if movie data needs to be (re)loaded and if so, starts a thread
     * to access the data. Clears the trailer adapter and adds all the current trailers
     * stored into the adapter to update the view
     */
    private void updateMovieDetail() {
        if (!mMovie.isMovieDetailLoaded()) {
            FetchMovieDetailTask task = new FetchMovieDetailTask(getActivity());
            task.setMovie(mMovie);
            task.setMovieTrailerArrayAdapter(mMovieTrailerAdapter);
            task.execute();
        }
       mMovieTrailerAdapter.clear();
       mMovieTrailerAdapter.addAll(mMovie.getMovieTrailers());
    }



}
