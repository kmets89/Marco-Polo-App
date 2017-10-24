package com.polo.marco.marcopoloapp.api.database;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.polo.marco.marcopoloapp.api.database.types.User;

import java.util.List;

/**
 * Created by Chase on 10/23/2017.
 */

public class Database {
    public static final String DEFAULT_TABLE_NAME = "datastore";
    private static final String AWS_ACCESS_KEY_ID = "AKIAIGU3FC76IFSNIO6Q";
    private static final String AWS_SECRET_KEY = "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";

    private static AmazonDynamoDBClient dbClient;
    private static DynamoDBMapper mapper;

    /*
        * Static initializier: initializes at startup. Creates a new instance
        * of @AmazonDynamoDBClient and sets a local variable. Same for DynamoDBMapper.
        * Both are essential for database querying.
        *
     */
    static {
        dbClient = Region.getRegion("us-east-2").createClient(AmazonDynamoDBClient.class, new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new AWSCredentials() {
                    @Override
                    public String getAWSAccessKeyId() {
                        return AWS_ACCESS_KEY_ID;
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return AWS_SECRET_KEY;
                    }
                };
            }

            @Override
            public void refresh() {

            }
        }, new ClientConfiguration());

        mapper = new DynamoDBMapper(dbClient);
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
        mapper.save(user);
    }

    /*
    * Returns an instance of the current User within DynamoDB. Requries a unique identifier,
    * UserId, to grab that object from the database. Note: untested.
    * */
    public static User getUser(final String userId) {
        return mapper.load(User.class, userId, new DynamoDBMapperConfig(DynamoDBMapperConfig.ConsistentReads.CONSISTENT));
    }

}
