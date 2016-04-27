package com.ubiq.android.app.mobilemovies.model;


import java.util.ArrayList;
import java.util.List;

/**
 * Movie contains all the information we will use in displaying Movies to the user.
 */
public class Movie {

    private boolean movieDetailLoaded;

    private int    id;
    private String title;
    private String rating;
    private String releaseYear;
    private String overview;
    private int numberOfReviews = 0;
    private int runningTime     = 0;
    private String backdropPath;
    private String posterPath;


    private List<MovieReview>  movieReviews = null;
    private List<MovieTrailer> trailers     = null;


    public Movie(int id, String title, String rating, String releaseYear, String overview,
                 String backdropPath, String posterPath) {
        this.id = id;
        this.title = title;
        this.rating = rating;
        this.overview = overview;
        this.releaseYear = releaseYear;
        this.backdropPath = backdropPath;  // not used just yet
        this.posterPath   = posterPath;

        movieDetailLoaded  = false;
    }

    /**
    * Not sure if we need this....
     */
    public String getBackdropPath() {
        return backdropPath;
    }

    public int getId() {
        return id;
    }

    /**
     *
     * @return number of reviews recorded. Used next iteration
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



    @Override
    public String toString() {
        return "Movie{" +
                "trailers=" + trailers +
                ", backdropPath='" + backdropPath + '\'' +
                ", id=" + id +
                ", movieReviews=" + movieReviews +
                ", overview='" + overview + '\'' +
                ", posterPath='" + posterPath + '\'' +
                ", movie_rating='" + rating + '\'' +
                ", releaseYear='" + releaseYear + '\'' +
                ", title='" + title + '\'' +
                '}';
    }



    public void addReview(MovieReview movieReview) {
        // performed for side-effect to initialize the movie reviews array
        getMovieReviews ();
        // add the review if it is not already present
        if (!movieReviews.contains(movieReview)) {
            movieReviews.add(movieReview);
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

    public void setMovieDetailLoaded(boolean movieDetailLoaded) {
        this.movieDetailLoaded = movieDetailLoaded;
    }

    public void setNumberOfReviews (int numberOfReviews) {
        this.numberOfReviews = numberOfReviews;
    }

    public void setRunningtime (int runtime) {
        this.runningTime = runtime;
    }

}

