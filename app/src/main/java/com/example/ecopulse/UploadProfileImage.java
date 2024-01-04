package com.example.ecopulse;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class UploadProfileImage extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Button uploadPicChooseButton;
    private Button uploadPicButton;
    private ImageButton btnBack;
    private ImageView imageViewProfileDp;
    private Uri selectedImageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;
    private String userId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_image);

        uploadPicChooseButton = findViewById(R.id.upload_pic_choose_button);
        uploadPicButton = findViewById(R.id.upload_pic_button);
        imageViewProfileDp = findViewById(R.id.imageView_profile_dp);
        btnBack = findViewById(R.id.backButton);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        uploadPicChooseButton.setOnClickListener(view -> chooseImage());
        uploadPicButton.setOnClickListener(view -> uploadImage());

        btnBack.setOnClickListener(view -> {
            finish();
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {
        if (selectedImageUri != null) {
            StorageReference imageRef = storageReference.child("profile_images/" + UUID.randomUUID());
            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            updateUserProfileImage(imageUrl);
                        });
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(UploadProfileImage.this, "Image upload failed", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Please choose an image first", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserProfileImage(String imageUrl) {
        DocumentReference userRef = firestore.collection("user").document(userId);
        userRef.update("imageUrl", imageUrl)
                .addOnSuccessListener(aVoid ->{
                        Toast.makeText(UploadProfileImage.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UploadProfileImage.this, Profile_user.class);
                        startActivity(intent);
                        finish()
                        ;})
                .addOnFailureListener(e ->
                        Toast.makeText(UploadProfileImage.this, "Failed to update image URL", Toast.LENGTH_SHORT).show());
    }
    @Override

    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            selectedImageUri = data.getData();
            imageViewProfileDp.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViewProfileDp.setImageURI(selectedImageUri);
        }
    }
}
