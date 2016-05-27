package com.ubiq.android.app.mobilemovies.utils;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.ubiq.android.app.mobilemovies.model.Movie;
import com.ubiq.android.app.mobilemovies.model.PopularMovies;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;



/**
 * The asynchronous task that fetches movie information from themoviedb.org database
 * via a RESTful interface. Right now we do not use the first parameter String,
 * rather we pass in an empty string, but this won't work without passing in something,
 * so don't pass a VOID as argument 1!!!!!
 *
 * An ArrayList of Movies are returned if successful.
 */
public class FetchMoviesTask extends
        AsyncTask<String, Void, ArrayList<Movie>> {

    private final String LOG_TAG = FetchMoviesTask.class.getCanonicalName();

    private String           mMoviesJsonStr = null;
    private MovieAdapter     mMovieAdapter  = null;
    private FragmentActivity mInvokingActivity = null;

    public void setInvokingActivity (FragmentActivity activity) {
        mInvokingActivity = activity;
    }

    public void setMovieAdapter (MovieAdapter movieAdapter) {
        mMovieAdapter = movieAdapter;
    }

    @Override
    protected ArrayList<Movie> doInBackground(
            String... urlArgs) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader        = null;

        // Will contain the raw JSON response as a string.

        try {
            // Construct the URL for the themoviedb.org query
            // Possible parameters are available at api.themoviedb.org

            Utils.SortOrder sortOrder = Utils.getRequestedSortOrder(mInvokingActivity);
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
            mMoviesJsonStr = buffer.toString();
            Log.v(LOG_TAG, "Movies JSON String " + mMoviesJsonStr);
            return Utils.parseMoviesFromJSONString(mMoviesJsonStr);
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
        PopularMovies.getInstance().clear();
        PopularMovies.getInstance().addAll(movies);
        Log.v(LOG_TAG, PopularMovies.getInstance().getMovies().toString());
        mMovieAdapter.clear();
        mMovieAdapter.addAll(movies);

    }


}
