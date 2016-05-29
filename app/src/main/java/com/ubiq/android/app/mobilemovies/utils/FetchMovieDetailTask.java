package com.ubiq.android.app.mobilemovies.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ubiq.android.app.mobilemovies.R;
import com.ubiq.android.app.mobilemovies.model.Movie;
import com.ubiq.android.app.mobilemovies.model.MovieReview;
import com.ubiq.android.app.mobilemovies.model.MovieTrailer;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * The Asynchronous task that fetches the detailed movie information
 */
public class FetchMovieDetailTask extends AsyncTask<Void, Void, Movie> {

    private final String LOG_TAG = FetchMovieDetailTask.class.getSimpleName();

    // The JSON string that will be populated by the RESTful call; initialized to null
    private String mMovieJsonStr = null;
    // The activity that invoked this AsyncTask; we need to save it to access a
    // view in the postExecute method
    private Activity mInvokingActivity;
    // the movie whose details we are fetching
    private Movie    mMovie;
    /// the adapter for the movie trailers
    private ArrayAdapter<MovieTrailer> mMovieTrailerAdapter = null;
    // the adapter for the movie reviews
    private ArrayAdapter<MovieReview>  mMovieReviewAdapter  = null;

    /**
     * Constructor for the class. Only here to accept and store the invoking
     * activity
     * @param activity: the activity that invoked this one. Needed in onPostExecute
     *                method to access a view
     */
    public FetchMovieDetailTask(Activity activity) {
        mInvokingActivity = activity;
    }

    public void setMovie(Movie movie) {
        mMovie = movie;
    }

    public void setMovieTrailerArrayAdapter(ArrayAdapter<MovieTrailer> adapter) {
        mMovieTrailerAdapter = adapter;
    }

    public void setMovieReviewArrayAdapter(ArrayAdapter<MovieReview> adapter) {
        mMovieReviewAdapter = adapter;
    }


    /**
     * The method that fetches the movie detail
     * @param params: not used
     * @return Movie: the movie object, embellished with detailed information
     */
    @Override
    protected Movie doInBackground(Void... params) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader    reader        = null;

        // Will contain the raw JSON response as a string.

        try {
            // Construct the URL for the themoviedb.org query
            // Possible parameters are available at api.themoviedb.org
            // https://api.themoviedb.org/3/discover/movie?API#&popular

            URL url = Utils.buildMovieDetailURL(mMovie.getId());
            Log.v(LOG_TAG, "Build Movie Detail URL: " + url);

            // Create the request to themoviedb, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do. Make a note in the log and return null
                Log.v(LOG_TAG, "No values returned from movie detail invocation");
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
            mMovieJsonStr = buffer.toString();
            Log.v(LOG_TAG, "Movies JSON String " + mMovieJsonStr);
            Utils.parseMovieDetailsFromJsonString(mMovie, mMovieJsonStr);
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        finally {
            Utils.closeQuietly(urlConnection);
            Utils.closeQuietly(reader);
        }
        return mMovie;
    }

    /**
     * Once the data has been fetched an parsed into the Movie object(s), update
     * the view to have the movie runtime displayed, then update the movie trailer
     * adapter to display the trailer stuff, if any
     * @param movie: not actually used -- needed to get this method to execute
     */
    protected void onPostExecute(Movie movie) {
        // the parameter "movie" isn't used; its necessary to get this method
        // to execute.
        final String LOG_TAG = "Fetch...PostExecute";

        // Add the running time information from the movie detail to the
        // detailed view
        TextView runningTimeTextView = (TextView) mInvokingActivity.findViewById(
                R.id.runningtime);
        runningTimeTextView.setText(mMovie.getRunningTime() + " mins");

        // If there are trailers, load them into an adapter
        // If there are no trailers, the header should display "No Trailers"

        List<MovieTrailer> trailers = mMovie.getMovieTrailers();
        Log.v(LOG_TAG, trailers.toString());
        // clear and addall, whatever the number of trailers
        mMovieTrailerAdapter.clear();
        mMovieTrailerAdapter.addAll(trailers);

        TextView trailerHeader = (TextView)mInvokingActivity.findViewById(R.id.trailer_heading);
        trailerHeader.setText ("Trailers");
        if (mMovie.getMovieTrailers().size() == 0) trailerHeader.setText ("No Trailers");

        FloatingActionButton reviewsButton =
                (FloatingActionButton)mInvokingActivity.findViewById(R.id.reviews_button);
        reviewsButton.setVisibility(View.VISIBLE);
        if (mMovie.getMovieTrailers().size() == 0) reviewsButton.setVisibility(View.INVISIBLE);

        mMovie.setMovieDetailLoaded(true);
    }
}



