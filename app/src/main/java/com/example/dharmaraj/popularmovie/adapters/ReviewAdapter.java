package com.example.dharmaraj.popularmovie.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dharmaraj.popularmovie.R;

import java.util.ArrayList;

/**
 * this adapter handles the populating of Reviews in the detail activity
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    //arrayList of String array to store the Review details
    //String[0] will contain the author name and
    //String[1] will contain the content of the review
    private ArrayList<String[]> mReviews = new ArrayList<>();
    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_list_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        //get the review detail at the needed position
        String[] review = mReviews.get(position);
        //set the textViews with appropriate strings
        holder.authorTextView.setText(review[0]);
        holder.contentTextView.setText(review[1]);
    }

    @Override
    public int getItemCount() {
        if(mReviews==null)return 0;
        return mReviews.size();
    }

    /**
     * this method is used to swap the data
     * it replace the old array list of review details with the new value whenever the new values
     * are available or the values changes
     * @param newData
     */
    public void swapData(ArrayList<String[]> newData) {
        mReviews = newData;
        //notifying the adapter that the data set has changed
        notifyDataSetChanged();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView authorTextView;
        TextView contentTextView;

        private ReviewViewHolder(View itemView) {
            super(itemView);
            authorTextView = (TextView) itemView.findViewById(R.id.review_author_textview);
            contentTextView = (TextView) itemView.findViewById(R.id.review_content_textView);
        }

    }
}
