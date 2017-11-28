package com.polo.marco.marcopoloapp.api.database.tasks.UserTasks;

import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig;
import com.polo.marco.marcopoloapp.api.database.Database;
import com.polo.marco.marcopoloapp.api.database.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.polo.marco.marcopoloapp.api.database.Database.mapper;

/**
 * Created by Krazy on 11/4/2017.
 */

public class LoadBatchUsersTask extends AsyncTask<String, Void, List<User>> {
    @Override
    protected List<User> doInBackground(String[] users) {
        ArrayList<Object> usersToGet = new ArrayList<Object>();
        for(int i = 0; i < users.length; i++){
            User user = new User();
            user.setUserId(users[i]);
            usersToGet.add(user);
        }

        Map<String, List<Object>> batchResults = Database.mapper.batchLoad(usersToGet, DynamoDBMapperConfig.DEFAULT);
        ArrayList<User> returnedUsers = new ArrayList<>();
        Object[] returned = batchResults.values().toArray();
        if (returned.length > 0){
            LinkedList list = (LinkedList) returned[0];
            for (int i = 0; i < list.size(); i++) {
                returnedUsers.add((User) list.get(i));
            }
        }

        return returnedUsers;
    }
}
