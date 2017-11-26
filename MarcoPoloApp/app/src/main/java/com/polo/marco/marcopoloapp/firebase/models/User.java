package com.polo.marco.marcopoloapp.firebase.models;

import java.util.List;

/**
 * Created by Krazy on 11/26/2017.
 */

public class User {
    String userId;
    String name;
    String loginApiType;
    String imgUrl;
    String firebaseToken;
    List<String> friendsListIds;

    public User() {}

    public User(String userId, String name, String loginApiType, String imgUrl, String firebaseToken, List<String> friendsListIds) {

        this.userId = userId;
        this.name = name;
        this.loginApiType = loginApiType;
        this.imgUrl = imgUrl;
        this.firebaseToken = firebaseToken;
        this.friendsListIds = friendsListIds;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getLoginApiType() {
        return loginApiType;
    }

    public String getImgUrl() {
        return imgUrl;
    }
    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String token){firebaseToken = token;}

    public List<String> getFriendsListIds() {
        return friendsListIds;
    }

    public boolean usingFacebook(){return this.loginApiType.equalsIgnoreCase("facebook");}
}
