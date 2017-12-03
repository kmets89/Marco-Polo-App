package com.polo.marco.marcopoloapp.activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.firebase.models.Marco;
import com.polo.marco.marcopoloapp.firebase.models.Polo;
import com.polo.marco.marcopoloapp.firebase.models.User;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarcoActivity extends AppCompatActivity {

    private Switch publicSwitch;
    private final double winWidth = 0.8;
    private final double privateHeight = 0.75;
    private final double publicHeight = 0.5;
    private String[] friends;
    List<String> recv = new ArrayList<String>();
    CheckBox checkBox;
    LinearLayout checkView;
    boolean friendsListExists = false;

    double lat = LoginActivity.currentUser.getLatitude();
    double lng = LoginActivity.currentUser.getLongitude();
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    Date date = new Date();
    private String currentDate = dateFormat.format(date);

    private DatabaseReference databaseMarcos = FirebaseDatabase.getInstance().getReference("marcos");
    private DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference databasePolos = FirebaseDatabase.getInstance().getReference("polos");

    @Override
    //Opens a popup window for entering and storing Marco information.
    //Popup covers only a percentage of the screen with the previous view displayed underneath
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams params = win.getAttributes();
        params.dimAmount = 0.6f;
        win.setAttributes(params);

        setContentView(R.layout.activity_marco);
        checkView = (LinearLayout) findViewById(R.id.check_layout);

        publicSwitch = (Switch) findViewById(R.id.switch_public);
        setWinSize(winWidth, publicHeight);

        friends = new String[LoginActivity.currentUser.friendsListIds.size()];
        //check which activity called this one, show specific user if from FriendsListActivity
        Intent extras = getIntent();
        if (extras.getStringExtra("callingActivity") != null && extras.getStringExtra("callingActivity").equals("CustomDialog")) {
            publicSwitch.setChecked(true);
            publicSwitch.setVisibility(View.GONE);
            TextView textView = (TextView) findViewById(R.id.publicText);
            textView.setVisibility(View.GONE);
            textView = (TextView) findViewById(R.id.privateText);
            textView.setVisibility(View.GONE);
            setWinSize(winWidth, 0.475);
            findViewById(R.id.textView1).setVisibility(View.VISIBLE);
            int userPosition = findUser(extras.getStringExtra("userId"));
            checkBox = new CheckBox(MarcoActivity.this);
            checkBox.setId(0);
            checkBox.setText(LoginActivity.currentUser.friendsList.get(userPosition).getName());
            checkBox.setChecked(true);
            checkView.addView(checkBox);
        }

        checkForDuplicates();

        //Set behavior for the public/private toggle.  Default is public.  Private enlarges
        //the window and displays user's friends list members as optional recipients
        publicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setWinSize(winWidth, privateHeight);
                    findViewById(R.id.textView1).setVisibility(View.VISIBLE);

                    //create checklist from FriendsList and add to view
                    if (LoginActivity.currentUser == null || LoginActivity.currentUser.friendsList == null || LoginActivity.currentUser.friendsListIds == null || friends.length == 0) {
                        showAlert(getResources().getString(R.string.empty_friends_list));
                        publicSwitch.setChecked(false);
                        findViewById(R.id.textView1).setVisibility(View.GONE);
                        setWinSize(winWidth, publicHeight);
                        return;
                    }

                    if (!friendsListExists) {
                        if (LoginActivity.currentUser.friendsList.size() != LoginActivity.currentUser.friendsListIds.size())
                            for (int i = 0; i < friends.length; i++)
                                pullUserNamefromDB(i, LoginActivity.currentUser.friendsListIds.get(i));
                        else
                            for (int i = 0; i < friends.length; i++)
                                pullUserLocally(i);

                        friendsListExists = true;
                    } else {
                        for (int i = 0; i < friends.length; i++)
                            findViewById(i).setVisibility(View.VISIBLE);
                    }
                } else {
                    for (int j = 0; j < friends.length; j++)
                        findViewById(j).setVisibility(View.GONE);
                    findViewById(R.id.textView1).setVisibility(View.GONE);
                    setWinSize(winWidth, publicHeight);
                }
            }
        });
    }

    //Resize the activity window as a fraction of the default size
    public void setWinSize(double w, double h) {
        DisplayMetrics dispMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dispMetrics);
        int width = dispMetrics.widthPixels;
        int height = dispMetrics.heightPixels;

        getWindow().setLayout((int) (width * w), (int) (height * h));
    }

    //Handle selections in checklist
    View.OnClickListener getOnClickDoSomething(final Button button) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ON_CLICK", "CheckBox ID: " + button.getId() + " Text: " + button.getText().toString());
            }
        };
    }

    //Close the activity, not saving any data
    public void onClickCancelMarco(View view) {
        finish();
    }

    //Grabs current system date and time, userId and location, Marco message, public/private bool,
    // and adds it to the Marco table in the database. If the message is private, the selected
    //recipients are stored as well
    public void onClickSendMarco(View view) {
        boolean isPublic;
        String userId = LoginActivity.currentUser.getUserId();

        String message = ((EditText) findViewById(R.id.marcoText)).getText().toString();
        if (message.equals("")) {
            showAlert(getResources().getString(R.string.empty_message));
            return;
        }

        //expiration time set for 12 hours from current time, measured in seconds from Epoch
        long expireTime = (System.currentTimeMillis() / 1000L) + 43200;

        Intent extras = getIntent();
        //sending a Marco for an individual user, called from FriendsListActivity
        if (extras.getStringExtra("callingActivity") != null && extras.getStringExtra("callingActivity").equals("CustomDialog")) {
            isPublic = false;
            Marco privateMarco = new Marco(LoginActivity.currentUser.getUserId(), LoginActivity.currentUser.getName(), message, currentDate, expireTime, lat, lng, isPublic, recv);
            databaseMarcos.child(LoginActivity.currentUser.getUserId()).setValue(privateMarco);
            int userPosition = findUser(extras.getStringExtra("userId"));
            User retrievedUser = LoginActivity.currentUser.friendsList.get(userPosition);
            recv.add(retrievedUser.getFirebaseToken());
            databaseMarcos.child(LoginActivity.currentUser.getUserId()).child("receiverList").setValue(recv);

            Polo polo = new Polo(retrievedUser.getUserId(), message, LoginActivity.currentUser.getName(), currentDate, lat, lng, false);
            databasePolos.child(retrievedUser.getUserId()).child(LoginActivity.currentUser.getUserId()).setValue(polo);
        } else {
            if (publicSwitch.isChecked()) {
                    sendPrivateMarcoFromDB(message, currentDate, expireTime);
            } else {
                isPublic = true;
                Marco publicMarco = new Marco(userId, LoginActivity.currentUser.getName(), message, currentDate, expireTime, lat, lng, isPublic);
                databaseMarcos.child(userId).setValue(publicMarco);
            }
        }
        finish();
    }

    public void showAlert(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //just continue here
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.width = ViewGroup.LayoutParams.MATCH_PARENT;
        positiveButton.setLayoutParams(positiveButtonLL);
    }

    public void pullUserNamefromDB(int n, String id) {
        final int i = n;
        databaseUsers.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d("TESTING", "it doesnt exist!");
                } else {
                    User retrievedUser = snapshot.getValue(User.class);
                    friends[i] = retrievedUser.getName();
                    checkBox = new CheckBox(MarcoActivity.this);
                    checkBox.setId(i);
                    checkBox.setText(friends[i]);
                    checkBox.setOnClickListener(getOnClickDoSomething(checkBox));
                    checkView.addView(checkBox);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void pullUserLocally(int i) {
        User retrievedUser = LoginActivity.currentUser.friendsList.get(i);
        friends[i] = retrievedUser.getName();
        checkBox = new CheckBox(MarcoActivity.this);
        checkBox.setId(i);
        checkBox.setText(friends[i]);
        checkBox.setOnClickListener(getOnClickDoSomething(checkBox));
        checkView.addView(checkBox);
    }

    public void checkForDuplicates() {
        databaseMarcos.child(LoginActivity.currentUser.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d("TESTING", "it doesnt exist!");
                } else {
                    showAlert(getResources().getString(R.string.duplicate_marco));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void sendPrivateMarcoFromDB (final String message, String currentDate, long expireTime){
        boolean isPublic = false;
        final String cd = currentDate;
        Marco privateMarco = new Marco(LoginActivity.currentUser.getUserId(), LoginActivity.currentUser.getName(), message, currentDate, expireTime, lat, lng, isPublic, recv);
        databaseMarcos.child(LoginActivity.currentUser.getUserId()).setValue(privateMarco);
        for (int i = 0; i < friends.length; i++) {
            if (((CheckBox) findViewById(i)).isChecked()) {
                Log.d("CHECKING USER", LoginActivity.currentUser.friendsListIds.get(i));
                final int n = i;
                databaseUsers.child(LoginActivity.currentUser.friendsListIds.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(getApplicationContext(),
                                    "User " + (CheckBox) ((CheckBox) findViewById(n)).getText() + "Could not be reached!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            User retrievedUser = snapshot.getValue(User.class);
                            if (!retrievedUser.getBlockList().contains(LoginActivity.currentUser.getUserId())) {
                                recv.add(retrievedUser.getFirebaseToken());
                                databaseMarcos.child(LoginActivity.currentUser.getUserId()).child("receiverList").setValue(recv);

                                Polo polo = new Polo(retrievedUser.getUserId(), message, LoginActivity.currentUser.getName(), cd, lat, lng, false);
                                databasePolos.child(retrievedUser.getUserId()).child(LoginActivity.currentUser.getUserId()).setValue(polo);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }
    }

    public int findUser(String id) {
        for (int i = 0; i < LoginActivity.currentUser.friendsList.size(); i++)
            if (LoginActivity.currentUser.friendsList.get(i).getUserId().equals(id))
                return i;
        return -1;
    }
}
