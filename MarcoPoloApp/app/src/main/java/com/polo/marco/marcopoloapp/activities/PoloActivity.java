package com.polo.marco.marcopoloapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.polo.marco.marcopoloapp.R;

public class PoloActivity extends AppCompatActivity {

    private final double winWidth = 0.8;
    private final double privateHeight = 0.75;
    private final double publicHeight = 0.42;
    private TextView sender;
    private TextView message;

    double lat = LoginActivity.currentUser.getLatitude();
    double lng = LoginActivity.currentUser.getLongitude();

    private DatabaseReference databaseMarcos = FirebaseDatabase.getInstance().getReference("marcos");
    private DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference databasePolos = FirebaseDatabase.getInstance().getReference("polos");

    @Override
    //Opens a popup window for entering and storing Marco information.
    //Popup covers only a percentage of the screen with the previous view displayed underneath
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_polo);
        setWinSize(winWidth, publicHeight);

        sender = (TextView) findViewById(R.id.sender);
        message = (TextView) findViewById(R.id.message);

        sender.append(" " + getIntent().getStringExtra("sender"));
        message.append(" " + getIntent().getStringExtra("message"));
    }

    //Resize the activity window as a fraction of the default size
    public void setWinSize(double w, double h) {
        DisplayMetrics dispMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dispMetrics);
        int width = dispMetrics.widthPixels;
        int height = dispMetrics.heightPixels;

        getWindow().setLayout((int) (width * w), (int) (height * h));
    }

    public void onClickDeleteMarco(View v) {
        Toast.makeText(this, getIntent().getStringExtra("userId"), Toast.LENGTH_LONG).show();
        databaseMarcos.child(getIntent().getStringExtra("userId")).removeValue();
        databasePolos.child(getIntent().getStringExtra("userId")).removeValue();
        databasePolos.child(LoginActivity.currentUser.getUserId()).removeValue();

        if (MapsActivity.lastMarkerClicked != null) {
            MapsActivity.lastMarkerClicked.remove();
            MapsActivity.lastMarkerClicked = null;
        }
    }

}
