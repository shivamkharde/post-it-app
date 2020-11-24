package com.shivamkharde.postit.ui.profile;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.security.ConfirmationPrompt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shivamkharde.postit.R;
import com.shivamkharde.postit.ui.addpost.AddPostActivity;
import com.shivamkharde.postit.ui.login.LoginActivity;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

//    variable declaration
    private ImageView profileImage;
    private TextView fullNameOfUser;
    private TextView userNameOfUser;
    private TextView noOfPosts;
    private Button logoutBtn;

//    Firebase Declaration
    private FirebaseAuth pAuth;
    private FirebaseFirestore pDb;
    private FirebaseUser pUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

//        inflating fragment for getting ui elements
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

//        variable initialization
        profileImage = root.findViewById(R.id.profile_image);
        fullNameOfUser = root.findViewById(R.id.full_name_of_user);
        userNameOfUser = root.findViewById(R.id.username_of_user);
        noOfPosts = root.findViewById(R.id.no_of_posts);
        logoutBtn = root.findViewById(R.id.logout_btn);

//        Firebase initialization
        pAuth = FirebaseAuth.getInstance();
        pDb  = FirebaseFirestore.getInstance();
        pUser = pAuth.getCurrentUser();

//        check if user is already login or not if not then redirect user to login page
        if(pUser!=null){
//            get data from Firebase and set it to ui elements
            pDb.collection("users")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if(task.isSuccessful()){
//                                making map data to store userInfo
                                Map<String,Object> userInfo = new HashMap<>();

//                                Iterate through data to find correct user and get the data
                                for (QueryDocumentSnapshot doc: task.getResult()){
                                    Map<String,Object> currentUserData = doc.getData();
                                    if(currentUserData.get("email").equals(pUser.getEmail())){
                                        userInfo = currentUserData;
                                        userInfo.put("username",doc.getId());
                                        break;
                                    }
                                }

//                                attach data in ui elements
                                fullNameOfUser.setText(userInfo.get("fullName").toString());
                                userNameOfUser.setText("@"+userInfo.get("username").toString());
                                noOfPosts.setText(userInfo.get("posts").toString());

//                                show the profile image if link is available
                                if(userInfo.get("profile_image").equals("default")){
//                                    do nothing
                                }else{
                                    Glide.with(getActivity().getApplicationContext())
                                            .load(userInfo.get("profile_image"))
                                            .into(profileImage);
                                }
                            }else{
//                                show error
                                showAlertDialog("Something went wrong...(Please check your internet connection)","Error");
                            }
                        }
                    });

//            when clicked on logout
            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

//                    get confirmation from user to logout from device
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Confirmation")
                            .setMessage("Do you really want to logout from this device?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    logout from this device
                                    pAuth.signOut();
//                                    send user to login page
                                    gotoLoginPage();
                                }
                            })
                            .setNegativeButton(android.R.string.no,null).show();
                }
            });
        }else{

//            go to login page
            gotoLoginPage();
        }

        return root;
    }

//    this function is to redirect user to login page
    public void gotoLoginPage(){
        Intent gotoLoginActivity = new Intent(getActivity(), LoginActivity.class);
        startActivity(gotoLoginActivity);
        getActivity().finish();
    }

//    this function is to show the alerts
    public void showAlertDialog(String message,String title){

        AlertDialog.Builder sDialog = new AlertDialog.Builder(getActivity().getApplicationContext());
        sDialog.setTitle(title);
        sDialog.setMessage(message);
        sDialog.setIcon(android.R.drawable.ic_dialog_alert);
        sDialog.setCancelable(true);
        sDialog.setPositiveButton("Okay",null);
        sDialog.show();
    }
}