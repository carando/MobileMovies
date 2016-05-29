package com.ubiq.android.app.mobilemovies.utils.decorators;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;

import com.ubiq.android.app.mobilemovies.utils.MovieAdapter;

/**
 * Created by T on 5/29/2016.
 */
public class MovieTaskDecorator implements Decorator {
    private FragmentActivity mInvokingActivity;
    private MovieAdapter     mMovieAdapter;

    public MovieTaskDecorator (FragmentActivity invokingActivity, MovieAdapter adapter) {
        mInvokingActivity = invokingActivity;
        mMovieAdapter     = adapter;
    }

    public FragmentActivity getInvokingActivity() {
        return mInvokingActivity;
    }

    public MovieAdapter getMovieAdapter() {
        return mMovieAdapter;
    }
}
