package com.polo.marco.marcopoloapp.api.database.tasks.UserTasks;

import android.os.AsyncTask;

import com.polo.marco.marcopoloapp.api.database.Database;
import com.polo.marco.marcopoloapp.api.database.User;

/**
 * Created by Chase on 10/30/2017.
 */

public class DeleteUserTask extends AsyncTask<User, Void, Void> {
    @Override
    protected Void doInBackground(User... params) {
        Database.mapper.delete(params[0]);
        return null;
    }
}
