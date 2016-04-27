package com.ubiq.android.app.mobilemovies.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ubiq.android.app.mobilemovies.R;
import com.ubiq.android.app.mobilemovies.model.MovieReview;
import com.ubiq.android.app.mobilemovies.utils.MovieReviewsAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieReviewsFragment extends Fragment {
    private ArrayAdapter<MovieReview>  mMovieReviewAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        DetailActivityFragment parentFragment = (DetailActivityFragment)getParentFragment();
        MovieReviewsAdapter adapter = parentFragment.getMovieReviewAdapter();
        View view = inflater.inflate(R.layout.movie_reviews_layout, container, false);
        Log.v("MovieReviewsFragment", String.valueOf(adapter.getCount()));
        ListView reviewView          = (ListView)view.findViewById(R.id.reviews_list_view);
        reviewView.setAdapter(adapter);
        return view;
    }
}
