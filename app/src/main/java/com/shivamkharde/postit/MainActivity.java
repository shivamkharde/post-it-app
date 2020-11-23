package com.shivamkharde.postit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shivamkharde.postit.ui.addpost.AddPostActivity;
import com.shivamkharde.postit.ui.login.LoginActivity;
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

//    firebase variable declaration
    private FirebaseAuth pAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        force system dark mode to not affect to our app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        variable initialization
        addNewPostBtn = findViewById(R.id.add_post_btn);
        pAuth = FirebaseAuth.getInstance();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_feed, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController,appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);


//        adding on click listener on add post btn
        addNewPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoAddPostActivity = new Intent(MainActivity.this, AddPostActivity.class);
                startActivity(gotoAddPostActivity);
            }
        });
    }

//    onStart method
    @Override
    protected void onStart() {
        super.onStart();
//        check if user is currently logged in or not
        FirebaseUser currentUser = pAuth.getCurrentUser();
        if(currentUser==null){
            gotoLoginPage();
        }
    }

    //    function is to go on register page
    public void gotoLoginPage(){
        Intent gotoLoginActivity = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(gotoLoginActivity);
        finish();
    }
}