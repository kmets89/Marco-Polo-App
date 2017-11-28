package com.polo.marco.marcopoloapp.api.database;

import android.util.Log;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chase on 10/30/2017.
 */

    /*
    * User object. We will be using this to store relevant data about the user.
    * Feel free to add new fields: these correlate to columns in DynamoDB.
    * */
@DynamoDBTable(tableName = "datastore")
public class User {
    private String userId;
    private String name;
    private String email;
    private String loginApiType;
    private List<String> friendsList;
    private double latitude;
    private double longitude;
    private String imgUrl;
    public List<User> friendsUserList;

    public User() {

    }

    public User(String userId, String name, String email, String loginApiType, List<String> friendsList, double latitude, double longitude, String imgUrl) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.loginApiType = loginApiType;
        if(friendsList == null)
        {
            this.friendsList = new ArrayList<String>();
        }else{
            this.friendsList = friendsList;
        }
        this.latitude = latitude;
        this.longitude = longitude;
        this.imgUrl = imgUrl;
    }

    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @DynamoDBAttribute(attributeName = "email")
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return name;
    }
    public void setName(String name) {this.name = name;}

    @DynamoDBAttribute(attributeName = "imgUrl")
    public String getImgUrl() {
        return imgUrl;
    }
    public void setImgUrl(String url) { this.imgUrl = url; }

    @DynamoDBAttribute(attributeName = "friendsList")
    public List<String> getFriendsList() {
        return friendsList;
    }

    public void setFriendsList(List<String> friendsList) {
        this.friendsList = friendsList;
    }

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBIndexRangeKey(attributeName = "userId")
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean usingFacebook()
    {
        return loginApiType.equalsIgnoreCase("facebook");
    }

    @DynamoDBAttribute(attributeName = "loginApiType")
    public String getLoginApiType() {
        return loginApiType;
    }
    public void setLoginApiType(String loginApiType) {
        this.loginApiType = loginApiType;
    }

    public String toString() {
        return "[" + userId + "] " + name + ": LoginAPIType~" + loginApiType + ":friendsList~" + Arrays.toString(getFriendsList().toArray(new String[friendsList.size()]));
    }

    public String queryIdByEmail(String email){
        Map<String, String> attributeNames = new HashMap<String, String>();
        attributeNames.put("#email", "email");

        Map<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
        attributeValues.put(":is", new AttributeValue().withS(email));

        DynamoDBScanExpression dynamoDBScanExpression = new DynamoDBScanExpression()
                .withFilterExpression("#email = :is")
                .withExpressionAttributeNames(attributeNames)
                .withExpressionAttributeValues(attributeValues);

        List<User> users = Database.getDynamoDBMapper().scan(User.class, dynamoDBScanExpression);
        for (int i = 0; i < users.size(); i++)
            Log.d("QUERY RESULT", users.get(i).getUserId());
        return "stuff";
    }
}
