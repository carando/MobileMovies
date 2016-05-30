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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * MovieAdapter holds the images of the movie posters that we've fetched from themoviedb.org
 */
public class MovieAdapter extends ArrayAdapter<Movie> {
    private final String TAG = MovieAdapter.class.getCanonicalName();
    private Movie mMovie;
    private FragmentActivity activity;

    /**
     * Instantiates a new Movie adapter.
     *
     * @param activity the invoking activity. Needed to inflate the layout
     * @param movies   the movies we are "adapting"
     */
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
        final int THUMB_NAIL_WIDTH = 550;
        final int THUMB_NAIL_HEIGHT = 775;

        View row = convertView;
        ViewHolder holder;

        // if no view was passed in, inflate one
        if (null == row) {
            row = activity.getLayoutInflater().
                    inflate(R.layout.movie_image_layout, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) row.findViewById(R.id.movie_poster_image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        try {
            // Get the movie for this list position and load it via Picasso call
            Log.v(TAG, "movie position = " + String.valueOf(position));
            mMovie = getItem(position);

            // check for a local file holder the poster bitmap
            String moviePosterFile = mMovie.getImageFile();
            if (isValidPosterFile (moviePosterFile) ){
                File file = new File (moviePosterFile);
                //Load the image from the URL or the file into imageView using Picasso library routines
                Picasso.with(getContext())
                        .load(file)
                        .resize(THUMB_NAIL_WIDTH, THUMB_NAIL_HEIGHT)
                        .error(R.drawable.play_it)
                        .into(holder.imageView);
            } else {
                URL url = Utils.buildMovieImageURL(mMovie.getPosterPath());
                Picasso.with(getContext())
                        .load(url.toString())
                        .resize(THUMB_NAIL_WIDTH, THUMB_NAIL_HEIGHT)
                        .error(R.drawable.play_it)
                        .into(holder.imageView);
            }

        } catch(MalformedURLException e){
        Log.e(TAG, e.toString());
    }
    return row;

}

protected class ViewHolder {
    ImageView imageView;
    }

    private boolean isValidPosterFile (String moviePosterFile) {
        if (moviePosterFile == null) return false;
        File file = new File(moviePosterFile);
        if (file.exists()) return true;
        return false;
    }
}
