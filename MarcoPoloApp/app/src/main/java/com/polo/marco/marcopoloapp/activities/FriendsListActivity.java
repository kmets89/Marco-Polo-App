package com.polo.marco.marcopoloapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.polo.marco.marcopoloapp.*;
import com.polo.marco.marcopoloapp.api.database.Database;
import com.polo.marco.marcopoloapp.api.database.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FriendsListActivity extends AppCompatActivity {

    private ListView listView;
    private List<User> friends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        listView = (ListView)findViewById(R.id.friendsListView);
        friends = LoginActivity.currentUser.friendsUserList;
        if(friends == null || friends.size() == 0){
            Toast.makeText(this, "You dont seem to have any friends!", Toast.LENGTH_LONG).show();
        }else{
            friends = LoginActivity.currentUser.friendsUserList;
            ArrayAdapter<User> adaptor = new MyListAdaptor();
            listView.setAdapter(adaptor);
        }
    }

    private class MyListAdaptor extends ArrayAdapter<User>
    {
        public MyListAdaptor() { super(FriendsListActivity.this, R.layout.friends_list_layout, friends);}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.friends_list_layout, parent, false);
            }

            //current friend
            User currentFriend = friends.get(position);

            TextView nameTextView = (TextView) itemView.findViewById(R.id.friendslist_user_name);
            ImageView profilePicView = (ImageView) itemView.findViewById(R.id.friendslist_profile_image);
            Picasso.with(FriendsListActivity.this).load(currentFriend.getImgUrl()).into(profilePicView);
            nameTextView.setText(currentFriend.getName());

            return itemView;
        }
    }
}