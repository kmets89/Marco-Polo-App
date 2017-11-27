package com.polo.marco.marcopoloapp.api.database.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.polo.marco.marcopoloapp.api.database.Database;
import com.polo.marco.marcopoloapp.api.database.User;

import java.util.Map;

/**
 * Created by kmets on 11/26/2017.
 */

public class Scans extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
        AmazonDynamoDB client = Database.getDBClient();

        ScanRequest scanRequest = new ScanRequest()
                .withTableName("User");

        ScanResult result = client.scan(scanRequest);
        for (Map<String, AttributeValue> item : result.getItems())
            Log.d("QUERY RESULT", item.toString());

        return null;
    }
}
