package com.shivamkharde.postit.ui.addpost;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shivamkharde.postit.R;
import com.shivamkharde.postit.ui.login.LoginActivity;
import com.shivamkharde.postit.ui.register.RegisterActivity;

import java.sql.Time;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddPostActivity extends AppCompatActivity {

    private static int LOAD_IMAGE = 1;
//    variable declaration
    private Button selectImageBtn;
    private Button postItBtn;
    private EditText description;
    private ImageView previewImageSection;
    private Uri imageFilePath;

//    firebase declaration
    private FirebaseAuth pAuth;
    private FirebaseFirestore pDb;
    private StorageReference pStorage;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        getSupportActionBar().setTitle("Add new post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        variable initialization
        selectImageBtn = findViewById(R.id.select_picture_btn);
        postItBtn = findViewById(R.id.post_it_btn);
        description = findViewById(R.id.post_description_field);
        previewImageSection = findViewById(R.id.picture_preview);
        pAuth = FirebaseAuth.getInstance();
        pDb = FirebaseFirestore.getInstance();
        pStorage = FirebaseStorage.getInstance().getReference();

//        getting current user
        currentUser = pAuth.getCurrentUser();

        if(currentUser!=null) {
//            when select picture btn is clicked
            selectImageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    choose image from gallery
                    chooseImageFromGallery();
                }
            });

//            when post it btn is pressed
            postItBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    getting all info
                    final String sDescription = description.getText().toString().trim();
                    String error = "";
                    if(imageFilePath == null){
                        error = "please select an image";
                    }else if(sDescription.length()>600){
                        error = "description is too long";
                    }else if(sDescription.isEmpty()){
                        error="please write a description";
                    }
                        if(!error.isEmpty()){
//                            show an error alert
                            showAlertDialog(error,"Oops! Error");
                        }else{
//                            make progress dialog to show the progress
                            final ProgressDialog loading = new ProgressDialog(AddPostActivity.this);
                            loading.setTitle("POSTING....");
                            loading.setMessage("Please stay still until we post for you...");
                            loading.setCancelable(false);
                            loading.setCanceledOnTouchOutside(false);
                            loading.show();
//                            store image in storage
                            final StorageReference postImageRef = pStorage.child("post_images/"+ Calendar.getInstance().getTimeInMillis()+imageFilePath.toString().substring(imageFilePath.toString().lastIndexOf(".")));
                            postImageRef.putFile(imageFilePath)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                            get download uri from image reference
                                            postImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
//                                                    get download image uri and description to store it in db
                                                    storeInfoInDB(sDescription,uri,loading);
                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            showAlertDialog(e.getMessage(),"Error posting..");
                                        }
                                    });
                        }
                }
            });

        }else{
//            redirect user to login page
            gotoLoginPage();
        }
    }

//    this function is t store the post data in database
    private void storeInfoInDB(String sDescription, Uri uri, final ProgressDialog loading) {
//        create hashMap to store in db
        Map<String,Object> postInfo = new HashMap<>();
        postInfo.put("email",currentUser.getEmail());
        postInfo.put("post_image",uri.toString());
        postInfo.put("post_description",sDescription);
        postInfo.put("post_likes",0);

//        store info in db
        pDb.collection("posts")
                .add(postInfo)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()){
//                            if task is successful then increment the post count in current user account
                            increasePostCount(currentUser.getEmail(),loading);
                        }else{
                            showAlertDialog("Something Went Wrong","Error posting...");
                        }
                    }
                });
    }

//    this function is to increase the post count
    private void increasePostCount(final String email, final ProgressDialog loading) {
//    increase the post count
        pDb.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot doc: task.getResult()){
//                                get data from doc and search until we find current user email
                                Map<String,Object> userData = doc.getData();
                                if(userData.get("email").equals(currentUser.getEmail())){
//                                    get doc reference and update post count field value
                                    doc.getReference().update("posts", FieldValue.increment(1))
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
//                                                    successfully posted message
                                                    Toast.makeText(getApplicationContext(),"Successfully posted...",Toast.LENGTH_LONG);
//                                                    clear all text field
                                                    clearTextFields();
//                                                    cancel the loading screen
                                                    loading.cancel();
//                                                    finish the activity to go to last page
                                                    finish();
                                                }
                                            });
                                    break;
                                }
                            }
                        }else{
                            showAlertDialog("something wen wrong","Error");
                        }
                    }
                });
    }

    //    this function is to clear the text fields
    public void clearTextFields(){
        imageFilePath = null;
        description.getText().clear();
        previewImageSection.setImageResource(R.drawable.ic_baseline_broken_image_24);
    }

//    choose image from gallery
    public void chooseImageFromGallery(){
        Intent selectImageIntent = new Intent();
        selectImageIntent.setType("image/*");
        selectImageIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(selectImageIntent,"Select a picture"),LOAD_IMAGE);
    }
//    this function is to redirect user to login page
    public void gotoLoginPage(){
        Intent gotoLoginActivity = new Intent(AddPostActivity.this, LoginActivity.class);
        startActivity(gotoLoginActivity);
        finish();
    }

//    this function is to show the alerts
    public void showAlertDialog(String message,String title){

        AlertDialog.Builder sDialog = new AlertDialog.Builder(this);
        sDialog.setTitle(title);
        sDialog.setMessage(message);
        sDialog.setIcon(android.R.drawable.ic_dialog_alert);
        sDialog.setCancelable(true);
        sDialog.setPositiveButton("Okay",null);
        sDialog.show();
    }

//    getting image info on activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LOAD_IMAGE && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            imageFilePath = data.getData();
            try {
                Glide.with(getApplicationContext())
                        .load(imageFilePath)
                        .into(previewImageSection);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}