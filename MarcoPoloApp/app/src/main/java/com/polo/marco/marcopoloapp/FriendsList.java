package com.polo.marco.marcopoloapp;

import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.polo.marco.marcopoloapp.api.database.Database;
import com.polo.marco.marcopoloapp.api.database.User;

import java.util.ArrayList;
import java.util.List;

public class FriendsList extends AppCompatActivity {

    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        listView = (ListView)findViewById(R.id.friendsListView);
        User user = Database.getUser("0102112301");
        List<String> friendsList = user.getFriendsList();
        User[] friends = Database.getListOfFriends(friendsList.toArray(new String[0]));
        String[] friendNames = new String[friends.length];
        for(int i = 0; i < friends.length; i++){
            friendNames[i] = friends[i].getName();
        }

        ArrayAdapter<String> adaptor = new ArrayAdapter<String>(this, R.layout.friends_list_layout, friendNames);
        listView.setAdapter(adaptor);
    }
}