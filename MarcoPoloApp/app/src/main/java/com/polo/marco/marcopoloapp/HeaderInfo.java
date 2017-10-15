package com.polo.marco.marcopoloapp;

import java.util.ArrayList;

//This class handles the behavior of groups in an expandable list, used in Notifications.java
public class HeaderInfo {

    private String name;
    private ArrayList<DetailInfo> childList = new ArrayList<DetailInfo>();

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public ArrayList<DetailInfo> getChildList(){
        return childList;
    }

    public void setChildList(ArrayList<DetailInfo> childList){
        this.childList = childList;
    }
}
