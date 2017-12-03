package com.polo.marco.marcopoloapp.api.notifications;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.activities.LoginActivity;
import com.polo.marco.marcopoloapp.firebase.models.Polo;

import java.util.ArrayList;
import java.util.LinkedHashMap;

//This class provides a popup window detailing a user's notifications inside an expandable list
//For alterations/debugging, consult CustomListAdapter, DetailInfo, and HeaderInfo classes and their
//XML files
public class Notifications extends AppCompatActivity implements OnClickListener{

    private LinkedHashMap<String, HeaderInfo> mySection = new LinkedHashMap<>();
    private ArrayList<HeaderInfo> sectionList = new ArrayList<>();
    private CustomListAdapter listAdapter;
    private ExpandableListView expandableListView;
    private DatabaseReference databasePolos = FirebaseDatabase.getInstance().getReference("polos");

    @Override
    //Opens a popup window containing user's notifications in an expandable list.
    //Popup covers only a percentage of the screen with the previous view displayed underneath
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.notifications);
        //Make this view a pop-up over the previous view with dimensions
        //relative to the parent, goes away when the user click outside
        //the popup window
        DisplayMetrics dispMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dispMetrics);
        int width = dispMetrics.widthPixels;
        int height = dispMetrics.heightPixels;

        Window win = getWindow();
        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.5));

        win.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams params = win.getAttributes();
        params.dimAmount = 0.6f;
        win.setAttributes(params);




        //Creates an adapter to pipe in data to the list view.  For now ignore the spinner
        //that we're not using
        //Spinner spinner = (Spinner) findViewById(R.id.spinGroup);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.dept_array, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner.setAdapter(adapter);

        addData();

        expandableListView = (ExpandableListView) findViewById(R.id.notificationListView);
        listAdapter = new CustomListAdapter(Notifications.this, sectionList);
        expandableListView.setAdapter(listAdapter);

        collapseAll();

        expandableListView.setOnChildClickListener(myListItemClicked);
        expandableListView.setOnGroupClickListener(myListGroupClicked);
    }

   private void expandAll(){
       int count = listAdapter.getGroupCount();
       for (int i = 0; i < count; i++){
           expandableListView.expandGroup(i);
       }
   }

   private void collapseAll(){
       int count = listAdapter.getGroupCount();
       for (int i = 0; i < count; i ++){
           expandableListView.collapseGroup(i);
       }
   }

   //dummy data for testing.  This is where our database query stuff will go
   private void addData(){
       databasePolos.child(LoginActivity.currentUser.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot snapshot) {
               if (!snapshot.exists()) {
                   Log.d("TESTING", "it doesnt exist!");
               }
               else {
                   for (DataSnapshot child : snapshot.getChildren()) {
                       Polo retrievedPolo = child.getValue(Polo.class);
                       String shortDate = retrievedPolo.getTimestamp().split(" ")[0];
                       addDatum(child.getKey(), "Marco from: "+ retrievedPolo.getSenderName(), "Sent on: " + shortDate + "\n" + retrievedPolo.getMessage());

                       expandableListView = (ExpandableListView) findViewById(R.id.notificationListView);
                       listAdapter = new CustomListAdapter(Notifications.this, sectionList);
                       expandableListView.setAdapter(listAdapter);

                       collapseAll();

                       expandableListView.setOnChildClickListener(myListItemClicked);
                       expandableListView.setOnGroupClickListener(myListGroupClicked);
                   }
               }
           }
           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });
   }

   //For now clicking on a child only prints out to screen, we can add functionality if needed
   private OnChildClickListener myListItemClicked = new OnChildClickListener() {
       @Override
       public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
           HeaderInfo headerInfo = sectionList.get(groupPosition);
           DetailInfo detailInfo = headerInfo.getChildList().get(childPosition);
           //Toast.makeText(getBaseContext(), "Clicked on detail " + headerInfo.getName() + "/" + detailInfo.getName(), Toast.LENGTH_LONG).show();
           showGoAheadDialog(getResources().getString(R.string.polo_prompt), detailInfo.getId());
           return false;
       }
   };

   //clicking on a group item expands that group and prints to screen
   private OnGroupClickListener myListGroupClicked = new OnGroupClickListener() {
       @Override
       public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
           HeaderInfo headerInfo = sectionList.get(groupPosition);
           //Toast.makeText(getBaseContext(), "Child on header " + headerInfo.getName(), Toast.LENGTH_LONG).show();
           return false;
       }
   };

   //adds one key, value pair to the expandable list
   private int addDatum(String id, String group, String child){
       int groupPosition = 0;

       HeaderInfo headerInfo = mySection.get(group);
       if (headerInfo == null){
           headerInfo = new HeaderInfo();
           headerInfo.setName(group);
           mySection.put(group, headerInfo);
           sectionList.add(headerInfo);
       }

       ArrayList<DetailInfo> childList = headerInfo.getChildList();
       int listSize = childList.size();
       listSize++;

       DetailInfo detailInfo = new DetailInfo();
       detailInfo.setName(child);
       detailInfo.setId(id);
       childList.add(detailInfo);
       headerInfo.setChildList(childList);

       groupPosition = sectionList.indexOf(headerInfo);
       return groupPosition;
   }

   //stub for handling button clicks in the expandable list view.  Since we have no buttons, this
    //is only here to satisfy implementing the abstract class
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            default:
                break;
        }
    }

    public void showGoAheadDialog(String prompt, String id){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage(prompt);
        final String senderId = id;
        alertDialogBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                databasePolos.child(LoginActivity.currentUser.getUserId()).child(senderId).child("responded").setValue(true);
                sectionList.clear();
                mySection.clear();
                addData();
                arg0.cancel();
            }
        });

        alertDialogBuilder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databasePolos.child(LoginActivity.currentUser.getUserId()).child(senderId).removeValue();
                sectionList.clear();
                mySection.clear();
                addData();
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
