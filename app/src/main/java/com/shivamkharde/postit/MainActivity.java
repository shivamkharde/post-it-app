package com.shivamkharde.postit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shivamkharde.postit.ui.addpost.AddPostActivity;
import com.shivamkharde.postit.ui.register.RegisterActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

//    variable declaration
    private FloatingActionButton addNewPostBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        force system dark mode to not affect to our app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_feed, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController,appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

//        variable initialization
        addNewPostBtn = findViewById(R.id.add_post_btn);
        int register = 0;

        if(register != 1){
            Intent gotoRegisterActivity = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(gotoRegisterActivity);
        }

//        adding on click listener on add post btn
        addNewPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoAddPostActivity = new Intent(MainActivity.this, AddPostActivity.class);
                startActivity(gotoAddPostActivity);
            }
        });
    }

}