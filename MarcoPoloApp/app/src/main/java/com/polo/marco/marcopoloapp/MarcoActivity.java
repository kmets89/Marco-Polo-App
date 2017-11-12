package com.polo.marco.marcopoloapp;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

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

    @Override
    //Opens a popup window containing user's notifications in an expandable list.
    //Popup covers only a percentage of the screen with the previous view displayed underneath
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //friendsList = getResources().getStringArray(R.array.friends_list);
        //checkedItems = new boolean[friendsList.length];

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.marco_activity);

        publicSwitch = (Switch) findViewById(R.id.switch_public);

        setWinSize(winWidth, publicHeight);

        publicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Toast.makeText(getApplicationContext(), "PRIVATE", Toast.LENGTH_LONG).show();
                    setWinSize(winWidth, privateHeight);
                }
                else{
                    Toast.makeText(getApplicationContext(), "PUBLIC", Toast.LENGTH_LONG).show();
                    setWinSize(winWidth, publicHeight);
                }
            }
        });
    }

    //Make this view a pop-up over the previous view with dimensions
    //relative to the parent, goes away when the user click outside
    //the popup window

    public void setWinSize(double w, double h){
        DisplayMetrics dispMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dispMetrics);
        int width = dispMetrics.widthPixels;
        int height = dispMetrics.heightPixels;

        getWindow().setLayout((int) (width * w), (int) (height * h));
    }


   /* //Function that's called when the marco button is clicked
    public void onClickBtnMarco(View view){
        //Begins building the Dialog
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle("Select your friends.");
        mBuilder.setMultiChoiceItems(friendsList, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            //Adds each checked friend to the arrayList "mSelectedItems".
            public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                if(isChecked){
                    if(!mSelectedItems.contains(position)){
                        mSelectedItems.add(position);
                    }
                }
                else if(mSelectedItems.contains(position)){
                    mSelectedItems.remove(mSelectedItems.indexOf(position));
                }
            }
        });
        mBuilder.setCancelable(false);
        mBuilder.setPositiveButton("Send!", new DialogInterface.OnClickListener() {
            @Override
            //This event listener calls the function which will sound out the Marco.
            public void onClick(DialogInterface dialog, int position) {
                SendMarco();
            }
        });
        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            //Cancels the Marco
            public void onClick(DialogInterface dialog, int position) {
                dialog.dismiss();
            }
        });
        mBuilder.setNeutralButton("Clear all", new DialogInterface.OnClickListener() {
            @Override
            //When "Clear all" is called, all the checkedItems are reset.
            public void onClick(DialogInterface dialog, int position) {
                for (int i = 0; i < checkedItems.length; i++){
                    checkedItems[i] = false;
                    mSelectedItems.clear();
                }
            }
        });
        //creates and displays the Dialog.
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }*/

    /*public void SendMarco(){
        Log.d("something", "marco button click");
        boolean switchStatus = publicSwitch.isChecked();
        Log.d("CRAP", "onClickBtnMarco: something");
        //TODO: Write code to send out a public Marco.
        if(switchStatus){
            Toast.makeText(this, "Sending a Public Marco!", Toast.LENGTH_SHORT).show();
        }
        //TODO: Write code to send out a private Marco.
        else{
            Toast.makeText(this, "Sending a Private Marco!", Toast.LENGTH_SHORT).show();
        }
    }*/

}
