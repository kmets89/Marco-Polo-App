package com.polo.marco.marcopoloapp.activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.firebase.models.Marco;
import com.polo.marco.marcopoloapp.firebase.models.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarcoActivity extends AppCompatActivity {

    private Switch publicSwitch;
    private final double winWidth = 0.8;
    private final double privateHeight = 0.8;
    private final double publicHeight = 0.4;
    private String[] friends;
    CheckBox checkBox;
    LinearLayout checkView;
    boolean friendsListExists = false;

    double lat = LoginActivity.currentUser.getLatitude();
    double lng = LoginActivity.currentUser.getLongitude();

    private DatabaseReference databaseMarcos = FirebaseDatabase.getInstance().getReference("marcos");;
    private DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");

    @Override
    //Opens a popup window for entering and storing Marco information.
    //Popup covers only a percentage of the screen with the previous view displayed underneath
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        friends = new String[LoginActivity.currentUser.friendsListIds.size()];
        for(int i = 0; i < friends.length; i++){
            pullUserName(i, LoginActivity.currentUser.friendsListIds.get(i));
        }

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_marco);
        checkView = (LinearLayout) findViewById(R.id.check_layout);

        publicSwitch = (Switch) findViewById(R.id.switch_public);
        setWinSize(winWidth, publicHeight);

        //Set behavior for the public/private toggle.  Default is public.  Private enlarges
        //the window and displays user's friends list members as optional recipients
        publicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    setWinSize(winWidth, privateHeight);
                    findViewById(R.id.textView1).setVisibility(View.VISIBLE);

                    //create checklist from FriendsList and add to view
                    if (friends.length == 0){
                        showAlert(getResources().getString(R.string.empty_friends_list));
                        publicSwitch.setChecked(false);
                        findViewById(R.id.textView1).setVisibility(View.GONE);
                        setWinSize(winWidth, publicHeight);
                    }
                    if (!friendsListExists) {
                        for (int i = 0; i < friends.length; i++) {
                            checkBox = new CheckBox(MarcoActivity.this);
                            checkBox.setId(i);
                            checkBox.setText(friends[i]);
                            checkBox.setId(i);
                            checkBox.setOnClickListener(getOnClickDoSomething(checkBox));
                            checkView.addView(checkBox);
                            friendsListExists = true;
                        }
                    }
                    else {
                        for (int i = 0; i < friends.length; i++)
                            findViewById(i).setVisibility(View.VISIBLE);
                    }
                }
                else{
                    for (int j = 0; j < friends.length; j++)
                        findViewById(j).setVisibility(View.GONE);
                    findViewById(R.id.textView1).setVisibility(View.GONE);
                    setWinSize(winWidth, publicHeight);
                }
            }
        });

//        Marco test = getMarco(LoginActivity.currentUser.getUserId());
//                    findViewById(R.id.textView1).setVisibility(View.GONE);
//        if (test != null)
//            showAlert(getResources().getString(R.string.duplicate_marco));
    }

    //Resize the activity window as a fraction of the default size
    public void setWinSize(double w, double h){
        DisplayMetrics dispMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dispMetrics);
        int width = dispMetrics.widthPixels;
        int height = dispMetrics.heightPixels;

        getWindow().setLayout((int) (width * w), (int) (height * h));
    }

    //Handle selections in checklist
    View.OnClickListener getOnClickDoSomething(final Button button){
        return new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("ON_CLICK", "CheckBox ID: " + button.getId() + " Text: " + button.getText().toString());
            }
        };
    }

    //Close the activity, not saving any data
    public void onClickCancelMarco(View view){
        finish();
    }

    //TODO: Add Marco to DB, get userID, delete log messages once done, add error checking
    //Grabs current system date and time, userId and location, Marco message, public/private bool,
    // and adds it to the Marco table in the database. If the message is private, the selected
    //recipients are stored as well
    public void onClickSendMarco(View view){
        boolean isPublic;
        String userId = LoginActivity.currentUser.getUserId();

        String message = ((EditText)findViewById(R.id.marcoText)).getText().toString();
        if (message.equals("")){
            showAlert(getResources().getString(R.string.empty_message));
            return;
        }

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date date = new Date();
        String currentDate = dateFormat.format(date);
        //expiration time set for 12 hours from current time, measured in seconds from Epoch
        long expireTime = (System.currentTimeMillis() / 1000L) + 43200;

        if (publicSwitch.isChecked()){
            isPublic = false;
            List<String> recv = new ArrayList<String>();
            for (int i = 0; i < friends.length; i++){
                if (((CheckBox)findViewById(i)).isChecked())
                    recv.add(LoginActivity.currentUser.friendsList.get(i).getFirebaseToken());
            }
            Marco privateMarco = new Marco(userId, message, currentDate, lat, lng, isPublic, recv);
            databaseMarcos.child(userId).setValue(privateMarco);
        }
        else{
            isPublic = true;
            Marco publicMarco = new Marco(userId, message, currentDate, lat, lng, isPublic);
            databaseMarcos.child(userId).setValue(publicMarco);
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

    public void pullUserName(int n, String id){
        final int i = n;
        databaseUsers.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d("TESTING", "it doesnt exist!");
                }
                else {
                    Log.d("TESTING", "it does exist!" + snapshot.getValue());
                    User retrievedUser = snapshot.getValue(User.class);
                    friends[i] = retrievedUser.getName();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
