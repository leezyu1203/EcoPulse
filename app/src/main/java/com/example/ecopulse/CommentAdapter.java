package com.example.ecopulse;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Context context;
    private List<DocumentSnapshot> commentList;

    public CommentAdapter(Context context, List<DocumentSnapshot> commentList) {
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
        DocumentSnapshot snapshot = this.commentList.get(position);
        Log.d(TAG,"adapter check: " + snapshot.getId());
        Comment current = snapshot.toObject(Comment.class);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(current.getUserID())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null) {
                            Log.e(TAG, error.getMessage(), error);
                        }

                        if(value != null && value.exists()) {
                            holder.TVCommentUserID.setText(value.getString("username"));
                            Picasso.get()
                                    .load(value.getString("imageUrl"))
                                    .placeholder(R.mipmap.ic_launcher)
                                    .fit()
                                    .centerCrop()
                                    .into(holder.IVCommentUserProfile);
                        }
                    }
                });
        //holder.TVCommentUserID.setText(current.getUserID());
        holder.TVUserComment.setText(current.getContent());
        /*Picasso.get()
                .load(R.mipmap.ic_launcher)
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.IVCommentUserProfile);*/
    }

    @Override
    public int getItemCount() {
        return commentList.size();
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
