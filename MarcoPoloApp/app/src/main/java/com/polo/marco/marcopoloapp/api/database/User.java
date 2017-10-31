package com.polo.marco.marcopoloapp.api.database;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;

/**
 * Created by Chase on 10/30/2017.
 */

    /*
    * User object. We will be using this to store relevant data about the user.
    * Feel free to add new fields: these correlate to columns in DynamoDB.
    * */
@DynamoDBTable(tableName = Database.DEFAULT_TABLE_NAME)
public class User {
    private String userId;
    private String name;
    private String loginApiType;
    private List<String> friendsList;

    public User() {

    }

    public User(String userId, String name, String loginApiType, List<String> friendsList) {
        this.userId = userId;
        this.name = name;
        this.loginApiType = loginApiType;
        this.friendsList = friendsList;
    }

    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBAttribute(attributeName = "friendList")
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

    @DynamoDBAttribute(attributeName = "loginApiType")
    public String getLoginApiType() {
        return loginApiType;
    }

    public void setLoginApiType(String loginApiType) {
        this.loginApiType = loginApiType;
    }
}
