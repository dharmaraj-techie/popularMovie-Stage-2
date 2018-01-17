package com.example.dharmaraj.popularmovie.data;

import android.net.Uri;

/**
 * TrailerDetails is a custom class which stores the trailer details such as
 * the trailer name and the trailer's url.
 */

public class TrailerDetails {
    //uri of the trailer
    private Uri trailerUri;
    //name of the trailer
    private String trailerName;

    public TrailerDetails(Uri uri, String name) {
        trailerUri = uri;
        trailerName = name;
    }

    /**
     * getter method for trailer uri
     *
     * @return uri of the trailer
     */
    public Uri getTrailerUrl() {
        return trailerUri;
    }

    /***
     * getter method for trailer name
     *
     * @return String trailer's name.
     */
    public String getTrailerName() {
        return trailerName;
    }
}
