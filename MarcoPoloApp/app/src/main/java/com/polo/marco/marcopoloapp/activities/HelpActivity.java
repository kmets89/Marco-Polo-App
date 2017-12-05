package com.polo.marco.marcopoloapp.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.api.notifications.CustomListAdapter;
import com.polo.marco.marcopoloapp.api.notifications.DetailInfo;
import com.polo.marco.marcopoloapp.api.notifications.HeaderInfo;
import com.polo.marco.marcopoloapp.api.notifications.Notifications;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Andrew on 12/1/2017.
 */

public class HelpActivity extends AppCompatActivity {
    private LinkedHashMap<String, HeaderInfo> mySection = new LinkedHashMap<>();
    private ArrayList<HeaderInfo> sectionList = new ArrayList<>();
    private CustomListAdapter listAdapter;
    private ExpandableListView expandableListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.help_nav_account);
        floatingActionButton.setImageResource(R.drawable.ic_person_white_24dp);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.help_nav_friends);
        floatingActionButton.setImageResource(R.drawable.ic_people_white_24dp);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.help_nav_notifications);
        floatingActionButton.setImageResource(R.drawable.ic_notifications_white_24dp);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.help_nav_help);
        floatingActionButton.setImageResource(R.drawable.ic_help_outline_white_24dp);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.help_nav_privacy_policy);
        floatingActionButton.setImageResource(R.drawable.ic_lock_white_24dp);


        addDatum("About", getString(R.string.help_about));
        addDatum("Marco", getString(R.string.help_marco));
        addDatum("Public", getString(R.string.help_public));
        addDatum("Private", getString(R.string.help_private));
        addDatum("Polo", getString(R.string.help_polo));
        addDatum("Friends", getString(R.string.help_friends));
        addDatum("Sync Contacts", getString(R.string.sync_function));

        expandableListView = (ExpandableListView) findViewById(R.id.help_view);
        listAdapter = new CustomListAdapter(HelpActivity.this, sectionList);
        expandableListView.setAdapter(listAdapter);

        collapseAll();

        expandableListView.setOnChildClickListener(myListItemClicked);
        expandableListView.setOnGroupClickListener(myListGroupClicked);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    private ExpandableListView.OnChildClickListener myListItemClicked = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            HeaderInfo headerInfo = sectionList.get(groupPosition);
            DetailInfo detailInfo = headerInfo.getChildList().get(childPosition);
          //  Toast.makeText(getBaseContext(), "Clicked on detail " + headerInfo.getName() + "/" + detailInfo.getName(), Toast.LENGTH_LONG).show();
            return false;
        }
    };

    //clicking on a group item expands that group and prints to screen
    private ExpandableListView.OnGroupClickListener myListGroupClicked = new ExpandableListView.OnGroupClickListener() {
        @Override
        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
            HeaderInfo headerInfo = sectionList.get(groupPosition);
            //Toast.makeText(getBaseContext(), "Child on header " + headerInfo.getName(), Toast.LENGTH_LONG).show();
            return false;
        }
    };

    //adds one key, value pair to the expandable list
    private int addDatum(String group, String child){
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
        childList.add(detailInfo);
        headerInfo.setChildList(childList);

        groupPosition = sectionList.indexOf(headerInfo);
        return groupPosition;
    }

    //stub for handling button clicks in the expandable list view.  Since we have no buttons, this
    //is only here to satisfy implementing the abstract class

    public void onClick(View v) {
        switch (v.getId()){
            default:
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }


}
