package com.example.dharmaraj.popularmovie.utilities;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.example.dharmaraj.popularmovie.data.TrailerDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static com.example.dharmaraj.popularmovie.data.MoviesContract.FavouriteMoviesEntry.COLUMN_MOVIE_DESCRIPTION;
import static com.example.dharmaraj.popularmovie.data.MoviesContract.FavouriteMoviesEntry.COLUMN_MOVIE_ID;
import static com.example.dharmaraj.popularmovie.data.MoviesContract.FavouriteMoviesEntry.COLUMN_MOVIE_NAME;
import static com.example.dharmaraj.popularmovie.data.MoviesContract.FavouriteMoviesEntry.COLUMN_MOVIE_POSTER;
import static com.example.dharmaraj.popularmovie.data.MoviesContract.FavouriteMoviesEntry.COLUMN_MOVIE_RATINGS;
import static com.example.dharmaraj.popularmovie.data.MoviesContract.FavouriteMoviesEntry.COLUMN_MOVIE_RELEASE_DATE;

/**
 * this class handles parsing of all data.
 */
public final class JsonParsingUtilities {
    //the base url for youtube with which the trailer paths will be appended
    public static final String BASE_URL_FOR_YOUTUBE = "https://www.youtube.com/watch";
    public static final String YOUTUBE_QUERY_PARAM = "v";
    //node names in the json objects
    private static final String MOVIE_DB_RESULT = "results";
    private static final String MOVIE_DB_POSTER_PATH = "poster_path";
    private static final String MOVIE_DB_OVERVIEW = "overview";
    private static final String MOVIE_DB_RELEASE_DATE = "release_date";
    private static final String MOVIE_DB_TITLE = "title";
    private static final String MOVIE_DB_VOTE = "vote_average";
    private static final String MOVIE_DB_ID = "id";

    /**
     * it parses the given json String and get the required values and put them as a contentValues
     *
     * @param jsonResponse
     * @return ContentValue Array which can be easily bulk inserted
     */
    public static ContentValues[] getContentValuesFromJson(String jsonResponse) {
        JSONObject jsonObject;
        ContentValues[] movies = null;
        try {
            jsonObject = new JSONObject(jsonResponse);
            JSONArray resultsJsonArray = jsonObject.getJSONArray(MOVIE_DB_RESULT);
            //initialize the contentValues array to the size of the json array result's length
            movies = new ContentValues[resultsJsonArray.length()];

            for (int i = 0; i < resultsJsonArray.length(); i++) {
                JSONObject movieJsonObject = resultsJsonArray.getJSONObject(i);
                //get all the required values from the json
                String posterPath = movieJsonObject.getString(MOVIE_DB_POSTER_PATH);
                String overview = movieJsonObject.getString(MOVIE_DB_OVERVIEW);
                String releaseDate = movieJsonObject.getString(MOVIE_DB_RELEASE_DATE);
                String title = movieJsonObject.getString(MOVIE_DB_TITLE);
                double voteAverage = movieJsonObject.getDouble(MOVIE_DB_VOTE);
                String movieid = movieJsonObject.getString(MOVIE_DB_ID);

                ContentValues contentValues = new ContentValues();
                //put all the values in to the contentValues
                contentValues.put(COLUMN_MOVIE_ID, movieid);
                contentValues.put(COLUMN_MOVIE_NAME, title);
                contentValues.put(COLUMN_MOVIE_DESCRIPTION, overview);
                contentValues.put(COLUMN_MOVIE_RATINGS, voteAverage);
                contentValues.put(COLUMN_MOVIE_RELEASE_DATE, releaseDate);
                //get the url to load image using the utility method
                URL url = NetworkUtilities.getImageUrl(posterPath);
                Bitmap posterImage = null;
                try {
                    //get the image from the url and decode it to Bitmap
                    posterImage = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //convert the BitMap to byte array using the utility method so it can be stored in the database
                contentValues.put(COLUMN_MOVIE_POSTER, DbBitmapUtility.getBytes(posterImage));
                movies[i] = contentValues;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return movies;
    }

    /**
     * get the trailer urls from the given json
     *
     * @param jsonResponse
     * @return ArrayList of youtube trailer urls of the given movie's json
     */
    public static ArrayList<TrailerDetails> parseTrailerUrlsFromJson(String jsonResponse) {
        JSONObject jsonObject;
        ArrayList<TrailerDetails> urlArrayList = new ArrayList<>();
        try {
            jsonObject = new JSONObject(jsonResponse);
            JSONArray resultsJsonArray = jsonObject.getJSONArray("results");
            for (int i = 0; i < resultsJsonArray.length(); i++) {
                JSONObject trailersJsonObject = resultsJsonArray.getJSONObject(i);
                String key = trailersJsonObject.getString("key");
                String name = trailersJsonObject.getString("name");
                //build the url for trailer.
                Uri uri = Uri.parse(BASE_URL_FOR_YOUTUBE).buildUpon()
                        .appendQueryParameter(YOUTUBE_QUERY_PARAM, key)
                        .build();
                urlArrayList.add(new TrailerDetails(uri, name));
            }
            return urlArrayList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get the trailer urls from the given json
     *
     * @param jsonResponse
     * @return
     */
    public static ArrayList<String[]> parseReviewsFromJson(String jsonResponse) {
        JSONObject jsonObject;
        ArrayList<String[]> reviewsArrayList = new ArrayList<>();
        try {
            jsonObject = new JSONObject(jsonResponse);
            JSONArray resultsJsonArray = jsonObject.getJSONArray("results");
            for (int i = 0; i < resultsJsonArray.length(); i++) {
                JSONObject reviewJsonObject = resultsJsonArray.getJSONObject(i);
                //get Author name , who has the has written this review
                String author = reviewJsonObject.getString("author");
                //get the review content
                String content = reviewJsonObject.getString("content").trim();
                reviewsArrayList.add(new String[]{author, content});
            }
            return reviewsArrayList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
