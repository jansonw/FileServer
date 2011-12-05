package com.cs456.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

import com.cs456.project.common.Credentials;
import com.cs456.project.exceptions.AuthenticationException;
import com.cs456.project.exceptions.DisconnectionException;
import com.cs456.project.exceptions.RequestPermissionsException;

public class NativeClientActivity extends Activity {

    private EditText user;
    private EditText pass;
    CC cc;
    private Handler handle = new Handler();
    private NativeClientActivity This = this;
    ProgressDialog pdialog;
    private Track track;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	user = (EditText) findViewById(R.id.username);
	pass = (EditText) findViewById(R.id.password);
	track = Track.getInstance();
	track.setContext(this);
	cc = CC.getInstance();
    }

    /***
     * New User, Pressed Register
     * 
     * @param view
     */
    public void onNewUser(View view) {
	Intent i = new Intent(this, RegisterActivity.class);
	startActivity(i);
    }

    public void onLogin(View view) {
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		boolean result = true;
		String message = "";
		try {
		    cc.getCC().setCredentials(new Credentials(user.getText().toString(),pass.getText().toString()));
		    cc.getCC().verifyCredentials();
		} catch (DisconnectionException e) {
		    message = e.getMessage();
		    result = false;
		} catch (AuthenticationException e) {
		    result = false;
		    message = e.getMessage();
		} catch (RequestPermissionsException e) {
		    result = false;
		    message = e.getMessage();
		}
	//	pdialog.dismiss();
		
		if (!result) {
		    handleError(message);
		    return;
		}
		Intent i = new Intent(This, MenuActivity.class);
		//cc.getCC().setCredentials(new Credentials(user.getText().toString(), pass.getText().toString()));
		startActivity(i);
	    }
	}).start();
	//pdialog = ProgressDialog.show(This, "",
	//	"Logging in wait for a few seconds...", true);
    }

    protected void handleError(final String error) {
	handle.post(new Runnable() {

	    @Override
	    public void run() {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(This);
		alertBuilder.setMessage(error);
		alertBuilder.setCancelable(false);

		alertBuilder.setNeutralButton("OK",
			new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			    }
			});

		AlertDialog alert = alertBuilder.create();
		alert.setTitle("Error!");
		alert.show();
	    }
	});
    }
}