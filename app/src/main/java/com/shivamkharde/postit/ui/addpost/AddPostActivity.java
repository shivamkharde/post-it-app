package com.shivamkharde.postit.ui.addpost;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.shivamkharde.postit.R;

public class AddPostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        getSupportActionBar().setTitle("Add new post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}