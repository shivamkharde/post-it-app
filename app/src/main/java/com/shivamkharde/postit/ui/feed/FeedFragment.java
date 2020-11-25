package com.shivamkharde.postit.ui.feed;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shivamkharde.postit.R;
import com.shivamkharde.postit.models.PostsModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FeedFragment extends Fragment {

//    variable declaration
    private RecyclerView feedPostsRecycleView;
    private boolean liked = false;

//    Firebase declaration
    private FirebaseFirestore pDb;
    private FirestoreRecyclerAdapter postsAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_feed, container, false);

//        variable initialization
        feedPostsRecycleView = root.findViewById(R.id.feed_posts_recycle_view);
        pDb = FirebaseFirestore.getInstance();

//        firebase query
        Query fireQuery = pDb.collection("posts");

//        recycler options
        FirestoreRecyclerOptions<PostsModel> postsRecyclerOptions = new FirestoreRecyclerOptions.Builder<PostsModel>()
                .setQuery(fireQuery,PostsModel.class)
                .build();

//        fireStore adapter
        postsAdapter = new FirestoreRecyclerAdapter<PostsModel,PostsViewHolder>(postsRecyclerOptions) {
            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

//                creating view object hold single post view
                View singlePostView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_post_design,parent,false);

                return new PostsViewHolder(singlePostView);
            }

            @Override
            protected void onBindViewHolder(@NonNull final PostsViewHolder holder, int position, @NonNull final PostsModel model) {

//                getting data from model class and applying it to ui elements
                Glide.with(getActivity().getApplicationContext())
                        .load(model.getPost_image())
                        .into(holder.postImage);
                holder.postDescription.setText(model.getPost_description());
                holder.postLikeCount.setText(model.getPost_likes()+"");
//                add onclick listener on like btn
                holder.postLikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(liked == false){
//                            set liked tag and increase like count in db
                            holder.postLikeBtn.setImageResource(R.drawable.ic_baseline_favorite_24);
                            liked = false;
                            new Timer()
                                    .schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            incrementLikeCount(model.getId(),holder);
                                        }
                                    },1000);

                        }else{
//                            remove liked tag and decrement like count in db
                            holder.postLikeBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                            liked = true;
                            new Timer()
                                    .schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            decrementLikeCount(model.getId(),holder);
                                        }
                                    },1000);

                        }
                    }
                });
            }
        };

        feedPostsRecycleView.setHasFixedSize(true);
        feedPostsRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        feedPostsRecycleView.setAdapter(postsAdapter);
        return root;
    }

//    this function is to decrement like count when like btn is clicked
    private void decrementLikeCount(final long id, final PostsViewHolder holder) {
        pDb.collection("posts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if data is available
                        if(task.isSuccessful()){
//                            get data and travers until you find the correct post using id
                            for (QueryDocumentSnapshot doc: task.getResult()){
                                final Map<String,Object> post = doc.getData();
                                if((long)post.get("id") == id){
                                    doc.getReference().update("post_likes", FieldValue.increment(-1))
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
//                                                    store likes in likes collection
                                                    pDb.collection("likes")
                                                            .get()
                                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if(task.isSuccessful()){
                                                                        for (QueryDocumentSnapshot doc: task.getResult()){
                                                                            Map<String,Object> postLike = doc.getData();
                                                                            if(postLike.get("email").toString().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                                                                                doc.getReference().delete();
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            });

                                                }
                                            });
                                    break;
                                }
                            }

                        }else{
                            Toast.makeText(getContext(),"Something went wrong try again",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //    this function is to increment the like count when like btn is clicked
    private void incrementLikeCount(final long id, final PostsViewHolder holder) {
        pDb.collection("posts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if data is available
                        if(task.isSuccessful()){
//                            get data and travers until you find the correct post using id
                            for (QueryDocumentSnapshot doc: task.getResult()){
                                Map<String,Object> post = doc.getData();
                                if((long)post.get("id") == id){
                                    doc.getReference().update("post_likes", FieldValue.increment(1))
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Map<String,Object> postLike = new HashMap<>();
                                                    postLike.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                                    postLike.put("post_id",id);
//                                                    store likes in likes collection
                                                    pDb.collection("likes")
                                                            .add(postLike)
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                    holder.postLikeBtn.setImageResource(R.drawable.ic_baseline_favorite_24);
                                                                }
                                                            });
                                                }
                                            });
                                    break;
                                }
                            }
                        }else{
                            Toast.makeText(getContext(),"Something went wrong try again",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private class PostsViewHolder extends RecyclerView.ViewHolder{

//        variable declaration
        private ImageView postProfileImage;
        private TextView postUsername;
        private ImageView postImage;
        private TextView postLikeCount;
        private TextView postDescription;
        private ImageView postLikeBtn;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
//            getting ui elements from item view which is passed by adapter method on create view holder
            postProfileImage = itemView.findViewById(R.id.profile_image_in_post);
            postUsername = itemView.findViewById(R.id.username_text_in_post);
            postImage = itemView.findViewById(R.id.post_image);
            postLikeCount = itemView.findViewById(R.id.post_like_count_text);
            postDescription = itemView.findViewById(R.id.description_text);
            postLikeBtn = itemView.findViewById(R.id.post_like_btn);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        postsAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        postsAdapter.stopListening();
    }
}