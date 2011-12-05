package com.cs456.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cs456.project.common.Credentials;
import com.cs456.project.exceptions.AuthenticationException;
import com.cs456.project.exceptions.DisconnectionException;
import com.cs456.project.exceptions.RequestExecutionException;
import com.cs456.project.exceptions.RequestPermissionsException;

public class AccountActivity extends Activity {

    private AccountActivity This = this;
    TextView user;
    String oldPass;
    CC cc;
    EditText password;
    ProgressDialog pdialog;
    private Handler handle = new Handler();
    Track track;
    String username;
    String pass;

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.account);
	cc = CC.getInstance();
	user = (TextView) findViewById(R.id.acctUser);
	username = cc.getCC().getUsername();
	pass = cc.getCC().getPassword();
	user.setText(username);
	track = Track.getInstance();
	track.setContext(this);
	oldPass = pass;
    }

    /***
     * Password box
     */
    private void alertPasswordBox() {
	LayoutInflater factory = LayoutInflater.from(this);
	final View textEntryView = factory.inflate(R.layout.password_dialog,
		null);

	AlertDialog.Builder alertbox = new AlertDialog.Builder(this);

	alertbox.setView(textEntryView);
	alertbox.setPositiveButton("Save",
		new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(final DialogInterface dialog, int which) {
			EditText old = (EditText) textEntryView.findViewById(R.id.oldpassword);
			password = (EditText) textEntryView
				.findViewById(R.id.dialog_pass1);
			EditText password2 = (EditText) textEntryView
				.findViewById(R.id.dialog_pass2);
			// Send to server wait for return
			if (!checkPass(pass,old.getText().toString())) {
			    Toast.makeText(This, "old password doesn't match", 5);
			    return;
			} 
			if (!checkPass(password.getText().toString(), password2
				.getText().toString())){
			    Toast.makeText(This, "Not same password", 5).show();
			    return;
			}
			else {
			    new Thread(new Runnable() {
				public void run() {
				    boolean result = false;
				    String message = "";
				    try {
					cc.getCC().requestPasswordChange(oldPass,
						password.getText().toString());
					result = true;
				    } catch (AuthenticationException e) {
					message = e.getMessage();
				    } catch (RequestPermissionsException e) {
					message = e.getMessage();
				    } catch (RequestExecutionException e) {
					message = e.getMessage();
				    } catch (DisconnectionException e) {
					message = e.getMessage();
				    }

				    if (!result) {
					handleError(message);
					return;
				    }
				    handle.post(new Runnable() {

					@Override
					public void run() {
					    Toast.makeText(track.getContext(),
						    "Modified password", 3)
						    .show();
					}
				    });
				    oldPass = password.getText().toString();
				    cc.getCC().setCredentials(new Credentials(username, oldPass));
				}
			    }).start();
			    dialog.dismiss();
			}
		    }
		});

	alertbox.setNegativeButton("Cancel",
		new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			dialog.cancel();
		    }
		});

	AlertDialog alert = alertbox.create();
	alert.setTitle("Change Password");
	alert.show();
    }

    /***
     * Checks password fields match Can further check for other guards
     * 
     * @return boolean
     */
    private boolean checkPass(String pass1, String pass2) {
	if (pass1.length() == 0) {
	    Toast.makeText(this, "Empty password is also not allowed", 5)
		    .show();
	    return false;
	}
	if (pass1.compareTo(pass2) == 0) {
	    return true;
	}
	return false;
    }

    /*************** Following are click handlers *******/

    public void onChangePassword(View view) {
	alertPasswordBox();
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
