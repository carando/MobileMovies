package com.ubiq.android.app.mobilemovies.data;

import android.content.ContentValues;
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

    public  static long   MOVIE_IN_FAVORITES = -2;
    private static String LOG_TAG = MovieOToRMapper.class.getSimpleName();

    private static MovieOToRMapper mMovieOToRMapper = null;
    private static MovieDBHelper   dbHelper         = null;

    private MovieOToRMapper(MovieDBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public static MovieOToRMapper getInstance(MovieDBHelper dbHelper) {
        if (mMovieOToRMapper == null) {
            mMovieOToRMapper = new MovieOToRMapper(dbHelper);
        }
        return mMovieOToRMapper;
    }
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
            favoriteMovies.add(movie);
            rowsToRead = cursor.moveToNext();
        }

        return favoriteMovies;
    }

    public long insertMovie(Movie aMovie) {
        Log.v(LOG_TAG, "***insert movie " + aMovie.toString());
        if (movieInFavorites(aMovie)) {
            return MOVIE_IN_FAVORITES;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = getValues (aMovie);
        long result = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
        return result;
      }

    public long insertMovie (ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = db.insert (MovieContract.MovieEntry.TABLE_NAME, null, values);
        return result;
    }



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
        return foundMovie;
    }

    private ContentValues getValues (Movie aMovie) {
        ContentValues values = new ContentValues();
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_ID,           aMovie.getId());
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,        aMovie.getTitle());
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_DESCRIPTION,  aMovie.getOverview());
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_YEAR, aMovie.getReleaseYear());
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_RATING,       aMovie.getRating());
        values.put (MovieContract.MovieEntry.COLUMN_MOVIE_RUNNING_TIME, aMovie.getRunningTime());
        values.put (MovieContract.MovieEntry.COLUMN_POSTER_PATH,        aMovie.getPosterPath());

        byte[] moviePoster  = null;
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
