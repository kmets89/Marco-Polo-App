package com.polo.marco.marcopoloapp.api.database.tasks;

import android.os.AsyncTask;

import com.polo.marco.marcopoloapp.api.database.Database;
import com.polo.marco.marcopoloapp.api.database.Marco;

/**
 * Created by kmets on 11/23/2017.
 */

public class DeleteMarcoTask extends AsyncTask<Marco, Void, Void> {
    @Override
    protected Void doInBackground(Marco... params) {
        Database.mapper.delete(params[0]);
        return null;
    }
}

