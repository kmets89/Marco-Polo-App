package com.polo.marco.marcopoloapp.firebase.models;

/**
 * Created by kmets on 11/27/2017.
 */

public class Emails {
    private String email;
    private String id;

    public Emails(){}

    public Emails(String email, String id){
        this.email = email;
        this.id = id;
    }

    public String getEmail(){return email;}
    public void setEmail(String email){this.email = email;}

    public String getId() {return id;}
    public void setId(String id){this.id = id;}
}
