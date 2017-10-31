package com.polo.marco.marcopoloapp.api.database;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.polo.marco.marcopoloapp.api.database.tasks.InitializeDatabaseTask;

import java.util.List;

/**
 * Created by Chase on 10/23/2017.
 */

public class Database {
    public static final String DEFAULT_TABLE_NAME = "datastore";
    public static final String AWS_ACCESS_KEY_ID = "AKIAIGU3FC76IFSNIO6Q";
    public static final String AWS_SECRET_KEY = "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";

    public static AmazonDynamoDBClient dbClient;
    public static DynamoDBMapper mapper;

    /*
        * Static initializier: initializes at startup. Creates a new instance
        * of @AmazonDynamoDBClient and sets a local variable. Same for DynamoDBMapper.
        * Both are essential for database querying.
        *
     */
    static {
        new InitializeDatabaseTask().doInBackground("us-east-2");
    }

    /*
     * Returns global instance of AmazonDynamoDBClient.
      * */
    public static AmazonDynamoDBClient getDBClient() {
        return dbClient;
    }

    /*
    * Returns global instance of DynamoDBMapper.
    * */
    public static DynamoDBMapper getDynamoDBMapper() {
        return mapper;
    }

    /*
    * User object. We will be using this to store relevant data about the user.
    * Feel free to add new fields: these correlate to columns in DynamoDB.
    * */
    @DynamoDBTable(tableName = DEFAULT_TABLE_NAME)
    public static class User {
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

    /*
    * Pass a User object and this method will either add it or update the current
    * User row within the database.
    * */
    public static void updateUser(final User user) {
        mapper.save(user);
    }

    /*
    * Returns an instance of the current User within DynamoDB. Requries a unique identifier,
    * UserId, to grab that object from the database. Note: untested.
    * */
    public static User getUser(final String userId) {
        return mapper.load(User.class, userId);
    }

}
