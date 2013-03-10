package com.example.roboarmapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.ImageView;

public class LockAxis extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock_axis);
		// Show the Up button in the action bar.
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		CheckBox xBox = (CheckBox)findViewById(R.id.lockXAxis);
		CheckBox yBox = (CheckBox)findViewById(R.id.lockYAxis);
		CheckBox zBox = (CheckBox)findViewById(R.id.lockZAxis);
		CheckBox rollBox = (CheckBox)findViewById(R.id.lockRoll);
		CheckBox pitchBox = (CheckBox)findViewById(R.id.lockPitch);
		
		ImageView rollImg = (ImageView)findViewById(R.id.roll);
		ImageView pitchImg = (ImageView)findViewById(R.id.pitch);
		
		rollImg.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
					case MotionEvent.ACTION_UP: {
						CheckBox rollBox = (CheckBox)findViewById(R.id.lockRoll);
						if (!rollBox.isChecked()) {
							MainActivity.rollLocked = true;
							rollBox.setChecked(true);
						} else {
							MainActivity.rollLocked = false;
							rollBox.setChecked(false);
						}
					}
				}
				return true;
			}
		});
		
		pitchImg.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
					case MotionEvent.ACTION_UP: {
						CheckBox pitchBox = (CheckBox)findViewById(R.id.lockPitch);
						if (!pitchBox.isChecked()) {
							MainActivity.pitchLocked = true;
							pitchBox.setChecked(true);
						} else {
							MainActivity.pitchLocked = false;
							pitchBox.setChecked(false);
						}
					}
				}
				return true;
			}
		});

		
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
	}*/

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
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/** Called when the user clicks the Done button */
    public void doneButton(View view) {
    	finish();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
