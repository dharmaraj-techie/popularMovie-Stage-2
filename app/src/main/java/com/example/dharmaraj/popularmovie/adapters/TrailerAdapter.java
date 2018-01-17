package com.example.dharmaraj.popularmovie.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dharmaraj.popularmovie.ListItemClickListener;
import com.example.dharmaraj.popularmovie.R;
import com.example.dharmaraj.popularmovie.data.TrailerDetails;

import java.util.ArrayList;

/**
 * this adapter is used to populate the list of trailers
 */
public class TrailerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //reference for click listener
    private final ListItemClickListener mListItemClickListener;
    //ArrayList of trailer details
    //TrailerDetails is a custom class which stores the trailer details such as the
    //trailer name and the trailer's url.
    private ArrayList<TrailerDetails> mTrailerDetails = new ArrayList<>();

    //Constructor for initializing values
    public TrailerAdapter(ListItemClickListener listItemClickListener) {
        mListItemClickListener = listItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trailer_list_item, parent, false);
        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TrailerViewHolder trailerViewHolder = (TrailerViewHolder) holder;
        //get the trailer details for the current position .
        TrailerDetails currentTrailer = mTrailerDetails.get(position);
        //set the trailer name to the text view
        trailerViewHolder.trailerTextView.setText(currentTrailer.getTrailerName());
    }

    @Override
    public int getItemCount() {
        if (mTrailerDetails == null) {
            return 0;
        }
        return mTrailerDetails.size();
    }

    /**
     * this method is used to swap the old data(i.e) ArrayList of TrailerDetails with new ArrayList
     *
     * @param newData
     */
    public void swapData(ArrayList<TrailerDetails> newData) {
        mTrailerDetails = newData;
        //notify the adapter that the data set has changed
        //so that the adapter can repopulate the recycler view with new data
        notifyDataSetChanged();
    }


    class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView trailerTextView;
        private TrailerViewHolder(View itemView) {
            super(itemView);
            trailerTextView = (TextView) itemView.findViewById(R.id.trailer_textView);
            trailerTextView.setOnClickListener(this);
        }

        /**
         * this onClick method is implemented so that we can pass the clicked position and get the uri for it .
         *
         * @param v
         */
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mListItemClickListener.onListItemClick(position);
        }
    }

}
