package com.polo.marco.marcopoloapp.api.notifications;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.Toast;

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
public class Notifications extends AppCompatActivity implements OnClickListener {

    Dialogs dialogs = new Dialogs();
    private LinkedHashMap<String, HeaderInfo> mySection = new LinkedHashMap<>();
    private ArrayList<HeaderInfo> sectionList = new ArrayList<>();
    private CustomListAdapter listAdapter;
    private ExpandableListView expandableListView;
    private ArrayList<PoloMore> poloList = new ArrayList<PoloMore>();
    private DatabaseReference databasePolos = FirebaseDatabase.getInstance().getReference("polos");
    //For now clicking on a child only prints out to screen, we can add functionality if needed
    private OnChildClickListener myListItemClicked = new OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            HeaderInfo headerInfo = sectionList.get(groupPosition);
            DetailInfo detailInfo = headerInfo.getChildList().get(childPosition);
            Toast.makeText(getBaseContext(), "Clicked on detail " + headerInfo.getName() + "/" + detailInfo.getName(), Toast.LENGTH_LONG).show();

            if ((headerInfo.getName()).contains("Marco sent from")) {
                String segments[] = headerInfo.getName().split(": ");

                dialogs.poloNotif(v, segments[segments.length - 1], detailInfo.getName());
            } else {
                dialogs.poloNotif(v, headerInfo.getName(), detailInfo.getName());
            }
            return false;
        }
    };
    //clicking on a group item expands that group and prints to screen
    private OnGroupClickListener myListGroupClicked = new OnGroupClickListener() {
        @Override
        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
            HeaderInfo headerInfo = sectionList.get(groupPosition);
            Toast.makeText(getBaseContext(), "Child on header " + headerInfo.getName(), Toast.LENGTH_LONG).show();
            return false;
        }
    };

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

        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.3));

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

    private void expandAll() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            expandableListView.expandGroup(i);
        }
    }

    private void collapseAll() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            expandableListView.collapseGroup(i);
        }
    }

    //dummy data for testing.  This is where our database query stuff will go
    private void addData() {
        databasePolos.child(LoginActivity.currentUser.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d("TESTING", "it doesnt exist!");
                } else {
                    for (DataSnapshot child : snapshot.getChildren()) {

                        Polo retrievedPolo = child.getValue(Polo.class);
                        Toast.makeText(getApplicationContext(), child.getKey(), Toast.LENGTH_LONG).show();

                        addDatum("Marco sent from: " + retrievedPolo.getSenderName(), retrievedPolo.getMessage());
                        poloList.add(new PoloMore(retrievedPolo, child.getKey()));
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

        addDatum("Current Meetup", "Onion");

    }

    private void getCurrentPolo() {
        databasePolos.child(LoginActivity.currentUser.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d("TESTING", "it doesnt exist!");
                } else {
                    Polo retrievedPolo = snapshot.getValue(Polo.class);
                    if (retrievedPolo.getResponded()) {

                        addDatum("Marco sent from: " + retrievedPolo.getSenderName(), retrievedPolo.getMessage());

                        expandableListView = (ExpandableListView) findViewById(R.id.notificationListView);
                        listAdapter = new CustomListAdapter(Notifications.this, sectionList);
                        expandableListView.setAdapter(listAdapter);

                        collapseAll();

                        expandableListView.setOnChildClickListener(myListItemClicked);
                        expandableListView.setOnGroupClickListener(myListGroupClicked);
                    }

                    addDatum("Fruits", "Onion");

                    addDatum("Fruits", "Apple");
                    addDatum("Fruits", "Orange");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //adds one key, value pair to the expandable list
    private int addDatum(String group, String child) {
        int groupPosition = 0;

        HeaderInfo headerInfo = mySection.get(group);
        if (headerInfo == null) {
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
        childList.add(detailInfo);
        headerInfo.setChildList(childList);

        groupPosition = sectionList.indexOf(headerInfo);
        return groupPosition;
    }


    //stub for handling button clicks in the expandable list view.  Since we have no buttons, this
    //is only here to satisfy implementing the abstract class
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }

    public class Dialogs {

        public void poloNotif(final View v, final String marcoer, String message) {
            AlertDialog.Builder altdial = new AlertDialog.Builder(Notifications.this);


            altdial.setMessage(message).setCancelable(false);
            altdial.setPositiveButton("Polo", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Toast.makeText(getApplicationContext(), "hello", Toast.LENGTH_SHORT).show();
                    poloBack(v, marcoer);

                }
            });


            altdial.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplicationContext(), "Maybe next time", Toast.LENGTH_SHORT).show();

                }
            });


            AlertDialog alert = altdial.create();
            alert.setTitle(marcoer + " has 'Marco'ed some friends and you!");
            alert.show();
        }

        public void poloBack(View v, final String marcoer) {
            String senderID = "";
            for (PoloMore poloMore: poloList) {
                if (poloMore.polo.getSenderName().equals(marcoer)) {
                    senderID = poloMore.senderId;
                    Toast.makeText(getApplicationContext(), senderID, Toast.LENGTH_SHORT).show();
                    databasePolos.child(LoginActivity.currentUser.getUserId()).child(senderID).child("responded").setValue(true);


                }
            }
            //Toast.makeText(getApplicationContext(), senderID, Toast.LENGTH_SHORT).show();

            //databasePolos.child(LoginActivity.currentUser.getUserId()).child(senderID).child("responded").setValue(true);

        }

/*
    public void meetupNotif(View v, String titleOfMeetup){
        AlertDialog.Builder altdial = new AlertDialog.Builder(Notifications.this);

        Integer sizeOfFriends = Notifications.hm.get("Friends").size();
        ArrayList<String> Friends = Notifications.hm.get("Friends");
        String MakeList = "<ul compact>Others invited:";
        for (int x = 0; x < sizeOfFriends; x++){
            MakeList += "<p>"+ Friends.get(x) +"</p>";
        }
        MakeList +="</ul>";
        Toast.makeText(getApplicationContext(), MakeList, Toast.LENGTH_SHORT).show();
        Spanned html = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            html = fromHtml(MakeList, Html.FROM_HTML_MODE_LEGACY);
        }
        altdial.setMessage(html).setCancelable(false);
        altdial.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "hello", Toast.LENGTH_SHORT).show();
                //MainActivity.textV.setText("MarcoState");
                Notifications.meetupState = true;


            }
        });


        altdial.setNegativeButton("Cancel Polo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "Maybe next time", Toast.LENGTH_SHORT).show();
                //Need polotable workhere

            }
        });


        AlertDialog alert = altdial.create();
        alert.setTitle("You are participating in" + titleOfMeetup );
        alert.show();
    }*/


    }
    public class PoloMore{
            Polo polo;
            String senderId;

        public PoloMore(){}
        public PoloMore(Polo polo,String senderId){
            this.polo = polo;
            this.senderId = senderId;
        }
    }
}
