package com.example.ecopulse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ManagePostsAdapter extends RecyclerView.Adapter<ManagePostsAdapter.ManagePostsHolder> {
    private Context context;
    private List<UploadEvent> eventList;    //check which class to be used

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
        /*
        // get the events posted by the collaborator var. current
        holder.TVCollaboratorPostsTitle.setText(current.getEventName());
        holder.TVCollaboratorPostedOn.setText(formatTimestamp(current.getTimestamp());

        holder.itemView.setOnClickListener(new View.OnClickListener()) {
            @Override
            public onClick(View v){
                // can refer to imageAdapter to navigate to post
                // navigate to EventPostFragment
            }
        }
         */
    }

    @Override
    public int getItemCount() {
        return 0;
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
}
