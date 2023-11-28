package com.example.ecopulse;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.*;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDate;
import java.time.LocalTime;

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
    private Button BtnAddImage;
    private ImageView IVUploadedImage;
    private Button BtnUpload;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private StorageReference storageRef;
    private DatabaseReference databaseRef;

    private StorageTask uploadTask;

    public CollaboratorUploadFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_collaborator_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        storageRef = FirebaseStorage.getInstance().getReference("events");
        databaseRef = FirebaseDatabase.getInstance().getReference("events");

        IBtnEventDateSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalDate currentDate = LocalDate.now();
                DatePickerDialog dialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        TVSelectedEventDate.setText(String.valueOf(dayOfMonth) + "/" +
                                String.valueOf(month+1) + "/" + String.valueOf(year));
                    }
                }, currentDate.getYear(), currentDate.getMonthValue() - 1, currentDate.getDayOfMonth());
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
                    uploadFile();
                    // navigate to community
                }
            }
        });
    }

    private void timeSelector(TextView showTime) {
        LocalTime currentTime = LocalTime.now();
        TimePickerDialog dialog = new TimePickerDialog(requireContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                showTime.setText(String.valueOf(hourOfDay) + ":" +
                        String.valueOf(minute));
            }
        }, currentTime.getHour(), currentTime.getMinute(), true);
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

    private void uploadFile(){
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
                                    UploadEvent uploadEvent = new UploadEvent(
                                            ETInputEventName.getText().toString().trim(),
                                            ETInputEventDesc.getText().toString().trim(),
                                            ETInputEventVenue.getText().toString().trim(),
                                            TVSelectedEventDate.getText().toString().trim(),
                                            TVSelectedStartTime.getText().toString().trim(),
                                            TVSelectedEndTime.getText().toString().trim(),
                                            imageUrl);
                                    String eventID =databaseRef.push().getKey();
                                    databaseRef.child(eventID).setValue(uploadEvent);

                                    Toast.makeText(requireContext(), "Event sucessfully posted", Toast.LENGTH_LONG).show();
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