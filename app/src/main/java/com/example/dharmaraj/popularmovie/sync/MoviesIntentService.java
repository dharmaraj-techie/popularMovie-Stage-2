package com.example.dharmaraj.popularmovie.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
/**
 * An subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class MoviesIntentService extends IntentService {

    public MoviesIntentService() {
        super("MoviesIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        MoviesSyncTask.syncMovie(this);
    }
}
