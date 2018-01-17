package com.example.dharmaraj.popularmovie;

/**
 * interface used as as a listener foe list item clicks
 */
public interface ListItemClickListener {
    //this method receives a long value which can be movie id
    //or index position in an adapter when an the list item that implemented this interface is clicked
    void onListItemClick(long l);
}
