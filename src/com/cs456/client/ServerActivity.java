package com.cs456.client;

import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.cs456.project.client.ClientConnection;
import com.cs456.project.common.Credentials;
import com.cs456.project.common.FileListManager;
import com.cs456.project.common.FileListObject;
import com.cs456.project.exceptions.AuthenticationException;
import com.cs456.project.exceptions.DeletionDelayedException;
import com.cs456.project.exceptions.DisconnectionException;
import com.cs456.project.exceptions.OutOfDateException;
import com.cs456.project.exceptions.RequestExecutionException;
import com.cs456.project.exceptions.RequestPermissionsException;

public class ServerActivity extends ListActivity {
    EditText currLoc;
    ListView lview;
    String username;
    CheckBox share;
    CC cc;

    private ServerActivity This = this;
    ProgressDialog pdialog;
    private Handler handle = new Handler();
    FileListManager mlist;

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.filelist_server);
	cc = CC.getInstance();
	username = cc.getCC().getUsername();
	pdialog = ProgressDialog.show(This, "", "Please wait a few seconds...",
		true);
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		// TODO Auto-generated method stub
		try {
		    mlist = cc.getCC().getFileList(username);
		} catch (AuthenticationException e) {
		    handleError("Authentication error");
		} catch (RequestPermissionsException e) {
		    handleError("Permission error");
		} catch (RequestExecutionException e) {
		    handleError("Could not execute");
		} catch (DisconnectionException e) {
		    handleError("Disconnected");
		}
		List<FileListObject> flist = mlist.getAll(true);
		lview = (ListView) findViewById(R.id.listserver);
		// or maybe i should just do This.setAdapter..
		lview.setAdapter(new ArrayAdapter<FileListObject>(This,
			android.R.layout.simple_list_item_1, flist));

		pdialog.dismiss();
	    }
	}).start();
    }

    /***
     * TODO: Need to change the list adapter to hold a specialized version where
     * there is file loc/ data mod/ share information
     * 
     * @param selected
     */
    private void alertSettingBox(final String selected) {
	LayoutInflater factory = LayoutInflater.from(this);
	final View textEntryView = factory.inflate(R.layout.share_dialog, null);
	AlertDialog.Builder alertbox = new AlertDialog.Builder(this);

	alertbox.setView(textEntryView);
	alertbox.setPositiveButton("Save",
		new DialogInterface.OnClickListener() {

		    @Override
		    public void onClick(final DialogInterface dialog, int which) {
			currLoc = (EditText) textEntryView
				.findViewById(R.id.share_file);
			share = (CheckBox) findViewById(R.id.share_file);
			currLoc.setText(selected);
			// Not sure if this waits for response
			// FileListObject flo = (FileListObject) lview
			// .getSelectedItem();
			new Thread(new Runnable() {

			    @Override
			    public void run() {
				try {
				    cc.getCC().requestPermissionsChange(
					    currLoc.getText().toString(),
					    share.isChecked());
				} catch (AuthenticationException e) {
				    handleError("Lost authentication token");
				} catch (RequestPermissionsException e) {
				    handleError("Permission denied");
				} catch (RequestExecutionException e) {
				    handleError("Execution failed");
				} catch (DisconnectionException e) {
				    handleError("Disconnected");
				}
				pdialog.dismiss();
				dialog.dismiss();
				handle.post(new Runnable() {

				    @Override
				    public void run() {
					Toast.makeText(This,
						"Changed Sharing Settings", 5)
						.show();
				    }
				});
			    }
			}).start();
//			pdialog = ProgressDialog.show(This, "",
//				"Changing settings...", true);
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
	alert.setTitle("Sharing permission");
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

    /******* onClick Handlers ***********/

    public void onDownload(View view) {
	// either use this or the other view/list view
	final FileListObject flo = (FileListObject) lview.getSelectedItem();
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		try {
		    // How does this work?
		    cc.getCC()
			    .requestFileDownload("", flo.toString(), username);
		} catch (AuthenticationException e) {
		    handleError("Lost authentication token");
		} catch (RequestPermissionsException e) {
		    handleError("Permission denied");
		} catch (RequestExecutionException e) {
		    handleError("Execution failed");
		} catch (DisconnectionException e) {
		    handleError("Disconnected");
		} catch (OutOfDateException e) {
		    handleError("Out of date");
		}
		pdialog.dismiss();
		Toast.makeText(This, "Done Download", 5);
	    }
	}).start();
	pdialog = ProgressDialog.show(This, "", "Downloading...", true);
    }

    public void onDelete(View view) {
	final FileListObject flo = (FileListObject) lview.getSelectedItem();
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		try {
		    // How does this work?
		    cc.getCC().requestFileDeletion(flo.toString());
		} catch (AuthenticationException e) {
		    handleError("Lost authentication token");
		} catch (RequestPermissionsException e) {
		    handleError("Permission denied");
		} catch (RequestExecutionException e) {
		    handleError("Execution failed");
		} catch (DisconnectionException e) {
		    handleError("Disconnected");
		} catch (DeletionDelayedException e) {
		    handleError("delayed due to someone downloading");
		}
		pdialog.dismiss();
		Toast.makeText(This, "Done Delete", 5);
	    }
	}).start();
	// I don't think this dialog is actually needed
	pdialog = ProgressDialog.show(This, "", "Deleting...", true);
    }

    /***
     * Need to change this to load another dialog box
     * 
     * @param view
     */
    public void onSettings(View view) {
	if (getListView().getSelectedItem() == null)
	    handleError("Please select an item before upload");
	else
	    alertSettingBox(getListView().getSelectedItem().toString());
    }
}
