package com.ubiq.android.app.mobilemovies.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;
import com.ubiq.android.app.mobilemovies.R;
import com.ubiq.android.app.mobilemovies.utils.Utils;
import com.ubiq.android.app.mobilemovies.activities.DetailActivity;
import com.ubiq.android.app.mobilemovies.activities.SettingsActivity;
import com.ubiq.android.app.mobilemovies.model.Movie;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static com.ubiq.android.app.mobilemovies.model.PopularMovies.getInstance;

/**
 * The fragment that holds poster views of the movies retrieved from Moviedb.org
 */
public class MovieGridFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = MovieGridFragment.class.getSimpleName();

    // The data adapter that will hold the movie poster images.
    private ArrayAdapter<Movie> mMovieDataAdapter = null;
    private Callback            mCallback         = null;
    private int                 mPosition         = ListView.INVALID_POSITION;

    public interface Callback {
        void onItemSelected(int position);
    }

    public MovieGridFragment() {

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
        mMovieDataAdapter = new MovieAdapter(getInstance().getMovies());
        gridView.setAdapter(mMovieDataAdapter);
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
        return rootView;
    }

    /**
     * On resuming this activity, check to see if the movies need updating.
     */
    @Override
    public void onResume() {
        super.onResume();
        updateMovies();
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
//       startActivity(intent);
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
    }

    /**
     *  Update the set of movies, if needed
     */
    private void updateMovies() {
        Utils.SortOrder sortOrder = Utils.getRequestedSortOrder(getActivity());
        boolean reloadedNeeded = getInstance().isReloadNeeded(sortOrder);
        Log.v(TAG, "updateMovies sortOrder " + sortOrder + " reloaded needed? " + reloadedNeeded);
        if (reloadedNeeded) {
            FetchMoviesTask moviesTask = new FetchMoviesTask();
            moviesTask.execute("");
        }
    }


    /**
     *  MovieAdapter holds the images of the movie posters that we've fetched from themoviedb.org
     */
    protected class MovieAdapter extends ArrayAdapter<Movie> {
        private final String TAG = MovieAdapter.class.getCanonicalName();
        private Movie mMovie;

        public MovieAdapter(ArrayList<Movie> movies) {
            super(getActivity(), android.R.layout.simple_list_item_1, movies);
        }

        /**
         * Load the movie's poster (image) into the grid
         * @param position: the position (index) of the movie to load
         * @param convertView: the view
         * @param parent: the parent
         * @return View
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int  THUMB_NAIL_WIDTH  = 550;
            final int  THUMB_NAIL_HEIGHT = 775;

            View       row = convertView;
            ViewHolder holder;

            // if no view was passed in, inflate one
            if (null == row) {
                row = getActivity().getLayoutInflater().
                        inflate(R.layout.movie_image_layout, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) row.findViewById(R.id.movie_poster_image);
                row.setTag(holder);
            }
            else {
                holder = (ViewHolder) row.getTag();
            }
            try {
                // Get the movie for this list position and load it via Picasso call
                Log.v(TAG, "movie position = " + String.valueOf(position));
                mMovie  = getItem(position);
                URL url = Utils.buildMovieImageURL(mMovie.getPosterPath());
                //Load the image from the URL into imageView using Picasso library routines
                Picasso.with(getContext())
                        .load(url.toString())
                        .resize(THUMB_NAIL_WIDTH, THUMB_NAIL_HEIGHT)
                        .into(holder.imageView);
            }
            catch (MalformedURLException e) {
                Log.e(TAG, e.toString());
            }
            return row;
        }

        protected class ViewHolder {
            ImageView imageView;
        }
    }

    /**
     * The asynchronous task that fetches movie information from themoviedb.org database
     * via a RESTful interface. Right now we do not use the first parameter String,
     * rather we pass in an empty string, but this won't work without passing in something,
     * so don't pass a VOID as argument 1!!!!!
     *
     * An ArrayList of Movies are returned if successful.
     */
    protected class FetchMoviesTask extends
            AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMoviesTask.class.getCanonicalName();

        private String moviesJsonStr = null;


        @Override
        protected ArrayList<Movie> doInBackground(
                String... urlArgs) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader    reader        = null;

            // Will contain the raw JSON response as a string.

            try {
                // Construct the URL for the themoviedb.org query
                // Possible parameters are available at api.themoviedb.org

                Utils.SortOrder sortOrder = Utils.getRequestedSortOrder(getActivity());
                URL url = Utils.buildMoviesURL(sortOrder);
                Log.v(LOG_TAG, "Build URI: " + url);

                // Create the request to themoviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do. Make a note in the log and return null
                    Log.v(LOG_TAG, "No values returned from themovedb invocation");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Movies JSON String " + moviesJsonStr);
                return Utils.parseMoviesFromJSONString(moviesJsonStr);
            }
            catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            finally {
                Utils.closeQuietly(urlConnection);
                Utils.closeQuietly(reader);
            }
            return null;
        }

        /**
         * After execution of the FetchMovies task doInBackground method, replace the
         * returned array list of movies (if any) in the PopularMovies singleton and
         * do an addAll to the movie adapter to update the display.
         *
         * @param movies The movies returned from the RESTful call
         */

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            // Set the initial capacity for the movies list -- no more than 35
            // are returned, usually
            final int INITIAL_CAPACITY = 35;
            super.onPostExecute(movies);
            if (movies == null) {
                movies = new ArrayList<Movie>(INITIAL_CAPACITY);
            }
            getInstance().clear();
            getInstance().addAll(movies);
            Log.v(LOG_TAG, getInstance().getMovies().toString());
            mMovieDataAdapter.addAll(movies);

        }
    }
}

