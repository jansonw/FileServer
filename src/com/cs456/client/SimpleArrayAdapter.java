package com.cs456.client;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/***
 * TODO: Change this to be files
 * 
 * @author Kuen
 *
 */
public class SimpleArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final List<String> values;

	public SimpleArrayAdapter(Context context, List<String> values) {
		super(context, R.layout.rowlayout, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
//		TextView textView = (TextView) rowView.findViewById(R.id.label);
//		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
//		textView.setText(values.get(position));
//
//		//TODO: Change to directory icon
//		String s = values.get(position);
//		if (s.startsWith("dir")) {
//			imageView.setImageResource(R.drawable.folder);
//		} else {
//			imageView.setVisibility(4);
//		}

		return rowView;
	}
}