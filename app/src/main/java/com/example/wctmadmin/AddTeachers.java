package com.example.wctmadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddTeachers extends AppCompatActivity {
private ImageView addTeacherImage;
private EditText addTeacherName;
private  EditText addTeacherEmail;
private EditText addTeacherPost;
private Spinner addTeacherCatogery;
private Button addTeacherButton;
private final int REQ = 1;
private Bitmap bitmap = null;
private String category;
 String name, email, post, downloadUrl = "";
    private ProgressDialog pd;
    private StorageReference storageReference;
    private DatabaseReference reference, dbRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teachers);
        addTeacherImage = findViewById(R.id.addTeacherImage);
        addTeacherName = findViewById(R.id.addTeacherName);
        addTeacherEmail = findViewById(R.id.addTeacherEmail);
        addTeacherPost = findViewById(R.id.addTeacherPost);
        addTeacherCatogery = findViewById(R.id.addTeacherCategory);
        addTeacherButton= findViewById(R.id.addTeacherButton);
        pd = new ProgressDialog(this);

        reference = FirebaseDatabase.getInstance().getReference().child("teacher");
        storageReference = FirebaseStorage.getInstance().getReference();
        String[] items = new String[]{"Select Category", "Btech", "Mtech", "Bca", "Mca", "Bba","Mba","Diploma" };
        addTeacherCatogery.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items));
        addTeacherCatogery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                category = addTeacherCatogery.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        addTeacherImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        addTeacherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkValidation();
            }
        });

    }

    private void checkValidation() {
        name= addTeacherName.getText().toString();
        email = addTeacherEmail.getText().toString();
        post = addTeacherPost.getText().toString();

        if(name.isEmpty()){
            addTeacherName.setError("Empty");
            addTeacherName.requestFocus();
        } else if (email.isEmpty()) {
            addTeacherEmail.setError("Empty");
            addTeacherEmail.requestFocus();
        }else if (post.isEmpty()) {
            addTeacherPost.setError("Empty");
            addTeacherPost.requestFocus();
        }
        else if(category.equals("Select Category")){
            Toast.makeText(this, "please select category", Toast.LENGTH_SHORT).show();
        } else if (bitmap == null) {
            pd.setMessage("Uploading...");
            pd.show();
            insertData();
        }
        else {
            pd.setMessage("Uploading...");
            pd.show();
            uploadImage();
        }
    }

    private void insertData() {
        dbRef = reference.child(category);
        final String uniqueKey = dbRef.push().getKey();

        TeacherData teacherData = new TeacherData(name,email,post,downloadUrl,uniqueKey);

        dbRef.child(uniqueKey).setValue(teacherData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(AddTeachers.this, "Teacher Added", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddTeachers.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }



        private void uploadImage() {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
            byte[] finalImage = baos.toByteArray();
            final StorageReference filePath;
            filePath = storageReference.child("Teachers").child(finalImage+"jpg");
            final UploadTask uploadTask = filePath.putBytes(finalImage);
            uploadTask.addOnCompleteListener(AddTeachers.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downloadUrl = String.valueOf(uri);
                                        insertData();
                                    }
                                });
                            }
                        });

                    }
                    else {
                        pd.dismiss();
                        Toast.makeText(AddTeachers.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    private void openGallery() {
        Intent pickImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImage,REQ);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ && resultCode ==  RESULT_OK){
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            addTeacherImage.setImageBitmap(bitmap);
        }
    }
}