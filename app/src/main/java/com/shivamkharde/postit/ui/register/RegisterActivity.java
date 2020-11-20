package com.shivamkharde.postit.ui.register;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shivamkharde.postit.R;
import com.shivamkharde.postit.ui.login.LoginActivity;

public class RegisterActivity extends AppCompatActivity {

//    variable declaration
    private TextView gotoLoginLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

//        onclick listener on Login link
        gotoLoginLink = findViewById(R.id.goto_login_link);
        gotoLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                goto Login page intent
                Intent gotoLoginActivity = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(gotoLoginActivity);
                finish();
            }
        });
    }
}