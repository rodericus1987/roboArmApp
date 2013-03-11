package com.example.roboarmapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsActivity extends Activity {
	
	private static final String[] items={"10Hz", "50Hz", "100Hz"};
	public static String prevIP, prevPort, prevPeriod;
	EditText ipAddress;
	EditText portText;
	EditText sendRateText;
	CheckBox vibrationCheck;
	CheckBox soundCheck;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		ipAddress = (EditText)findViewById(R.id.editIP);
		ipAddress.setRawInputType(InputType.TYPE_CLASS_NUMBER);
		portText = (EditText)findViewById(R.id.editPort);
		portText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
		sendRateText = (EditText)findViewById(R.id.editSendRate);
		sendRateText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
		
		vibrationCheck = (CheckBox)findViewById(R.id.vibration);
		vibrationCheck.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (MainActivity.doVibrate) {
					MainActivity.doVibrate = false;
					vibrationCheck.setChecked(false);
				} else {
					MainActivity.doVibrate = true;
					vibrationCheck.setChecked(true);
				}
			}
		});
		
		TextView vibrationText = (TextView)findViewById(R.id.vibration_text);
		vibrationText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (MainActivity.doVibrate) {
					MainActivity.doVibrate = false;
					vibrationCheck.setChecked(false);
				} else {
					MainActivity.doVibrate = true;
					vibrationCheck.setChecked(true);
				}
			}
		});
		
		soundCheck = (CheckBox)findViewById(R.id.sound);
		soundCheck.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (MainActivity.doSound) {
					MainActivity.doSound = false;
					soundCheck.setChecked(false);
				} else {
					MainActivity.doSound = true;
					soundCheck.setChecked(true);
				}
			}
		});
		
		TextView soundText = (TextView)findViewById(R.id.sound_text);
		soundText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (MainActivity.doSound) {
					MainActivity.doSound = false;
					soundCheck.setChecked(false);
				} else {
					MainActivity.doSound = true;
					soundCheck.setChecked(true);
				}
			}
		});
		
		prevIP = MainActivity.serverIP;
		prevPort = MainActivity.serverPort;
		prevPeriod = MainActivity.period;
		
		// Show the Up button in the action bar.
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		Spinner spin=(Spinner)findViewById(R.id.accelHZ);
		//spin.setOnItemSelectedListener(this);
		ArrayAdapter<String> aa=new ArrayAdapter<String>(this, R.layout.simple_spinner_item,items);
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(aa);
	}
	
	@Override
	public void onResume() {
		ipAddress.setText(MainActivity.serverIP);
		portText.setText(MainActivity.serverPort);
		sendRateText.setText(MainActivity.period);
		if (MainActivity.doVibrate) {
			vibrationCheck.setChecked(true);
		} else {
			vibrationCheck.setChecked(false);
		}
		if (MainActivity.doSound) {
			soundCheck.setChecked(true);
		} else {
			soundCheck.setChecked(false);
		}
		super.onResume();
	}
	
	@Override
	public void onPause() {
		File fileTest = getFileStreamPath("settings.txt");
		if (fileTest.exists()) {
			fileTest.delete();
		}
		try {
			FileOutputStream out = openFileOutput("settings.txt", Context.MODE_PRIVATE);
			int i;
			String entry = "";
			entry += MainActivity.serverIP;
			entry += "|";
			entry += MainActivity.serverPort;
			entry += "|";
			entry += MainActivity.period;
			entry += "|";
			if (MainActivity.doVibrate) {
				entry += "1";
			} else {
				entry += "0";
			}
			entry += "|";
			if (MainActivity.doSound) {
				entry += "1";
			} else {
				entry += "0";
			}
			out.write(entry.getBytes());
			out.getFD().sync();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onPause();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if ((keyCode == KeyEvent.KEYCODE_BACK))
	    {
	    	updateSettings();
	        finish();
	    }
	    return super.onKeyDown(keyCode, event);
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
			updateSettings();
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void updateSettings() {
		MainActivity.serverIP = ipAddress.getText().toString().trim();
		MainActivity.serverPort = portText.getText().toString().trim();
		MainActivity.period = sendRateText.getText().toString().trim();
		
		if (MainActivity.serverIP.equals("")) {
			MainActivity.serverIP = prevIP;
		}
		
		if (MainActivity.serverPort.equals("")) {
			MainActivity.serverPort = prevPort;
		}
		
		if (MainActivity.period.equals("")) {
			MainActivity.period = prevPeriod;
		}
		
		if ((!prevIP.equals(MainActivity.serverIP)) || (!prevPort.equals(MainActivity.serverPort))) {
			MainActivity.serverSettingsChanged = true;
		}
	}
}
