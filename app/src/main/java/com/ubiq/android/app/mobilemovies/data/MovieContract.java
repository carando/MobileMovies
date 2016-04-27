package com.ubiq.android.app.mobilemovies.data;

import android.provider.BaseColumns;

/**
 * Created by T on 11/22/2015.
 */
public class MovieContract {

    public static final class MovieEntry implements BaseColumns {

        public static final String TABLE_NAME                = "favorite_movies";
        // the movie unique identifier; we'll use this as a foreign key elsewhere
        public static final String COLUMN_MOVIE_ID           = "movie_id";
        // the title of the movie
        public static final String COLUMN_MOVIE_TITLE        = "movie_title";
        // the movie rating
        public static final String COLUMN_MOVIE_RATING       = "movie_rating";
        // the year the movie was released
        public static final String COLUMN_MOVIE_RELEASE_YEAR = "movie_release_year";
        // a description of the movie
        public static final String COLUMN_MOVIE_DESCRIPTION  = "movie_description";
        // the movie running time
        public static final String COLUMN_MOVIE_RUNNING_TIME = "movie_runtime";
        // the path to the poster -- an image
        public static final String COLUMN_POSTER_PATH        = "movie_poster_path";
        // the poster image
        public static final String COLUMN_POSTER_IMAGE       = "movie_poster_image";

    }


    public static final String MOVIE_ENTRY_CREATE_SQL =
            "CREATE TABLE " + MovieEntry.TABLE_NAME      + "(" +
                    MovieEntry.COLUMN_MOVIE_ID           + " INTEGER PRIMARY KEY, " +
                    MovieEntry.COLUMN_MOVIE_TITLE        + " TEXT NOT NULL, " +
                    MovieEntry.COLUMN_MOVIE_RATING       + " TEXT, " +
                    MovieEntry.COLUMN_MOVIE_RELEASE_YEAR + " TEXT, " +
                    MovieEntry.COLUMN_MOVIE_DESCRIPTION  + " TEXT,"  +
                    MovieEntry.COLUMN_MOVIE_RUNNING_TIME + " TEXT,"  +
                    MovieEntry.COLUMN_POSTER_PATH        + " TEXT, " +
                    MovieEntry.COLUMN_POSTER_IMAGE       + " BLOB);";

}