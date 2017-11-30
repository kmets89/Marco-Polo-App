package com.polo.marco.marcopoloapp.firebase.tasks;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.polo.marco.marcopoloapp.activities.LoginActivity;
import com.polo.marco.marcopoloapp.firebase.models.User;

import java.util.ArrayList;
import java.util.List;

import static com.polo.marco.marcopoloapp.activities.LoginActivity.firebaseToken;
import static com.polo.marco.marcopoloapp.activities.LoginActivity.currentUser;
/**
 * Created by Krazy on 11/26/2017.
 */

public class LoadUserFromDbEvent implements ValueEventListener {
    private String name;
    private String id;
    private String loginApiType;
    private String imgUrl;
    private String email;
    private DatabaseReference databaseUsers;

    private final String TAG = "lOAD_USER_FROM_FIREBASE";

    public LoadUserFromDbEvent(DatabaseReference databaseUsers, String id, String name, String loginApiType, String imgUrl, String email) {
        this.name = name;
        this.id = id;
        this.loginApiType = loginApiType;
        this.imgUrl = imgUrl;
        this.databaseUsers = databaseUsers;
        this.email = email;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        User retrievedUser = dataSnapshot.getValue(User.class);
        if(retrievedUser != null){
            currentUser = retrievedUser;
            currentUser.friendsListIds = retrievedUser.friendsListIds;
            //If the firebasetoken is not null, it means it has been updated.
            //So we must update the users' token in the database.
            if(firebaseToken != null){
                currentUser.setFirebaseToken(firebaseToken);
                databaseUsers.child(id).setValue(currentUser);
            }
            currentUser = new User(id, name, loginApiType, currentUser.friendsListIds, currentUser.blockList, imgUrl, email, firebaseToken);
            databaseUsers.child(id).setValue(currentUser);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d("seomthing", databaseError.getDetails());
        Log.d("seomthing", databaseError.getMessage());
    }
}