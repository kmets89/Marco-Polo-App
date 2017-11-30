package com.polo.marco.marcopoloapp.api.database.tasks.EmailTasks;

import android.os.AsyncTask;

import com.polo.marco.marcopoloapp.api.database.Database;
import com.polo.marco.marcopoloapp.api.database.Email;

/**
 * Created by kmets on 11/26/2017.
 */

public class SaveEmailTask extends AsyncTask<Email, Void, Void> {
    @Override
    protected Void doInBackground(Email... params) {
        Database.mapper.save(params[0]);
        return null;
    }
}
