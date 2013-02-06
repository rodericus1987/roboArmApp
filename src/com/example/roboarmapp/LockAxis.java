package com.example.roboarmapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

public class LockAxis extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock_axis);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		CheckBox xBox = (CheckBox)findViewById(R.id.lockXAxis);
		CheckBox yBox = (CheckBox)findViewById(R.id.lockYAxis);
		CheckBox zBox = (CheckBox)findViewById(R.id.lockZAxis);
		CheckBox rollBox = (CheckBox)findViewById(R.id.lockRoll);
		CheckBox pitchBox = (CheckBox)findViewById(R.id.lockPitch);
		if (MainActivity.xAxisLocked) {
			xBox.setChecked(true);
		}
		if (MainActivity.yAxisLocked) {
			yBox.setChecked(true);
		}
		if (MainActivity.zAxisLocked) {
			zBox.setChecked(true);
		}
		if (MainActivity.rollLocked) {
			rollBox.setChecked(true);
		}
		if (MainActivity.pitchLocked) {
			pitchBox.setChecked(true);
		}
		xBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					MainActivity.xAxisLocked = true;
				} else {
					MainActivity.xAxisLocked = false;
				}
			}
		});
		yBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					MainActivity.yAxisLocked = true;
				} else {
					MainActivity.yAxisLocked = false;
				}
			}
		});
		zBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					MainActivity.zAxisLocked = true;
				} else {
					MainActivity.zAxisLocked = false;
				}
			}
		});
		rollBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					MainActivity.rollLocked = true;
				} else {
					MainActivity.rollLocked = false;
				}
			}
		});
		pitchBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					MainActivity.pitchLocked = true;
				} else {
					MainActivity.pitchLocked = false;
				}
			}
		});
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_lock_axis, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/
	
	/** Called when the user clicks the Done button */
    public void doneButton(View view) {
    	finish();
    }

}
