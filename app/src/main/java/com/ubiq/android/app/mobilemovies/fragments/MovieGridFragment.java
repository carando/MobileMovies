package com.ubiq.android.app.mobilemovies.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import com.ubiq.android.app.mobilemovies.R;
import com.ubiq.android.app.mobilemovies.data.MovieOToRMapper;
import com.ubiq.android.app.mobilemovies.model.PopularMovies;
import com.ubiq.android.app.mobilemovies.utils.FetchMoviesTask;
import com.ubiq.android.app.mobilemovies.utils.MovieAdapter;
import com.ubiq.android.app.mobilemovies.utils.Utils;
import com.ubiq.android.app.mobilemovies.activities.DetailActivity;
import com.ubiq.android.app.mobilemovies.activities.SettingsActivity;
import com.ubiq.android.app.mobilemovies.model.Movie;

import java.util.ArrayList;
import java.util.List;

import static com.ubiq.android.app.mobilemovies.model.PopularMovies.getInstance;

/**
 * The fragment that holds poster views of the movies retrieved from Moviedb.org
 */
public class MovieGridFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = MovieGridFragment.class.getSimpleName();

    private static final String MOST_POPULAR_MOVIES_TITLE = "Most Popular Movies";
    private static final String HIGHEST_RATED_MOVIES_TITLE = "Highest Rated Movies";
    private static final String YOUR_FAVORITE_MOVIES_TITLE = "Your Favorite Movies";

    // The data adapter that will hold the movie poster images.
    private MovieAdapter  mMovieAdapter = null;
    private Callback      mCallback         = null;
    private int           mPosition         = ListView.INVALID_POSITION;

    public interface Callback {
        void onItemSelected(int position);
    }

    public MovieGridFragment() {
        Log.v (TAG, "constructor");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (Callback) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                                                 + " must implement Callback");
        }
    }

    /**
     * This is all pretty vanilla
     * @param savedInstanceState: not used because we don't need to save anything. Really!
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Set up the movie adapter that holds the grid view of the movie posters.
     * @param inflater the inflater
     * @param container the container
     * @param savedInstanceState so far, not needed
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View     rootView = inflater.inflate(R.layout.movie_grid_layout, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.movie_grid);

        // Set the MovieAdapter to the movies in the singleton. Will be updated after
        // the call to updateMovies
        mMovieAdapter = new MovieAdapter(getActivity(), getInstance().getMovies());
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(this);

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if ((null != savedInstanceState) && savedInstanceState.containsKey(Utils.MOVIE_KEY)) {
            mPosition = savedInstanceState.getInt(Utils.MOVIE_KEY);
        }
        updateMovies();
        setActionBarTitle();
        return rootView;
    }

    /**
     * On resuming this activity, check to see if the movies need updating.
     */
    @Override
    public void onResume() {
        super.onResume();
        updateMovies();
        setActionBarTitle();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt (Utils.MOVIE_KEY, mPosition);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * We're only interested if the Sort Order option has been selected. Then
     * we'll start the appropriate activity ...
     * @param item: the item selected
     * @return true (unless something weird happens in the super class
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_sort_order) {
            Log.v (TAG, "sort order option selected");
            setPrefSortOrder();
            updateMovies();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * When the user selects a movie poster image, start DetailActivity to display
     * detailed movie information on the selected movie. The movie's position within
     * the singleton class PopuarMovies is passed to the activity through the intent.
     * @param parent: The parent (not used)
     * @param view: The view (not used)
     * @param position: The position of the poster image
     * @param id: an id (not used).
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       mPosition = position;
       Intent intent = new Intent(getActivity(), DetailActivity.class);
       intent.putExtra(Utils.MOVIE_KEY, position);
       ((Callback) getActivity()).onItemSelected(mPosition);
    }

    /**
     * User has clicked "Settings", selects a sort order for the movies (within
     * SettingsActivity). Once selected, the (possibly new) sort order is stored
     * in a field in the Utils class.
     */
    private void setPrefSortOrder () {
        Intent intent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(intent);
        // These lines for logging only to be sure sortOrder is being set properly
        Utils.SortOrder sortOrder = Utils.getRequestedSortOrder (getActivity());
        Log.v(TAG, "sort order " + sortOrder);
        setActionBarTitle();
    }

    /**
     *  Update the set of movies, if needed
     */
    private void updateMovies() {

        Utils.SortOrder sortOrder = Utils.getRequestedSortOrder(getActivity());
        PopularMovies movies = PopularMovies.getInstance();

        // If the sortOrder is FAVORITES, always refetch because user could
        // have added to favorites between now and last fetch
        if (sortOrder == Utils.SortOrder.FAVORITES) {
            MovieOToRMapper mapper = MovieOToRMapper.getInstance(getContext());
            List<Movie> favorites = mapper.getAllFavorites();
            movies.clear();
            movies.addAll((ArrayList)favorites);
            mMovieAdapter.addAll(movies.getMovies());
            return;
        }

        boolean reloadedNeeded = movies.isReloadNeeded(sortOrder);
        Log.v(TAG, "updateMovies sortOrder " + sortOrder + " reloaded needed? " + reloadedNeeded);

        if (reloadedNeeded) {
            FetchMoviesTask moviesTask = new FetchMoviesTask();
            moviesTask.setInvokingActivity(getActivity());
            moviesTask.setMovieAdapter(mMovieAdapter);
            moviesTask.execute("");

        }
    }

    private void setActionBarTitle () {
        Activity thisActivity = getActivity();
        Utils.SortOrder sortOrder = Utils.getRequestedSortOrder(getActivity());
        switch (sortOrder) {
            case FAVORITES:
                thisActivity.setTitle(YOUR_FAVORITE_MOVIES_TITLE);
                break;
            case HIGHEST_RATED:
                thisActivity.setTitle(HIGHEST_RATED_MOVIES_TITLE);
                break;
            case MOST_POPULAR:
                thisActivity.setTitle(MOST_POPULAR_MOVIES_TITLE);
                break;
            default :
                thisActivity.setTitle(" ");
        }
    }

}

