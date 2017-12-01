package com.polo.marco.marcopoloapp.api.notifications;

//This class handles the behavior of children in an expandable list, used in Notifications.java
public class DetailInfo {

    private String name = "";
    private String id = "";

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }
}
