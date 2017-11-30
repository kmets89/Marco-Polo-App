package com.polo.marco.marcopoloapp.api.database;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;

/**
 * Created by kmets on 11/26/2017.
 */

@DynamoDBTable(tableName = "email")
public class Email {
    private String email;
    private String associatedId;

    @DynamoDBHashKey(attributeName = "email")
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    @DynamoDBAttribute(attributeName = "associatedId")
    public String getAssociatedId() {return associatedId;}
    public void setFuckyou(String fuckyou) {this.associatedId = associatedId;}

    public Email() {

    }

    //constructor for a public Marco
    public Email(String email, String associatedId){
        this.email = email;
        this.associatedId = associatedId;
    }
}
