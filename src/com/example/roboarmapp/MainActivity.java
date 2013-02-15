package com.example.roboarmapp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity implements SensorEventListener {

	private static boolean buttonIsDown;
	public static boolean xAxisLocked = false;
	public static boolean yAxisLocked = false;
	public static boolean zAxisLocked = false;
	public static boolean rollLocked = false;
	public static boolean pitchLocked = false;
	public static String serverIP = "192.168.43.139";
	public static String serverPort = "4012";
	public static Socket mySocket;
	public static OutputStream out;

	// Sensors
	private SensorManager mSensorManager;
	private long lastMeasurement1, lastMeasurement2;
	private Sensor mAccelerometer;
	private Sensor mOrientation;
	private Sensor mGyroscope;
	private boolean reference;
	private float referenceRoll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button myMainButton = (Button) findViewById(R.id.startStopButton);
		myMainButton.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					buttonIsDown = true;
					SeekBar gripperBar = (SeekBar) findViewById(R.id.gripperBar);
					gripperBar.setEnabled(false);
					Button homeButton = (Button) findViewById(R.id.homeButton);
					Button lockButton = (Button) findViewById(R.id.lockButton);
					homeButton.setEnabled(false);
					lockButton.setEnabled(false);
					startSensors();
					moveRobotArm(System.currentTimeMillis());
					return true;
				}

				case MotionEvent.ACTION_UP: {
					buttonIsDown = false;
					SeekBar gripperBar = (SeekBar) findViewById(R.id.gripperBar);
					gripperBar.setEnabled(true);
					Button homeButton = (Button) findViewById(R.id.homeButton);
					Button lockButton = (Button) findViewById(R.id.lockButton);
					homeButton.setEnabled(true);
					lockButton.setEnabled(true);
					stopSensors();
					resetMainButton();
					return true;
				}

				default:
					return false;
				}
			}
		});

		Button homeButton = (Button) findViewById(R.id.homeButton);
		homeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == Dialog.BUTTON_POSITIVE) {
							// TODO: HOME ROBOT POSITION
						}
					}
				};

				new AlertDialog.Builder(v.getContext())
						.setMessage(R.string.home_dialog_message)
						.setPositiveButton(R.string.proceed, listener)
						.setNegativeButton(R.string.cancel, listener)
						.setTitle(R.string.home_dialog_title).show();
			}
		});

		SeekBar gripperBar = (SeekBar) findViewById(R.id.gripperBar);
		gripperBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar arg0, int progress,
					boolean arg2) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void moveRobotArm(long pressTime) {
		Button myMainButton = (Button) findViewById(R.id.startStopButton);
		// wait two seconds before starting
		/*
		 * while ((System.currentTimeMillis() - pressTime) < 2000) { // DO
		 * NOTHING }
		 */
		myMainButton.setBackgroundColor(Color.RED);
		myMainButton.setText(R.string.release_to_stop);
		/*
		 * while (buttonIsDown) { // DO MAIN ARM MOVEMENTS }
		 * myMainButton.setBackgroundColor(Color.GREEN);
		 */
		// myMainButton.setText("@string/start_stop");
	}

	private void resetMainButton() {
		Button myMainButton = (Button) findViewById(R.id.startStopButton);
		myMainButton.setBackgroundColor(Color.GREEN);
		myMainButton.setText(R.string.start_stop);
	}

	/** Called when the user clicks the Lock Axis button */
	public void lockAxis(View view) {
		Intent intent = new Intent(this, LockAxis.class);
		startActivity(intent);
	}

	@SuppressWarnings("deprecation")
	public void startSensors() {
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mOrientation,
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);

		lastMeasurement1 = System.nanoTime();
		lastMeasurement2 = System.nanoTime();
		reference = true;
	}

	public void stopSensors() {
		mSensorManager.unregisterListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
		return true;
	}

	@Override
	public void onResume() {
		Button myMainButton = (Button) findViewById(R.id.startStopButton);
		myMainButton.setBackgroundColor(Color.YELLOW);
		myMainButton.setText(R.string.connecting);
		if (serverIP == null) {
			myMainButton.setText(R.string.no_ip);
		} else {
			myMainButton.setText(R.string.connecting);
			new ConnectToServer().execute();
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		try {
			out.flush();
			out.close();
			mySocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onPause();
	}

	public class ConnectToServer extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {
			Log.d("DEBUG: ", "Before connection");

			try {
				mySocket = new Socket(serverIP, Integer.parseInt(serverPort));
				out = mySocket.getOutputStream();
				/*out.write(1);
				ObjectOutputStream out = new ObjectOutputStream(mySocket.getOutputStream());
				DataPacket packetToServer = new DataPacket();
				packetToServer.x = (float)1.1;
				packetToServer.y = (float)2.2;
				packetToServer.z = (float)3.3;
				out.writeObject(packetToServer);
				out.flush();
				out.close();*/
				Log.d("DEBUG: ", "Tried Connection");
				//mySocket.close();
			} catch (IOException e) {
				Log.e("ERR: ", e.getMessage());
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Button myMainButton = (Button) findViewById(R.id.startStopButton);
			if (!mySocket.isConnected()) {
				myMainButton.setText(R.string.connect_error);
			} else {
				myMainButton.setBackgroundColor(Color.GREEN);
				myMainButton.setText(R.string.start_stop);
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.equals(mGyroscope)) {
			float roll = event.values[1];   // around axis y
			long timeInterval = event.timestamp - lastMeasurement2;
			lastMeasurement2 = event.timestamp;
			// Assume the device has been turning with same speed for the whole interval
			roll = (float) ((roll * (timeInterval / 1000000) / 1000) * 360 / Math.PI);
			Log.d("DEBUG: ", "The current roll value is " + roll);
			int rollData = Float.floatToRawIntBits(roll);
			byte outFloatData [] = new byte[4];
			outFloatData[3] = (byte)(rollData >> 24);
			outFloatData[2] = (byte)(rollData >> 16);
			outFloatData[1] = (byte)(rollData >> 8);
			outFloatData[0] = (byte)(rollData);
			try {
				out.write(outFloatData);
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/*
		if (event.sensor.equals(mOrientation)) {
			if (reference) {
				referenceRoll = event.values[2];
				reference = false;
			}
			float roll = event.values[2] - referenceRoll;
			Log.d("DEBUG: ", "The current roll value is " + roll);
			int rollData = Float.floatToRawIntBits(roll);
			byte outFloatData [] = new byte [4];
			outFloatData[3] = (byte)(rollData >> 24);
			outFloatData[2] = (byte)(rollData >> 16);
			outFloatData[1] = (byte)(rollData >> 8);
			outFloatData[0] = (byte)(rollData);
			try {
				out.write(outFloatData);
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
	}
}
