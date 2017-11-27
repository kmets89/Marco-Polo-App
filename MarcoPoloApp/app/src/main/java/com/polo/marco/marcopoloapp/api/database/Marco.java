package com.polo.marco.marcopoloapp.api.database;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;

/**
 * Created by kmets on 11/23/2017.
 */

@DynamoDBTable(tableName = "marco")
public class Marco {
    private String userId;
    private String message;
    private String timestamp;
    private long expires;
    private double latitude;
    private double longitude;
    private boolean isPublic;
    private List<String> receiverList;

    @DynamoDBHashKey(attributeName = "userId")
    public String getUserId() {return userId;}
    public void setUserId(String id) {this.userId = id;}

    @DynamoDBAttribute(attributeName = "message")
    public String getMessage() {return message;}
    public void setMessage(String m) {this.message = m;}

    @DynamoDBAttribute(attributeName = "timestamp")
    public String getTimestamp() {return timestamp;}
    public void setTimestamp(String t) {this.timestamp = t;}

    @DynamoDBAttribute(attributeName = "expires")
    public long getExpires() {return expires;}
    public void setExpires(long e) {this.expires = e;}

    @DynamoDBAttribute(attributeName = "latitude")
    public double getLatitude() {return latitude;}
    public void setLatitude(double lat) {this.latitude = lat;}

    @DynamoDBAttribute(attributeName = "longitude")
    public double getLongitude() {return longitude;}
    public void setLongitude(double lon) {this.longitude = lon;}

    @DynamoDBAttribute(attributeName = "isPublic")
    public boolean getStatus() {return isPublic;}
    public void setStatus(boolean p) {this.isPublic = p;}

    @DynamoDBAttribute(attributeName = "receiverList")
    public List<String> getFriendsList() {
        return receiverList;
    }
    public void setFriendsList(List<String> recv) {
        this.receiverList = recv;
    }

    public Marco() {
    }

    //constructor for a public Marco
    public Marco(String id, String m, String t, long e, double lat, double lon, boolean p){
        this.userId = id;
        this.message = m;
        this.timestamp = t;
        this.expires = e;
        this.latitude = lat;
        this.longitude = lon;
        this.isPublic = p;
        this.receiverList = null;
    }

    //constructor for a private Marco
    public Marco(String id, String m, String t, long e, double lat, double lon, boolean p, List<String>recv){
        this.userId = id;
        this.message = m;
        this.timestamp = t;
        this.expires = e;
        this.latitude = lat;
        this.longitude = lon;
        this.isPublic = p;
        this.receiverList = recv;
    }
}
