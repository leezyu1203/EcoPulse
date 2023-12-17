package com.example.ecopulse;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventPostFragment extends Fragment {
    private ImageView IVEventPostPoster;
    private TextView TVEventPostTitle;
    private TextView TVPostedOn;
    private TextView TVPostDesc;
    private TextView TVEventVenue;
    private TextView TVEventDate;
    private TextView TVEventTime;
    private ProgressBar PBLoadPost;

    private RecyclerView RVComments;
    private CommentAdapter adapter;
    private List<Comment> commentList;
    private EditText ETInputComment;
    private ImageButton IBtnSend;
    private TextView TVNoCommentMsg;
    private ProgressBar PBLoadComments;

    private Button BtnShare;
    private Button BtnAddReminder;

    private String eventID;
    private DatabaseReference databaseRef;

    public EventPostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // if navigate from ManagePostsFragment
        // -> gone comment...
        // -> change BtnShare into BtnEdit
        // -> change BtnAddReminder into BtnDelete
        return inflater.inflate(R.layout.fragment_event_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        IVEventPostPoster = view.findViewById(R.id.IVEventPostPoster);
        TVEventPostTitle = view.findViewById(R.id.TVEventPostTitle);
        TVPostedOn = view.findViewById(R.id.TVPostedOn);
        TVPostDesc = view.findViewById(R.id.TVPostDesc);
        TVEventVenue = view.findViewById(R.id.TVEventVenue);
        TVEventDate = view.findViewById(R.id.TVEventDate);
        TVEventTime = view.findViewById(R.id.TVEventTime);
        PBLoadPost = view.findViewById(R.id.PBLoadPost);

        RVComments = view.findViewById(R.id.RVComments);
        ETInputComment = view.findViewById(R.id.ETInputComment);
        IBtnSend = view.findViewById(R.id.IBtnSend);
        PBLoadComments = view.findViewById(R.id.PBLoadComments);
        TVNoCommentMsg = view.findViewById(R.id.TVNoCommentMsg);

        BtnShare = view.findViewById(R.id.BtnShare);
        BtnAddReminder = view.findViewById(R.id.BtnAddReminder);

        Bundle args = getArguments();
        if (args != null) {
            eventID = args.getString("eventID");
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("events").child(eventID);
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    UploadEvent upload = snapshot.getValue(UploadEvent.class);

                    TVEventPostTitle.setText(upload.getEventName());
                    TVPostedOn.setText("Posted on " + formatTimestamp(upload.getTimestamp()));
                    TVPostDesc.setText(upload.getEventDesc());
                    TVEventVenue.setText("Venue: " + upload.getEventVenue());
                    TVEventDate.setText("Date: " + formatDate(upload.getEventDate()));
                    TVEventTime.setText("Time: " + upload.getEventStartTime() + " to " + upload.getEventEndTime());
                    Picasso.get()
                            .load(upload.getImageUrl())
                            .placeholder(R.mipmap.ic_launcher)
                            .fit()
                            .centerCrop()
                            .into(IVEventPostPoster);
                    PBLoadPost.setVisibility(View.INVISIBLE);
                    PBLoadComments.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(requireActivity(), "Event not found", Toast.LENGTH_SHORT).show();
                    PBLoadPost.setVisibility(View.INVISIBLE);
                    getActivity().finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireActivity(), error.getMessage(),Toast.LENGTH_SHORT).show();
                PBLoadPost.setVisibility(View.INVISIBLE);
            }
        });

        RVComments.setHasFixedSize(false);
        RVComments.setLayoutManager(new LinearLayoutManager(requireContext()));

        commentList = new ArrayList<>();
        adapter = new CommentAdapter(requireActivity(), commentList);

        RVComments.setAdapter(adapter);

        databaseRef = FirebaseDatabase.getInstance().getReference("comment").child(eventID);
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();

                for(DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    commentList.add(comment);
                }
                adapter.notifyDataSetChanged();

                if(commentList.isEmpty()) {
                    TVNoCommentMsg.setVisibility(View.VISIBLE);
                } else {
                    TVNoCommentMsg.setVisibility(View.GONE);
                }

                PBLoadComments.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                PBLoadComments.setVisibility(View.INVISIBLE);
            }
        });

        IBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isETEmpty(ETInputComment)) {
                    Toast.makeText(requireActivity(), "Write a comment", Toast.LENGTH_SHORT).show();
                } else {
                    comment();
                }
            }
        });

        BtnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                String msgToSend = "Let's join " + TVEventPostTitle.getText().toString()
                        + "\n\t" + TVEventVenue.getText().toString()
                        + "\n\t" + TVEventDate.getText().toString()
                        + "\n\t" + TVEventTime.getText().toString();
                intent.putExtra(Intent.EXTRA_TEXT, msgToSend);
                intent.setType("text/plain");

                if(intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        /*
        BtnAddReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
         */
    }

    private String formatTimestamp(String timestamp) {
        long time = Long.parseLong(timestamp);
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    private String formatDate(String inputDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(inputDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return inputDate;
        }
    }

    private boolean isETEmpty(EditText editText) {
        return TextUtils.isEmpty(editText.getText().toString().trim());
    }

    private void comment() {
        Comment comment = new Comment(ETInputComment.getText().toString().trim());
        databaseRef.push().setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(requireActivity(), "Comment added", Toast.LENGTH_SHORT).show();
                ETInputComment.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}