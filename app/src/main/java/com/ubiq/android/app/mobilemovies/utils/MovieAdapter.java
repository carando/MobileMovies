package com.ubiq.android.app.mobilemovies.utils;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.ubiq.android.app.mobilemovies.R;
import com.ubiq.android.app.mobilemovies.fragments.MovieGridFragment;
import com.ubiq.android.app.mobilemovies.model.Movie;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 *  MovieAdapter holds the images of the movie posters that we've fetched from themoviedb.org
 */
public class MovieAdapter extends ArrayAdapter<Movie> {
    private final String TAG = MovieAdapter.class.getCanonicalName();
    private Movie mMovie;
    private FragmentActivity activity;

    public MovieAdapter(FragmentActivity activity, ArrayList<Movie> movies) {
        super(activity, android.R.layout.simple_list_item_1, movies);
        this.activity = activity;
    }

    /**
     * Load the movie's poster (image) into the grid
     * @param position: the position (index) of the movie to load
     * @param convertView: the view
     * @param parent: the parent
     * @return View
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int  THUMB_NAIL_WIDTH  = 550;
        final int  THUMB_NAIL_HEIGHT = 775;

        View       row = convertView;
        ViewHolder holder;

        // if no view was passed in, inflate one
        if (null == row) {
            row = activity.getLayoutInflater().
                    inflate(R.layout.movie_image_layout, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) row.findViewById(R.id.movie_poster_image);
            row.setTag(holder);
        }
        else {
            holder = (ViewHolder) row.getTag();
        }
        try {
            // Get the movie for this list position and load it via Picasso call
            Log.v(TAG, "movie position = " + String.valueOf(position));
            mMovie      = getItem(position);
            URL url     = Utils.buildMovieImageURL(mMovie.getPosterPath());
            String moviePosterFile = mMovie.getImageFile();
            String loadSource = url.toString();
            if (moviePosterFile != null) loadSource = moviePosterFile;

            //Load the image from the URL or the file into imageView using Picasso library routines
            Picasso.with(getContext())
                    .load(loadSource)
                    .resize(THUMB_NAIL_WIDTH, THUMB_NAIL_HEIGHT)
                    .into(holder.imageView);
        }
        catch (MalformedURLException e) {
            Log.e(TAG, e.toString());
        }
        return row;
    }

    protected class ViewHolder {
        ImageView imageView;
    }
}