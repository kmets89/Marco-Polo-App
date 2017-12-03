package com.polo.marco.marcopoloapp.api.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.polo.marco.marcopoloapp.R;

import java.util.ArrayList;

//This class handles the behavior of groups and children inside an expandable list and how data
//is piped into the list to be displayed in text views.  Overriden methods are mostly getters
//relating to getting group and child size, position, count, etc.
public class CustomListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<HeaderInfo> groupList;

    public CustomListAdapter(Context context, ArrayList<HeaderInfo> groupList){
        this.context = context;
        this.groupList = groupList;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition){
        ArrayList<DetailInfo> childList = groupList.get(groupPosition).getChildList();
        return childList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition){
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent){
        if (!groupList.isEmpty()) {
            DetailInfo detailInfo = (DetailInfo) getChild(groupPosition, childPosition);
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.notification_children, null);
            }

            TextView childItem = (TextView) view.findViewById(R.id.childItem);
            childItem.setText(detailInfo.getName().trim());

            return view;
        }
        else{
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.notification_children, null);
            return view;
        }
    }

    @Override
    public int getChildrenCount(int groupPosition){
        ArrayList<DetailInfo> childList = groupList.get(groupPosition).getChildList();
        return childList.size();
    }

    @Override
    public Object getGroup(int groupPosition){
        return groupList.get(groupPosition);
    }

    @Override
    public int getGroupCount(){
        return groupList.size();
    }

    @Override
    public long getGroupId(int groupPosition){
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isLastChild, View view, ViewGroup parent){
        if (!groupList.isEmpty()) {
            HeaderInfo headerInfo = (HeaderInfo) getGroup(groupPosition);
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.notification_groups, null);
            }

            TextView heading = (TextView) view.findViewById(R.id.heading);
            heading.setText(headerInfo.getName().trim());

            return view;
        }
        else{
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.notification_groups, null);
            return view;
        }
    }

    @Override
    public boolean hasStableIds(){
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition){
        return true;
    }
}
