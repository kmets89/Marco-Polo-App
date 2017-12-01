package com.polo.marco.marcopoloapp.firebase.models;

/**
 * Created by kmets on 11/30/2017.
 */

public class Polo {
    private String receiverId;
    private String message;
    private String senderName;
    private String timestamp;
    private double latitude;
    private double longitude;
    private boolean responded;

    public Polo(){}

    public Polo (String rId, String msg, String senderName, String timestamp, double lat, double lng, boolean responded){
        this.receiverId = rId;
        this.message = msg;
        this.senderName = senderName;
        this.timestamp = timestamp;
        this.latitude = lat;
        this.longitude = lng;
        this.responded = responded;
    }

    public String getReceiverId() {return this.receiverId;}
    public void setReceiverId(String id) {this.receiverId = id;}

    public String getMessage() {return this.message;}
    public void setMessage(String message) {this.message = message;}

    public String getSenderName() {return this.senderName;}
    public void setSenderName(String name) {this.senderName = name;}

    public String getTimestamp() {return this.timestamp;}
    public void setTimestamp(String timestamp) {this.timestamp = timestamp;}

    public double getLatitude() {return this.latitude;}
    public void setLatitude(double lat) {this.latitude = lat;}

    public double getLongitude() {return this.longitude;}
    public void setLongitude(double lng) {this.longitude = lng;}

    public boolean getResponded() {return this.responded;}
    public void setResponded(boolean responded) {this.responded = responded;}

}
