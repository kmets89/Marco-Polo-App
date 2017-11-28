package com.polo.marco.marcopoloapp.api.database;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.polo.marco.marcopoloapp.api.database.tasks.EmailTasks.DeleteEmailTask;
import com.polo.marco.marcopoloapp.api.database.tasks.EmailTasks.LoadEmailTask;
import com.polo.marco.marcopoloapp.api.database.tasks.EmailTasks.SaveEmailTask;
import com.polo.marco.marcopoloapp.api.database.tasks.MarcoTasks.DeleteMarcoTask;
import com.polo.marco.marcopoloapp.api.database.tasks.UserTasks.DeleteUserTask;
import com.polo.marco.marcopoloapp.api.database.tasks.InitializeDatabaseTask;
import com.polo.marco.marcopoloapp.api.database.tasks.UserTasks.LoadBatchUsersTask;
import com.polo.marco.marcopoloapp.api.database.tasks.MarcoTasks.LoadMarcoTask;
import com.polo.marco.marcopoloapp.api.database.tasks.UserTasks.LoadUserTask;
import com.polo.marco.marcopoloapp.api.database.tasks.MarcoTasks.SaveMarcoTask;
import com.polo.marco.marcopoloapp.api.database.tasks.UserTasks.SaveUserTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Chase on 10/23/2017.
 */

public class Database {
    //public static final String DEFAULT_TABLE_NAME = "datastore";
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
    * Pass a User object and this method will either delete the current
    * User row within the database.
    * */
    public static void deleteUser(final User user) {
        new DeleteUserTask().execute(user);
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

    // Returns an array of users. Params: an array of user ID's to get.
    public static List<User> getListOfFriends(final List<String> users){
        try {
            if(users == null){
                return new ArrayList<User>();
            }
            return new LoadBatchUsersTask().execute(users.toArray(new String[users.size()])).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateMarco(final Marco marco) { new SaveMarcoTask().execute(marco);}

    public static void deleteMarco(final Marco marco) {new DeleteMarcoTask().execute(marco);}

    public static Marco getMarco(final String userId) {
        try {
            return new LoadMarcoTask().execute(userId).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateEmail(final Email email) { new SaveEmailTask().execute(email);}

    public static void deleteEmail(final Email email) {new DeleteEmailTask().execute(email);}

    public static Email getUserEmail(final String email) {
        try {
            return new LoadEmailTask().execute(email).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}