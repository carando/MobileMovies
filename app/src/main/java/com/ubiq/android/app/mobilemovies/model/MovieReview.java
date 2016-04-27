package com.ubiq.android.app.mobilemovies.model;

/**
 * Object with information on movie reviews. Used in next iteration
 */
public class MovieReview {
    private String id;
    private String author;
    private String review;

    public MovieReview(String id, String author, String review) {
        this.id     = id;
        this.author = author;
        this.review = review;
    }
    /**
     * Equality check for movie reviews
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MovieReview that = (MovieReview) o;

        if (!id.equals(that.id)) return false;
        return author.equals(that.author);
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getReview() {
        return review;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + author.hashCode();
        return result;
    }
    @Override
    public String toString() {
        return "MovieReview{" +
                "author='" + author + '\'' +
                ", id=" + id +
                ", review='" + review + '\'' +
                '}';
    }


}
