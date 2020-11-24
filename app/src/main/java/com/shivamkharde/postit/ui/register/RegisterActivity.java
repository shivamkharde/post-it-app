package com.shivamkharde.postit.ui.register;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shivamkharde.postit.R;
import com.shivamkharde.postit.ui.login.LoginActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

//    variable declaration
    private TextView gotoLoginLink;
    private EditText fullName;
    private EditText username;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private Button registerNowBtn;

//    Firebase Stuff
    private FirebaseAuth pAuth;
    private FirebaseFirestore pDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

//        variable initialization
        gotoLoginLink = findViewById(R.id.goto_login_link);
        registerNowBtn = findViewById(R.id.register_btn);
        fullName = findViewById(R.id.full_name_field);
        username = findViewById(R.id.username_field);
        email = findViewById(R.id.email_field);
        password = findViewById(R.id.password_field);
        confirmPassword = findViewById(R.id.password_confirm_field);
        pAuth = FirebaseAuth.getInstance();
        pDb = FirebaseFirestore.getInstance();

//        adding listener on register now btn
        registerNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String sFullName =fullName.getText().toString().trim();
                final String sUserName = username.getText().toString().trim();
                final String sEmail = email.getText().toString().trim();
                final String sPassword = password.getText().toString().trim();
                String sPasswordConfirm = confirmPassword.getText().toString().trim();

//                stack to store all errors
                final Stack<String> errors= new Stack<>();

                if(sFullName.isEmpty() || sUserName.isEmpty() || sEmail.isEmpty() || sPassword.isEmpty() || sPasswordConfirm.isEmpty()){
                    errors.push("Please fill all the fields");
                }else if(!sFullName.matches("^[a-zA-Z_ ]*$")){
                    errors.push("Please input valid full name");
                }else if(sUserName.length() > 20){
                    errors.push("Username should be less than 20 characters");
                }else if(sUserName.contains("@") || sUserName.contains("*") || sUserName.contains("&")){
                    errors.push("Username should not contain '@','*','&' these Symbols");
                }else if(!sEmail.contains("@") || !sEmail.contains(".com")){
                    errors.push("Please enter valid email address");
                }else if(sPassword.length()<8){
                    errors.push("Password should not be less than 8 character");
                }else if(!sPassword.equals(sPasswordConfirm)){
                    errors.push("Password is not matching with confirm password");
                }
//                check if any errors are there
                if(!errors.isEmpty()){
//                    making error string
                    String errorMessageString = "";
                    for (int i=0;i<errors.size();i++){
                        errorMessageString +=errors.get(i)+"";
                    }

//                    showing error alert message
                    showAlertDialog(errorMessageString,"Oops!! Error");
                }else{
                    final Toast waitToast =  Toast.makeText(RegisterActivity.this,"Please Wait..........",Toast.LENGTH_LONG);
                    waitToast.show();
                    pDb.collection("users")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    int isUsernameAlreadyExist = 0;
                                    if(task.isSuccessful()){
                                        for(QueryDocumentSnapshot document: task.getResult()){
                                            if(document.getId().equals(sUserName)){
                                                isUsernameAlreadyExist =1;
                                                break;
                                            }
                                        }

//                                       check if username already exists
                                        if(isUsernameAlreadyExist == 1){

//                                            cancel wait toast
                                            waitToast.cancel();
//                                           show username already exists error
                                            showAlertDialog("Username already exists please try another one!!","Oops!!!");
                                        }else if(errors.isEmpty()){

//                                            show wait toast
                                            waitToast.show();

//                                            disable register btn
                                            registerNowBtn.setEnabled(false);

//                                           create user account and store user info
                                            pAuth.createUserWithEmailAndPassword(sEmail,sPassword)
                                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<AuthResult> task) {

//                                                            check if auth is successful or not
                                                            if(task.isSuccessful()){
//                                                                cancel wait toast
                                                                waitToast.cancel();

//                                                                store email, fullName, username, amd post count in users collection
                                                                storeUserInfoInDatabase(task,sFullName,sUserName);

                                                            }else{
//                                                                show error of register process
                                                                showAlertDialog(task.getException().getMessage(),"Error while registering..");

//                                                                set register now btn enable
                                                                registerNowBtn.setEnabled(true);
                                                            }
                                                        }
                                                    });

                                        }
                                    }else{
//                                       Toast.makeText(RegisterActivity.this,"Something went wrong please try again",Toast.LENGTH_SHORT).show();
                                        errors.push("Something went wrong please try again");
                                    }
                                }
                            });
                }//end of errors else
            }// end of onclick method inside of btn click listener
        });//end of register now btn click listener

//        goto login page when clicked on goto login link
        gotoLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                goto Login page intent
                gotoLoginPage();
            }
        });

    }//end of onCreate method

//    this method is to store the user info in database
    private void storeUserInfoInDatabase(final Task<AuthResult> authTask, String sFullName, String sUserName) {

//        making data for storing
        Map<String, Object> userDataObj = new HashMap<>();
        userDataObj.put("email",authTask.getResult().getUser().getEmail());
        userDataObj.put("fullName",sFullName);
        userDataObj.put("posts",0);
        userDataObj.put("profile_image","default");

//        storing data in collection
        pDb.collection("users")
                .document(sUserName)
                .set(userDataObj)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
//                            show successful toast on register
                            Toast.makeText(RegisterActivity.this,"Successfully Registered..",Toast.LENGTH_SHORT).show();
//
//                            sign out the user so user can login on their own
                            pAuth.signOut();

//                            set register now btn enabled
                            registerNowBtn.setEnabled(true);

//                            goto Login Page After registration
                            gotoLoginPage();
                        }else{
//                            show error message of something went wrong
                            Toast.makeText(RegisterActivity.this,"Something went wrong please try again..",Toast.LENGTH_SHORT).show();

//                            delete user account so user can create new one
                            authTask.getResult().getUser().delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Log.d("UserCreationFailed","deleted the user account because user info storage procedure failed");
                                                registerNowBtn.setEnabled(true);
                                            }
                                        }
                                    });

//                            set register now btn enabled
                            registerNowBtn.setEnabled(true);
                        }

                    }
                });
    }

//    goto login page
    public void gotoLoginPage(){
        Intent gotoLoginActivity = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(gotoLoginActivity);
        finish();
    }

//    Alert dialog
    public void showAlertDialog(String message,String title){
        AlertDialog.Builder errorAlert= new AlertDialog.Builder(RegisterActivity.this);
        errorAlert.setTitle(title);
        errorAlert.setMessage(message);
        errorAlert.setIcon(android.R.drawable.ic_dialog_alert);
        errorAlert.setCancelable(true);
        errorAlert.setPositiveButton("Okay",null);
        errorAlert.show();
    }

}