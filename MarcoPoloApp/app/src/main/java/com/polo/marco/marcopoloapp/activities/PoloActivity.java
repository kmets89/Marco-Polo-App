package com.polo.marco.marcopoloapp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.firebase.models.Polo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PoloActivity extends AppCompatActivity {

    private final double winWidth = 0.8;
    private final double privateHeight = 0.75;
    private final double publicHeight = 0.35;
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
    private DatabaseReference databaseNotifs = FirebaseDatabase.getInstance().getReference("notifs");

    @Override
    //Opens a popup window for entering and storing Marco information.
    //Popup covers only a percentage of the screen with the previous view displayed underneath
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams params = win.getAttributes();
        params.dimAmount = 0.6f;
        win.setAttributes(params);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_polo);
        setWinSize(winWidth, publicHeight);

        sender = (TextView) findViewById(R.id.sender);
        message = (TextView) findViewById(R.id.message);
        cancelPolo = (Button) findViewById(R.id.cancelPolo);
        //poloText = (EditText) findViewById(R.id.poloText);

        if (getIntent().getStringExtra("private") != null && getIntent().getStringExtra("private") != null && getIntent().getStringExtra("private").equalsIgnoreCase("false")) {
            cancelPolo.setText("Dismiss");
        }

        sender.append(" " + getIntent().getStringExtra("sender"));
        message.append(" " + getIntent().getStringExtra("message"));

        if (getIntent().getStringExtra("sender") == null || getIntent().getStringExtra("sender").equalsIgnoreCase("null")) {
            finish();
        }
    }

    //Resize the activity window as a fraction of the default size
    public void setWinSize(double w, double h) {
        DisplayMetrics dispMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dispMetrics);
        int width = dispMetrics.widthPixels;
        int height = dispMetrics.heightPixels;

        getWindow().setLayout((int) (width * w), (int) (height * h));
    }

    public void showAlert(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //just continue here
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.width = ViewGroup.LayoutParams.MATCH_PARENT;
        positiveButton.setLayoutParams(positiveButtonLL);
    }

    public void onClickSendPolo(View v) {
        if (getIntent().getStringExtra("userId") == null) {
            Toast.makeText(this, getResources().getString(R.string.nulled_object), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (getIntent().getStringExtra("userId") != null && getIntent().getStringExtra("userId").equalsIgnoreCase(LoginActivity.currentUser.getUserId())) {
            showAlert("You cannot send a polo to yourself!");
            return;
        }

        if (getIntent().getStringExtra("private") != null && getIntent().getStringExtra("private").equalsIgnoreCase("false")) {
            Polo polo = new Polo(getIntent().getStringExtra("userId"), "", "senderName", currentDate, getIntent().getDoubleArrayExtra("latlng")[0], getIntent().getDoubleArrayExtra("latlng")[1], true);
            databasePolos.child(LoginActivity.currentUser.getUserId()).child(getIntent().getStringExtra("userId")).setValue(polo);
        }

        databasePolos.child(LoginActivity.currentUser.getUserId()).child(getIntent().getStringExtra("userId")).child("responded").setValue(true);
        Polo polo = new Polo(getIntent().getStringExtra("userId"), "", LoginActivity.currentUser.getName(), currentDate, lat, lng, true);
        databasePolos.child(getIntent().getStringExtra("userId")).child(LoginActivity.currentUser.getUserId()).setValue(polo);

        databaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseNotifs.child(getIntent().getStringExtra("userId")).child("message").setValue("You have received a polo!");
        finish();
    }

    public void onClickDeletePolo(View v) {
        if (getIntent().getStringExtra("private") == null || (getIntent().getStringExtra("private").equalsIgnoreCase("false"))) {
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
