package com.ubiq.android.app.mobilemovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ubiq.android.app.mobilemovies.model.Movie;
import com.ubiq.android.app.mobilemovies.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;

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
