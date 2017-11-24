package com.polo.marco.marcopoloapp.api.database.tasks;

import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig;
import com.polo.marco.marcopoloapp.api.database.Database;
import com.polo.marco.marcopoloapp.api.database.Marco;

/**
 * Created by kmets on 11/23/2017.
 */

public class LoadMarcoTask extends AsyncTask<String, Void, Marco> {
    @Override
    protected Marco doInBackground(String... params) {
        return Database.mapper.load(Marco.class, params[0], DynamoDBMapperConfig.DEFAULT);
    }
}
