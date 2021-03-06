package com.harasoft.relaunch.Preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import com.harasoft.relaunch.R;
import com.harasoft.relaunch.ReLaunchApp;
import com.harasoft.relaunch.Support.Filters;
import com.harasoft.relaunch.Utils.UtilIcons;

public class FiltersActivity extends Activity {
	private String TAG = "Filters";
	private ReLaunchApp app;
	private FTArrayAdapter adapter;
	private ListView lv;
	private List<String[]> itemsArray = new ArrayList<>();
	private Filters filters;

	public class myOnItemSelectedListener implements OnItemSelectedListener {
		final int position;

		myOnItemSelectedListener(int pos) {
			position = pos;
		}

		public void onItemSelected(AdapterView<?> parent, View v1, int pos, long row) {
			if (position >= 0 && position < itemsArray.size()) {
				itemsArray.set(position, new String[] { Integer.toString(pos),itemsArray.get(position)[1] });
				adapter.notifyDataSetChanged();
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	class FTArrayAdapter extends ArrayAdapter<HashMap<String, String>> {
		private Context context;
		private LayoutInflater lInflater;

		FTArrayAdapter(Context context, int resource) {
			super(context, resource);
			this.context = context;
			lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return itemsArray.size();
		}

		@Override
		public View getView(final int position, View convertView,ViewGroup parent) {
			// Do not reuse convertView and create new view each time !!!
			// I know it is not so efficient, but I don't know how to prevent
			// spinner's value for different position in list be mixed
			// otherwise.
			// Anyway filters list can't be big, so I hope its OK.
			if (convertView == null) {
				convertView = lInflater.inflate(R.layout.item_filters, parent, false);
			}
            if (convertView == null){
                return null;
            }
			final String[] item = itemsArray.get(position);
			if (item == null) {
				return convertView;
			}

			Spinner methodSpn = (Spinner) convertView.findViewById(R.id.filters_method);
			ImageButton rmBtn = (ImageButton) convertView.findViewById(R.id.filters_delete);
			Button valBtn = (Button) convertView.findViewById(R.id.filters_type);
			TextView condTxt = (TextView) convertView.findViewById(R.id.filters_condition);

			// Set spinner
			Integer spos = 0;
			try {
				spos = Integer.parseInt(item[0]);
			} catch (NumberFormatException e) {
                //
			}

			ArrayAdapter<CharSequence> sadapter = ArrayAdapter.createFromResource(context, R.array.filter_values,
							android.R.layout.simple_spinner_item);
			sadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			methodSpn.setAdapter(sadapter);
			methodSpn.setSelection(spos, false);
			methodSpn.setOnItemSelectedListener(new myOnItemSelectedListener(position));

			// Set remove button
			rmBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					itemsArray.remove(position);
					adapter.notifyDataSetChanged();
				}
			});

			// Set value button
			if (spos == filters.FLT_SELECT) {
				valBtn.setText("");
				valBtn.setEnabled(false);
			}else if(spos == filters.FLT_NEW|| spos == filters.FLT_NEW_AND_READING){
                valBtn.setText("");
                valBtn.setEnabled(false);
                itemsArray.set(position,new String[] { item[1]," " });
                adapter.notifyDataSetChanged();
            }else{
				valBtn.setText(item[1]);
				valBtn.setEnabled(true);

				valBtn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						// "Filter value:"
						builder.setTitle(getResources().getString(R.string.jv_filters_value));
						final EditText input = new EditText(context);
						input.setText(item[1]);
						builder.setView(input);
						// "Ok"
						builder.setPositiveButton(getResources().getString(R.string.app_ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										String value = String.valueOf(input.getText());
										if (value.equals(""))
											// "Can't be empty!"
											Toast.makeText(context,getResources().getString(R.string.jv_filters_cant_be_empty),Toast.LENGTH_LONG).show();
										else {
											itemsArray.set(position,new String[] { item[0],value });
											adapter.notifyDataSetChanged();
											dialog.dismiss();
										}
									}
								});

						// "Cancel"
						builder.setNegativeButton(getResources().getString(R.string.app_cancel),new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
					}
				});
            }
			// Set condition text
			if (position >= itemsArray.size() - 1)
				condTxt.setText("");
			else if (filters.filters_and)
				// "AND"
				condTxt.setText(getResources().getString(
						R.string.app_and));
			else
				// "OR"
				condTxt.setText(getResources()
						.getString(R.string.jv_filters_or));

			return convertView;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Create global storage with values
		app = (ReLaunchApp) getApplicationContext();
        if(app == null ) {
            finish();
        }
		app.setOptionsWindowActivity(this);
		setContentView(R.layout.layout_filters);
		UtilIcons utilIcons = new UtilIcons(getBaseContext());
		lv = (ListView) findViewById(R.id.filters_lv);
		filters = new Filters(app.getBaseContext());

		itemsArray = filters.getList();
		adapter = new FTArrayAdapter(this, R.layout.item_filters);
		lv.setAdapter(adapter);

		// OK/Save button
		Button okBtn = (Button) findViewById(R.id.filters_ok);
		okBtn.setCompoundDrawablesWithIntrinsicBounds( new BitmapDrawable(getResources(), utilIcons.getIcon("OK")), null, null, null);
		okBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Save list
				filters.writeFile(itemsArray);

				// Save and/or flag
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("filtersAnd", filters.filters_and);
				editor.commit();

				setResult(Activity.RESULT_OK);
				finish();
			}
		});

		// Add new button
		Button addBtn = (Button) findViewById(R.id.filters_new);
		addBtn.setCompoundDrawablesWithIntrinsicBounds( new BitmapDrawable(getResources(), utilIcons.getIcon("ADD")), null, null, null);
		addBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				itemsArray.add(new String[] { "0", "" });
				adapter.notifyDataSetChanged();
			}
		});

		// AND/OR button
		final Button andorBtn = (Button) findViewById(R.id.filters_andor);
		if (filters.filters_and)
			// "OR"
			andorBtn.setText(getResources().getString(R.string.jv_filters_or));
		else
			// "AND"
			andorBtn.setText(getResources().getString(R.string.app_and));
		andorBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				filters.filters_and = !filters.filters_and;
				if (filters.filters_and)
					// "OR"
					andorBtn.setText(getResources().getString(R.string.jv_filters_or));
				else
					// "AND"
					andorBtn.setText(getResources().getString(R.string.app_and));
				adapter.notifyDataSetChanged();
			}
		});

		// Cancel button
		Button cancelBtn = (Button) findViewById(R.id.filters_cancel);
		cancelBtn.setCompoundDrawablesWithIntrinsicBounds( new BitmapDrawable(getResources(), utilIcons.getIcon("DELETE")), null, null, null);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});

		// Back button - work as cancel
		ImageButton backBtn = (ImageButton) findViewById(R.id.back_btn);
		backBtn.setImageBitmap(utilIcons.getIcon("EXIT"));
		backBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});
		// Icon
		ImageView search_icon = (ImageView) findViewById(R.id.filters_icon);
		search_icon.setImageBitmap(utilIcons.getIcon("SELNUMCOL"));
	}

	@Override
	protected void onResume() {
		super.onResume();
		app.generalOnResume(TAG);
	}
}
