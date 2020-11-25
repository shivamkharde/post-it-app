package com.shivamkharde.postit.models;

public class PostsModel {

//    variable declaration
    private String email;
    private String post_description;
    private String post_image;
    private long post_likes;
    private long id;

//    empty constructor for firebase
    private PostsModel(){}

    private PostsModel(String email, String post_description, String post_image, long post_likes,long id) {
        this.email = email;
        this.post_description = post_description;
        this.post_image = post_image;
        this.post_likes = post_likes;
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPost_description(String post_description) {
        this.post_description = post_description;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setPost_likes(long post_likes) {
        this.post_likes = post_likes;
    }

    public String getEmail() {
        return email;
    }

    public String getPost_description() {
        return post_description;
    }

    public String getPost_image() {
        return post_image;
    }

    public long getPost_likes() {
        return post_likes;
    }
}
