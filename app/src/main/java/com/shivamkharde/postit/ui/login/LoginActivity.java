package com.shivamkharde.postit.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
import com.shivamkharde.postit.MainActivity;
import com.shivamkharde.postit.R;
import com.shivamkharde.postit.ui.register.RegisterActivity;

import java.util.Stack;

public class LoginActivity extends AppCompatActivity {

//    variables
    private TextView gotoRegisterLink;
    private EditText email;
    private EditText password;
    private Button loginBtn;

//    Firebase variables
    FirebaseAuth pAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        hide action bar
        getSupportActionBar().hide();

//        variable initialization
        gotoRegisterLink = findViewById(R.id.goto_register_link);
        email = findViewById(R.id.email_field_login);
        password = findViewById(R.id.password_field_login);
        loginBtn = findViewById(R.id.login_btn);

//        Firebase initialization
        pAuth = FirebaseAuth.getInstance();

//        set onclick listener on login btn
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                getting email and password from field
                String sEmail = email.getText().toString().trim();
                String sPassword = password.getText().toString().trim();

//                errors stack to store errors
                final Stack<String> errors= new Stack<>();

//                check if above info is correct or not
                if(sEmail.isEmpty() || sPassword.isEmpty()){
                    errors.push("Please fill all the fields");
                }else if(!sEmail.contains("@") || !sEmail.contains(".com")){
                    errors.push("Please enter valid email address");
                }else if(sPassword.length()<8){
                    errors.push("Password should not be less than 8 character");
                }

//                check if errors are available in stack or not
                if(!errors.isEmpty()){
//                    making error string
                    String errorMessageString = "";
                    for (int i=0;i<errors.size();i++){
                        errorMessageString +=errors.get(i)+"";
                    }

//                    showing error alert message
                    showAlertDialog(errorMessageString,"Oops!! Error");
                }else{
//                    sign in the user
                    signInUser(sEmail,sPassword);

                }//end of errors else

            }//end of on click method inside of on click listener
        });//end of on click listener

//        goto register page when click on registerLink
        gotoRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                goto register page intent
                Intent gotoRegisterActivity = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(gotoRegisterActivity);
            }//end of on click method inside of on click listener
        });//end of on click listener

    }//end of on create method

//    goto Main page On  Sign in
    public void gotoMainPage(){
        Intent gotoMainActivity = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(gotoMainActivity);
        finish();
    }

//    Alert dialog
    public void showAlertDialog(String message,String title){
        AlertDialog.Builder errorAlert= new AlertDialog.Builder(LoginActivity.this);
        errorAlert.setTitle(title);
        errorAlert.setMessage(message);
        errorAlert.setIcon(android.R.drawable.ic_dialog_alert);
        errorAlert.setCancelable(true);
        errorAlert.setPositiveButton("Okay",null);
        errorAlert.show();
    }

//    sign in user method
    public void signInUser(String email, String password){
//        show please wait toast
        final Toast waitToast = Toast.makeText(LoginActivity.this,"Please Wait...",Toast.LENGTH_LONG);
        waitToast.show();

//        sign in with email and password
        pAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
//                            remove wait toast
                            waitToast.cancel();

//                            show successful login toast
                            Toast.makeText(LoginActivity.this, "Login successful...", Toast.LENGTH_SHORT).show();

//                            goto main homepage
                            gotoMainPage();
                        }else{
//                            remove waitToast
                            waitToast.cancel();

//                            show error message from firebase
                            showAlertDialog(task.getException().getMessage(),"Login Failed!!");
                        }
                    }
                });
    }
}