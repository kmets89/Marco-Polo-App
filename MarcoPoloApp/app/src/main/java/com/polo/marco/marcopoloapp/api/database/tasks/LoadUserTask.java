package com.polo.marco.marcopoloapp.api.database.tasks;

import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig;
import com.polo.marco.marcopoloapp.api.database.Database;
import com.polo.marco.marcopoloapp.api.database.User;

/**
 * Created by Chase on 10/30/2017.
 */

public class LoadUserTask extends AsyncTask<String, Void, User> {
    @Override
    protected User doInBackground(String... params) {
        return Database.mapper.load(User.class, params[0], DynamoDBMapperConfig.DEFAULT);
    }
}
