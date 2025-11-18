package edu.sjsu.android.servicesfinder.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.sjsu.android.servicesfinder.R;
import edu.sjsu.android.servicesfinder.model.ServiceReview;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final List<ServiceReview> reviews = new ArrayList<>();

    public void setReviews(List<ServiceReview> newReviews) {
        reviews.clear();
        if (newReviews != null) {
            reviews.addAll(newReviews);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_review, parent, false);
        return new ReviewViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ServiceReview review = reviews.get(position);

        holder.nameText.setText(review.getUserName() != null
                ? review.getUserName() : "Anonymous");

        holder.commentText.setText(review.getComment() != null
                ? review.getComment() : "");

        holder.ratingBar.setRating((float) review.getRating());

        long ts = review.getTimestamp();
        if (ts > 0) {
            String dateText = DateFormat.getDateInstance()
                    .format(new Date(ts));
            holder.dateText.setText(dateText);
        } else {
            holder.dateText.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {

        TextView nameText;
        TextView commentText;
        TextView dateText;
        RatingBar ratingBar;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.review_name);
            commentText = itemView.findViewById(R.id.review_comment);
            dateText = itemView.findViewById(R.id.review_date);
            ratingBar = itemView.findViewById(R.id.review_rating);
        }
    }
}
