package com.ubiq.android.app.mobilemovies.utils;

/**
 * Created by T on 4/8/2016.
 */

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ubiq.android.app.mobilemovies.R;
import com.ubiq.android.app.mobilemovies.model.MovieTrailer;

import java.util.ArrayList;

/**
 * The array adapter that handles the display of the movie trailers
 */
public class MovieTrailerAdapter extends ArrayAdapter<MovieTrailer> {
    private final String TAG = MovieTrailerAdapter.class.getSimpleName();
    private ArrayList<MovieTrailer> mTrailers;
    private MovieTrailer            mMovieTrailer;
    private int                     mPosition;
    private Activity                mActivity;

    public MovieTrailerAdapter(ArrayList<MovieTrailer> trailers, Activity activity) {
        super(activity, android.R.layout.simple_list_item_1, trailers);
        mTrailers = trailers;
        mActivity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View       row = convertView;
        ViewHolder holder;
        //           Log.v(TAG, "mMovie = " + mMovie);
        Log.v(TAG, "position: " + String.valueOf(position));
        Log.v(TAG, "***all trailers " + mTrailers);
        mPosition = position;
        mMovieTrailer = mTrailers.get(position);

        ImageButton trailerButton;
        // if no view was passed in, inflate one
        if (null == row) {
            row = mActivity.getLayoutInflater().
                    inflate(R.layout.movie_trailer_layout, parent, false);
            holder = new ViewHolder();
            trailerButton = (ImageButton) row.findViewById(R.id.trailer_button);
            holder.trailerButton = trailerButton;
            holder.trailerName = (TextView) row.findViewById(R.id.trailer_name);
            row.setTag(holder);
        }
        else {
            holder = (ViewHolder) row.getTag();
        }
        //Load the trailer name and the image of the play button
        holder.trailerName.setText(mMovieTrailer.getName());
        Log.v(TAG, "holder.trailerName.text = " + holder.trailerName.getText());
        holder.trailerButton.setImageResource(R.mipmap.ic_play);

        // Set up the trailer button for activating trailer play
        trailerButton = (ImageButton) row.findViewById(R.id.trailer_button);
        trailerButton.setOnClickListener(new View.OnClickListener() {

            // When the trailer button is clicked, this code will play the trailer
            // as a Youtube video. We can't handle other trailer types just yet.
            @Override
            public void onClick(View v) {
                // playTrailer(v, mMovieTrailer);
                MovieTrailer movieTrailer = getMovieTrailer();
                Toast toast = Toast.makeText(mActivity,
                                             movieTrailer.getName(),
                                             Toast.LENGTH_SHORT);
                toast.show();
                String youtubeId = movieTrailer.getYoutubeSource();
                Log.v(TAG,
                      "Launching YouTube video for trailer " + Utils.YOU_TUBE_URL + youtubeId +
                              " named " + movieTrailer.getName());

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                                               Uri.parse(Utils.YOU_TUBE_APP_INTENT + youtubeId));
                    mActivity.startActivity(intent);
                }
                catch (ActivityNotFoundException ex) {
                    Log.v(TAG, ex.toString());
                    Log.v(TAG, Uri.parse(Utils.YOU_TUBE_URL + youtubeId).toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                                               Uri.parse(Utils.YOU_TUBE_URL + youtubeId));
                    mActivity.startActivity(intent);
                }

            }
        });

        return row;
    }


    /**
     * ViewHolder is just a tuple for holding the trailer button and trailer name
     */
    protected class ViewHolder {
        ImageButton trailerButton;
        TextView    trailerName;
    }

//    protected void playTrailer(View v, MovieTrailer trailer) {
//
//        Toast toast = Toast.makeText(mActivity,
//                                     trailer.getName(),
//                                     Toast.LENGTH_SHORT);
//        toast.show();
//        String youtubeId = trailer.getYoutubeSource();
//        Log.v(TAG,
//              "Launching YouTube video for trailer " + Utils.YOU_TUBE_URL + youtubeId +
//                      " named " + trailer.getName());
//
//        try {
//            Intent intent = new Intent(Intent.ACTION_VIEW,
//                                       Uri.parse(Utils.YOU_TUBE_APP_INTENT + youtubeId));
//            mActivity.startActivity(intent);
//        }
//        catch (ActivityNotFoundException ex) {
//            Log.v(TAG, ex.toString());
//            Log.v(TAG, Uri.parse(Utils.YOU_TUBE_URL + youtubeId).toString());
//            Intent intent = new Intent(Intent.ACTION_VIEW,
//                                       Uri.parse(Utils.YOU_TUBE_URL + youtubeId));
//            mActivity.startActivity(intent);
//        }
 //   }
    protected MovieTrailer getMovieTrailer() {
        return mMovieTrailer;
    }
}
