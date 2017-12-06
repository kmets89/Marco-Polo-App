package com.polo.marco.marcopoloapp.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.Polyline;
import com.facebook.login.LoginManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polo.marco.marcopoloapp.api.marker.MarkerInfo;
import com.polo.marco.marcopoloapp.api.marker.MarkerTask;
import com.polo.marco.marcopoloapp.api.notifications.Notifications;
import com.polo.marco.marcopoloapp.R;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.polo.marco.marcopoloapp.api.directions.Route;
import com.polo.marco.marcopoloapp.api.directions.RouteFinder;
import com.polo.marco.marcopoloapp.api.directions.RouteFinderListener;
import com.polo.marco.marcopoloapp.firebase.models.Marco;
import com.polo.marco.marcopoloapp.firebase.models.Polo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapLongClickListener,
        RouteFinderListener {

    //Google maps stuff
    private static GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation = null;
    private LatLng destinationPoint;
    private Marker currentLocationMarker = null;
    private Marker destinationMarker = null;
    final private int PERMISSIONS_REQUEST_CODE = 124;
    private static boolean friendsRead = false;
    private Polyline polyline = null;

    private DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference databaseMarcos = FirebaseDatabase.getInstance().getReference("marcos");
    private DatabaseReference databasePolos = FirebaseDatabase.getInstance().getReference("polos");

    //
    public static boolean mIsInForegroundMode = false;
    private static boolean menuOpened = false;
    public String tmpImgUrl = "";

    // Active Polo Session Variables
    public static final double END_DISTANCE = 0.025;
    public boolean hasActivePolo = false;
    public boolean preventReloop = false;
    public String activePoloerUserId = "000000000000000000000000";
    public static ArrayList<Marker> mapMarkerArray = new ArrayList<>();

    //test
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mIsInForegroundMode = true;

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.expand_button);
        floatingActionButton.setImageResource(R.drawable.ic_menu_white_24dp);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        databaseMarcos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        final Marco marco = child.getValue(Marco.class);
                        if (marco.isPublic()) {
                            redrawPins();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databasePolos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (final DataSnapshot child : dataSnapshot.getChildren()) {
                        for (final DataSnapshot childs : child.getChildren()) {
                            final Polo polo = childs.getValue(Polo.class);
                            Log.d("MapsActivity", "Poloer key: " + child.getKey() + ", my key: " + LoginActivity.currentUser.getUserId());
                            if (child.getKey().equalsIgnoreCase(LoginActivity.currentUser.getUserId())) {
                                if ((polo.getMessage() != null && polo.getMessage().equalsIgnoreCase("delete")) || preventReloop) {
                                    preventReloop = false;
                                    return;
                                }

                                if (!hasActivePolo && distance(LoginActivity.currentUser.getLatitude(), LoginActivity.currentUser.getLongitude(), polo.getLatitude(), polo.getLongitude()) <= END_DISTANCE) {
                                    Toast.makeText(getBaseContext(), "You are already in the same location!", Toast.LENGTH_LONG).show();
                                    removeMarcoMarker(getMarker(childs.getKey()));
                                    removeMarcoMarker(getMarker(activePoloerUserId));

                                    databasePolos.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snap) {
                                            if (snap.exists()) {
                                                for (DataSnapshot c : snap.getChildren()) {
                                                    if (c.exists()) {
                                                        if (c.getKey().equalsIgnoreCase(LoginActivity.currentUser.getUserId())) {
                                                            for (DataSnapshot d : c.getChildren()) {
                                                                if (d.exists()) {
                                                                    databasePolos.child(LoginActivity.currentUser.getUserId()).child(d.getKey()).child("message").setValue("DELETE");
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                    preventReloop = true;
                                    return;
                                }

                                Marker m = getMarker(childs.getKey());
                                if (!hasActivePolo && m == null) {
                                    activePoloerUserId = childs.getKey();

                                    databaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot != null) {
                                                for (DataSnapshot c : dataSnapshot.getChildren()) {
                                                    if (c.getKey().equalsIgnoreCase(childs.getKey())) {
                                                        addMarcoMarker(polo.getLatitude(), polo.getLongitude(), polo.getMessage(), polo.getSenderName(), childs.getKey(), c.child("imgUrl").getValue().toString(), false);
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                    databasePolos.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snap) {
                                            if (snap.exists()) {
                                                if (snap.child(childs.getKey()).exists()) {
                                                    Log.d("MapsActivity", "Now has active polo");
                                                    hasActivePolo = true;
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                } else if (m != null) {
                                    m.setPosition(new LatLng(polo.getLatitude(), polo.getLongitude()));
                                    if (polo.getResponded() == true) {
                                        hasActivePolo = true;
                                    }
                                }

                                if (distance(LoginActivity.currentUser.getLatitude(), LoginActivity.currentUser.getLongitude(), polo.getLatitude(), polo.getLongitude()) <= END_DISTANCE) {
                                    Toast.makeText(getBaseContext(), "You have found eachother, congrats!", Toast.LENGTH_LONG).show();
                                    hasActivePolo = false;

                                    if (m != null)
                                        removeMarcoMarker(m);


                                    databasePolos.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {
                                            if (snapshot.hasChild(LoginActivity.currentUser.getUserId())) {
                                                databasePolos.child(LoginActivity.currentUser.getUserId()).child(activePoloerUserId).child("message").setValue("DELETE");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                    activePoloerUserId = "000000000000000000000000";
                                }
                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage("Do you want to send a Marco from this location?");

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Intent intent = new Intent(MapsActivity.this, MarcoActivity.class);
                intent.putExtra("callingActivity", "MapsActivity");
                intent.putExtra("latitude", latLng.latitude);
                intent.putExtra("longitude", latLng.longitude);
                startActivity(intent);
                arg0.cancel();
            }
        });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing here
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void getRoute(List<Route> routes) {

        if(polyline !=  null){
            polyline.remove();
        }

        for (Route route : routes) {

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(7);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polyline = mMap.addPolyline(polylineOptions);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        mIsInForegroundMode = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsInForegroundMode = true;

    }

    public void onClickExpandButton(View view) {
        menuOpened = !menuOpened;
        int visibility;

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.expand_button);

        if (menuOpened) {
            visibility = View.VISIBLE;
            floatingActionButton.setImageResource(R.drawable.ic_clear_white_24dp);
        } else {
            visibility = View.INVISIBLE;
            floatingActionButton.setImageResource(R.drawable.ic_menu_white_24dp);
        }

        floatingActionButton = (FloatingActionButton) findViewById(R.id.nav_account);
        floatingActionButton.setVisibility(visibility);
        floatingActionButton.setImageResource(R.drawable.ic_person_white_24dp);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.nav_friends);
        floatingActionButton.setVisibility(visibility);
        floatingActionButton.setImageResource(R.drawable.ic_people_white_24dp);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.nav_notifications);
        floatingActionButton.setVisibility(visibility);
        floatingActionButton.setImageResource(R.drawable.ic_notifications_white_24dp);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.nav_help);
        floatingActionButton.setVisibility(visibility);
        floatingActionButton.setImageResource(R.drawable.ic_help_outline_white_24dp);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.nav_privacy_policy);
        floatingActionButton.setVisibility(visibility);
        floatingActionButton.setImageResource(R.drawable.ic_lock_white_24dp);
    }

    public void onClickNavAccount(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onClickNavNotifications(View view) {
        Intent intent = new Intent(this, Notifications.class);
        startActivity(intent);
    }

    public void onClickNavFriends(View view) {
        Intent intent = new Intent(this, FriendsListActivity.class);
        startActivity(intent);
    }

    public void onClickNavHelp(View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public void onClickNavPrivacyPolicy(View view) {
        Intent intent = new Intent(this, PrivacyPolicyActivity.class);
        startActivity(intent);
    }

    //Function that's called when the marco button is clicked
    public void onClickBtnMarco(View view) {
        if (hasActivePolo) {
            Toast.makeText(this, getResources().getString(R.string.has_active_polo), Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, MarcoActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission is granted
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (client == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        //SettingsActivity settingsActivity = new SettingsActivity();
                        //settingsActivity.showSyncDialog();
                    }
                }
                //permission is denied
                else {
                    Toast.makeText(this, "Permission was Denied!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));

        mMap.setPadding(0, 300, 0, 0);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }


        mMap.setOnMapLongClickListener(this);

    }

    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        client.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
        if (lastLocation != null) {
            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }

        locationRequest = new LocationRequest();

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
        Intent intent = this.getIntent();
        Bundle extras = intent.getExtras();
        if (!extras.isEmpty()) {
            boolean hasMarkerLocations = extras.containsKey("latitude");
            if (hasMarkerLocations) {
                LatLng extraLatlng = new LatLng(extras.getDouble("latitude"), extras.getDouble("longitude"));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(extraLatlng);
                mMap.addMarker(markerOptions);
            }
        }
    }

    //Getting current location
    private void getCurrentLocation() {
        //Creating a location object
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
        }
    }

    public void checkLocationPermission() {
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("android.permission.ACCESS_FINE_LOCATION");
        if (!addPermission(permissionsList, Manifest.permission.READ_CONTACTS))
            permissionsNeeded.add("android.permission.READ_CONTACTS");

        if (permissionsNeeded.size() > 0)
            ActivityCompat.requestPermissions(this,
                    permissionsList.toArray(new String[permissionsList.size()]),
                    PERMISSIONS_REQUEST_CODE);
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                return false;
        }
        return true;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;

        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts decimal degrees to radians						 :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts radians to decimal degrees						 :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        currentLocationMarker = mMap.addMarker(markerOptions);

/*        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));*/

        /*if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }*/

        if (LoginActivity.currentUser != null) {
            //update User in DB
            LoginActivity.currentUser.setLatitude(location.getLatitude());
            LoginActivity.currentUser.setLongitude(location.getLongitude());

            Log.d("MapsActivity", "hasActivePolo: " + hasActivePolo);
            if (hasActivePolo) {
                databasePolos.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.hasChild(activePoloerUserId)) {
                            databasePolos.child(activePoloerUserId).child(LoginActivity.currentUser.getUserId()).child("latitude").setValue(LoginActivity.currentUser.getLatitude());
                            databasePolos.child(activePoloerUserId).child(LoginActivity.currentUser.getUserId()).child("longitude").setValue(LoginActivity.currentUser.getLongitude());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    public void addMarcoMarker(final double lat, final double lng, final String message, final String sender, final String userId, final String imgUrl, final boolean privat) {
        LatLng extraLatlng = new LatLng(lat, lng);
        try {
            MarkerOptions markerOptions = new MarkerTask().execute(new MarkerInfo(getBaseContext(), imgUrl)).get();
            markerOptions.position(extraLatlng);
            markerOptions.snippet(privat + "|" + sender + "|" + message + "|" + userId).title(userId);
            Marker marker = mMap.addMarker(markerOptions);
            mapMarkerArray.add(marker);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static Marker getMarker(String userId) {
        for (final Marker marker : mapMarkerArray) {
            if (marker.getTitle() != null && marker.getTitle().equalsIgnoreCase(userId)) {
                return marker;
            }
        }
        return null;
    }

    public static void removeMarcoMarker(Marker marker) {
        if (marker != null) {
            mapMarkerArray.remove(marker);
            marker.remove();
        }
    }

    public static Marker lastMarkerClicked = null;

    @Override
    public boolean onMarkerClick(final Marker marker) {
        destinationMarker = marker;
        if (currentLocationMarker.getPosition().latitude != marker.getPosition().latitude && currentLocationMarker.getPosition().longitude != marker.getPosition().longitude) {
            String currentLatitude = String.valueOf(lastLocation.getLatitude());
            String currentLongitude = String.valueOf(lastLocation.getLongitude());
            String destinationLatitude = String.valueOf(marker.getPosition().latitude);
            String destinationLongitude = String.valueOf(marker.getPosition().longitude);

            try {
                new RouteFinder(this, currentLatitude + "," + currentLongitude, destinationLatitude + "," + destinationLongitude).execute();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        marker.setInfoWindowAnchor(10000, 10000);
        final String[] data = marker.getSnippet().split("\\|");

        Toast.makeText(this, LoginActivity.currentUser.getUserId() + ":" + data[3], Toast.LENGTH_SHORT).show();


        if (hasActivePolo && LoginActivity.currentUser.getUserId().equalsIgnoreCase(data[3])) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Do you want to delete your polo?");
            dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    removeMarcoMarker(marker);
                    redrawPins();
                    databasePolos.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot child : snapshot.getChildren()) {
                                    if (child.getKey().equalsIgnoreCase(data[3])) {
                                        for (DataSnapshot c : child.getChildren()) {
                                            if (c.child("responded").toString().equalsIgnoreCase("true")) {
                                                databasePolos.child(data[3]).removeValue();
                                                databasePolos.child(c.getKey()).removeValue();
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });

            dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    return;
                }
            });

            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        } else if (data[3].equalsIgnoreCase(LoginActivity.currentUser.getUserId())) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Do you want to delete your marco?");
            dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    databaseMarcos.child(data[3]).removeValue();
                    removeMarcoMarker(marker);
                    redrawPins();
                }
            });

            dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    return;
                }
            });

            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
            return false;
        }

        if (hasActivePolo) {
            if (!LoginActivity.currentUser.getUserId().equalsIgnoreCase(data[3])) {
                Toast.makeText(this, getResources().getString(R.string.has_active_polo), Toast.LENGTH_LONG).show();
            }
            return false;
        }

//        if (data[0].equalsIgnoreCase("false")) {
//            databasePolos.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    boolean responded = false;
//                    if (dataSnapshot != null) {
//                        for (DataSnapshot child : dataSnapshot.getChildren()) {
//                            if (child.exists() && child.getKey().equalsIgnoreCase(data[3])) {
//                                for (DataSnapshot c : child.getChildren()) {
//                                    if (c.exists() && c.child("responded").getValue().toString().equalsIgnoreCase("true")) {
//                                        responded = true;
//                                        break;
//                                    }
//                                }
//
//                                if (responded)
//                                    Toast.makeText(getBaseContext(), "This user currently has an active polo!", Toast.LENGTH_LONG).show();
//                                break;
//                            }
//                        }
//
//                        /*if (!responded) {
//                            Handler mainHandler = new Handler(getBaseContext().getMainLooper());
//                            Runnable r = new Runnable() {
//                                @Override
//                                public void run() {
//                                    Log.d("MapsActivity", "wat");
//
//                                    Intent intent = new Intent(getBaseContext(), QuickMarcoActivity.class);
//                                    if (marker.getSnippet() != null && marker.getSnippet().contains("|")) {
//                                        intent.putExtra("private", data[0]);
//                                        intent.putExtra("sender", data[1]);
//                                        intent.putExtra("message", data[2]);
//                                        intent.putExtra("userId", data[3]);
//                                    }
//                                    startActivity(intent);
//                                }
//                            };
//                            mainHandler.post(r);
//                        }*/
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        } else {
        Intent intent = new Intent(this, PoloActivity.class);
        if (marker.getSnippet() != null && marker.getSnippet().contains("|")) {
            intent.putExtra("private", data[0]);
            intent.putExtra("sender", data[1]);
            intent.putExtra("message", data[2]);
            intent.putExtra("userId", data[3]);
        }
        startActivity(intent);
        //}
        lastMarkerClicked = marker;
        return false;
    }

    public void redrawPins() {
        for (Marker marker : mapMarkerArray) {
            marker.remove();
        }
        mapMarkerArray.clear();

        databaseMarcos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        final Marco marco = child.getValue(Marco.class);
                        if (marco.isPublic()) {
                            databaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot != null) {
                                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                                            if (child.getKey().equalsIgnoreCase(marco.getUserId())) {
                                                addMarcoMarker(marco.getLatitude(), marco.getLongitude(), marco.getMessage(), marco.getName(), marco.getUserId(), child.child("imgUrl").getValue().toString(), false);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
