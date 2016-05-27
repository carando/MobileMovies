package com.ubiq.android.app.mobilemovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Class to manage SQLite database
 */
public class MovieDBHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = MovieDBHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "movie.db";
    private static final int    DATABASE_VERSION = 2;


    public MovieDBHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v (LOG_TAG, "***onCreate");
        db.execSQL(MovieContract.MOVIE_ENTRY_CREATE_SQL);
        db.execSQL(MovieContract.MOVIE_TRAILERS_ENTRY_CREATE_SQL);
        db.execSQL(MovieContract.MOVIE_REVIEWS_ENTRY_CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       // When mods are made to the database, the correct set of ALTER statements
       // will appear here
    }

}
