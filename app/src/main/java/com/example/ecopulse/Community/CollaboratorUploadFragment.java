package com.example.ecopulse.Community;

import static android.app.Activity.RESULT_OK;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.*;

import com.example.ecopulse.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;

public class CollaboratorUploadFragment extends Fragment {
    private EditText ETInputEventName;
    private EditText ETInputEventDesc;
    private EditText ETInputEventVenue;
    private ImageButton IBtnEventDateSelector;
    private TextView TVSelectedEventDate;
    private ImageButton IBtnStartTimeSelector;
    private TextView TVSelectedStartTime;
    private ImageButton IBtnEndTimeSelector;
    private TextView TVSelectedEndTime;
    private AppCompatButton BtnAddImage;
    private ImageView IVUploadedImage;
    private AppCompatButton BtnUpload;

    private TextView title;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private Bundle temp;

    private StorageReference storageRef;
    private FirebaseFirestore db;
    private FirebaseUser user;

    private StorageTask uploadTask;

    public CollaboratorUploadFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_collaborator_upload, container, false);

        title = (TextView) getActivity().findViewById(R.id.current_title);

        ETInputEventName = view.findViewById(R.id.ETInputEventName);
        ETInputEventDesc = view.findViewById(R.id.ETInputEventDesc);
        ETInputEventVenue = view.findViewById(R.id.ETInputEventVenue);
        IBtnEventDateSelector = view.findViewById(R.id.IBtnEventDateSelector);
        TVSelectedEventDate = view.findViewById(R.id.TVSelectedEventDate);
        IBtnStartTimeSelector = view.findViewById(R.id.IBtnStartTimeSelector);
        TVSelectedStartTime = view.findViewById(R.id.TVSelectedStartTime);
        IBtnEndTimeSelector = view.findViewById(R.id.IBtnEndTimeSelector);
        TVSelectedEndTime = view.findViewById(R.id.TVSelectedEndTime);
        BtnAddImage = view.findViewById(R.id.BtnAddImage);
        IVUploadedImage = view.findViewById(R.id.IVUploadedImage);
        BtnUpload = view.findViewById(R.id.BtnUpload);

        db = FirebaseFirestore.getInstance();
        temp = new Bundle();

        if(getArguments().containsKey("editPost")) {
            title.setText("Edit Post");
            String eventID = getArguments().getString("eventID");

            db.collection("events").document(eventID)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            if(error != null) {
                                Log.e(TAG, "CollaboratorUploadFragment: Listen exist doc error", error);
                            }

                            if(value != null && value.exists()){
                                UploadEvent loadedEvent = value.toObject(UploadEvent.class);

                                ETInputEventName.setText(loadedEvent.getEventName());
                                ETInputEventDesc.setText(loadedEvent.getEventDesc());
                                ETInputEventVenue.setText(loadedEvent.getEventVenue());
                                TVSelectedEventDate.setText(loadedEvent.getEventDate());
                                TVSelectedStartTime.setText(loadedEvent.getEventStartTime());
                                TVSelectedEndTime.setText(loadedEvent.getEventEndTime());
                                Picasso.get()
                                        .load(loadedEvent.getImageUrl())
                                        .placeholder(R.mipmap.ic_launcher)
                                        .fit()
                                        .into(IVUploadedImage);

                                temp.putString("timestamp", loadedEvent.getTimestamp());
                                temp.putString("userID", loadedEvent.getUserID());
                                temp.putString("url", loadedEvent.getImageUrl());
                                temp.putString("eventID", eventID);
                            }
                        }
                    });
        } else {
            title.setText("New Post");
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        storageRef = FirebaseStorage.getInstance().getReference("events");
        user = FirebaseAuth.getInstance().getCurrentUser();

        IBtnEventDateSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(requireContext(), R.style.DatePickerTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        TVSelectedEventDate.setText(dayOfMonth + "-" + (month+1) + "-" + year);
                    }
                }, mYear, mMonth, mDay);
                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                dialog.show();
            }
        });

        IBtnStartTimeSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeSelector(TVSelectedStartTime);
            }
        });

        IBtnEndTimeSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeSelector(TVSelectedEndTime);
            }
        });

        BtnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        BtnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isETEmpty(ETInputEventName) || isETEmpty(ETInputEventDesc) || isETEmpty(ETInputEventVenue)
                        || isTVEmpty(TVSelectedEventDate) || isTVEmpty(TVSelectedStartTime) || isTVEmpty(TVSelectedStartTime)) {
                    Toast.makeText(requireContext(), "Complete the details", Toast.LENGTH_SHORT).show();
                } else if(uploadTask != null && uploadTask.isInProgress()) {
                    Toast.makeText(requireContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    UploadEvent event = new UploadEvent(
                            ETInputEventName.getText().toString().trim(),
                            ETInputEventDesc.getText().toString().trim(),
                            ETInputEventVenue.getText().toString().trim(),
                            TVSelectedEventDate.getText().toString().trim(),
                            TVSelectedStartTime.getText().toString().trim(),
                            TVSelectedEndTime.getText().toString().trim());
                    uploadFile(event);
                }
            }
        });
    }

    private void timeSelector(TextView showTime) {
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinutes = c.get(Calendar.MINUTE);
        TimePickerDialog dialog = new TimePickerDialog(requireContext(),R.style.DatePickerTheme, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                showTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }
        }, mHour, mMinutes, false);
        dialog.show();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            IVUploadedImage.setImageURI(imageUri);
        }
    }

    private boolean isETEmpty(EditText editText) {
        return TextUtils.isEmpty(editText.getText().toString().trim());
    }

    private boolean isTVEmpty(TextView textView) {
        return TextUtils.isEmpty(textView.getText().toString().trim());
    }

    private void uploadFile(UploadEvent event){
        if(imageUri != null) {
            StorageReference fileRef = storageRef.child(ETInputEventName.getText().toString().trim()
                    + "_" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
            uploadTask = fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    Bundle args = getArguments();
                                    String userID = "";
                                    if (args != null) {
                                        userID = args.getString("userID");
                                    }
                                    event.setImageUrl(imageUrl);
                                    if (getArguments().containsKey("editPost")) {
                                        event.setTimestamp(temp.getString("timestamp"));
                                        event.setUserID(temp.getString("userID"));
                                        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(temp.getString("url"));
                                        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.d(TAG, "EventPostFragment: Storage image deleted");
                                            }
                                        });
                                        db.collection("events")
                                                .document(getArguments().getString("eventID"))
                                                .set(event)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(getContext(), "Event successfully edited", Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(), "Edit unsuccessful. Try again later.", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    } else {
                                        event.setUserID(userID);
                                        event.setTimestamp(String.valueOf(System.currentTimeMillis()));
                                        db.collection("events")
                                                .add(event).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Toast.makeText(requireContext(), "Event successfully posted", Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(requireContext(), "Failed to post the event", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();

            transaction.replace(R.id.main_fragment, new ManagePostsFragment());
            transaction.commit();

        } else if(imageUri == null && getArguments().containsKey("editPost")){
            event.setImageUrl(temp.getString("url"));
            event.setTimestamp(temp.getString("timestamp"));
            event.setUserID(temp.getString("userID"));
            db.collection("events")
                    .document(temp.getString("eventID"))
                    .set(event)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getContext(), "Event successfully edited", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Edit unsuccessful. Try again later.", Toast.LENGTH_LONG).show();
                        }
                    });
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();

            transaction.replace(R.id.main_fragment, new ManagePostsFragment());
            transaction.commit();
        } else {
            Toast.makeText(requireContext(), "Upload an image", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = requireActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}