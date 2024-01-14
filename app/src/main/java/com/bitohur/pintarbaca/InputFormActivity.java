package com.bitohur.pintarbaca;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bitohur.pintarbaca.model.Model;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.github.muddz.styleabletoast.StyleableToast;

public class InputFormActivity extends AppCompatActivity {
    private EditText etTitle, etAuthor, etPage, etRating, etDescription, etLocation;
    private Button btSave, btShow;
    private CardView selectPhoto;
    private ImageView ivUploadImage;
    private Uri imageUri;
    private Bitmap bitmap;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private StorageReference mStorageRef;

    private String pId, pTitle, pAuthor, pPage, pRating, pLocation, pImageUrl, pDescription;

    private String photoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_form);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference();

        selectPhoto = findViewById(R.id.cv_upload_img);
        ivUploadImage = findViewById(R.id.iv_image_upload);
        etTitle = findViewById(R.id.et_book_title);
        etAuthor = findViewById(R.id.et_book_author);
        etDescription = findViewById(R.id.et_description);
        etPage = findViewById(R.id.et_book_page);
        etRating = findViewById(R.id.et_book_rating);
        etLocation = findViewById(R.id.et_location);
        btSave = findViewById(R.id.bt_save);
        btShow = findViewById(R.id.bt_show);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            btSave.setText("Update");
            pId = bundle.getString("pId");
            pTitle = bundle.getString("pTitle");
            pAuthor = bundle.getString("pAuthor");
            pPage = bundle.getString("pPage");
            pRating = bundle.getString("pRating");
            pLocation = bundle.getString("pLocation");
            pImageUrl = bundle.getString("pImageUrl");
            pDescription = bundle.getString("pDescription");

            etTitle.setText(pTitle);
            etAuthor.setText(pAuthor);
            etPage.setText(pPage);
            etRating.setText(pRating);
            etLocation.setText(pLocation);
            Picasso.get().load(pImageUrl).into(ivUploadImage);
            etDescription.setText(pDescription);
        } else {
            btSave.setText("Save");
        }

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bundle != null) {
                    String id = pId;
                    String title = etTitle.getText().toString().trim();
                    String author = etAuthor.getText().toString().trim();
                    String page = etPage.getText().toString().trim();
                    String rating = etRating.getText().toString().trim();
                    String location = etLocation.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String imageUrl = photoUrl;

                    updateData(id, title, author, page, description, rating, location, imageUrl);
                } else {
                    uploadBookInfo();
                }

            }
        });
        btShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(InputFormActivity.this, MainActivity.class));
                finish();
            }
        });
        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageFromGallery();
            }
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ivUploadImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirestore(final Map<String, Object> bookData, final String bookId) {
        if (imageUri != null) {
            final StorageReference myRef = mStorageRef.child("photo/" + UUID.randomUUID().toString());
            myRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    myRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if (uri != null) {
                                String photoUrl = uri.toString();
                                bookData.put("url", photoUrl);
                                saveBookToFirestore(bookData, bookId);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            StyleableToast.makeText(InputFormActivity.this, "Gagal mendapatkan URL gambar.", R.style.toast).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    StyleableToast.makeText(InputFormActivity.this, "Gagal mengunggah gambar.", R.style.toast).show();
                }
            });
        }
    }

    private void saveBookToFirestore(Map<String, Object> bookData, String bookId) {
        // Simpan data buku ke Firestore
        firestore.collection("Documents").document(bookId)
                .set(bookData, SetOptions.merge()) // Gunakan SetOptions.merge() agar tidak mengganti data yang sudah ada
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        StyleableToast.makeText(InputFormActivity.this, "Gagal mengunggah buku: " + e.getMessage(), R.style.toast).show();
                    }
                });
    }

    private void updateData(String id, String title, String author, String page, String description, String rating, String location, String imageUrl) {

        firestore.collection("Documents").document(id)
                .update("title", title, "description", description, "author", author, "page", page, "rating", rating, "location", location, "imageUrl", imageUrl)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        StyleableToast.makeText(InputFormActivity.this, "Update..", R.style.toast).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        StyleableToast.makeText(InputFormActivity.this, e.getMessage(), R.style.toast).show();
                    }
                });
    }

    private void uploadBookInfo() {
        String title = etTitle.getText().toString().trim();
        String rating = etRating.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String page = etPage.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(rating) && TextUtils.isEmpty(description) && TextUtils.isEmpty(page) && TextUtils.isEmpty(author)) {
            StyleableToast.makeText(this, "Tolong isi semua bidang", R.style.toast).show();
        } else {
            String id = UUID.randomUUID().toString();
            Map<String, Object> doc = new HashMap<>();
            doc.put("id", id);
            doc.put("title", title);
            doc.put("rating", rating);
            doc.put("description", description);
            doc.put("page", page);
            doc.put("author", author);
            doc.put("location", location);

            uploadImageToFirestore(doc, id);
            firestore.collection("Documents").document(id).set(doc).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    StyleableToast.makeText(InputFormActivity.this, "Uploaded...", R.style.toast).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    StyleableToast.makeText(InputFormActivity.this, e.getMessage(), R.style.toast).show();
                }
            });
        }
    }
}