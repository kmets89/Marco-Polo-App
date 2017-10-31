package com.polo.marco.marcopoloapp.api.database.tasks;

import android.os.AsyncTask;

import com.polo.marco.marcopoloapp.api.database.Database;

/**
 * Created by Chase on 10/30/2017.
 */

public class LoadUserTask extends AsyncTask<String, Void, Database.User> {
    @Override
    public Database.User doInBackground(String... params) {
        return Database.mapper.load(Database.User.class, params[0]);
    }
}
