package com.example.ecopulse;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private Context context;
    private List<DocumentSnapshot> eventList;
    private OnItemClickListener listener;

    public ImageAdapter(Context context, List<DocumentSnapshot> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public ImageAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(this.context).inflate(R.layout.community_post_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ImageViewHolder holder, int position) {
        DocumentSnapshot snapshot = this.eventList.get(position);
        UploadEvent current = snapshot.toObject(UploadEvent.class);
        holder.postTitle.setText(current.getEventName());
        Picasso.get()
                .load(current.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.postPoster);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                EventPostFragment eventPostFragment = new EventPostFragment();

                Bundle bundle = new Bundle();
                bundle.putString("eventID", snapshot.getId());
                Log.d(TAG, "ImageAdapter eventID: " + bundle.getString("eventID"));
                eventPostFragment.setArguments(bundle);

                transaction.replace(R.id.main_fragment, eventPostFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.eventList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public TextView postTitle;
        public ImageView postPoster;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            postTitle = itemView.findViewById(R.id.TVPostTitle);
            postPoster = itemView.findViewById(R.id.IVPostPoster);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    public void setOnItemClickListener(ImageAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}