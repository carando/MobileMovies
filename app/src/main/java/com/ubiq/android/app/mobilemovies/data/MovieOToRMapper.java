package com.ubiq.android.app.mobilemovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;

import com.ubiq.android.app.mobilemovies.model.Movie;
import com.ubiq.android.app.mobilemovies.model.MovieReview;
import com.ubiq.android.app.mobilemovies.model.MovieTrailer;
import com.ubiq.android.app.mobilemovies.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps Movie, Trailer, Reviews object-to-relational and back
 */
public class MovieOToRMapper {
    private static String LOG_TAG = MovieOToRMapper.class.getSimpleName();

    /**
     * The database location for the Movie in favorites.
     */
    public  static long   MOVIE_IN_FAVORITES = -2;


    private static MovieOToRMapper mMovieOToRMapper = null;
    private static MovieDBHelper   dbHelper         = null;

    private MovieOToRMapper(Context context) {
        dbHelper = new MovieDBHelper(context);
        dbHelper.getWritableDatabase();
    }

    /**
     * Gets instance.
     *
     * @param context the context
     * @return the instance
     */

    public static MovieOToRMapper getInstance(Context context) {
        if (mMovieOToRMapper == null) {
            mMovieOToRMapper = new MovieOToRMapper(context);
        }
        return mMovieOToRMapper;
    }


    /**
     * Gets all favorites.
     *
     * @return the all favorites
     */
    public List<Movie> getAllFavorites () {
          List <Movie> favoriteMovies;
          SQLiteDatabase db = dbHelper.getReadableDatabase();

        favoriteMovies = getMovies(db);
        for (int i=0; i<favoriteMovies.size(); i++) {
            Movie aMovie = favoriteMovies.get(i);
            getTrailers(db, aMovie);
            getReviews (db, aMovie);
            aMovie.setMovieDetailLoaded(true);
        }
        db.close();
        return favoriteMovies;
    }

    /**
     * Insert movie into database if not there already.
     *
     * @param aMovie the movie
     * @return long location of movie in database
     */
    public long insertMovie(Movie aMovie) {
        final int   INSERT_FAILED = -1;
        long        insertResult  = INSERT_FAILED;
        Log.v(LOG_TAG, "***insert movie " + aMovie.toString());

        if (movieInFavorites(aMovie)) {
            return MOVIE_IN_FAVORITES;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = getContentValues(aMovie);
        try {
            db.beginTransaction();
            insertResult = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
            if (insertResult == INSERT_FAILED) {
                db.endTransaction();
                db.close();
                return INSERT_FAILED;
            }
            boolean success = insertTrailers(aMovie);
            if (!success) {
                db.endTransaction();
                db.close();
                return INSERT_FAILED;
            }
            success = insertReview(aMovie);
            if (!success) {
                db.endTransaction();
                db.close();
                return INSERT_FAILED;
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        } catch (Exception e) {
            Log.e (LOG_TAG, "***" + e.toString());
        }
        return insertResult;
      }

    /**
     * Insert movie into database if not there already.
     *
     * @param values the content values for the movie
     * @return long location of movie in database
     */
    public long insertMovie (ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert (MovieContract.MovieEntry.TABLE_NAME, null, values);
    }


    /**
     * Returns true of the movie is already in the database
     *
     * @param aMovie the movie
     * @return boolean
     */
    public boolean movieInFavorites(Movie aMovie) {
        Log.v (LOG_TAG, "***movieInFavorites");
        String rawQueryString = "SELECT " + MovieContract.MovieEntry.COLUMN_MOVIE_TITLE +
                " FROM  " + MovieContract.MovieEntry.TABLE_NAME      +
                " WHERE " + MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor =  db.rawQuery(rawQueryString,
                                     new String[]{String.valueOf(aMovie.getId())});

        boolean foundMovie = cursor.moveToFirst();
        Log.v (LOG_TAG, "***movieInFavorites cursor isEmpty " + foundMovie);
        cursor.close();
        return foundMovie;
    }
    /**
     * Generates the content values for a movie
     *
     * @param aMovie the movie
     * @return ContentValues
     */
    private ContentValues getContentValues(Movie aMovie) {
        ContentValues values = new ContentValues();
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_ID,           aMovie.getId());
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,        aMovie.getTitle());
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_DESCRIPTION,  aMovie.getOverview());
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_YEAR, aMovie.getReleaseYear());
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_RATING,       aMovie.getRating());
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_RUNNING_TIME, aMovie.getRunningTime());
        values.put (MovieContract.MovieEntry.COLUMN_POSTER_PATH,        aMovie.getPosterPath());
        values.put (MovieContract.MovieEntry.COLUMN_POSTER_FILE,        aMovie.getImageFile());

        byte[] moviePoster;
        try {
            URL  moviePosterURL = Utils.buildMovieImageURL(aMovie.getPosterPath());
            Log.v (LOG_TAG, "***moviePosterURL " + moviePosterURL);
            moviePoster = Utils.downloadMovieImage(moviePosterURL);
            // TODO can we do a direct conversion of a movie image to a bitmap?
            Bitmap bitmap = Utils.convertByteArrayToBitmap(moviePoster);
            // need to save the image to a file and store the file name in the database
            String baseFileName = Utils.createBitmapFileName();
            String bitmapFileName = Utils.saveBitmapToFile(bitmap,baseFileName);
            values.put (MovieContract.MovieEntry.COLUMN_POSTER_FILE, bitmapFileName);
        }
        catch (MalformedURLException e) {
            Log.e (LOG_TAG, "***" + e.toString());
            e.printStackTrace();
        }
        return values;
    }

    private ContentValues getContentValues (int movieID, MovieTrailer aTrailer) {
        ContentValues values = new ContentValues();
        values.put (MovieContract.MovieTrailers.COLUMN_MOVIE_ID, movieID);
        values.put (MovieContract.MovieTrailers.COLUMN_TRAILER_NAME, aTrailer.getName());
        values.put (MovieContract.MovieTrailers.COLUMN_TRAILER_LOCATION, aTrailer.getYoutubeSource());
        return values;
    }

    private ContentValues getContentValues (int movieID, MovieReview aReview) {
        ContentValues values = new ContentValues();
        values.put (MovieContract.MovieReviews.COLUMN_MOVIE_ID, movieID);
        values.put (MovieContract.MovieReviews.COLUMN_REVIEWER_NAME, aReview.getAuthor());
        values.put (MovieContract.MovieReviews.COLUMN_REVIEW_TEXT, aReview.getReview());
        return values;
    }

    private boolean insertTrailers (Movie aMovie) {
        final int   INSERT_FAILED = -1;
        List <MovieTrailer> trailers = aMovie.getMovieTrailers();
        int size = trailers.size();
        if (size == 0) return true;

        int movieID = aMovie.getId();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (int i=0; i<size; i++) {
            ContentValues values = getContentValues(movieID,trailers.get(i));
            long result = db.insert(MovieContract.MovieTrailers.TABLE_NAME, null, values);
            if (result == INSERT_FAILED) return false;
        }
        return true;
    }


    private boolean insertReview (Movie aMovie) {
        final int   INSERT_FAILED = -1;
        List <MovieReview> reviews = aMovie.getMovieReviews();
        int size = reviews.size();
        if (size == 0) return true;

        int movieID = aMovie.getId();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (int i=0; i<size; i++) {
            ContentValues values = getContentValues(movieID,reviews.get(i));
            long result = db.insert(MovieContract.MovieReviews.TABLE_NAME, null, values);
            if (result == INSERT_FAILED) return false;
        }
        return true;
    }

    private List <Movie> getMovies (SQLiteDatabase db) {
        ArrayList <Movie> favoriteMovies = null;
        Log.v(LOG_TAG, "***entered getMovies");
        String rawQueryString = "SELECT " + MovieContract.MovieEntry.COLUMN_MOVIE_ID           + "," +
                MovieContract.MovieEntry.COLUMN_MOVIE_TITLE        + "," +
                MovieContract.MovieEntry.COLUMN_MOVIE_DESCRIPTION  + "," +
                MovieContract.MovieEntry.COLUMN_MOVIE_RATING       + "," +
                MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_YEAR + "," +
                MovieContract.MovieEntry.COLUMN_MOVIE_RUNNING_TIME + "," +
                MovieContract.MovieEntry.COLUMN_POSTER_PATH        + "," +
                MovieContract.MovieEntry.COLUMN_POSTER_FILE        +
                " FROM  " + MovieContract.MovieEntry.TABLE_NAME ;
        Log.v (LOG_TAG, "query: " + rawQueryString);
        Cursor cursor =  db.rawQuery(rawQueryString, null);

        boolean rowsToRead = cursor.moveToFirst();
        if (rowsToRead) {
            favoriteMovies = new ArrayList<Movie>();
            Log.v (LOG_TAG, "***Movie titles in Favorites");
        }

        while (rowsToRead) {
            int    id          = cursor.getInt    (0);
            String title       = cursor.getString (1);
            String description = cursor.getString (2);
            String rating      = cursor.getString (3);
            String year        = cursor.getString (4);
            String runningTime = cursor.getString (5);
            String path        = cursor.getString (6);
            String imageFile   = cursor.getString (7);
            Log.d (LOG_TAG, "*** movie title: " + title);
            Movie movie = new Movie (id,title,rating,year,description,path);
            movie.setFavorite(true);
            favoriteMovies.add(movie);
            rowsToRead  = cursor.moveToNext();
        }
        cursor.close();
        return favoriteMovies;
    }

    private void getTrailers (SQLiteDatabase db, Movie aMovie) {
        int movieID = aMovie.getId();
        String rawQueryString = "SELECT " +
                MovieContract.MovieTrailers.COLUMN_TRAILER_NAME        + ", " +
                MovieContract.MovieTrailers.COLUMN_TRAILER_LOCATION    + "  " +
                "FROM "  + MovieContract.MovieTrailers.TABLE_NAME      + "  " +
                "WHERE " + MovieContract.MovieTrailers.COLUMN_MOVIE_ID + " = " +
                movieID;

        Log.v (LOG_TAG, "query: " + rawQueryString);

        Cursor cursor =  db.rawQuery(rawQueryString, null);
        boolean rowsToRead = cursor.moveToFirst();
        while (rowsToRead) {
            String trailerName     = cursor.getString(0);
            String trailerLocation = cursor.getString(1);
            MovieTrailer trailer = new MovieTrailer(trailerName,trailerLocation);
            aMovie.addTrailer(trailer);
            rowsToRead = cursor.moveToNext();
        }
        cursor.close();
    }

    private void getReviews (SQLiteDatabase db, Movie aMovie) {
        int movieID = aMovie.getId();
        String rawQueryString = "SELECT " +
                MovieContract.MovieReviews.COLUMN_REVIEWER_NAME       + ", " +
                MovieContract.MovieReviews.COLUMN_REVIEW_TEXT         + "  " +
                "FROM "  + MovieContract.MovieReviews.TABLE_NAME      + "  " +
                "WHERE " + MovieContract.MovieReviews.COLUMN_MOVIE_ID + " = " +
                movieID;

        Log.v (LOG_TAG, "query: " + rawQueryString);

        Cursor cursor =  db.rawQuery(rawQueryString, null);
        boolean rowsToRead = cursor.moveToFirst();
        int     id = 0;
        while (rowsToRead) {
            String reviewName  = cursor.getString(0);
            String reviewText  = cursor.getString(1);
            MovieReview review = new MovieReview(String.valueOf(id++),reviewName,reviewText);
            aMovie.addReview(review);
            rowsToRead = cursor.moveToNext();
        }
        cursor.close();
    }
}
