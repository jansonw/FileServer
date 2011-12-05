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
import com.cs456.project.exceptions.DisconnectionException;
import com.cs456.project.exceptions.RequestExecutionException;

public class RegisterActivity extends Activity {

    private EditText username;
    private EditText pass;
    private RegisterActivity This = this;
    ProgressDialog pdialog;
    private Handler handle = new Handler();
    private CC cc;

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.registration);
	username = (EditText) findViewById(R.id.regUsername);
	pass = (EditText) findViewById(R.id.regPassword1);
	cc = CC.getInstance();
    }

    /***
     * Specific to registrations because of listener intent
     * 
     * @param message
     * @param type
     * @param listener
     */
    protected void alertBoxGen(String message, String type, boolean listener) {
	AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
	// set the message to display
	alertbox.setMessage(message);

	// add a neutral button to the alert box and assign a click listener
	alertbox.setTitle(type);
	if (listener) {
	    alertbox.setPositiveButton("OK",
		    new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			    onCancel(null);
			}
		    });
	} else {
	    alertbox.setPositiveButton("OK", null);
	}
	alertbox.show();
    }

    protected void alertBox(String message, String type) {
	alertBoxGen(message, type, false);
    }

    public void onRegister(View view) {
	boolean correct = true;
	if (username.getText().toString().isEmpty()) {
	    alertBox("Empty username", "Error");
	    correct = false;
	} else if (!checkPass()) {
	    correct = false;
	    alertBox("Passwords don't match or empty", "Error");
	} else if (username.getText().toString().contains(" ")){
	    correct = false;
	    alertBox("Username cannot contain a space","Error");
	}

	if (!correct)
	    return;
	
	new Thread(new Runnable() {
	    
	    @Override
	    public void run() {
		boolean result = true;
		String message = "";
		    try {
			cc.getCC().requestUserRegistration(username.getText().toString(), pass
			    .getText().toString());
		    } catch (RequestExecutionException e) {
			message = e.getMessage();
			result = false;
		    } catch (DisconnectionException e) {
			message = e.getMessage();
			result = false;
		    }
//		    pdialog.dismiss();
		    if (!result) {
			handleError(message);
			return;
		    }
		    cc.getCC().setCredentials(new Credentials(username.getText().toString(), pass.getText().toString()));
		    Intent i = new Intent(This, MenuActivity.class);
		    
		    startActivity(i);
	    }
	}).start();
	
//	pdialog = ProgressDialog.show(This, "",
//		"Registering wait for a few seconds...", true);
    }

    /***
     * Checks password fields match Can further check for other guards
     * 
     * @return boolean
     */
    private boolean checkPass() {
	EditText pass2 = (EditText) findViewById(R.id.regPassword2);
	if (pass2.getText().toString().length() == 0)
	    return false;
	if (pass.getText().toString().compareTo(pass2.getText().toString()) == 0) {
	    return true;
	}
	return false;
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
    
    /***
     * Go back to previous screen (Login screen)
     * 
     * @param view
     */
    public void onCancel(View view) {
	Intent i = new Intent(this, NativeClientActivity.class);

	startActivity(i);
    }
}
