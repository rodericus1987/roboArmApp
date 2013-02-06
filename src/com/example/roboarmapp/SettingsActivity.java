package com.example.roboarmapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class SettingsActivity extends Activity {
	
	private static final String[] items={"10Hz", "50Hz", "100Hz"};
	EditText ipAddress;
	EditText portText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		ipAddress = (EditText)findViewById(R.id.editIP);
		ipAddress.setText(MainActivity.serverIP);
		portText = (EditText)findViewById(R.id.editPort);
		portText.setText(MainActivity.serverPort);
		Spinner spin=(Spinner)findViewById(R.id.accelHZ);
		//spin.setOnItemSelectedListener(this);
		ArrayAdapter<String> aa=new ArrayAdapter<String>(this, R.layout.simple_spinner_item,items);
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(aa);
	}
	
	@Override
	public void onPause() {
		MainActivity.serverIP = ipAddress.getText().toString();
		MainActivity.serverPort = portText.getText().toString();
		super.onPause();
	}
}
