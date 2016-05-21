package com.ubiq.android.app.mobilemovies.utils;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ubiq.android.app.mobilemovies.R;
import com.ubiq.android.app.mobilemovies.model.MovieReview;
import com.ubiq.android.app.mobilemovies.widget.ExpandableTextView;

import java.util.ArrayList;

/**
 * The array adapter that handles the display of the movie trailers
 */
public class MovieReviewsAdapter extends ArrayAdapter<MovieReview> {
    private final String TAG = MovieReviewsAdapter.class.getSimpleName();
    private ArrayList<MovieReview> mReviews;
    private Activity               mActivity;

    public MovieReviewsAdapter(ArrayList<MovieReview> reviews, Activity activity) {
        super(activity, android.R.layout.simple_list_item_1, reviews);
        this.mReviews = reviews;
        this.mActivity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View       row = convertView;
        ViewHolder holder;
        Log.v(TAG, "position: " + String.valueOf(position));
        Log.v(TAG, "***all reviews " + mReviews);
        MovieReview review = mReviews.get(position);

        TextView reviewerName;
        TextView reviewText;

        // if no view was passed in, inflate one
        if (null == row) {
            row = mActivity.getLayoutInflater().
                    inflate(R.layout.movie_review_layout, parent, false);
            holder = new ViewHolder();

            reviewerName = (TextView) row.findViewById(R.id.reviewer_name);
            holder.reviewerName = reviewerName;

            reviewText = (TextView) row.findViewById(R.id.review_text);
            holder.reviewText = reviewText;

            row.setTag(holder);
        }
        else {
            holder = (ViewHolder) row.getTag();
        }
        //Load the trailer name and the image of the play button
        holder.reviewerName.setText("Review by: " + review.getAuthor());
        holder.reviewText.setText(review.getReview());
        return row;
    }

    /**
     * ViewHolder is just a tuple for holding the trailer button and trailer name
     */
    protected class ViewHolder {
        TextView           reviewerName;
        TextView reviewText;
    }
}
