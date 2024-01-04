package com.example.ecopulse;

import static android.view.View.GONE;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
    private List<DocumentSnapshot> commentList;
    private EditText ETInputComment;
    private ImageButton IBtnSend;
    private TextView TVNoCommentMsg;
    private ProgressBar PBLoadComments;

    private AppCompatButton BtnShareNEdit;
    private AppCompatButton BtnAddReminderNDelete;

    //private String eventTimestamp;
    private String eventID;
    private TextView title;
    private Bundle args;
    private FirebaseFirestore db;

    public EventPostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_event_post, container, false);
        getActivity().findViewById(R.id.backButton).setVisibility(View.VISIBLE);
        ETInputComment = root.findViewById(R.id.ETInputComment);
        IBtnSend = root.findViewById(R.id.IBtnSend);

        BtnShareNEdit = root.findViewById(R.id.BtnShare);
        BtnAddReminderNDelete = root.findViewById(R.id.BtnAddReminder);

        if(getArguments().containsKey("fromManagePost")){
            Log.d(TAG,"EventPostFragment: Check fromManagePost");

            ETInputComment.setVisibility(View.GONE);
            IBtnSend.setVisibility(View.GONE);

            BtnShareNEdit.setText("Edit");
            BtnShareNEdit.setCompoundDrawablesWithIntrinsicBounds(R.drawable.edit_icon, 0, 0, 0);

            BtnAddReminderNDelete.setText("Delete");
            BtnAddReminderNDelete.setCompoundDrawablesWithIntrinsicBounds(R.drawable.delete_icon,0,0,0);
            BtnAddReminderNDelete.setCompoundDrawableTintList(ColorStateList.valueOf(Color.parseColor("#E58C8C")));
            BtnAddReminderNDelete.setTextColor(android.graphics.Color.parseColor("#E58C8C"));
            BtnAddReminderNDelete.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E58C8C")));
        }

        IVEventPostPoster = root.findViewById(R.id.IVEventPostPoster);
        TVEventPostTitle = root.findViewById(R.id.TVEventPostTitle);
        TVPostedOn = root.findViewById(R.id.TVPostedOn);
        TVPostDesc = root.findViewById(R.id.TVPostDesc);
        TVEventVenue = root.findViewById(R.id.TVEventVenue);
        TVEventDate = root.findViewById(R.id.TVEventDate);
        TVEventTime = root.findViewById(R.id.TVEventTime);
        PBLoadPost = root.findViewById(R.id.PBLoadPost);

        args = getArguments();
        if(args != null) {
            eventID = args.getString("eventID");
        }

        Log.d(TAG, "EventPostFragment: Check eventID" +eventID);
        db = FirebaseFirestore.getInstance();
        db.collection("events")
                .document(eventID)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null) {
                            Log.e(TAG, "EventPostFragment: Listen event fail", error);
                            return;
                        }

                        if(value.exists() && value != null) {
                            UploadEvent current = value.toObject(UploadEvent.class);

                            title = (TextView) getActivity().findViewById(R.id.current_title);
                            title.setText(current.getEventName());

                            TVEventPostTitle.setText(current.getEventName());
                            TVPostedOn.setText("Posted on " + formatTimestamp(current.getTimestamp()));
                            TVPostDesc.setText(current.getEventDesc());
                            TVEventVenue.setText("Venue: " + current.getEventVenue());
                            TVEventDate.setText("Date: " + formatDate(current.getEventDate()));
                            TVEventTime.setText("Time: " + current.getEventStartTime() + " to " + current.getEventEndTime());
                            Picasso.get()
                                    .load(current.getImageUrl())
                                    .placeholder(R.mipmap.ic_launcher)
                                    .fit()
                                    .centerCrop()
                                    .into(IVEventPostPoster);
                            PBLoadPost.setVisibility(View.INVISIBLE);
                            PBLoadComments.setVisibility(View.VISIBLE);
                        } else{
                            Log.e(TAG, "EventPostFragement: Fail to load event");
                        }
                    }
                });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RVComments = view.findViewById(R.id.RVComments);
        PBLoadComments = view.findViewById(R.id.PBLoadComments);
        TVNoCommentMsg = view.findViewById(R.id.TVNoCommentMsg);

        RVComments.setHasFixedSize(false);
        RVComments.setLayoutManager(new LinearLayoutManager(requireContext()));

        commentList = new ArrayList<>();
        adapter = new CommentAdapter(requireActivity(), commentList);

        RVComments.setAdapter(adapter);

        db.collection("events").document(eventID)
                .collection("comments")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                if(error != null) {
                                    Log.e(TAG, "EventPostFragment: Listen comments fail", error);
                                }

                                if(value != null){
                                    commentList.clear();

                                    for(QueryDocumentSnapshot snapshot : value) {
                                        commentList.add(snapshot);
                                    }

                                    adapter.notifyDataSetChanged();

                                    if(commentList.isEmpty()) {
                                        TVNoCommentMsg.setVisibility(View.VISIBLE);
                                    } else {
                                        TVNoCommentMsg.setVisibility(View.INVISIBLE);
                                    }

                                    PBLoadComments.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
        /*db.collection("events").document(eventID)
                .collection("comments")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            commentList.clear();

                            for(DocumentSnapshot snapshots : task.getResult()) {
                                commentList.add(snapshots);
                                Log.d(TAG, "check snapshot: " + snapshots.getId());
                            }

                            adapter.notifyDataSetChanged();

                            if(commentList.isEmpty()) {
                                TVNoCommentMsg.setVisibility(View.VISIBLE);
                            } else {
                                TVNoCommentMsg.setVisibility(View.INVISIBLE);
                            }

                            PBLoadComments.setVisibility(View.INVISIBLE);
                        }
                    }
                });8?
        /*
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
        });*/

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

        BtnShareNEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getArguments().containsKey("fromManagePost")) {
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();

                    CollaboratorUploadFragment cuf = new CollaboratorUploadFragment();
                    args.putBoolean("editPost", true);
                    cuf.setArguments(args);

                    transaction.replace(R.id.main_fragment, cuf);
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    String msgToSend = "Let's join " + TVEventPostTitle.getText().toString()
                            + "\n\t" + TVEventVenue.getText().toString()
                            + "\n\t" + TVEventDate.getText().toString()
                            + "\n\t" + TVEventTime.getText().toString();
                    intent.putExtra(Intent.EXTRA_TEXT, msgToSend);
                    intent.setType("text/plain");

                    if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            }
        });

        BtnAddReminderNDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getArguments().containsKey("fromManagePost")) {
                    Log.d(TAG, "EventPostFragment: Check delete process...");

                    PopUpDialogFragment popUp = new PopUpDialogFragment();
                    popUp.setArguments(getArguments());
                    popUp.show(requireActivity().getSupportFragmentManager(), "PopUpDialog");
                } else {
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();

                    uploadFragmentReminder ufr = new uploadFragmentReminder();
                    args.putBoolean("fromPost", true);
                    ufr.setArguments(args);

                    transaction.replace(R.id.main_fragment, ufr);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        });
    }

    private String formatTimestamp(String timestamp) {
        long time = Long.parseLong(timestamp);
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    private String formatDate(String inputDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = "";
        if(user != null){
            userEmail = user.getEmail();
        }

        db.collection("user")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                            String userID = snapshot.getId();

                            Comment comment = new Comment(ETInputComment.getText().toString().trim(),
                                    userID);

                            db.collection("events").document(eventID)
                                    .collection("comments")
                                    .add(comment)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(getContext(), "Successfully comment", Toast.LENGTH_SHORT).show();
                                            ETInputComment.setText("");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getContext(),"Comment failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });

        //Comment comment = new Comment(ETInputComment.getText().toString().trim());
        /*databaseRef.push().setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        }); */
    }
}