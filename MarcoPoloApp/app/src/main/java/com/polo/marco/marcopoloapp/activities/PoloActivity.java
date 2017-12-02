package com.polo.marco.marcopoloapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.firebase.models.Polo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PoloActivity extends AppCompatActivity {

    private final double winWidth = 0.8;
    private final double privateHeight = 0.75;
    private final double publicHeight = 0.42;
    private TextView sender;
    private TextView message;
    private Button cancelPolo;
    private EditText poloText;

    double lat = LoginActivity.currentUser.getLatitude();
    double lng = LoginActivity.currentUser.getLongitude();

    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    Date date = new Date();
    private String currentDate = dateFormat.format(date);

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
        cancelPolo = (Button) findViewById(R.id.cancelPolo);
        poloText = (EditText) findViewById(R.id.poloText);

        if (getIntent().getStringExtra("private").equalsIgnoreCase("false")) {
            cancelPolo.setText("Dismiss");
        }

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

    public void onClickSendPolo(View v) {
        databasePolos.child(LoginActivity.currentUser.getUserId()).child(getIntent().getStringExtra("userId")).child("responded").setValue(true);

        Polo polo = new Polo(getIntent().getStringExtra("userId"), poloText.getText().toString(), LoginActivity.currentUser.getName(), currentDate, lat, lng, true);
        databasePolos.child(getIntent().getStringExtra("userId")).child(LoginActivity.currentUser.getUserId()).setValue(polo);

        finish();
    }

    public void onClickDeletePolo(View v) {
        if (getIntent().getStringExtra("private").equalsIgnoreCase("false")) {
            finish();
            return;
        }

        Toast.makeText(this, getIntent().getStringExtra("userId"), Toast.LENGTH_LONG).show();
        databasePolos.child(LoginActivity.currentUser.getUserId()).child(getIntent().getStringExtra("userId")).removeValue();

        if (MapsActivity.lastMarkerClicked != null) {
            MapsActivity.removeMarcoMarker(MapsActivity.lastMarkerClicked);
            MapsActivity.lastMarkerClicked = null;
        }

        finish();
    }

}
