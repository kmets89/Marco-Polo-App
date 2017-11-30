package com.polo.marco.marcopoloapp.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.activities.MapsActivity;

import java.util.Map;

/**
 * Created by Krazy on 11/26/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> payload = remoteMessage.getData();
            showNotification(payload);
        }
    }

    private void showNotification(Map<String, String> payload) {

        if (MapsActivity.mIsInForegroundMode) {
            final String lat = payload.get("latitude");
            final String lng = payload.get("longitude");
            final String msg = payload.get("message");
            final String sender = payload.get("sender");

            Handler mainHandler = new Handler(this.getBaseContext().getMainLooper());

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    MapsActivity.addMarcoMarker(Double.parseDouble(lat),
                            Double.parseDouble(lng), msg, sender);
                }
            };

            mainHandler.post(r);
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setSmallIcon(R.drawable.ic_launcher);
            builder.setContentTitle("You have received a Marco!");
            builder.setContentText(payload.get("sender"))
                    .setAutoCancel(true);

            String lat = payload.get("latitude");
            String lng = payload.get("longitude");

            Intent resultIntent = new Intent(this, MapsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", Double.parseDouble(lat));
            bundle.putDouble("longitude", Double.parseDouble(lng));
            bundle.putString("message", payload.get("message"));
            bundle.putString("sender", payload.get("sender"));
            resultIntent.putExtras(bundle);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, builder.build());
        }

    }
}