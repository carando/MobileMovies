package com.ubiq.android.app.mobilemovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ubiq.android.app.mobilemovies.model.Movie;
import com.ubiq.android.app.mobilemovies.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by T on 3/28/2016.
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
        ArrayList <Movie> favoriteMovies = null;
        Log.v(LOG_TAG, "***entered getAllFavorites");
        String rawQueryString = "SELECT " + MovieContract.MovieEntry.COLUMN_MOVIE_ID           + "," +
                                            MovieContract.MovieEntry.COLUMN_MOVIE_TITLE        + "," +
                                            MovieContract.MovieEntry.COLUMN_MOVIE_DESCRIPTION  + "," +
                                            MovieContract.MovieEntry.COLUMN_MOVIE_RATING       + "," +
                                            MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_YEAR + "," +
                                            MovieContract.MovieEntry.COLUMN_MOVIE_RUNNING_TIME + "," +
                                            MovieContract.MovieEntry.COLUMN_POSTER_IMAGE       + "," +
                                            MovieContract.MovieEntry.COLUMN_POSTER_PATH        +
                " FROM  " + MovieContract.MovieEntry.TABLE_NAME ;
        Log.v (LOG_TAG, "query: " + rawQueryString);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
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
            byte[] image       = cursor.getBlob   (6);
            String path        = cursor.getString (7);
            Log.d (LOG_TAG, "*** movie title: " + title);
            Movie movie = new Movie (id,title,rating,year,description,null,path);
            movie.setPosterImage(image);
            favoriteMovies.add(movie);
            rowsToRead  = cursor.moveToNext();
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
        Log.v(LOG_TAG, "***insert movie " + aMovie.toString());
        if (movieInFavorites(aMovie)) {
            return MOVIE_IN_FAVORITES;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = getValues (aMovie);
        return db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);

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
    private boolean movieInFavorites(Movie aMovie) {
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
    private ContentValues getValues (Movie aMovie) {
        ContentValues values = new ContentValues();
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_ID,           aMovie.getId());
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,        aMovie.getTitle());
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_DESCRIPTION,  aMovie.getOverview());
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_YEAR, aMovie.getReleaseYear());
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_RATING,       aMovie.getRating());
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_RUNNING_TIME, aMovie.getRunningTime());
        values.put (MovieContract.MovieEntry.COLUMN_POSTER_PATH,        aMovie.getPosterPath());

        byte[] moviePoster;
        try {
            URL  moviePosterURL = Utils.buildMovieImageURL(aMovie.getPosterPath());
            Log.v (LOG_TAG, "***moviePosterURL " + moviePosterURL);
            moviePoster = Utils.downloadMovieImage(moviePosterURL);
            values.put (MovieContract.MovieEntry.COLUMN_POSTER_IMAGE, moviePoster);
        }
        catch (MalformedURLException e) {
            Log.e (LOG_TAG, "***" + e.toString());
            e.printStackTrace();
        }
        return values;
    }


}
