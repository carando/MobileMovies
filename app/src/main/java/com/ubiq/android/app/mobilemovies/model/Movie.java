package com.ubiq.android.app.mobilemovies.model;


import android.graphics.Bitmap;

import com.ubiq.android.app.mobilemovies.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Movie contains all the information we will use in displaying Movies to the user.
 */
public class Movie {

    private boolean movieDetailLoaded;

    private int     id;
    private String  title;
    private String  rating;
    private String  releaseYear;
    private String  overview;
    private int     numberOfReviews = 0;
    private int     runningTime     = 0;
    private String  posterPath;
    private String  imageFile = null;
    private boolean favorite = false;  // not stored in database


    private List<MovieReview>  movieReviews = null;
    private List<MovieTrailer> trailers     = null;


    public Movie(int id, String title, String rating, String releaseYear, String overview,
                 String posterPath) {
        this.id           = id;
        this.title        = title;
        this.rating       = rating;
        this.overview     = overview;
        this.releaseYear  = releaseYear;
        this.posterPath   = posterPath;
        this.favorite     = false;

        movieDetailLoaded = false;
        imageFile         = null;
    }


    public int getId() {
        return id;
    }

    public String getImageFile () {
        return imageFile;
    }

    public boolean isFavorite() {
        return favorite;
    }

    /**
     *
     * @return number of reviews recorded.
     */
    public int getNumberOfReviews() {
        return numberOfReviews;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getReleaseYear() {
        return releaseYear;
    }

    public int getRunningTime () {
        return runningTime;
    }

    public String getTitle() {
        return title;
    }

    public String getRating() {
        return rating;
    }

    public boolean isMovieDetailLoaded() {
        return movieDetailLoaded;
    }



    public void addReview(MovieReview movieReview) {
        // performed for side-effect to initialize the movie reviews array
        getMovieReviews ();
        // add the review if it is not already present
        if (!movieReviews.contains(movieReview)) {
            movieReviews.add(movieReview);
            numberOfReviews = movieReviews.size();
        }
    }

    public void addTrailer (MovieTrailer trailer) {
        // performed for side-effect to initialize the movie trailers
        getMovieTrailers();
        if (!trailers.contains(trailer)) {
            trailers.add(trailer);
        }
    }

    public List<MovieReview> getMovieReviews() {
        // return the movie reviews; if none have been loaded
        // return an empty list with default capacity
        if (null == movieReviews) {
            movieReviews = new ArrayList <MovieReview> ();
        }
        return movieReviews;
    }



    public List<MovieTrailer> getMovieTrailers() {
        final  int INITIAL_CAPACITY = 5;
        // return the movie trailers; if no trailers have been loaded
        // return an empty list of small capacity (they're aren't many
        // trailers, usually
        if (null == trailers) {
            trailers = new ArrayList <MovieTrailer>(INITIAL_CAPACITY);
        }
        return trailers;
    }

    public void setFavorite (boolean state) {
        favorite = state;
    }

    /**
     * Want to indicate that detail for the movie has been loaded so
     * as to avoid re-accessing it. This will only be called to set
     * the value to true
     * @param movieDetailLoaded boolean indicating detail is loaded
     */
    public void setMovieDetailLoaded(boolean movieDetailLoaded) {
        this.movieDetailLoaded = movieDetailLoaded;
    }

    public void setNumberOfReviews (int numberOfReviews) {
        this.numberOfReviews = numberOfReviews;
    }

    /**
     * Stores the movie poster (the bytearray) as a bitmap into a
     * file
     * @param byteArray: the movie poster
     */
    public void setPosterImage(byte[] byteArray) {
        if (imageFile != null) return;
        Bitmap bitmap = Utils.convertByteArrayToBitmap(byteArray);
        String baseFileName  = Utils.createBitmapFileName();
        imageFile = Utils.saveBitmapToFile(bitmap, baseFileName);
    }

    public void setRunningtime (int runtime) {
        this.runningTime = runtime;
    }

    public void setImageFile (String fileName) {
        imageFile = fileName;
    }





    @Override
    public String toString() {
        return "Movie{" +
                ", movieDetailLoaded=" + movieDetailLoaded +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", rating='" + rating + '\'' +
                ", releaseYear='" + releaseYear + '\'' +
                ", overview='" + overview + '\'' +
                ", numberOfReviews=" + numberOfReviews +
                ", runningTime=" + runningTime +
                ", posterPath='" + posterPath + '\'' +
                ", imageFile='" + imageFile + '\'' +
                ", favorite ='" + favorite + '\'' +
                ", movieReviews=" + movieReviews +
                ", trailers=" + trailers +
                '}';
    }
}

