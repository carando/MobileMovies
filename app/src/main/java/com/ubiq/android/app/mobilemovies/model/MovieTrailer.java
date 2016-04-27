package com.ubiq.android.app.mobilemovies.model;

/**
 * A MovieTrailer for a Movie. The name is assumed to be unique. We will only
 * store (and allow for replay) the youtube trailer for now.
 */
public class MovieTrailer {
    private String name;
    private String youtubeSource; // only use youtube source for now


    public MovieTrailer (String name, String source) {
        this.name          = name;
        this.youtubeSource = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MovieTrailer that = (MovieTrailer) o;

        return !(name != null ? !name.equals(that.name) : that.name != null);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public String getName() {
        return name;
    }

    public String getYoutubeSource() {
        return youtubeSource;
    }

    @Override
    public String toString() {
        return "MovieTrailer{" +
                "name='" + name + '\'' +
                ", youtubeSource='" + youtubeSource + '\'' +
                '}';
    }

}
