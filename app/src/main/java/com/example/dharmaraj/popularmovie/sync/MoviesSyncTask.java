package com.example.dharmaraj.popularmovie.sync;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;

import com.example.dharmaraj.popularmovie.data.MoviesContract;
import com.example.dharmaraj.popularmovie.utilities.JsonParsingUtilities;
import com.example.dharmaraj.popularmovie.utilities.NetworkUtilities;

import java.net.URL;
import java.util.ArrayList;

import static com.example.dharmaraj.popularmovie.utilities.NetworkUtilities.fetchData;


public class MoviesSyncTask {
    /**
     * Performs the network request for updated movies, parses the JSON from that request, and
     * inserts the new movie data information into our ContentProvider.
     *
     * @param context Used to access utility methods and the ContentResolver
     */
    synchronized public static void syncMovie(Context context) {

        try {
            //get all the url needed to be fetched (popular and top rated)
            //the 1st index stores the popular movie url
            //the 2nd index stores the top rated movie url
            URL[] urls = NetworkUtilities.getUrlForPopularAndTopRated(context);
            //variable to store the json response from the urls
            String[] jsonString = new String[2];
            for (int i = 0; i < 2; i++) {
                jsonString[i] = fetchData(urls[i]);
            }
            //create content values to be inserted in the db.
            ContentValues[] popularMoviesContentValue = JsonParsingUtilities.getContentValuesFromJson(jsonString[0]);
            ContentValues[] TopRatedMoviesContentValue = JsonParsingUtilities.getContentValuesFromJson(jsonString[1]);

            if (popularMoviesContentValue != null && popularMoviesContentValue.length != 0 &&
                    TopRatedMoviesContentValue != null && TopRatedMoviesContentValue.length != 0) {
                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver resolver = context.getContentResolver();
                /* Delete old movie data because we don't need to keep multiple data */
                resolver.delete(
                        MoviesContract.PopularMoviesEntry.CONTENT_URI,
                        null,
                        null);
                resolver.delete(
                        MoviesContract.TopRatedMoviesEntry.CONTENT_URI,
                        null,
                        null);
                //insert new data
                resolver.bulkInsert(
                        MoviesContract.PopularMoviesEntry.CONTENT_URI,
                        popularMoviesContentValue);
                resolver.bulkInsert(
                        MoviesContract.TopRatedMoviesEntry.CONTENT_URI,
                        TopRatedMoviesContentValue);
            }

        } catch (Exception e) {
            /* Server probably invalid */
            e.printStackTrace();
        }
        syncReviewsAndTrailer(context);
    }

    /**
     * sync the review and trailer details of the popular and top rated movies
     *
     * @param context
     */
    private static void syncReviewsAndTrailer(Context context) {
         /*we need to know the movies ids available in popular and top rated tables
         so that we can get the reviews and trailers related to those movie ids
         so this variable stores the list of movie ids
         */
        ArrayList<Long> movie_ids = new ArrayList<>();

        ContentValues[] trailerAndReviewContentValues;
        //we need only the movie ids
        String[] projection = new String[]{
                MoviesContract.CommonFields.COLUMN_MOVIE_ID
        };
        //query the top rated table for the list of available movie ids
        Cursor cursor1 = context.getContentResolver().query(
                MoviesContract.TopRatedMoviesEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
        //query the popular table for the list of available movie ids
        Cursor cursor2 = context.getContentResolver().query(
                MoviesContract.PopularMoviesEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        if (null != cursor1 && cursor2 != null) {
            //get those movies id from the cursors and add them to the array list
            while (cursor1.moveToNext()) {
                long l = cursor1.getLong(cursor1.getColumnIndex(MoviesContract.TopRatedMoviesEntry.COLUMN_MOVIE_ID));
                movie_ids.add(l);
            }
            while (cursor2.moveToNext()) {
                long l = cursor2.getLong(cursor2.getColumnIndex(MoviesContract.PopularMoviesEntry.COLUMN_MOVIE_ID));
                movie_ids.add(l);
            }

        }
        //initialize the content value array size with movie id's arraList size
        trailerAndReviewContentValues = new ContentValues[movie_ids.size()];

        for (int i = 0; i < movie_ids.size(); i++) {
            //get url to fetch the trailers based on the movie id
            URL urlForTrailers = NetworkUtilities.getAppropriateUrlForReviewOrTrailer(movie_ids.get(i), NetworkUtilities.KEY_FOR_GETTING_TRAILER_URL);
            //get url to fetch the reviews based on the movie id
            URL urlForReviews = NetworkUtilities.getAppropriateUrlForReviewOrTrailer(movie_ids.get(i), NetworkUtilities.KEY_FOR_GETTING_REVIEW_URL);
            //fetch the trailer data and store the Json response from it as a string
            String trailers = NetworkUtilities.fetchData(urlForTrailers);
            //fetch the review data and store the Json response from it as a string
            String reviews = NetworkUtilities.fetchData(urlForReviews);

            //create a content value to insert into the trailer and review database
            ContentValues contentValues = new ContentValues();
            contentValues.put(MoviesContract.TrailersAndReviewsEntry.COLUMN_MOVIE_ID, movie_ids.get(i));
            contentValues.put(MoviesContract.TrailersAndReviewsEntry.COLUMN_TRAILERS, trailers);
            contentValues.put(MoviesContract.TrailersAndReviewsEntry.COLUMN_REVIEWS, reviews);
            //add this content value to the content values array
            trailerAndReviewContentValues[i] = contentValues;
        }
        //before deleting the old trailer and review entries
        //get the favourite movie ids. so that we eliminate this id from deleting
        //because they mighty be still needed by the favourite entry table
        Cursor fav_movie_cursor = context.getContentResolver().query(
                MoviesContract.FavouriteMoviesEntry.CONTENT_URI,
                new String[]{MoviesContract.FavouriteMoviesEntry.COLUMN_MOVIE_ID},
                null,
                null,
                null
        );
        //list to store the favourite movie ids
        ArrayList<String> fav_movie_ids = new ArrayList<>();
        String selection = null;
        //get all the favourite movie ids from the cursor and store it in the fav_movie_ids arrayList.
        if (fav_movie_cursor != null && fav_movie_cursor.getCount() != 0) {
            while (fav_movie_cursor.moveToNext()) {
                String s = fav_movie_cursor.getString(fav_movie_cursor.getColumnIndex(MoviesContract.FavouriteMoviesEntry.COLUMN_MOVIE_ID));
                fav_movie_ids.add(s);
            }
            //declare the selection parameter when some favourite movie ids are available
            //we need to check the trailer and review detail which we are going to delete is not needed anymore
            //so we create where class that checks that the current movie id we are going to delete is not equal to any of the
            //movie id in the favourite movie table
            selection = MoviesContract.TrailersAndReviewsEntry.COLUMN_MOVIE_ID + " != ? ";
        }

        /*
        we need to check the movie id is in favourite table , if the movie id is in favourite table then
        we need to skip this (i.e) we don't want it to deleted it because this review and trailer details
         mighty be needed by the favourite table.
         inorder to do a group of deletion we use ContentProviderOperation.
         ContentProviderOperation is efficient for batch deletion/insertion/update in one transaction.
         */
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        ContentProviderOperation operation;

        //delete the old entries from the trailer and review entry table
        for (String s : fav_movie_ids) {
            operation = ContentProviderOperation
                    .newDelete(MoviesContract.TrailersAndReviewsEntry.CONTENT_URI)
                    .withSelection(selection, new String[]{s})
                    .build();
            operations.add(operation);
        }
        try {
            context.getContentResolver().applyBatch(MoviesContract.CONTENT_AUTHORITY, operations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }

        //insert the new data.
        context.getContentResolver().
                bulkInsert(MoviesContract.TrailersAndReviewsEntry.CONTENT_URI,
                        trailerAndReviewContentValues);
    }
}
