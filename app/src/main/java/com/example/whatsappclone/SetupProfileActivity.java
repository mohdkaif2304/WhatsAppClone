package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.example.whatsappclone.databinding.ActivitySetupProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SetupProfileActivity extends AppCompatActivity {

    ActivitySetupProfileBinding binding;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;
    Uri selectedImage;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setTitle("Please Wait....");
        dialog.setMessage("Updating profile");
        dialog.setCancelable(false);
        getSupportActionBar().hide();

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 45);
            }
        });
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = binding.nameBox.getText().toString();
                if (name.isEmpty()) {
                    binding.nameBox.setError("Please Type a name ");
                    return;
                }
                dialog.show();
                if (selectedImage != null) {
                    StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        dialog.dismiss();
                                        String imageUrl = uri.toString();
                                        String uid = auth.getUid();
                                        String phone = auth.getCurrentUser().getPhoneNumber();
                                        String name = binding.nameBox.getText().toString();
                                        User user = new User(uid, name, phone, imageUrl);
                                        database.getReference()
                                                .child("uid")
                                                .child("users")
                                                .setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        startActivity(new Intent(SetupProfileActivity.this, MainActivity.class));
                                                        finish();
                                                    }
                                                });

                                    }
                                });
                            }
                        }
                    });
                } else {
                    String uid = auth.getUid();
                    String phone = auth.getCurrentUser().getPhoneNumber();

                    User user = new User(uid, name, phone, "No Image");

                    database.getReference()
                            .child("users")
                            .child(uid)
                            .setValue(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (data.getData() != null) {
                binding.imageView.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }
    }
}