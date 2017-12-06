package com.polo.marco.marcopoloapp.api.marker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;

public class MarkerTask extends AsyncTask<MarkerInfo, Void, MarkerOptions> {

    @Override
    protected MarkerOptions doInBackground(MarkerInfo... args) {

        MarkerOptions markerOptions = new MarkerOptions();

        try {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(Picasso.with(args[0].getContext()).load(args[0].getImgUrl()).get(), 100, 100, false)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return markerOptions;
    }
}