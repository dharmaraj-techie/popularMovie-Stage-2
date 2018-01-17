package com.example.dharmaraj.popularmovie;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;

import com.example.dharmaraj.popularmovie.adapters.ReviewAdapter;
import com.example.dharmaraj.popularmovie.adapters.TrailerAdapter;
import com.example.dharmaraj.popularmovie.data.MoviesContract;
import com.example.dharmaraj.popularmovie.data.TrailerDetails;
import com.example.dharmaraj.popularmovie.databinding.ActivityDetailBinding;
import com.example.dharmaraj.popularmovie.utilities.DbBitmapUtility;
import com.example.dharmaraj.popularmovie.utilities.JsonParsingUtilities;

import java.util.ArrayList;
import java.util.List;

import static com.example.dharmaraj.popularmovie.data.MoviesContract.CommonFields.COLUMN_MOVIE_DESCRIPTION;
import static com.example.dharmaraj.popularmovie.data.MoviesContract.CommonFields.COLUMN_MOVIE_ID;
import static com.example.dharmaraj.popularmovie.data.MoviesContract.CommonFields.COLUMN_MOVIE_NAME;
import static com.example.dharmaraj.popularmovie.data.MoviesContract.CommonFields.COLUMN_MOVIE_POSTER;
import static com.example.dharmaraj.popularmovie.data.MoviesContract.CommonFields.COLUMN_MOVIE_RATINGS;
import static com.example.dharmaraj.popularmovie.data.MoviesContract.CommonFields.COLUMN_MOVIE_RELEASE_DATE;

public class DetailActivity extends AppCompatActivity implements ListItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    //constants for different loaders
    //loader for movie details
    private static final int DETAIL_ACTIVITY_MOVIE_LOADER = 200;
    //loader that loads trailers and reviews detail
    private static final int DETAIL_ACTIVITY_TRAILER_AND_REVIEW_LOADER = 746;
    //loader to check that this movie is in fav list or not.
    private static final int DETAIL_ACTIVITY_FAVOURITE_LIST_LOADER = 839;
    //list of available trailers for the current movie
    //the trailer details are stored in Custom class known as TrailerDetails
    ArrayList<TrailerDetails> mArrayListOfTrailerDetails;
    //list if favourite movie ids
    ArrayList<Long> mArrayListOfFavoriteMovieList;
    //Details of the movies
    String mMovie_title;
    String mReleaseDate;
    String mDescription;
    String mVotes;
    //the poster image is stored in byte array, so that it can be saved in database
    byte[] mPoster_image_blob;
    String mMovieId;
    //uri of the current movie
    Uri mUri;
    //value to check whether the movie is in favourite list
    boolean isAlreadyInFav = false;
    //cursor containing the current movie details
    Cursor cursor_for_movies;
    ActivityDetailBinding activityDetailBinding;
    //Reference to the trailer adapter which handle the list of movie Trailers
    private TrailerAdapter mTrailerAdapter;
    //Reference to the review adapter which handle the list of movie Reviews
    private ReviewAdapter mReviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);
        //get the Uri from the intent
        String uri_string = getIntent().getExtras().getString(MainActivity.CLICKED_POSITION_KEY);
        mUri = Uri.parse(uri_string);
        //loader to fetch the movie details
        getSupportLoaderManager().initLoader(DETAIL_ACTIVITY_MOVIE_LOADER, null, this);
        //loader to fetch the list of favourite movie ids
        getSupportLoaderManager().initLoader(DETAIL_ACTIVITY_FAVOURITE_LIST_LOADER, null, this);
        //loader to fetch the movies trailer and review details
        getSupportLoaderManager().initLoader(DETAIL_ACTIVITY_TRAILER_AND_REVIEW_LOADER, null, this);
        //Initialising the adapters
        mTrailerAdapter = new TrailerAdapter(this);
        mReviewAdapter = new ReviewAdapter();
        //setting the adapter to the corresponding recycler view
        activityDetailBinding.trailerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        activityDetailBinding.trailerRecyclerView.setAdapter(mTrailerAdapter);
        activityDetailBinding.reviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        activityDetailBinding.reviewRecyclerView.setAdapter(mReviewAdapter);
    }

    /**
     * method to check whether the current movie's id is in favourite movies id list.
     */
    private void checkAlreadyInFav() {
        for (long l : mArrayListOfFavoriteMovieList)
            if (l == Long.valueOf(mUri.getLastPathSegment())) {
                isAlreadyInFav = true;
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_activity_menu, menu);
        /**
         * getting the path segment
         * to find out whether it is from FavouriteEntry
         */
        List<String> strings = mUri.getPathSegments();

        //if the movie is one of favourite movie (i.e) it from favouriteEntry show the delete menu and change the icon to fav.
        if ((0 == strings.get(0).compareTo(MoviesContract.PATH_FAV) || isAlreadyInFav)) {
            menu.getItem(1).setVisible(true);
            menu.getItem(0).setIcon(R.drawable.ic_favorite_48px);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.fav_button:
                addToFavouriteList();
                item.setIcon(R.drawable.ic_favorite_48px);
                break;
            case R.id.delete:
                deleteFromFavourite();
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * method to delete the favourite movie from the favourite list
     */
    private void deleteFromFavourite() {
        //uri that points to delete the content from favourite entry
        Uri uri = Uri.parse(MoviesContract.FavouriteMoviesEntry.CONTENT_URI.toString())
                .buildUpon()
                .appendPath(mMovieId)
                .build();
        getContentResolver().delete(
                uri,
                null,
                null);
    }

    /**
     * method to save the current movie into the favourite movie list
     */
    public void addToFavouriteList() {
        ContentValues contentValues = new ContentValues();
        //add all the current movie details to the Content values
        contentValues.put(COLUMN_MOVIE_ID, mMovieId);
        contentValues.put(COLUMN_MOVIE_NAME, mMovie_title);
        contentValues.put(COLUMN_MOVIE_DESCRIPTION, mDescription);
        contentValues.put(COLUMN_MOVIE_RATINGS, mVotes);
        contentValues.put(COLUMN_MOVIE_RELEASE_DATE, mReleaseDate);
        contentValues.put(COLUMN_MOVIE_POSTER, mPoster_image_blob);
        getContentResolver().insert(MoviesContract.FavouriteMoviesEntry.CONTENT_URI, contentValues);
    }

    /**
     * this method is called when any of the trailer is clicked
     *
     * @param index the clicked trailer's index in the list.
     */
    @Override
    public void onListItemClick(long index) {
        // get the trailerDetails of the list of trailers available for the current movie
        TrailerDetails currentTrailer = mArrayListOfTrailerDetails.get((int) index);
        //get the trailer url and
        // create an intent to open the trailer
        Intent intent = new Intent(Intent.ACTION_VIEW, currentTrailer.getTrailerUrl());

        //get the list of available activities to open the trailer url
        PackageManager packageManager = getPackageManager();
        List activities = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        //if any one app is available then startActivity using that intent
        boolean isIntentSafe = activities.size() > 0;
        if (isIntentSafe) {
            startActivity(intent);
        }
    }
    /**
     * set the movie values to the corresponding views
     */
    void setMovieData() {
        activityDetailBinding.title.setText(mMovie_title);
        activityDetailBinding.date.setText(mReleaseDate);
        activityDetailBinding.discription.setText("\t"+mDescription);
        activityDetailBinding.votes.setText(String.valueOf(mVotes)+"/10");
        activityDetailBinding.movieImage.setImageBitmap(DbBitmapUtility.getImage(mPoster_image_blob));
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            //loader for movie details
            case DETAIL_ACTIVITY_MOVIE_LOADER:
                String[] projection = new String[]{
                        COLUMN_MOVIE_ID,
                        COLUMN_MOVIE_NAME,
                        COLUMN_MOVIE_POSTER,
                        COLUMN_MOVIE_RELEASE_DATE,
                        COLUMN_MOVIE_RATINGS,
                        COLUMN_MOVIE_DESCRIPTION
                };
                return new CursorLoader(this,
                        mUri,
                        projection,
                        null,
                        null,
                        null);
            //loader for trailer and review details of the current movie
            case DETAIL_ACTIVITY_TRAILER_AND_REVIEW_LOADER:
                Uri uri = MoviesContract.TrailersAndReviewsEntry.CONTENT_URI
                        .buildUpon()
                        .appendPath(mUri.getLastPathSegment())
                        .build();
                String[] projection1 = new String[]{
                        MoviesContract.TrailersAndReviewsEntry.COLUMN_TRAILERS,
                        MoviesContract.TrailersAndReviewsEntry.COLUMN_REVIEWS
                };
                return new CursorLoader(this,
                        uri,
                        projection1,
                        null,
                        null,
                        null);
            //loader that get the list of favourite movie ids
            case DETAIL_ACTIVITY_FAVOURITE_LIST_LOADER:
                return new CursorLoader(
                        this,
                        MoviesContract.FavouriteMoviesEntry.CONTENT_URI,
                        new String[]{MoviesContract.CommonFields.COLUMN_MOVIE_ID},
                        null,
                        null,
                        null
                );
            default:
                throw new UnsupportedOperationException("unknown Loader Id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //get the loader id
        int id = loader.getId();
        switch (id) {
            case DETAIL_ACTIVITY_MOVIE_LOADER:
                cursor_for_movies = data;
                if (cursor_for_movies != null && cursor_for_movies.moveToFirst()) {
                    //get all the movie details from the cursor and assign it to the member variables.
                    mMovie_title = cursor_for_movies.getString(cursor_for_movies.getColumnIndex(COLUMN_MOVIE_NAME));
                    mReleaseDate = cursor_for_movies.getString(cursor_for_movies.getColumnIndex(COLUMN_MOVIE_RELEASE_DATE));
                    mDescription = cursor_for_movies.getString(cursor_for_movies.getColumnIndex(COLUMN_MOVIE_DESCRIPTION));
                    mVotes = cursor_for_movies.getString(cursor_for_movies.getColumnIndex(COLUMN_MOVIE_RATINGS));
                    mPoster_image_blob = cursor_for_movies.getBlob(cursor_for_movies.getColumnIndex(COLUMN_MOVIE_POSTER));
                    mMovieId = cursor_for_movies.getString(cursor_for_movies.getColumnIndex(COLUMN_MOVIE_ID));
                    //set the movie values to the corresponding views
                    setMovieData();
                }
                break;
            case DETAIL_ACTIVITY_TRAILER_AND_REVIEW_LOADER:
                //check if the cursor is not null.
                if (data != null && data.moveToFirst()) {
                    //get the json response of Trailer details stored in the TrailersAndReviewsEntry table database
                    String trailerJsonString = data.getString(
                            data.getColumnIndex(MoviesContract.TrailersAndReviewsEntry.COLUMN_TRAILERS));
                    //get the required details from the json response
                    mArrayListOfTrailerDetails = JsonParsingUtilities.parseTrailerUrlsFromJson(trailerJsonString);
                    //swap the loaded new data into to TrailerAdapter
                    mTrailerAdapter.swapData(mArrayListOfTrailerDetails);

                    //get the json response of Review details stored in the TrailersAndReviewsEntry table database
                    String reviewJsonString = data.getString(
                            data.getColumnIndex(MoviesContract.TrailersAndReviewsEntry.COLUMN_REVIEWS));
                    //get the required review details from the json response
                    ArrayList<String[]> reviews = JsonParsingUtilities.parseReviewsFromJson(reviewJsonString);
                    //swap the loaded new data into to ReviewAdapter
                    mReviewAdapter.swapData(reviews);
                }
                break;

            case DETAIL_ACTIVITY_FAVOURITE_LIST_LOADER:
                mArrayListOfFavoriteMovieList = new ArrayList<>();
                while (data.moveToNext()) {
                    //get the list of all favourite movie ids and store it in a arrayList
                    long l = data.getLong(data.getColumnIndex(MoviesContract.CommonFields.COLUMN_MOVIE_ID));
                    mArrayListOfFavoriteMovieList.add(l);
                }
                //check if the current movie if in favourite list
                checkAlreadyInFav();
                break;
            default:
                throw new IllegalArgumentException("unKnown loader id " + id);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case DETAIL_ACTIVITY_MOVIE_LOADER:
                cursor_for_movies = null;
                break;
            case DETAIL_ACTIVITY_FAVOURITE_LIST_LOADER:
                mArrayListOfFavoriteMovieList.clear();
                break;
        }
    }

}

