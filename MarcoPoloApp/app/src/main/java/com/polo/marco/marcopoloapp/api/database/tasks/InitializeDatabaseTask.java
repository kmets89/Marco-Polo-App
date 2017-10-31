package com.polo.marco.marcopoloapp.api.database.tasks;

import android.os.AsyncTask;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.polo.marco.marcopoloapp.api.database.Database;

/**
 * Created by Chase on 10/30/2017.
 */

public class InitializeDatabaseTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
        Database.dbClient = Region.getRegion(params[0]).createClient(AmazonDynamoDBClient.class, new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new AWSCredentials() {
                    @Override
                    public String getAWSAccessKeyId() {
                        return Database.AWS_ACCESS_KEY_ID;
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return Database.AWS_SECRET_KEY;
                    }
                };
            }

            @Override
            public void refresh() {

            }
        }, new ClientConfiguration());

        Database.mapper = new DynamoDBMapper(Database.dbClient);
        return null;
    }
}
