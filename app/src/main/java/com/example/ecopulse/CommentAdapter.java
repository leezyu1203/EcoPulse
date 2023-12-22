package com.example.ecopulse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Context context;
    private List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(this.context).inflate(R.layout.comment_item, parent,false);
        return new CommentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment currentComment = this.commentList.get(position);
        holder.TVCommentUserID.setText("UserID");       //modify to userID
        holder.TVUserComment.setText(currentComment.getContent());
        Picasso.get()
                .load(R.mipmap.ic_launcher)
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.IVCommentUserProfile);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView IVCommentUserProfile;
        TextView TVCommentUserID;
        TextView TVUserComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            IVCommentUserProfile = itemView.findViewById(R.id.IVCommentUserProfile);
            TVCommentUserID = itemView.findViewById(R.id.TVCommentUserID);
            TVUserComment = itemView.findViewById(R.id.TVUserComment);
        }
    }
}
