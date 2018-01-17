package com.example.dharmaraj.popularmovie.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.dharmaraj.popularmovie.ListItemClickListener;
import com.example.dharmaraj.popularmovie.R;
import com.example.dharmaraj.popularmovie.data.MoviesContract;
import com.example.dharmaraj.popularmovie.utilities.DbBitmapUtility;

/**
 * Created by Dharmaraj on 05-06-2017.
 */

/**
 * This adapter handles populating the mainActivity's list of movies
 */
public class MoviesCursorAdapter extends RecyclerView.Adapter<MoviesCursorAdapter.CursorViewHolder> {
    //listener for handling click events of the list item in the MainActivity
    private ListItemClickListener mListItemClickListener;
    //cursor of the which holds the current movie details
    private Cursor mCursor;

    public MoviesCursorAdapter(ListItemClickListener listItemClickListener) {
        this.mListItemClickListener = listItemClickListener;
    }

    @Override
    public CursorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_items, parent, false);
        return new CursorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CursorViewHolder holder, int position) {
        //move the cursor to the corresponding position
        if (mCursor.moveToPosition(position)) {
            //get the image from the cursor
            byte[] imageBytes = mCursor.getBlob(mCursor.getColumnIndex(MoviesContract.FavouriteMoviesEntry.COLUMN_MOVIE_POSTER));
            //set the image to the imageView
            holder.movieImageView.setImageBitmap(DbBitmapUtility.getImage(imageBytes));
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    /**
     * this method will swap the new cursor value to old cursor value in the adapter .
     *
     * @param newCursor
     */
    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }


    class CursorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView movieImageView;

        private CursorViewHolder(View itemView) {
            super(itemView);
            //find the image view
            movieImageView = (ImageView) itemView.findViewById(R.id.movie_Image_view);
            //set the clickListener for it.
            movieImageView.setOnClickListener(this);
        }

        /**
         * handles the click event of the image view
         *
         * @param v
         */
        @Override
        public void onClick(View v) {
            //get the clicked movie's id
            if (mCursor.moveToPosition(getAdapterPosition())) {
                long movie_id = mCursor.getLong(mCursor.getColumnIndex(MoviesContract.CommonFields.COLUMN_MOVIE_ID));
                // pass the movie id to the ListItemClickListener so that it can handle it in MainActivity
                mListItemClickListener.onListItemClick(movie_id);
            }
        }
    }
}
