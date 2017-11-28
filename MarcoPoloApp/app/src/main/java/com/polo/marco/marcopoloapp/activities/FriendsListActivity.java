package com.polo.marco.marcopoloapp.activities;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.polo.marco.marcopoloapp.firebase.models.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FriendsListActivity extends AppCompatActivity {

    private ListView listView;
    private List<User> friends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView)findViewById(R.id.friendsListView);
        friends = LoginActivity.currentUser.friendsList;
        if(friends == null || friends.size() == 0){
            Toast.makeText(this, "You don't seem to have any friends who use this app!", Toast.LENGTH_LONG).show();
        }else{
            friends = LoginActivity.currentUser.friendsList;
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
            Log.d("FriendListActivity", "img: " + currentFriend.getImgUrl());
            Picasso.with(FriendsListActivity.this).load(currentFriend.getImgUrl()).into(profilePicView);
            nameTextView.setText(currentFriend.getName());

            return itemView;
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    public void syncContacts() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Log.d("LOOK HERE", id + ": " + name);
                //if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.com.polo.marco.marcopoloapp.activities.Contacts.HAS_PHONE_NUMBER))) > 0) {
                //Query phone here.  Covered next
            }
        }
    }
}
