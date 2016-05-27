package com.ubiq.android.app.mobilemovies.data;

import android.provider.BaseColumns;

/**
 *  The tables that store favorite movie information:
 *  MovieEntry: Parent table
 *  MovieTrailers: child table
 *  MovieReviews: child table
 */
public class MovieContract {

    public static final class MovieEntry implements BaseColumns {

        public static final String TABLE_NAME = "favorite_movies";
        // the movie unique identifier; we'll use this as a foreign key elsewhere
        public static final String COLUMN_MOVIE_ID = "movie_id";
        // the title of the movie
        public static final String COLUMN_MOVIE_TITLE = "movie_title";
        // the movie rating
        public static final String COLUMN_MOVIE_RATING = "movie_rating";
        // the year the movie was released
        public static final String COLUMN_MOVIE_RELEASE_YEAR = "movie_release_year";
        // a description of the movie
        public static final String COLUMN_MOVIE_DESCRIPTION = "movie_description";
        // the movie running time
        public static final String COLUMN_MOVIE_RUNNING_TIME = "movie_runtime";
        // the path to the poster -- an image
        public static final String COLUMN_POSTER_PATH = "movie_poster_path";
        // the poster image
        public static final String COLUMN_POSTER_FILE = "movie_poster_file";

    }

    public static final class MovieTrailers implements BaseColumns {
        // the table name
        public static final String TABLE_NAME = "movie_trailers";
        // the movie unique identifier: a foreign key
        public static final String COLUMN_MOVIE_ID = "movie_id";
        // the title of the movie trailer
        public static final String COLUMN_TRAILER_NAME = "trailer_name";
        // the youtube url
        public static final String COLUMN_TRAILER_LOCATION = "trailer_location";
    }

    public static final class MovieReviews implements BaseColumns {
        // the table name
        public static final String TABLE_NAME              = "movie_reviews";
        // the movie unique identifier: a foreign key
        public static final String COLUMN_MOVIE_ID         = "movie_id";
        // the name of the reviewer
        public static final String COLUMN_REVIEWER_NAME    = "reviewer_name";
        // the actual review
        public static final String COLUMN_REVIEW_TEXT      = "review_text";
    }

    public static final String MOVIE_ENTRY_CREATE_SQL =
            "CREATE TABLE " + MovieEntry.TABLE_NAME + "(" +
                    MovieEntry.COLUMN_MOVIE_ID + " INTEGER PRIMARY KEY, " +
                    MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                    MovieEntry.COLUMN_MOVIE_RATING + " TEXT, " +
                    MovieEntry.COLUMN_MOVIE_RELEASE_YEAR + " TEXT, " +
                    MovieEntry.COLUMN_MOVIE_DESCRIPTION + " TEXT," +
                    MovieEntry.COLUMN_MOVIE_RUNNING_TIME + " TEXT," +
                    MovieEntry.COLUMN_POSTER_PATH + " TEXT, " +
                    MovieEntry.COLUMN_POSTER_FILE + " TEXT);";

    public static final String MOVIE_TRAILERS_ENTRY_CREATE_SQL =
            "CREATE TABLE " + MovieTrailers.TABLE_NAME     + "(" +
                    BaseColumns._ID                        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MovieTrailers.COLUMN_MOVIE_ID          + " INTEGER, " +
                    MovieTrailers.COLUMN_TRAILER_NAME      + " TEXT NOT NULL, " +
                    MovieTrailers.COLUMN_TRAILER_LOCATION  + " TEXT NOT NULL, "  +
                    "FOREIGN KEY (" + MovieTrailers.COLUMN_MOVIE_ID + ") REFERENCES " +
                        MovieEntry.TABLE_NAME + ");";

    public static final String MOVIE_REVIEWS_ENTRY_CREATE_SQL =
            "CREATE TABLE " + MovieReviews.TABLE_NAME     + "(" +
                    BaseColumns._ID                       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MovieReviews.COLUMN_MOVIE_ID          + " INTEGER, " +
                    MovieReviews.COLUMN_REVIEWER_NAME     + " TEXT NOT NULL, " +
                    MovieReviews.COLUMN_REVIEW_TEXT       + " TEXT NOT NULL, "  +
                    "FOREIGN KEY (" + MovieReviews.COLUMN_MOVIE_ID + ") REFERENCES " +
                    MovieEntry.TABLE_NAME + ");";

}

