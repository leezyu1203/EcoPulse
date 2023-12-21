package com.example.ecopulse;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ManagePostsAdapter extends RecyclerView.Adapter<ManagePostsAdapter.ManagePostsHolder> {
    private Context context;
    private List<UploadEvent> eventList;    //check which class to be used
    private OnItemClickListener listener;

    public ManagePostsAdapter(Context context, List<UploadEvent> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public ManagePostsAdapter.ManagePostsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(this.context).inflate(R.layout.posts_item, parent, false);
        return new ManagePostsAdapter.ManagePostsHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ManagePostsAdapter.ManagePostsHolder holder, int position) {
        // get the events posted by the collaborator var. current
        UploadEvent current = this.eventList.get(position);
        holder.TVCollaboratorPostsTitle.setText(current.getEventName());
        holder.TVCollaboratorPostedOn.setText(formatTimestamp(current.getTimestamp()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                EventPostFragment epf = new EventPostFragment();

                Bundle bundle = new Bundle();
                bundle.putString("eventTimestamp", current.getTimestamp());
                epf.setArguments(bundle);

                transaction.replace(R.id.main_fragment, epf);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.eventList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public void setOnItemClickListener(ManagePostsAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ManagePostsHolder extends RecyclerView.ViewHolder {
        public TextView TVCollaboratorPostsTitle;
        public TextView TVCollaboratorPostedOn;

        public ManagePostsHolder(@NonNull View itemView) {
            super(itemView);

            TVCollaboratorPostsTitle = itemView.findViewById(R.id.TVCollaboratorPostTitle);
            TVCollaboratorPostedOn = itemView.findViewById(R.id.TVCollaboratorPostedOn);
        }
    }

    public String formatTimestamp(String inTimestamp) {
        long timestamp = Long.parseLong(inTimestamp);
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        return sdf.format(date);
    }
}
