package com.polo.marco.marcopoloapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.api.directions.Route;
import com.polo.marco.marcopoloapp.api.directions.RouteFinder;
import com.polo.marco.marcopoloapp.api.directions.RouteFinderListener;
import com.polo.marco.marcopoloapp.api.notifications.Notifications;
import com.polo.marco.marcopoloapp.firebase.models.Marco;
import com.polo.marco.marcopoloapp.firebase.models.Polo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


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
    private Boolean pathCleared = true;

    private DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference databaseMarcos = FirebaseDatabase.getInstance().getReference("marcos");
    private DatabaseReference databasePolos = FirebaseDatabase.getInstance().getReference("polos");

    public static boolean mIsInForegroundMode = false;
    private static boolean menuOpened = false;
    public String tmpImgUrl = "";

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
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        databaseMarcos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Marco marco = child.getValue(Marco.class);
                        if (marco.getStatus() == true) {



                            addMarcoMarker(marco.getLatitude(), marco.getLongitude(), marco.getMessage(), marco.getName(), marco.getUserId(), false, null);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*databasePolos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        for (DataSnapshot childs : child.getChildren()) {
                            Polo polo = childs.getValue(Polo.class);
                            if (child.getKey().equalsIgnoreCase(LoginActivity.currentUser.getUserId())) {
                                addMarcoMarker(polo.getLatitude(), polo.getLongitude(), polo.getMessage(), polo.getSenderName(), childs.getKey(), true, null);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

    }

    public  void onMapLongClick (LatLng destination) {
        Location currentLocationHolder;

        currentLocationHolder = lastLocation;
        mMap.clear();
        lastLocation = currentLocationHolder;
        pathCleared = true;

        destinationPoint = new LatLng(destination.latitude, destination.longitude);
        LatLng currentLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(true);
        markerOptions.title("destination");

        currentLocationMarker = mMap.addMarker(markerOptions.position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        destinationMarker = mMap.addMarker(markerOptions.position(destination).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        destinationMarker.showInfoWindow();

    }

    public void getRoute(List<Route> routes) {

        mMap.clear();

        for (Route route : routes) {
            currentLocationMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title(route.startAddress)
                    .position(route.startPoint));
            destinationMarker = mMap.addMarker(new MarkerOptions()
                    .title(route.endAddress)
                    .snippet("Expected time: " + route.duration.text + ", Distance: " + route.distance.text)
                    .position(route.endPoint));


            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(7);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            mMap.addPolyline(polylineOptions);
            pathCleared = false;
        }

    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        if(pathCleared && destinationMarker != null && destinationMarker.getPosition().latitude == marker.getPosition().latitude && destinationMarker.getPosition().longitude == marker.getPosition().longitude){
            String currentLatitude = String.valueOf(lastLocation.getLatitude());
            String currentLongitude = String.valueOf(lastLocation.getLongitude());
            String destinationLatitude = String.valueOf(destinationPoint.latitude);
            String destinationLongitude = String.valueOf(destinationPoint.longitude);

            try {
                new RouteFinder(this, currentLatitude + "," + currentLongitude, destinationLatitude + "," + destinationLongitude).execute();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        lastMarkerClicked = marker;
        Intent intent = new Intent(this, PoloActivity.class);
        if (marker.getSnippet() != null && marker.getSnippet().contains("|")) {
            String[] data = marker.getSnippet().split("\\|");
            intent.putExtra("private", data[0]);
            intent.putExtra("sender", data[1]);
            intent.putExtra("message", data[2]);
            intent.putExtra("userId", data[3]);
        }
        startActivity(intent);

        return false;
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

    public void onClickExpandButton (View view){
        menuOpened = !menuOpened;
        int visibility;

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.expand_button);

        if (menuOpened) {
            visibility = View.VISIBLE;
            floatingActionButton.setImageResource(R.drawable.ic_clear_white_24dp);
        }
        else {
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

    public void onClickNavAccount (View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onClickNavNotifications (View view){
        Intent intent = new Intent(this, Notifications.class);
        startActivity(intent);
    }

    public void onClickNavFriends (View view){
        Intent intent = new Intent(this, FriendsListActivity.class);
        startActivity(intent);
    }

    public void onClickNavHelp (View view){
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public void onClickNavPrivacyPolicy (View view){
        Intent intent = new Intent(this, PrivacyPolicyActivity.class);
        startActivity(intent);
    }

    //Function that's called when the marco button is clicked
    public void onClickBtnMarco(View view) {
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

        mMap.setPadding(575, 300, 0, 0);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }


        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
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

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }

        if (LoginActivity.currentUser != null) {
            //update User in DB
            LoginActivity.currentUser.setLatitude(location.getLatitude());
            LoginActivity.currentUser.setLongitude(location.getLongitude());
            databaseUsers.child(LoginActivity.currentUser.getUserId()).child("latitude").setValue(location.getLatitude());
            databaseUsers.child(LoginActivity.currentUser.getUserId()).child("longitude").setValue(location.getLongitude());
        }
    }

    public static void addMarcoMarker(double lat, double lng, String message, String sender, String userId, boolean privat, Bitmap bitmap) {
        LatLng extraLatlng = new LatLng(lat, lng);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(extraLatlng);

        markerOptions.title(sender + ": " + message);
        if(bitmap != null)
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        mMap.addMarker(markerOptions).showInfoWindow();
    }

    public static Marker lastMarkerClicked = null;

}
