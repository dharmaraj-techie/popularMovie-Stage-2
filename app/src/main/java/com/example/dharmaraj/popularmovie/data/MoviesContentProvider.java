package com.example.dharmaraj.popularmovie.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * This class serves as the ContentProvider for all of Movie Data. This class allows us to
 * bulkInsert data, query data, and delete data.
 */
public class MoviesContentProvider extends ContentProvider {
    // This Constants are used by the UriMatcher
    public static final int FAVOURITE = 100;
    public static final int FAVOURITE_WITH_ID = 101;
    public static final int POPULAR = 200;
    public static final int POPULAR_WITH_ID = 201;
    public static final int TOP_RATED = 300;
    public static final int TOP_RATED_WITH_ID = 301;
    public static final int TRAILER_AND_REVIEW = 400;
    public static final int TRAILER_AND_REVIEW_WITH_ID = 401;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mMoviesDbHelper;

    /**
     * build the UriMather by matching the above constants to the corresponding Uri
     *
     * @return
     */
    public static UriMatcher buildUriMatcher() {
        //initialize the matcher to the no Match constant.
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        //mather for uri that points to a whole Favourite table
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_FAV, FAVOURITE);
        //mather for uri that points to a single row in the Favourite table
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_FAV + "/#", FAVOURITE_WITH_ID);
        //mather for uri that points to a whole Popular table
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_POPULAR, POPULAR);
        //mather for uri that points to a single row in the Popular table
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_POPULAR + "/#", POPULAR_WITH_ID);
        //mather for uri that points to a whole top rated table
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_TOP_RATED, TOP_RATED);
        //mather for uri that points to a single row in the top rated table
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_TOP_RATED + "/#", TOP_RATED_WITH_ID);
        //mather for uri that points to a whole trailers and review table
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_TRAILER_AND_REVIEWS, TRAILER_AND_REVIEW);
        //mather for uri that points to a single row in the trailers and review table
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_TRAILER_AND_REVIEWS + "/#", TRAILER_AND_REVIEW_WITH_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mMoviesDbHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mMoviesDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor cursor;
        switch (match) {
            //in case of a uri referring to the whole table
            case FAVOURITE:
                cursor = db.query(MoviesContract.FavouriteMoviesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            //in case of a uri referring to the single row in a table
            case FAVOURITE_WITH_ID:
                //get the movie id (i.e) the lastPathSegment and store it as the selection argument
                selectionArgs = new String[]{uri.getLastPathSegment()};
                //query the db for the movie with the corresponding movie id
                cursor = db.query(MoviesContract.FavouriteMoviesEntry.TABLE_NAME,
                        projection,
                        MoviesContract.FavouriteMoviesEntry.COLUMN_MOVIE_ID + " = ?",
                        selectionArgs,
                        null,
                        null,
                        null);
                break;

            case POPULAR:
                cursor = db.query(MoviesContract.PopularMoviesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case POPULAR_WITH_ID:
                //get the movie id (i.e) the lastPathSegment and store it as the selection argument
                String[] selectionArguments = new String[]{uri.getLastPathSegment()};
                cursor = db.query(MoviesContract.PopularMoviesEntry.TABLE_NAME,
                        projection,
                        MoviesContract.PopularMoviesEntry.COLUMN_MOVIE_ID + " =? ",
                        selectionArguments,
                        null,
                        null,
                        null);
                break;

            case TOP_RATED:
                cursor = db.query(MoviesContract.TopRatedMoviesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case TOP_RATED_WITH_ID:
                //get the movie id (i.e) the lastPathSegment and store it as the selection argument
                selectionArgs = new String[]{uri.getLastPathSegment()};
                cursor = db.query(MoviesContract.TopRatedMoviesEntry.TABLE_NAME,
                        projection,
                        MoviesContract.TopRatedMoviesEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case TRAILER_AND_REVIEW_WITH_ID:
                //get the movie id (i.e) the lastPathSegment and store it as the selection argument
                selectionArgs = new String[]{uri.getLastPathSegment()};
                cursor = db.query(MoviesContract.TrailersAndReviewsEntry.TABLE_NAME,
                        projection,
                        MoviesContract.TrailersAndReviewsEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        //we don't need this method so we don't implement any thing in here
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case FAVOURITE:
                long id = db.insert(MoviesContract.FavouriteMoviesEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(MoviesContract.FavouriteMoviesEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("insertion failed ito the row : " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case POPULAR:
                db.beginTransaction();
                int rowsInsertedInPopular = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.PopularMoviesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInsertedInPopular++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInsertedInPopular > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInsertedInPopular;

            case TOP_RATED:
                db.beginTransaction();
                int rowsInsertedInToprated = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.TopRatedMoviesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInsertedInToprated++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInsertedInToprated > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInsertedInToprated;

            case TRAILER_AND_REVIEW:
                db.beginTransaction();
                int rowsInsertedInTrailerAndReview = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.TrailersAndReviewsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInsertedInTrailerAndReview++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInsertedInTrailerAndReview > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInsertedInTrailerAndReview;

            default:
                throw new UnsupportedOperationException("no Bulk Data inserted : " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();
        int i;
        switch (sUriMatcher.match(uri)) {
            case POPULAR:
                i = db.delete(MoviesContract.PopularMoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case TOP_RATED:
                i = db.delete(MoviesContract.TopRatedMoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case FAVOURITE_WITH_ID:
                selectionArgs = new String[]{uri.getLastPathSegment()};
                i = db.delete(MoviesContract.FavouriteMoviesEntry.TABLE_NAME,
                        MoviesContract.FavouriteMoviesEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectionArgs);
                break;

            case TRAILER_AND_REVIEW:
                i = db.delete(MoviesContract.TrailersAndReviewsEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (i > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            return i;
        } else {
            return 0;
        }
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        //our app does not require any update to db. so we do not implement it.
        return 0;
    }
}
