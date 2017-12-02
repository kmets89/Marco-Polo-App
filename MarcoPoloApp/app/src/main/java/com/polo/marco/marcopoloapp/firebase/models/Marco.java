package com.polo.marco.marcopoloapp.firebase.models;

import java.util.List;

/**
 * Created by kmets on 11/23/2017.
 */

public class Marco {
    private String userId;
    private String name;
    private String message;
    private String timestamp;
    private double latitude;
    private double longitude;
    private boolean isPublic;
    private List<String> receiverList;

    public String getUserId() {return userId;}
    public void setUserId(String id) {this.userId = id;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getMessage() {return message;}
    public void setMessage(String m) {this.message = m;}

    public String getTimestamp() {return timestamp;}
    public void setTimestamp(String t) {this.timestamp = t;}

    public double getLatitude() {return latitude;}
    public void setLatitude(double lat) {this.latitude = lat;}

    public double getLongitude() {return longitude;}
    public void setLongitude(double lon) {this.longitude = lon;}

    public boolean getStatus() {return isPublic;}
    public void setStatus(boolean p) {this.isPublic = p;}

    public List<String> getReceiverList() {
        return receiverList;
    }
    public void setReceiverList(List<String> recv) {
        this.receiverList = recv;
    }

    public Marco() {}

    //constructor for a public Marco
    public Marco(String id, String n, String m, String t, double lat, double lon, boolean p){
        this.userId = id;
        this.name = n;
        this.message = m;
        this.timestamp = t;
        this.latitude = lat;
        this.longitude = lon;
        this.isPublic = p;
        this.receiverList = null;
    }

    //constructor for a private Marco
    public Marco(String id, String n, String m, String t, double lat, double lon, boolean p, List<String>recv){
        this.userId = id;
        this.name = n;
        this.message = m;
        this.timestamp = t;
        this.latitude = lat;
        this.longitude = lon;
        this.isPublic = p;
        this.receiverList = recv;
    }
}

