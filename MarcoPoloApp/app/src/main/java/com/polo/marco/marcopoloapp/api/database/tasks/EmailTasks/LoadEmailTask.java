package com.polo.marco.marcopoloapp.api.database.tasks.EmailTasks;

import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig;
import com.polo.marco.marcopoloapp.api.database.Database;
import com.polo.marco.marcopoloapp.api.database.Email;

/**
 * Created by kmets on 11/26/2017.
 */

public class LoadEmailTask extends AsyncTask<String, Void, Email> {
    @Override
    protected Email doInBackground(String... params) {
        return Database.mapper.load(Email.class, params[0], DynamoDBMapperConfig.DEFAULT);
    }
}

