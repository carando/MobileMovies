package com.ubiq.android.app.mobilemovies.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ubiq.android.app.mobilemovies.R;
import com.ubiq.android.app.mobilemovies.model.MovieTrailer;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieTrailersFragment extends Fragment {
    private ArrayAdapter<MovieTrailer> mMovieTrailerAdapter = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        DetailActivityFragment parentFragment = (DetailActivityFragment) getParentFragment();
        mMovieTrailerAdapter = parentFragment.getMovieTrailerAdapter();
        View     view       = inflater.inflate(R.layout.movie_trailers_layout, container, false);
        Log.v("MovieTrailersFragment", String.valueOf(mMovieTrailerAdapter.getCount()));
        ListView trailersView = (ListView) view.findViewById(R.id.movie_trailers_layout_listview);
        trailersView.setAdapter(mMovieTrailerAdapter);
        parentFragment.setTrailerView(trailersView);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
