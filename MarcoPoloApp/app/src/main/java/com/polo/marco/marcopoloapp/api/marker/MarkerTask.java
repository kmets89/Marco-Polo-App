package com.polo.marco.marcopoloapp.api.marker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.URL;

public class MarkerTask extends AsyncTask<String, Void, MarkerOptions> {

    @Override
    protected MarkerOptions doInBackground(String... args) {

        MarkerOptions markerOptions = new MarkerOptions();

        try {
            URL url = new URL(args[0]);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeStream(url.openConnection().getInputStream()), 100, 100, false)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return markerOptions;
    }
}