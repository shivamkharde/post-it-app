package com.shivamkharde.postit.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shivamkharde.postit.R;
import com.shivamkharde.postit.ui.register.RegisterActivity;

public class LoginActivity extends AppCompatActivity {

//    variables
    private TextView gotoRegisterLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        hide action bar
        getSupportActionBar().hide();

//        onclick listener on register here link
        gotoRegisterLink = findViewById(R.id.goto_register_link);
        gotoRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                goto register page intent
                Intent gotoRegisterActivity = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(gotoRegisterActivity);
                finish();
            }
        });

    }
}