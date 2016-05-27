package com.ubiq.android.app.mobilemovies.model;

import com.ubiq.android.app.mobilemovies.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * PopularMovies is a singleton class that holds the set of movies retrieved from
 * themoviedb.org using the user-specified sort option.
 */
public class PopularMovies {

    private final static int TIME_FOR_RELOAD = 4;   //reload data after 4 hours

    // the date when the movie data was loaded, if this application is on the stack for
    // a looooong time
    private static Calendar      loadDate       = null;
    // the static variable that implements the singleton
    private static PopularMovies sPopularMovies = null;

    // the array list of movies
    private ArrayList<Movie> mMovies = null;
    // the sort order of the current set of movies
    private Utils.SortOrder currentSortOrder;
    // the new requested sort order specified by the user, if any
    private Utils.SortOrder requestedSortOrder;

    /**
     * Constructor is private to ensure singleton
     */
    private PopularMovies() {
        final int INITIAL_CAPACITY = 40;
        mMovies = new ArrayList<Movie>(INITIAL_CAPACITY);
        currentSortOrder = Utils.SortOrder.NOT_SPECIFIED;
        requestedSortOrder = currentSortOrder;
    }

    /**
     * Returns or creates the static instance of the PopularMovies
     * @return PopularMovies instance
     */
    public static PopularMovies getInstance() {
        if (sPopularMovies == null) {
            sPopularMovies = new PopularMovies();
            loadDate       = new GregorianCalendar();
        }
        return sPopularMovies;
    }

    public Utils.SortOrder getSortOrder() {
        return currentSortOrder;
    }

    public void setSortOrder(Utils.SortOrder sortOrder) {
        this.currentSortOrder = sortOrder;
    }

    public void setRequestedSortOrder (Utils.SortOrder sortOrder) {
        requestedSortOrder = sortOrder;
    }

    public ArrayList<Movie> getMovies() {
        return mMovies;
    }

    public Movie getMovie(int index) {
        return mMovies.get(index);
    }


    public void addAll (ArrayList <Movie> movies) {
        mMovies.addAll(movies);
    }



    // Clear the current movie collection and set the load date.
    // New movie data should be immediately loaded after the clear either by the
    // add or addAll method
    public void clear(){
        mMovies.clear();
        loadDate = new GregorianCalendar();
        setSortOrder(Utils.SortOrder.NOT_SPECIFIED);
    }

    // Determine if a call the RESTful interface is needed.
    // Three conditions might indicate a reload:
    //  1. There are no movies loaded
    //  2. The sort order for the collection has changed (a different set
    //     of movies was requested).
    //  3. More than 4 hours have passed since the movies were loaded

    public boolean isReloadNeeded(Utils.SortOrder sortOrderPref){
        if (PopularMovies.getInstance().getMovies().isEmpty()) return true;

        // Check if the newly specified sort order (sortOrderPref) is the same as that
        // for the current collection (currentSortOrder). If the sort order is not the same,
        // we'll need to fetch new movies.
        // Save sortOrderPref here because when we reload the collection, we will have
        // used sortOrderPref to select the new batch of movies and we must remember it.
        if (sortOrderPref != currentSortOrder) {
            requestedSortOrder = sortOrderPref;
            return true;
        }

        // Check when the current set of movies were loaded. Reload if they're old

        Calendar now         = new GregorianCalendar();
        int dayOfMonthNow    = now.get(Calendar.DAY_OF_MONTH);
        int dayOfWeekNow     = now.get(Calendar.DAY_OF_WEEK);
        int hourNow          = now.get(Calendar.HOUR_OF_DAY);

        int dayOfMonthLoaded = loadDate.get(Calendar.DAY_OF_MONTH);
        int dayOfWeekLoaded  = loadDate.get(Calendar.DAY_OF_WEEK);
        int hourLoaded       = loadDate.get(Calendar.HOUR_OF_DAY);

        // If day of month or day of week differ from current month, day: reload
        if ((dayOfMonthNow != dayOfMonthLoaded) || (dayOfWeekNow != dayOfWeekLoaded)){
            return true;
        }

        // If movies were loaded > 4 hours ago: reload
        if ((hourNow - hourLoaded) >= TIME_FOR_RELOAD) return true;

        return false;
    }

    protected void add (Movie movie) {
        mMovies.add(movie);
    }
}

