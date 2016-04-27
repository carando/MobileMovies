package com.ubiq.android.app.mobilemovies.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import com.ubiq.android.app.mobilemovies.R;
import com.ubiq.android.app.mobilemovies.data.MovieDBHelper;
import com.ubiq.android.app.mobilemovies.data.MovieOToRMapper;
import com.ubiq.android.app.mobilemovies.model.Movie;
import com.ubiq.android.app.mobilemovies.model.MovieReview;
import com.ubiq.android.app.mobilemovies.model.MovieTrailer;
import com.ubiq.android.app.mobilemovies.model.PopularMovies;
import com.ubiq.android.app.mobilemovies.utils.FetchMovieDetailTask;
import com.ubiq.android.app.mobilemovies.utils.MovieReviewsAdapter;
import com.ubiq.android.app.mobilemovies.utils.Utils;
import com.ubiq.android.app.mobilemovies.widget.ExpandableTextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The fragment that displays the detailed movie view
 */
public class DetailActivityFragment extends Fragment {
    private final String LOG_TAG              = DetailActivityFragment.class.getSimpleName();
    private final String TRAILER_FRAGMENT_TAG = "TF";
    private final String REVIEW_FRAMENT_TAG   = "RF";

    private Movie mMovie;
    private int                        mMoviePosition = ListView.INVALID_POSITION;
    private ArrayAdapter<MovieTrailer> mMovieTrailerAdapter = null;
    private ArrayAdapter<MovieReview>  mMovieReviewAdapter  = null;

    private ListView      mTrailerView   = null;
    //   private ListView      mReviewView    = null;
    private MovieDBHelper mMovieDBHelper = null;

    private TextView           mMovieTitleLargeTextView;
    private TextView           mMovieYearTextView;
    private TextView           mRunningTimeTextView;
    private TextView           mMovieRatingTextView;
    private ExpandableTextView mMovieDescriptionTextView;
    private ImageView          mMoviePosterImageView;
    private Button             mFavoriteButton;

    private Switch mSwitch;

//    private TextView  mMovieTrailersTextView;
//    private TextView  mMovieReviewsTextView;

    private boolean viewsInitialized = false;


    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.movie_detail, container, false);
        initializeViews(rootView);

        Bundle arguments = getArguments();
        if (arguments != null) {

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
                    String toastString = "Whoops! Nothing happened";
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

            mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                  @Override
                  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                      if(isChecked){
                          Log.v(LOG_TAG, "Switch is currently ON");
                          Fragment reviewsFragment = new MovieReviewsFragment();
                          switchFragment(reviewsFragment, REVIEW_FRAMENT_TAG,TRAILER_FRAGMENT_TAG);
                      }else{
                          Log.v(LOG_TAG, "Switch is currently OFF");
                          Fragment trailersFragment = new MovieTrailersFragment();
                          switchFragment(trailersFragment, TRAILER_FRAGMENT_TAG,REVIEW_FRAMENT_TAG);

                      }
                   }
            }
            );

            //     bind mMovieTrailerAdapter with empty trailers for now
            ArrayList < MovieTrailer > movieTrailers = new ArrayList<MovieTrailer>();
//          alternate implementation
//            mMovieTrailerAdapter   = new MovieTrailerAdapter(movieTrailers);
            mMovieTrailerAdapter   = new
                    com.ubiq.android.app.mobilemovies.utils.MovieTrailerAdapter(movieTrailers, getActivity());
            //     bind mMovieTrailerAdapter with empty trailers for now
            ArrayList<MovieReview> movieReviews = new ArrayList<MovieReview>();
            mMovieReviewAdapter  = new MovieReviewsAdapter(movieReviews, getActivity());

//            Intent intent = getActivity().getIntent();
//            intent.putExtra (Utils.MOVIE_KEY, mMoviePosition);

            updateMovieDetail();

            switchFragment (new MovieTrailersFragment(),TRAILER_FRAGMENT_TAG,REVIEW_FRAMENT_TAG);
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

    private long addToFavorites(Movie movie) {
        mMovieDBHelper = new MovieDBHelper(getContext());
        mMovieDBHelper.getWritableDatabase();
        MovieOToRMapper mapper = MovieOToRMapper.getInstance(mMovieDBHelper);
        long result = mapper.insertMovie(movie);
        return result;
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
        mSwitch  = (Switch)rootView.findViewById(R.id.trailer_or_review_switch);
        viewsInitialized = true;
    }

    /**
     * Determines if movie data needs to be (re)loaded and if so, starts a thread
     * to access the data. Clears the adapter and adds all the current movies stored
     * into the adapter to update the view
     */
    private void updateMovieDetail() {
        if (!mMovie.isMovieDetailLoaded()) {
            FetchMovieDetailTask task = new FetchMovieDetailTask(getActivity());
            task.setMovie(mMovie);
            task.setMovieTrailerArrayAdapter(mMovieTrailerAdapter);
            task.setMovieReviewArrayAdapter(mMovieReviewAdapter);
            task.execute();
        }
       mMovieTrailerAdapter.clear();
       mMovieTrailerAdapter.addAll(mMovie.getMovieTrailers());
       mMovieReviewAdapter.clear();
       mMovieReviewAdapter.addAll(mMovie.getMovieReviews());
    }

    /**
     * The array adapter that handles the display of the movie trailers
     */
    protected class MovieTrailerAdapter extends ArrayAdapter<MovieTrailer> {
        private final String TAG = MovieTrailerAdapter.class.getSimpleName();

        public MovieTrailerAdapter(ArrayList<MovieTrailer> trailers) {
            super(getActivity(), android.R.layout.simple_list_item_1, trailers);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View       row = convertView;
            ViewHolder holder;
            //           Log.v(TAG, "mMovie = " + mMovie);
           // Log.v(TAG, "position: " + String.valueOf(position));
            List<MovieTrailer> trailers = mMovie.getMovieTrailers();
           // Log.v(TAG, "***all trailers " + trailers);
            MovieTrailer trailer = trailers.get(position);

            ImageButton trailerButton;
            // if no view was passed in, inflate one
            if (null == row) {
                row = getActivity().getLayoutInflater().
                        inflate(R.layout.movie_trailer_layout, parent, false);
                holder = new ViewHolder();
                trailerButton = (ImageButton) row.findViewById(R.id.trailer_button);
                holder.trailerButton = trailerButton;
                holder.trailerName = (TextView) row.findViewById(R.id.trailer_name);
                row.setTag(holder);
            }
            else {
                holder = (ViewHolder) row.getTag();
            }
            //Load the trailer name and the image of the play button
            holder.trailerName.setText(trailer.getName());
           // Log.v(TAG, "holder.trailerName.text = " + holder.trailerName.getText());
            holder.trailerButton.setImageResource(R.mipmap.ic_play);

            // Set up the trailer button for activating trailer play
            trailerButton = (ImageButton) row.findViewById(R.id.trailer_button);
            trailerButton.setOnClickListener(new View.OnClickListener() {

                // When the trailer button is clicked, this code will play the trailer
                // as a Youtube video. We can't handle other trailer types just yet.
                @Override
                public void onClick(View v) {
                    playTrailer(v);
                }
            });

            return row;
        }

        /**
         * ViewHolder is just a tuple for holding the trailer button and trailer name
         */
        protected class ViewHolder {
            ImageButton trailerButton;
            TextView    trailerName;
        }

        protected void playTrailer(View v) {
            int          position = mTrailerView.getPositionForView(v);
            MovieTrailer trailer  = mMovie.getMovieTrailers().get(position);
            Toast toast = Toast.makeText(getActivity(),
                                         "Play trailer [" + position + "] " + trailer.getName() +
                                                 " for movie " + mMovie.getTitle(),
                                         Toast.LENGTH_SHORT);
            toast.show();
            String youtubeId = trailer.getYoutubeSource();
            Log.v(LOG_TAG,
                  "Launching YouTube video for trailer " + Utils.YOU_TUBE_URL + youtubeId +
                          " named " + trailer.getName());

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                                           Uri.parse(Utils.YOU_TUBE_APP_INTENT + youtubeId));
                startActivity(intent);
            }
            catch (ActivityNotFoundException ex) {
                Log.v(LOG_TAG, ex.toString());
                Log.v(LOG_TAG, Uri.parse(Utils.YOU_TUBE_URL + youtubeId).toString());
                Intent intent = new Intent(Intent.ACTION_VIEW,
                                           Uri.parse(Utils.YOU_TUBE_URL + youtubeId));
                startActivity(intent);
            }
        }
    }

    public MovieReviewsAdapter getMovieReviewAdapter() {
        return (MovieReviewsAdapter) mMovieReviewAdapter;
    }

    public ArrayAdapter<MovieTrailer> getMovieTrailerAdapter() {
        return mMovieTrailerAdapter;
    }

    public ListView getTrailerView () {
        return mTrailerView;
    }

    public void setTrailerView (ListView trailerView) {
        mTrailerView = trailerView;
    }

    protected void switchFragment (Fragment newFragment, String newFragmentTag, String oldFragmentTag) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        Fragment oldFragment = fm.findFragmentByTag(oldFragmentTag);
        if (oldFragment != null) transaction.remove(oldFragment);
        transaction.add(R.id.container_trailer_or_reviews, newFragment,newFragmentTag);
        transaction.commit();
    }
}
