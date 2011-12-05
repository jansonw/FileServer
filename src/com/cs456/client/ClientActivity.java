package com.cs456.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cs456.project.exceptions.AuthenticationException;
import com.cs456.project.exceptions.DisconnectionException;
import com.cs456.project.exceptions.OutOfDateException;
import com.cs456.project.exceptions.RequestExecutionException;
import com.cs456.project.exceptions.RequestPermissionsException;

public class ClientActivity extends ListActivity {

    CC cc;
    String username;
    String password;
    CheckBox share;
    private File sdCardRoot = Environment.getExternalStorageDirectory();
    private Handler handle = new Handler();
    private ClientActivity This;
    EditText currLoc = null;
    EditText saveas = null;
    List<String> list = null;
    private List<String> item = null;
    private List<String> path = null;
    private TextView myPath;
    private Track track;

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.filelist_client);
	This = this;
	myPath = (TextView) findViewById(R.id.path);
	track = Track.getInstance();
	track.setContext(this);
	getDir(sdCardRoot.getAbsolutePath());
	cc = CC.getInstance();
    }

    private void getDir(String dirPath) {
	myPath.setText("Location: " + dirPath.trim());
	item = new ArrayList<String>();
	path = new ArrayList<String>();
	File f = new File(dirPath);
	File[] files = f.listFiles();
	if (!dirPath.trim().equals(sdCardRoot.getAbsoluteFile().toString())) {
	    item.add("../");
	    path.add(f.getParent());
	}
	for (int i = 0; i < files.length; i++) {
	    File file = files[i];
	    path.add(file.getPath());
	    if (file.isDirectory())
		item.add(file.getName() + "/");
	    else
		item.add(file.getName());
	}
	ArrayAdapter<String> fileList = new ArrayAdapter<String>(this,
		R.layout.rowlayout, item);
	setListAdapter(fileList);
    }

    private void alertUploadBox(File f) {
	LayoutInflater factory = LayoutInflater.from(This);
	final View textEntryView = factory
		.inflate(R.layout.upload_dialog, null);
	final String absPath = f.getAbsolutePath();
	AlertDialog.Builder alertbox = new AlertDialog.Builder(This);
	alertbox.setView(textEntryView);
	currLoc = (EditText) textEntryView.findViewById(R.id.upload_phoneloc);
	share = (CheckBox) textEntryView.findViewById(R.id.upload_share);
	alertbox.setPositiveButton("Upload",
		new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			saveas = (EditText) textEntryView
				.findViewById(R.id.upload_serverloc);
			String tmp = saveas.getText().toString();
			if (tmp.isEmpty()) {
			    handleError("Need a location to save to");
			    return;
			}

			// Run in background
			new Thread(new Runnable() {
			    public void run() {
				boolean result = false;
				String msg = "";
				final String currVal = currLoc.getText()
					.toString();
				try {
				    cc.getCC().requestFileUpload(absPath, saveas
					    .getText().toString(), share
					    .isChecked());
				    result = true;
				} catch (DisconnectionException e) {
				    msg = e.getMessage();
				} catch (AuthenticationException e) {
				    msg = e.getMessage();
				} catch (RequestExecutionException e) {
				    msg = e.getMessage();
				} catch (RequestPermissionsException e) {
				    msg = e.getMessage();
				} catch (OutOfDateException e) {
				    msg = e.getMessage();
				}
				if (!result) {
				    handleError(msg);
				    return;
				}

				handle.post(new Runnable() {
				    @Override
				    public void run() {
					Toast.makeText(
						track.getContext(),
						"Your upload of " + currVal
							+ " has completed", 5)
						.show();
				    }
				});
			    }
			}).start();
			dialog.dismiss();
			handle.post(new Runnable() {
			    @Override
			    public void run() {
				Toast.makeText(track.getContext(),
					"Your upload has started", 5).show();
			    }
			});
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
	alert.setTitle("Upload to Server");
	alert.show();
	currLoc.setText(f.getName());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	File file = new File(path.get(position));
	if (file.isDirectory()) {
	    if (file.canRead())
		getDir(path.get(position));
	    else {
	    }
	} else {
	    alertUploadBox(file);
	}
    }

    // Creates a dialog box stating there was an error, and prints the text
    // which the calling code provides it
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
