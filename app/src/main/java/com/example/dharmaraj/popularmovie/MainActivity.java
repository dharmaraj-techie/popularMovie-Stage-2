package com.example.dharmaraj.popularmovie;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.dharmaraj.popularmovie.adapters.MoviesCursorAdapter;
import com.example.dharmaraj.popularmovie.data.MoviesContract;
import com.example.dharmaraj.popularmovie.sync.SyncUtils;
import com.example.dharmaraj.popularmovie.utilities.NetworkUtilities;
import com.facebook.stetho.Stetho;

import static com.example.dharmaraj.popularmovie.utilities.NetworkUtilities.getCurrentUri;

public class MainActivity extends AppCompatActivity implements ListItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor> {

    //the key used to identify the clicked position which is passed in an intent
    public static final String CLICKED_POSITION_KEY = "clicked-position";
    //the id for the loader which gets the movie poster images of the the movies
    private static final int MOVIES_LOADER = 100;

    //member variable reference for views in the UI
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    //Custom RecyclerAdapter Reference
    private MoviesCursorAdapter mMoviesCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Stetho.initializeWithDefaults(this);
        setContentView(R.layout.activity_main);

        //find and refer all the views to reference using findViewById
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRecyclerView = (RecyclerView) findViewById(R.id.movie_recycler_view);

        //find the orientation of the device and set the number of columns for RecyclerView based on the device orientation
        int column_size;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            //if the orientation is Landscape set number of column to be 3
            column_size = 3;
        else
            //if the orientation is portrait set number of column to be 2
            column_size = 2;

        //setting the recycler view with proper layoutManager
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, column_size);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        //initialise the adapter and set it to the recycler view
        mMoviesCursorAdapter = new MoviesCursorAdapter(this);
        mRecyclerView.setAdapter(mMoviesCursorAdapter);
        //show the progress bar to indicate that some background work is going on
        showprogressBar();
        //start the loader.
        getSupportLoaderManager().initLoader(MOVIES_LOADER, null, this);
        //getting the sharedPreferences and registering it to the listener
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        //if the background task are not initialized this method will initialize them
        SyncUtils.initialize(this);
    }

    /**
     * this check if the device is connected to the internet or not
     *
     * @return boolean (connectivity state)
     */
    boolean checkConnectivity() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    /**
     * shows the error message that the device is not connected to the internet
     * and hides the rest of the views progressBar and RecyclerView
     */
    public final void showErrorMessage() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    /**
     * shows only the loading progressbar
     */
    public final void showprogressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    /**
     * hides the error message and progressBar, shows the data
     */
    public final void showData() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_menu:
                //open the settings activity when the settings menu is selected
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return true;
    }

    /**
     * this is a implemented method for handling when the recyclerView item are clicked.
     * it would open the DetailActivity with corresponding data
     *
     * @param
     */
    @Override
    public void onListItemClick(long movie_id) {
        Intent intent = new Intent(this, DetailActivity.class);
        //corresponding uri to be opened in the detail view based on movie id
        Uri uri = Uri.parse(NetworkUtilities.getCurrentUri(this).toString())
                .buildUpon()
                .appendPath(String.valueOf(movie_id))
                .build();
        intent.putExtra(CLICKED_POSITION_KEY, uri.toString());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        //unregister the listener
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    /**
     * when the preference is changed this method is called and it fetches the data according to the new Preference
     *
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        getSupportLoaderManager().restartLoader(MOVIES_LOADER, null, this);
        mMoviesCursorAdapter.notifyDataSetChanged();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //get the current uri to load based on the shared preference using the getCurrentUri helper method
        Uri uri = getCurrentUri(this);
        String[] projection = new String[]{
                MoviesContract.CommonFields.COLUMN_MOVIE_ID,
                MoviesContract.CommonFields.COLUMN_MOVIE_POSTER};
        return new CursorLoader(MainActivity.this, uri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //when the load is finished swap the cursor with new data
        mMoviesCursorAdapter.swapCursor(data);
        if (data.getCount() != 0)
            //stop showing the loading indicator and show the data
            showData();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesCursorAdapter.swapCursor(null);
    }

}
