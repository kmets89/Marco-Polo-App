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
import com.polo.marco.marcopoloapp.api.database.tasks.LoadBatchUsersTask;
import com.polo.marco.marcopoloapp.api.database.tasks.LoadUserTask;
import com.polo.marco.marcopoloapp.api.database.tasks.SaveUserTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

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
        new InitializeDatabaseTask().execute("us-east-2");
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
    * Pass a User object and this method will either add it or update the current
    * User row within the database.
    * */
    public static void updateUser(final User user) {
        new SaveUserTask().execute(user);
    }

    /*
    * Returns an instance of the current User within DynamoDB. Requries a unique identifier,
    * UserId, to grab that object from the database. Note: untested.
    * */
    public static User getUser(final String userId) {
        try {
            return new LoadUserTask().execute(userId).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User[] getListOfFriends(final String[] users){
        try {
            return new LoadBatchUsersTask().execute(users).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

}
