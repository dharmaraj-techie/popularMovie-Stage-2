package com.example.dharmaraj.popularmovie.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.dharmaraj.popularmovie.data.MoviesContract.FavouriteMoviesEntry;
import com.example.dharmaraj.popularmovie.data.MoviesContract.PopularMoviesEntry;
import com.example.dharmaraj.popularmovie.data.MoviesContract.TopRatedMoviesEntry;

/**
 * this class handles the creation of database
 */
public class MoviesDbHelper extends SQLiteOpenHelper {
    //version of the database
    public static final int DATABASE_VERSION = 10;
    //name of the database
    public static final String DATABASE_NAME = "movies.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //command String for creating Favourite movie entry
          final String SQL_CREATE_FAVOURITE_MOVIE_ENTRIES =
                "CREATE TABLE " + FavouriteMoviesEntry.TABLE_NAME + " (" +
                        FavouriteMoviesEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        FavouriteMoviesEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE ON CONFLICT REPLACE ," +
                        FavouriteMoviesEntry.COLUMN_MOVIE_NAME + " TEXT NOT NULL," +
                        FavouriteMoviesEntry.COLUMN_MOVIE_POSTER + " BLOB, "+
                        FavouriteMoviesEntry.COLUMN_MOVIE_RELEASE_DATE+ " TEXT, "+
                        FavouriteMoviesEntry.COLUMN_MOVIE_RATINGS+" REAL, "+
                        FavouriteMoviesEntry.COLUMN_MOVIE_DESCRIPTION +" TEXT "  + " )";

        //command string for creating Popular movie entry
        final String SQL_CREATE_POPULAR_MOVIE_ENTRIES =
                "CREATE TABLE " + PopularMoviesEntry.TABLE_NAME + " (" +
                        PopularMoviesEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        PopularMoviesEntry.COLUMN_MOVIE_ID + " INTEGER, " +
                        PopularMoviesEntry.COLUMN_MOVIE_NAME + " TEXT NOT NULL," +
                        PopularMoviesEntry.COLUMN_MOVIE_POSTER + " BLOB, "+
                        PopularMoviesEntry.COLUMN_MOVIE_RELEASE_DATE+ " TEXT, "+
                        PopularMoviesEntry.COLUMN_MOVIE_RATINGS+" REAL, "+
                        PopularMoviesEntry.COLUMN_MOVIE_DESCRIPTION +" TEXT " + " )";

        //command string for creating Top Rated movie entry
        final String SQL_CREATE_TOP_RATED_MOVIE_ENTRIES =
                "CREATE TABLE " + TopRatedMoviesEntry.TABLE_NAME + " (" +
                        TopRatedMoviesEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        TopRatedMoviesEntry.COLUMN_MOVIE_ID + " INTEGER," +
                        TopRatedMoviesEntry.COLUMN_MOVIE_NAME + " TEXT NOT NULL," +
                        TopRatedMoviesEntry.COLUMN_MOVIE_POSTER + " BLOB, "+
                        TopRatedMoviesEntry.COLUMN_MOVIE_RELEASE_DATE+ " TEXT, "+
                        TopRatedMoviesEntry.COLUMN_MOVIE_RATINGS+" REAL, "+
                        TopRatedMoviesEntry.COLUMN_MOVIE_DESCRIPTION +" TEXT "+ " )";

        //command string for creating trailer and review details entry
        final String SQL_CREATE_TRAILER_AND_REVIEWS_ENTRIES =
                "CREATE TABLE " + MoviesContract.TrailersAndReviewsEntry.TABLE_NAME + " (" +
                        MoviesContract.TrailersAndReviewsEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MoviesContract.TrailersAndReviewsEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE, " +
                        MoviesContract.TrailersAndReviewsEntry.COLUMN_TRAILERS + " TEXT, " +
                        MoviesContract.TrailersAndReviewsEntry.COLUMN_REVIEWS + " TEXT " + " )";

        db.execSQL(SQL_CREATE_FAVOURITE_MOVIE_ENTRIES);
        db.execSQL(SQL_CREATE_POPULAR_MOVIE_ENTRIES);
        db.execSQL(SQL_CREATE_TOP_RATED_MOVIE_ENTRIES);
        db.execSQL(SQL_CREATE_TRAILER_AND_REVIEWS_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //drop the old DataBase
        db.execSQL("DROP TABLE IF EXISTS "+ FavouriteMoviesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ PopularMoviesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ TopRatedMoviesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ MoviesContract.TrailersAndReviewsEntry.TABLE_NAME);

        //create a new DataBase
        onCreate(db);
    }
}
