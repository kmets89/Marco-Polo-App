package com.polo.marco.marcopoloapp.activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.polo.marco.marcopoloapp.R;

import java.util.ArrayList;

public class MarcoActivity extends AppCompatActivity {

    //UI stuff
    private Switch publicSwitch;
    private final double winWidth = 0.8;
    private final double privateHeight = 0.8;
    private final double publicHeight = 0.4;
    private String[] friendsList;
    private boolean[] checkedItems;
    ArrayList<Integer> mSelectedItems = new ArrayList<>();
    CheckBox checkBox;
    LinearLayout checkView;
    boolean friendsListExists = false;

    @Override
    //Opens a popup window for entering and storing Marco information.
    //Popup covers only a percentage of the screen with the previous view displayed underneath
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        friendsList = getResources().getStringArray(R.array.friends_list);
        checkedItems = new boolean[friendsList.length];

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.marco_activity);
        checkView = (LinearLayout) findViewById(R.id.check_layout);

        publicSwitch = (Switch) findViewById(R.id.switch_public);
        setWinSize(winWidth, publicHeight);

        //Set behavior for the public/private toggle.  Default is public.  Private enlarges
        //the window and displays user's friends list members as optional recipients
        publicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Toast.makeText(getApplicationContext(), "PRIVATE", Toast.LENGTH_LONG).show();
                    setWinSize(winWidth, privateHeight);
                    findViewById(R.id.textView1).setVisibility(View.VISIBLE);

                    //create checklist from FriendsList and add to view
                    if (!friendsListExists) {
                        for (int i = 0; i < friendsList.length; i++) {
                            checkBox = new CheckBox(MarcoActivity.this);
                            checkBox.setId(i);
                            checkBox.setText(friendsList[i]);
                            checkBox.setId(i);
                            checkBox.setOnClickListener(getOnClickDoSomething(checkBox));
                            checkView.addView(checkBox);
                            friendsListExists = true;
                        }
                    }
                    else
                        for (int i = 0; i < friendsList.length; i++)
                            findViewById(i).setVisibility(View.VISIBLE);
                    }
                else{
                    Toast.makeText(getApplicationContext(), "PUBLIC", Toast.LENGTH_LONG).show();
                    findViewById(R.id.textView1).setVisibility(View.GONE);
                    for (int j = 0; j < friendsList.length; j++)
                        findViewById(j).setVisibility(View.GONE);
                    setWinSize(winWidth, publicHeight);
                }
            }
        });
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
        Toast.makeText(getApplicationContext(), "CANCEL", Toast.LENGTH_LONG).show();
        finish();
    }

    //TODO: Add Marco to DB
    public void onClickSendMarco(View view){
        Toast.makeText(getApplicationContext(), "SEND", Toast.LENGTH_LONG).show();
        finish();
    }
}
