package com.example.dharmaraj.popularmovie.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * this class contains all the constants required for the database
 * it contains the column names, uri and paths
 */
public final class MoviesContract {
    private MoviesContract() {
    }
    //authority it must be unique
    public static final String CONTENT_AUTHORITY="com.example.dharmaraj.popularmovie";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+ CONTENT_AUTHORITY);
    //all the table paths
    public static final String PATH_FAV = FavouriteMoviesEntry.TABLE_NAME;
    public static final String PATH_POPULAR = PopularMoviesEntry.TABLE_NAME;
    public static final String PATH_TOP_RATED = TopRatedMoviesEntry.TABLE_NAME;
    public static final String PATH_TRAILER_AND_REVIEWS = TrailersAndReviewsEntry.TABLE_NAME;

    //this are the common fields in all the tables
    public static class CommonFields implements BaseColumns{
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_MOVIE_NAME = "name";
        public static final String COLUMN_MOVIE_POSTER = "poster_img";
        public static final String COLUMN_MOVIE_RELEASE_DATE = "release_date";
        public static final String COLUMN_MOVIE_RATINGS = "ratings";
        public static final String COLUMN_MOVIE_DESCRIPTION = "description";
    }

    public static class FavouriteMoviesEntry extends CommonFields {
        //the uri for table containing favourite movies
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAV).build();
        //name of the table
        public static final String TABLE_NAME = "favouriteMovies";

    }


    public static class PopularMoviesEntry extends CommonFields{
        //the uri for table containing Popular movies
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POPULAR).build();

        public static final String TABLE_NAME = "popularMovies";
    }

    public static class TopRatedMoviesEntry extends CommonFields{
        //the uri for table containing Top rated movies
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TOP_RATED).build();

        public static final String TABLE_NAME = "topRatedMovies";
    }

    public static class TrailersAndReviewsEntry implements BaseColumns{

        public static final String TABLE_NAME = "trailersAndReviews";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TRAILERS = "trailers";
        public static final String COLUMN_REVIEWS = "reviews";
        //the uri for table containing trailer and review details of the movies
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER_AND_REVIEWS).build();
    }



}
