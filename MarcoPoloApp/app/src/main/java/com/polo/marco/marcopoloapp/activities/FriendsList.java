package com.polo.marco.marcopoloapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.api.database.Database;
import com.polo.marco.marcopoloapp.api.database.User;

import java.util.List;

public class FriendsList extends AppCompatActivity {

    private ListView listView;
    private User[] friends;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        listView = (ListView)findViewById(R.id.friendsListView);
        User user = Database.getUser("0");
        List<String> friendsList = user.getFriendsList();
        friends = Database.getListOfFriends(friendsList.toArray(new String[0]));

        ArrayAdapter<User> adaptor = new MyListAdaptor();
        listView.setAdapter(adaptor);
    }

    private class MyListAdaptor extends ArrayAdapter<User>
    {
        public MyListAdaptor() {
            super(FriendsList.this, R.layout.friends_list_layout, friends);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null){
                itemView = getLayoutInflater().inflate(R.layout.friends_list_layout, parent, false);
            }

            //current friend
            User currentFriend = friends[position];

            TextView nameTextView = (TextView) itemView.findViewById(R.id.friendslist_user_name);
            nameTextView.setText(currentFriend.getName());

            return itemView;
        }
    }
}