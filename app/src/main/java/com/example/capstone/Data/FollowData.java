package com.example.capstone.Data;

public class FollowData {

    String email, follow, email_follow;

    public FollowData() {
        this.follow = "";
        this.email = "";
        this.email_follow = "";
    }

    public FollowData(String email, String follow) {
        this.email = email;
        this.follow = follow;
        this.email_follow = email + "_" + follow;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFollow() {
        return follow;
    }

    public void setFollow(String follow) {
        this.follow = follow;
    }

    public String getEmail_follow() {
        return email_follow;
    }

    public void setEmail_follow(String email_follow) {
        this.email_follow = email_follow;
    }

}
