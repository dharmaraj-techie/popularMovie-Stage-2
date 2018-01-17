package com.example.dharmaraj.popularmovie.sync;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * class which starts the job service in the background using the firebase job dispatcher
 */
public class MoviesFirebaseJobService extends JobService {
    //background async task to start the service in background, since all android components starts on the main thread.
    private AsyncTask<Void, Void, Void> mFetchMoviesTask;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        mFetchMoviesTask = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Context context = getApplicationContext();
                MoviesSyncTask.syncMovie(context);
                jobFinished(jobParameters, false);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(jobParameters, false);
            }
        };

        mFetchMoviesTask.execute();
        return true;
    }


    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mFetchMoviesTask != null) {
            mFetchMoviesTask.cancel(true);
        }
        return true;
    }
}
