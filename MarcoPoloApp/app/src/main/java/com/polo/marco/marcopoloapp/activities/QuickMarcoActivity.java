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
import com.polo.marco.marcopoloapp.firebase.models.Marco;
import com.polo.marco.marcopoloapp.firebase.models.Polo;
import com.polo.marco.marcopoloapp.firebase.models.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class QuickMarcoActivity extends AppCompatActivity {

    private final double winWidth = 0.8;
    private final double publicHeight = 0.5;

    double lat = LoginActivity.currentUser.getLatitude();
    double lng = LoginActivity.currentUser.getLongitude();
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    Date date = new Date();
    private String currentDate = dateFormat.format(date);

    private DatabaseReference databaseMarcos = FirebaseDatabase.getInstance().getReference("marcos");
    private DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference databasePolos = FirebaseDatabase.getInstance().getReference("polos");
    private EditText mapMessage;

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

        setContentView(R.layout.activity_quick_marco);
        setWinSize(winWidth, publicHeight);

        if (getIntent().getStringExtra("message") == null || getIntent().getStringExtra("userId") == null) {
            Toast.makeText(this, getResources().getString(R.string.nulled_object), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mapMessage = (EditText) findViewById(R.id.message_text);
        mapMessage.setText(getIntent().getStringExtra("message"));
        mapMessage.setEnabled(false);
    }

    //Resize the activity window as a fraction of the default size
    public void setWinSize(double w, double h) {
        DisplayMetrics dispMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dispMetrics);
        int width = dispMetrics.widthPixels;
        int height = dispMetrics.heightPixels;

        getWindow().setLayout((int) (width * w), (int) (height * h));
    }

    //Handle selections in checklist
    View.OnClickListener getOnClickDoSomething(final Button button) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ON_CLICK", "CheckBox ID: " + button.getId() + " Text: " + button.getText().toString());
            }
        };
    }

    //Close the activity, not saving any data
    public void onClickCancelMarco(View view) {
        finish();
    }

    //Grabs current system date and time, userId and location, Marco message, public/private bool,
    // and adds it to the Marco table in the database. If the message is private, the selected
    //recipients are stored as well
    public void onClickSendMarco(View view) {
        String userId = LoginActivity.currentUser.getUserId();

        String message = ((EditText) findViewById(R.id.marcoText)).getText().toString();
        if (message == null || message.equals("")) {
            showAlert(getResources().getString(R.string.empty_message));
            return;
        }

        //expiration time set for 12 hours from current time, measured in seconds from Epoch
        long expireTime = (System.currentTimeMillis() / 1000L) + 43200;
        sendPrivateMarcoFromDB(message, currentDate, expireTime);
        finish();
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

    public void sendPrivateMarcoFromDB(final String message, String currentDate, long expireTime) {
        final String cd = currentDate;
        final String marcoUserId = getIntent().getStringExtra("userId");
        final ArrayList<String> recv = new ArrayList<>();
        final Marco privateMarco = new Marco(LoginActivity.currentUser.getUserId(), LoginActivity.currentUser.getName(), message, currentDate, expireTime, lat, lng, false, recv);

        databaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(getApplicationContext(), "User " + marcoUserId + " Could not be reached!", Toast.LENGTH_LONG).show();
                } else {
                    for (final DataSnapshot child : snapshot.getChildren()) {
                        if (child.exists() && child.getKey().equalsIgnoreCase(marcoUserId)) {
                            User retrievedUser = child.getValue(User.class);
                            if (!retrievedUser.getBlockList().contains(LoginActivity.currentUser.getUserId())) {
                                recv.add(retrievedUser.getFirebaseToken());
                                databaseMarcos.child(LoginActivity.currentUser.getUserId()).child("receiverList").setValue(recv);

                                Polo polo = new Polo(retrievedUser.getUserId(), message, LoginActivity.currentUser.getName(), cd, lat, lng, false);
                                databasePolos.child(retrievedUser.getUserId()).child(LoginActivity.currentUser.getUserId()).setValue(polo);

                                databaseMarcos.child(LoginActivity.currentUser.getUserId()).setValue(privateMarco);
                                break;
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
}
