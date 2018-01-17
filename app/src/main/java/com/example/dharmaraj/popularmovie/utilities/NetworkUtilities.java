package com.example.dharmaraj.popularmovie.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.example.dharmaraj.popularmovie.R;
import com.example.dharmaraj.popularmovie.data.MoviesContract;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Class for all the NetWorkUtility function
 */

public final class NetworkUtilities {

    //base url for the poster image
    private static final String BASE_IMAGE_URL_FOR_IMAGE = "http://image.tmdb.org/t/p/";
    //size of the image(poster)
    private static final String SIZE = "w185";
    //base url to fetch movie database
    private static final String BASR_URL = "https://api.themoviedb.org/3/movie";
    //api key
    private static final String API_KEY = "api_key";
    private static final String API_KEY_VALUE = "YOUR_API_KEY";

    public static final String KEY_FOR_GETTING_TRAILER_URL = "videos";
    public static final String KEY_FOR_GETTING_REVIEW_URL = "reviews";

    /**
     * builds a url to fetch movie database
     *
     * @return Uri current uri based on sharedPreference
     */
    public static Uri getCurrentUri(Context context) {
        Uri uri = null;
        //get the orderBy preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String orderBy = sharedPreferences.getString(context.getString(R.string.orderby_lsit_preference_key),
                context.getString(R.string.orderby_popular));

        if (orderBy.equals(context.getString(R.string.orderby_favourite))) {
            uri = MoviesContract.FavouriteMoviesEntry.CONTENT_URI;
        } else if (orderBy.equals(context.getString(R.string.orderby_popular))) {
            uri = MoviesContract.PopularMoviesEntry.CONTENT_URI;
        } else if (orderBy.equals(context.getString(R.string.orderby_top_rated))) {
            uri = MoviesContract.TopRatedMoviesEntry.CONTENT_URI;
        }
        return uri;
    }

    /**
     * this method returns the urls need to fetch data for both popular and top rated movies
     *
     * @param context
     * @return URL[] containing the urls for popular movies and top rated movies.
     */
    public static URL[] getUrlForPopularAndTopRated(Context context) {
        URL[] returnUrl = new URL[2];
        String orderBy = null;
        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                orderBy = context.getString(R.string.orderby_popular);
            } else if (i == 1) {
                orderBy = context.getString(R.string.orderby_top_rated);
            }
            Uri uri = Uri.parse(BASR_URL).buildUpon()
                    .appendPath(orderBy)
                    .appendQueryParameter(API_KEY, API_KEY_VALUE)
                    .build();
            try {
                returnUrl[i] = new URL(uri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return returnUrl;
    }

    /**
     * helper method to connect to the internet and get json data
     */
    public static String fetchData(URL url) {
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream in = httpURLConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return null;
    }

    /**
     * returns the url to fetch the image
     *
     * @param posterPath
     * @return
     */
    public static URL getImageUrl(String posterPath) {
        URL url = null;
        try {
            url = new URL(BASE_IMAGE_URL_FOR_IMAGE + SIZE + posterPath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * this method returns the url required to fetch trailer or review detail .
     *
     * @param movie_id unique id of the movie
     * @param extra    key which determine whether to return url for trailer or review
     * @return
     */
    public static URL getAppropriateUrlForReviewOrTrailer(long movie_id, String extra) {
        if (!(extra.equals(KEY_FOR_GETTING_TRAILER_URL) || extra.equals(KEY_FOR_GETTING_REVIEW_URL))) {
            return null;
        }
        Uri uri = Uri.parse(BASR_URL).buildUpon()
                .appendPath(String.valueOf(movie_id))
                .appendPath(extra)
                .appendQueryParameter(API_KEY, API_KEY_VALUE)
                .build();
        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
