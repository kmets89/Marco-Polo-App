package com.polo.marco.marcopoloapp.api.directions;


import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Route {
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endPoint;
    public String startAddress;
    public LatLng startPoint;

    public List<LatLng> points;
}
