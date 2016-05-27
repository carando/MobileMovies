package com.ubiq.android.app.mobilemovies.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ubiq.android.app.mobilemovies.R;
import com.ubiq.android.app.mobilemovies.model.Movie;
import com.ubiq.android.app.mobilemovies.model.MovieReview;
import com.ubiq.android.app.mobilemovies.model.MovieTrailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A Utility class where general purpose methods reside and constants that are used
 * in the rest of the application.
 */
public class Utils {


    private static String TAG = Utils.class.getSimpleName();

    // The location for storing favorited movies
    private static String INTERNAL_FILE_DIRECTORY = "popularmovies";
    // The directory where the INTERNAL_FILE_DIRECTORY is rooted
    private static File   ROOT_DIRECTORY          = null;

    // End point URI's for fetching movies and posters;
    private static final String URI_MOVIE_BASE        = "https://api.themoviedb.org/3/discover/movie?";
    private static final String URI_IMAGE_BASE        = "http://image.tmdb.org/t/p/";
    private static final String URI_SINGLE_MOVIE_BASE = "http://api.themoviedb.org/3/movie/";

    // Recommended image size for poster
    private static final String IMAGE_SIZE = "w185";

    // API Key for themoviedb.org request
    private static final String API_MOVIE_KEY = "523c46ff603be26582c7dee52e4d3d29";
     //private static final String API_MOVIE_KEY = null;

    // Arguments to themoviedb.org RESTful interfaces
    private static final String API_KEY            = "api_key";
    private static final String SORT_ORDER         = "sort_by";
    private static final String APPEND_TO_RESPONSE = "append_to_response";
    private static final String TRAILERS           = "trailers";
    private static final String REVIEWS            = "reviews";

    // Argument indicating sort order for movies from themoviedb.org
    // Default is popularity.desc

    public static final String MOST_POPULAR_PARAMETER   = "popularity.desc";   // most popular
    public static final String HIGHEST_RATED_PARAMETER  = "vote_average.desc"; // highest rated
    public static final String FAVORITES_RATED          = "user_favorites";    // locally stored


    /**
     * Sort order indicates the set of movies requested for display;  they are either
     * the most popular movies from themoviedb.org (MOST_POPULAR), the highest rated
     * from themoviedb.org (HIGHEST_RATED)  or those that have been stored in the database
     * as favorites (FAVORITES)-- not yet implemented.
     * NOT_SPECIFIED should only be set before any movies have been retrieved.
     */
    public enum SortOrder {
        NOT_SPECIFIED  ("Not Specified", null),
        MOST_POPULAR   ("Most Popular",  MOST_POPULAR_PARAMETER),
        HIGHEST_RATED  ("Highest Rated", HIGHEST_RATED_PARAMETER),
        FAVORITES      ("Favorites",     FAVORITES_RATED);

        private  String name         = null;
        private  String urlParameter = null;

        SortOrder (String name, String urlParameter) {
            this.name = name;
            this.urlParameter = urlParameter;
        }

        public String getName () {
            return name;
        }

        public String getURLParameter () {
            return urlParameter;
        }
    }

   // public static SortOrder   sortOrder = SortOrder.NOT_SPECIFIED;

    // Movie rating from 0 - 10; Rating is displayed as a fraction. e.g.,  <rating> / 10
    public static final String MAXIMUM_MOVIE_RATING = "10";

    // the key for passing the movie selected for detailed view between intents
    public static final  String MOVIE_KEY              = "movie";

    // size for movie posters in detail movie view
    public static final  int    IMAGE_SIZE_WIDTH       = 600;
    public static final  int    IMAGE_SIZE_HEIGHT      = 825;

    // Youtube URL and uri.
    public static final String YOU_TUBE_URL            = "http://www.youtube.com/watch?v=";
    public static final String YOU_TUBE_APP_INTENT     = "vnd.youtube:";

    /**
     *     This class is a "service" class; not meant to be instantiated;
     */
    private Utils() {
    }

    // Close the reader stream quietly; we don't care about null or exceptions
    public static void closeQuietly(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            }
            catch (final IOException e) {
                Log.e("Utils.closeQuietly", "Error closing stream", e);
            }
        }
    }

    /**
     * Construct the URL for the themoviedb.org query
     *  Possible parameters are available at api.themoviedb.org
     *  https://api.themoviedb.org/3/discover/movie?#####
     * @param sortOrder the user-preferred sort order: MOST POPULAR, HIGHEST RATED,
     *                  and eventually FAVORITES
     * @return URL the URL to access the movies in the specified order
     * @throws MalformedURLException
     */
    public static URL buildMoviesURL (Utils.SortOrder sortOrder) throws MalformedURLException {
        String sortOrderURLParameter = sortOrder.getURLParameter();
        Uri uri = Uri.parse(URI_MOVIE_BASE).buildUpon().
                appendQueryParameter(API_KEY, Utils.getAPIMovieKey()).
                appendQueryParameter(SORT_ORDER, sortOrderURLParameter).
                build();
        URL url = new URL(uri.toString());
        Log.v(TAG, "Build Movie URL: " + url);

        return url;
    }

    /**
     * Builds the URL for the movie poster image from the image id. If the image is
     * null, returns a URL that will display as "No Image Available"
     * @param imageId: the imageId as retrieved from themoviedb.org
     * @return URL the full URL for the image
     * @throws MalformedURLException
     */
    public static URL buildMovieImageURL(String imageId) throws MalformedURLException {
        // Compose the URL for the accessing the movie's image
        if (imageId == null) {
            Log.v(TAG, "Null movie image ");
            Log.v(TAG, "Returning " + missingPoster());
            return new URL (missingPoster());
        }
        String url_base = URI_IMAGE_BASE + IMAGE_SIZE + "/" + imageId;
        Uri uri = Uri.parse(url_base).buildUpon().
                appendQueryParameter(API_KEY, Utils.getAPIMovieKey()).
                build();
        URL url = new URL(uri.toString());
        Log.v(TAG, "Build Image URL: " + url);
        return url;
    }

    // Disconnect from the URL quietly; we don't care about null or exceptions
    public static void closeQuietly(HttpURLConnection urlConnection) {
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }

    // Close the reader stream quietly; we don't care about null or exceptions
    public static void closeQuietly(FileOutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            }
            catch (final IOException e) {
                Log.e("Utils.closeQuietly", "Error closing stream", e);
            }
        }
    }


    /**
     * Converts the byte array of a movie poster into a bitmap
     * @param byteArray the movie poster to be coverted to bitmap
     * @return Bitmap returns the converted movie poster
     */
    public static Bitmap convertByteArrayToBitmap (byte [] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    /**
     * Creates a random file name to be used to store a bitmap of a movie
     * poster.
     * @return a file name
     */
    public static String createBitmapFileName () {
        final String fileNamePrefix = "favmovie";
        final String fileTypeSuffix = ".bmp";
        int suffix = (int) (Math.random() * 10000);
        return  fileNamePrefix + suffix + fileTypeSuffix;
    }

    /**
     * Downloads a movie poster image from the URL specified and converts
     * the image to a byte array for storage.
     * @param movieImageURL the URL where the image resides
     * @return byte [] the byte array version of the image
     */
    public static byte [] downloadMovieImage (URL movieImageURL) {
        Log.v ("Utils", "*** downloadMovieImage");
        byte [] result = null;
        try {
            InputStream in = new BufferedInputStream(movieImageURL.openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int n;
            while (-1 !=(n=in.read(buffer))) {
                out.write(buffer, 0, n);
            }
            out.close();
            in.close();
            result = out.toByteArray();
            Log.v ("Utils", "*** Image byte size:" + result.length + " " + result.toString());
        }
        catch (IOException e) {
            Log.e ("Utils", "***ERROR in downloadMovieImage " + e.toString());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Builds the URL that will be used to get a movie's details, such as reviews
     * and trailers
     * @param movieId: the id from themoviedb.org. Used in building the URL
     * @return  URL for accessing the movie details
     * @throws MalformedURLException
     */
    public static URL buildMovieDetailURL (int movieId ) throws MalformedURLException{
        // Compose the URL for the accessing the movie's image
        String url_base = URI_SINGLE_MOVIE_BASE + movieId;
        Uri uri = Uri.parse(url_base).buildUpon().
                appendQueryParameter(API_KEY, Utils.getAPIMovieKey()).
                appendQueryParameter(APPEND_TO_RESPONSE,TRAILERS + "," + REVIEWS).
                build();
        URL url = new URL(uri.toString());
        Log.v(TAG, "Build Movie Detail URL: " + url);
        return url;
    }

    /**
     * Initialize the local internal file directory.
     *
     * @param context the context
     */

    public static void initFileDirectory (Context context) {
        if (ROOT_DIRECTORY == null) {
            ROOT_DIRECTORY = new File(context.getFilesDir(), INTERNAL_FILE_DIRECTORY);
            boolean success = ROOT_DIRECTORY.mkdirs();
        }
    }


    /**
     * Extracts the movie information from the JSON String we downloaded.
     * @param moviesJsonString JSON formatted string of movie information
     * @return ArrayList<Movie> The list of movies extracted as objects
     * @throws JSONException
     */
    public static ArrayList<Movie> parseMoviesFromJSONString(
            String moviesJsonString) throws
            JSONException {
        final String RESULTS               = "results";
        final String MOVIE_ID              = "id";
        final String MOVIE_TITLE           = "title";
        final String RATING                = "vote_average";
        final String RELEASE_DATE          = "release_date";
        final String OVERVIEW              = "overview";
        final String BACKDROP_PATH         = "backdrop_path";
        final String POSTER_PATH           = "poster_path";
        // TODO remove IMDB_ID
      //  final String IMDB_ID               = "imdb_id";

        JSONObject moviesJson              = new JSONObject(moviesJsonString);
        JSONArray  moviesArray             = moviesJson.getJSONArray(RESULTS);
        ArrayList<Movie> movies            = new ArrayList<Movie>();

        for (int i = 0; i < moviesArray.length(); i++) {
            // Get the JSON object representing the movie
            JSONObject jMovie = moviesArray.getJSONObject(i);
            // Extract the movie details from the JSON object
            int id                = jMovie.getInt(MOVIE_ID);
            String title          = jMovie.getString(MOVIE_TITLE);
            String rating         = jMovie.getString(RATING);
            String releaseYear    = extractReleaseYear(jMovie.getString(RELEASE_DATE));
            String overview       = jMovie.getString(OVERVIEW);
            String backdropPath   = jMovie.getString(BACKDROP_PATH);
            String posterPath     = jMovie.getString(POSTER_PATH);
            if (posterPath.equals("null")) posterPath = null;



            // Create a new movie instance and add it to the movies to be returned
            Movie movie = new Movie(id, title, rating, releaseYear, overview, posterPath);
            // Some of the movie data is really sparse; don't add bogus stuff
            if (!isBogus (movie)) movies.add(movie);
        }
        return movies;
    }

    /**
     * Parse out the details of a movie (e.g., runtime length, trailers, reviews...)
     * and add these values to the set of attributes of the movie object
     * @param theMovie the movie for which details will be added
     * @param movieJsonString the JSON string containing the movie details
     * @throws JSONException
     */
    public static void parseMovieDetailsFromJsonString (Movie theMovie, String movieJsonString)
            throws JSONException{
        // constants used in extracting the JSON values
        final String RUNTIME       = "runtime";
        final String REVIEWS       = "reviews";
        final String TOTAL_RESULTS = "total_results";
        final String RESULTS       = "results";
        final String MOVIE_ID      = "id";
        final String AUTHOR        = "author";
        final String CONTENT       = "content";
        final String TRAILERS      = "trailers";
        final String YOU_TUBE      = "youtube";
        final String TRAILER_NAME  = "name";
        final String TRAILER_SOURCE= "source";

        // Extract the movie's runtime and save it.
        JSONObject movieJson       = new JSONObject(movieJsonString);
        int        runtime         = movieJson.getInt(RUNTIME);
        theMovie.setRunningtime(runtime);

        // If there are reviews associated with the movie, save them
        JSONObject reviewsObject   = movieJson.getJSONObject(REVIEWS);
        int        totalReviews    = reviewsObject.getInt(TOTAL_RESULTS);
        Log.v (TAG, "total reviews = " + String.valueOf(totalReviews));
        theMovie.setNumberOfReviews(totalReviews);

        JSONArray  reviewsArray    = reviewsObject.getJSONArray(RESULTS);
        Log.v(TAG, "Reviews: " + reviewsArray.toString());
        for (int i=0; i<reviewsArray.length(); i++) {
            JSONObject jReview = reviewsArray.getJSONObject(i);
            String id      = jReview.getString(MOVIE_ID);
            String author  = jReview.getString(AUTHOR);
            String content = jReview.getString(CONTENT);
            MovieReview movieReview = new MovieReview (id, author, content);
            theMovie.addReview(movieReview);
        }

        // If the movie has trailers save them.
        JSONObject trailers        = movieJson.getJSONObject(TRAILERS);
        Log.v (TAG, "Trailers: " + trailers.toString());
        // We'll only keep youtube trailers for now.
        JSONArray  youTubeTrailers = trailers.getJSONArray(YOU_TUBE);
        Log.v(TAG, youTubeTrailers.toString());

        for(int i=0; i<youTubeTrailers.length(); i++){
            JSONObject jTrailer = youTubeTrailers.getJSONObject(i);
            Log.v(TAG, "***Trailer " + jTrailer);
            String     name     = jTrailer.getString(TRAILER_NAME);
            String     source   = jTrailer.getString(TRAILER_SOURCE);
            MovieTrailer movieTrailer = new MovieTrailer (name, source);
            theMovie.addTrailer(movieTrailer);
        }
    }


    /**
     * If a movie does not have a poster, return this image instead. Not ideal,
     * but better than nothing
     * @return URL of a poster that says "No Image"
     */
    public static String missingPoster () {
        return "http://comicbookmoviedatabase.com/wp-content/uploads/2014/04/no-poster-available-336x500.jpg";
    }

    //TODO add documentation
    public static String saveBitmapToFile(Bitmap bitmap, String fileName) {
        String           bitmapFile = null;
        FileOutputStream out        = null;
        try {
            out = new FileOutputStream(new File(ROOT_DIRECTORY, fileName));
            // PNG is a lossless format, the compression factor (100) is ignored
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bitmap is your Bitmap instance
            bitmapFile = ROOT_DIRECTORY + "/" + fileName;
        } catch (Exception e) {
            Log.e (TAG, "***exception " + e.toString());
            e.printStackTrace();
        } finally {
            closeQuietly(out);
        }
        return bitmapFile;
    }

    /**
     * When the user sets the sort order through the "Settings" facility, return the
     * associated enum of SortOrder
     * @param context: not used
     * @return the appropriate enum of SortOrder
     */
    public static SortOrder getRequestedSortOrder (Activity context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                context);
        String sortOrderString = prefs.getString(context.getString(R.string.pref_sort_order_key),
                                          context.getString(R.string.pref_sort_order_most_popular));
        if (sortOrderString.equals(SortOrder.FAVORITES.getName())) {
            return SortOrder.FAVORITES;
        }
        if (sortOrderString.equals(SortOrder.HIGHEST_RATED.getName())) {
            return SortOrder.HIGHEST_RATED;
        }
        if (sortOrderString.equals(SortOrder.MOST_POPULAR.getName())) {
            return SortOrder.MOST_POPULAR;
        }
        Log.e (TAG, "Bad sort order " + sortOrderString);

        return SortOrder.MOST_POPULAR;
    }

    /**
     * Return the api key that stored as a constant in this class. If the key is missing
     * show a message to the user.
     * @return the API Key
     */
    protected static String getAPIMovieKey() {
        return API_MOVIE_KEY;
    }

    /**
     * Extract the release year from the larger release date, if a date is present.
     * If not, return a "?"
     * @param releaseDate either a fully qualified date with YYYY-MM-DD or null
     * @return the year YYYY
     */
    private static String extractReleaseYear (String releaseDate) {
        final int    RELEASE_YEAR_START    = 0;
        final int    RELEASE_YEAR_LENGTH   = 4;
        String releaseYear;
        if ((releaseDate != null) && (releaseDate.length() >= RELEASE_YEAR_LENGTH)) {
            releaseYear = releaseDate.substring(RELEASE_YEAR_START, RELEASE_YEAR_LENGTH);
        } else {
            releaseYear = "?";
        }
        return releaseYear;
    }

    /**
     * Cull movies that don't at least have an overview. Somehow some bogus stuff has gotten
     * into the database and it's not useful to the users.
     * This cull may be enhanced to include other attributes at a later time
     * @param movie : the movie that may be culled
     * @return true or false, depending on whether this movie should be included in
     * the set of movies displayed to end users.
     */
    private static boolean isBogus (Movie movie) {
        final String NO_OVERVIEW = "No Overview Found.";
        // If there is no overview or it is null, this is a bogus movie
        if ((movie.getOverview() == null) || movie.getOverview().isEmpty()) return true;
        // If the overview is "No Overview Found.", this is a bogus movie
        return movie.getOverview().equalsIgnoreCase(NO_OVERVIEW);

    }


}

