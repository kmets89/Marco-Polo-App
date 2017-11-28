package com.polo.marco.marcopoloapp.firebase.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Chase on 10/30/2017.
 */

    /*
    * User object. We will be using this to store relevant data about the user.
    * Feel free to add new fields: these correlate to columns in DynamoDB.
    * */
public class User {
    private String userId;
    private String name;
    private String loginApiType;
    private double latitude;
    private double longitude;
    private String imgUrl;
    private String firebaseToken;
    public List<String> friendsListIds;
    public List<User> friendsList;

    public String email;

    public User() {
        if (friendsListIds == null) {
            this.friendsListIds = new ArrayList<String>();
        } else {
            this.friendsListIds = friendsListIds;
        }

        if (friendsList == null) {
            this.friendsList = new ArrayList<User>();
        } else {
            this.friendsList = friendsList;
        }
    }
    //User(id, name, loginApiType, imgUrl, firebaseToken, new ArrayList<String>());
    public User(String userId, String name, String loginApiType, List<String> friendsListIds, String imgUrl, String email, String firebaseToken) {
        this.userId = userId;
        this.name = name;
        this.loginApiType = loginApiType;

        if (friendsListIds == null) {
            this.friendsListIds = new ArrayList<String>();
        } else {
            this.friendsListIds = friendsListIds;
        }

        if (friendsList == null) {
            this.friendsList = new ArrayList<User>();
        } else {
            this.friendsList = friendsList;
        }

        this.latitude = latitude;
        this.longitude = longitude;
        this.imgUrl = imgUrl;
        this.email = email;
        this.firebaseToken = firebaseToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User(String userId, String name, String imgUrl) {
        this.userId = userId;
        this.name = name;
        this.loginApiType = "";
        if (friendsListIds == null) {
            this.friendsListIds = new ArrayList<String>();
        } else {
            this.friendsListIds = friendsListIds;
        }

        if (friendsList == null) {
            this.friendsList = new ArrayList<User>();
        } else {
            this.friendsList = friendsList;
        }
        this.latitude = 0;
        this.longitude = 0;
        this.imgUrl = imgUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String url) {
        this.imgUrl = url;
    }

    public List<User> getFriendsList() {
        return friendsList;
    }

    public void setFriendsList(List<User> friendsList) {
        this.friendsList = friendsList;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public List<String> getFriendsListIds() {
        return friendsListIds;
    }

    public void setFriendsListIds(List<String> friendsListIds) {
        this.friendsListIds = friendsListIds;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean usingFacebook() {
        return loginApiType.equalsIgnoreCase("facebook");
    }

    public String getLoginApiType() {
        return loginApiType;
    }

    public void setLoginApiType(String loginApiType) {
        this.loginApiType = loginApiType;
    }

    public String toString() {
        return "[" + userId + "] " + name + ": LoginAPIType~" + loginApiType + ":friendsList~" + Arrays.toString(getFriendsList().toArray(new String[friendsList.size()]));
    }
}