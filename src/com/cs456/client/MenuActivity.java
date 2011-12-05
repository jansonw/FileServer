package com.cs456.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.cs456.project.exceptions.AuthenticationException;
import com.cs456.project.exceptions.DisconnectionException;
import com.cs456.project.exceptions.RequestExecutionException;
import com.cs456.project.exceptions.RequestPermissionsException;

public class MenuActivity extends Activity {

    String username = null;
    String password = null;
    String serverlocation = "";
    CC cc;
    private MenuActivity This = this;
    ProgressDialog pdialog;
    private Handler handle = new Handler();
    AutoCompleteTextView url;
    EditText saveas;
    private Track track;

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.menu);
	cc = CC.getInstance();
	username = cc.getCC().getUsername();
	password = cc.getCC().getPassword();
	track = Track.getInstance();
	track.setContext(this);
    }

    private void alertRemoteDownloadBox() {
	LayoutInflater factory = LayoutInflater.from(this);
	final View textEntryView = factory
		.inflate(R.layout.remote_dialog, null);

	AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
	alertbox.setView(textEntryView);
	alertbox.setPositiveButton("Download",
		new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(final DialogInterface dialog, int which) {
			url = (AutoCompleteTextView) textEntryView
				.findViewById(R.id.autoCompleteTextView1);
			saveas = (EditText) textEntryView
				.findViewById(R.id.dialog_saveas);
			if (saveas.getText().toString().isEmpty()) {
			    dialog.cancel();
			}
			serverlocation = saveas.getText().toString();
			new Thread(new Runnable() {
			    public void run() {
				String msg = "";
				boolean result = false;
				try {
				    cc.getCC().requestRemoteFileDownload(url.getText()
					    .toString(), saveas.getText()
					    .toString(), false);
				    result = true;
				} catch (AuthenticationException e) {
				    msg = e.getMessage();
				} catch (RequestPermissionsException e) {
				    msg = e.getMessage();
				} catch (RequestExecutionException e) {
				    msg = e.getMessage();
				} catch (DisconnectionException e) {
				    msg = e.getMessage();
				}

				if (!result) {
				    handleError(msg);
				    return;
				}

				handle.post(new Runnable() {
				    @Override
				    public void run() {
					Toast.makeText(track.getContext(),
						"Remote Download "+serverlocation+" started", 5).show();
				    }
				});
			    }
			}).start();

			new Thread(new ParamRunnable(serverlocation) {
			    @Override
			    public void run() {
				boolean result = false;
				String msg = "";
				boolean returned = false;
				while (true) {
				    try {
					Thread.sleep(5000);
				    } catch (InterruptedException e1) {
					break;
				    }
				    try {
					returned = cc.getCC().requestFileExistance(
						serverlocation, username);
					result = true;
				    } catch (AuthenticationException e) {
					msg = e.getMessage();
				    } catch (RequestPermissionsException e) {
					msg = e.getMessage();
				    } catch (RequestExecutionException e) {
					msg = e.getMessage();
				    } catch (DisconnectionException e) {
					msg = e.getMessage();
				    }

				    if (!result) {
					handleError(msg);
					break;
				    }
				    if (returned) {
					handle.post(new Runnable() {
					    public void run() {
						Toast.makeText(
							track.getContext(),
							"Remote download "+param+" completed",
							5).show();
					    }
					});
					break;
				    }
				}
			    }
			}).start();
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
	alert.setTitle("Remote Download");
	alert.show();
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

    /*************** Following are click handlers *******/

    public void onAccount(View view) {
	Intent i = new Intent(this, AccountActivity.class);
	Bundle b = new Bundle();
	b.putString("username", username);
	b.putString("password", password);
	i.putExtras(b);
	startActivity(i);
    }

    public void onServer(View view) {
	Intent i = new Intent(this, ServerActivity.class);
	Bundle b = new Bundle();
	b.putString("username", username);
	b.putString("password", password);
	i.putExtras(b);
	startActivity(i);
    }

    public void onRemoteDownload(View view) {
	alertRemoteDownloadBox();
    }

    public void onUpload(View view) {
	Intent i = new Intent(this, ClientActivity.class);
	Bundle b = new Bundle();
	b.putString("username", username);
	b.putString("password", password);
	i.putExtras(b);
	startActivity(i);
    }
}
