package com.polo.marco.marcopoloapp.api.database.tasks;

import android.os.AsyncTask;

import com.polo.marco.marcopoloapp.api.database.Database;

/**
 * Created by Chase on 10/30/2017.
 */

public class SaveUserTask extends AsyncTask<Database.User, Void, Void> {
    @Override
    protected Void doInBackground(Database.User... params) {
        Database.mapper.save(params[0]);
        return null;
    }
}
