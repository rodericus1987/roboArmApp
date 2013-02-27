package com.example.roboarmapp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

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
	public static String serverIP = "192.168.43.89";
	public static String serverPort = "4012";
	public static Socket mySocket;
	public static OutputStream out;
	public static boolean socketConnected = false;
	
	public static boolean aboutToLock;
	
	private final int period = 1000; // # of ms between tcp/ip data send

	// Sensors
	private SensorManager mSensorManager;
	private long lastMeasurement1, lastMeasurement2;
	private Sensor mAccelerometer;
	private Sensor mOrientation;
	private Sensor mGyroscope;
	private boolean reference;
	private static final float NS2S = 1.0f / 1000000000.0f;
	public static float[] prevSentDataVector;
	public static float rollAngle;
	public static float pitchAngle;
	private float referenceRoll;
	private float referencePitch;
	public static float grip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		prevSentDataVector = new float[6];
		for (int i = 0; i < 0; i++) {
			prevSentDataVector[i] = 0.0f;
		}
		
		// set default settings
		//serverIP = "192.168.0.10";
		//serverPort = "4012";
		//xAxisLocked = false;
		//yAxisLocked = false;
		//zAxisLocked = false;
		//rollLocked = false;
		//pitchLocked = false;
		//socketConnected = false;
		
		doSendTimerTask myTask = new doSendTimerTask();
        Timer myTimer = new Timer();
        myTimer.schedule(myTask, period, period);

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
				grip = (float) progress;
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				Log.d("CHECK:", "Current progress is " + grip);
				//float[] outFloatData = { rollAngle, pitchAngle, 0, 0, 0, grip };
				//doSend(outFloatData);
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
		aboutToLock = true;
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
		mSensorManager.registerListener(this, mGyroscope,
				SensorManager.SENSOR_DELAY_NORMAL);

		lastMeasurement1 = System.nanoTime();
		lastMeasurement2 = System.nanoTime();
		reference = true;
		//rollAngle = 0;
		//pitchAngle = 0;
	}

	public void stopSensors() {
		mSensorManager.unregisterListener(this);
	}

	/*public void doSend(float[] outFloatData) {
		for (int i = 0; i < outFloatData.length; i++) {
			int data = Float.floatToRawIntBits(outFloatData[i]);
			byte outByteData[] = new byte[4];
			outByteData[3] = (byte) (data >> 24);
			outByteData[2] = (byte) (data >> 16);
			outByteData[1] = (byte) (data >> 8);
			outByteData[0] = (byte) (data);
			try {
				out.write(outByteData);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

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
		aboutToLock = false;
		Button myMainButton = (Button) findViewById(R.id.startStopButton);
		if (serverIP == null) {
			myMainButton.setText(R.string.no_ip);
		} else if (!socketConnected){
			myMainButton.setBackgroundColor(Color.YELLOW);
			myMainButton.setText(R.string.connecting);
			myMainButton.setText(R.string.connecting);
			new ConnectToServer().execute();
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		try {
			if ((socketConnected) && (!aboutToLock)) {
				socketConnected = false;
				out.flush();
				out.close();
				mySocket.close();
			}
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
				socketConnected = true;
				/*
				 * out.write(1); ObjectOutputStream out = new
				 * ObjectOutputStream(mySocket.getOutputStream()); DataPacket
				 * packetToServer = new DataPacket(); packetToServer.x =
				 * (float)1.1; packetToServer.y = (float)2.2; packetToServer.z =
				 * (float)3.3; out.writeObject(packetToServer); out.flush();
				 * out.close();
				 */
				Log.d("DEBUG: ", "Tried Connection");
				// mySocket.close();
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
			float roll = event.values[1]; // around axis y
			float pitch = event.values[0]; // around axis x
			long timeInterval = event.timestamp - lastMeasurement2;
			lastMeasurement2 = event.timestamp;
			// Assume the device has been turning with same speed for the whole
			// interval
			if (!rollLocked) {
				roll = (float) (roll * timeInterval * NS2S);
				rollAngle = rollAngle + roll;
			}
			
			if (!pitchLocked) {
				pitch = (float) (pitch * timeInterval * NS2S);
				pitchAngle = pitchAngle + pitch;
			}
			/*Log.d("DEBUG: ", "The current roll value is " + rollAngle * 360 / 2
					/ Math.PI);*/
			//float[] outFloatData = { rollAngle, pitchAngle, 0, 0, 0, grip };
			//doSend(outFloatData);
		}

		if (event.sensor.equals(mOrientation)) {
			float roll = event.values[2];
			float pitch = event.values[1];
			if (pitch > 90) {
				if (roll > 0)
					roll = 180 - roll;
				else 
					roll = -180 - roll;
			}
			
			if (reference) {
				referenceRoll = roll;
				referencePitch = pitch;
				reference = false;
			}
			
			float tempRoll = (float) (referenceRoll + rollAngle * 360 / 2 / Math.PI);
			float tempPitch = (float) (referencePitch + pitchAngle * 360 / 2 / Math.PI);
			while (tempRoll > 180) {
				tempRoll = tempRoll - 360;
			}
			while (tempRoll < -180) {
				tempRoll = tempRoll + 360;
			}
			while (tempPitch > 180) {
				tempPitch = tempPitch - 360;
			}
			while (tempPitch < -180) {
				tempPitch = tempPitch + 360;
			}
			
			float diffRoll = roll - tempRoll;
			float diffPitch = pitch - tempPitch;
			if (diffRoll > 180) {
				diffRoll = diffRoll - 360;
			}
			else if (diffRoll < -180) {
				diffRoll = diffRoll + 360;
			}
			if (diffPitch > 180) {
				diffPitch = diffPitch - 360;
			}
			else if (diffPitch < -180) {
				diffPitch = diffPitch + 360;
			}
			Log.d("CHECK:", "The current roll is " + tempRoll + "; The expected roll is " + roll);
			//Log.d("CHECK:", "The current roll is " + (rollAngle * 360 / 2 / Math.PI) + "; The expected roll is " + (diffPitch + rollAngle * 360 / 2 / Math.PI));
		}

	}
}


class doSendTimerTask extends TimerTask {
	public void run() {
		if (MainActivity.socketConnected) {
			float[] outFloatData = { MainActivity.rollAngle, MainActivity.pitchAngle, 0, 0, 0, MainActivity.grip };
			boolean dataHasChanged = false;
			for (int i = 0; i < outFloatData.length; i++) {
				if (outFloatData[i] != MainActivity.prevSentDataVector[i]) {
					dataHasChanged = true;
					break;
				}
			}
			if (dataHasChanged) {
				for (int i = 0; i < outFloatData.length; i++) {
					MainActivity.prevSentDataVector[i] = outFloatData[i];
					int data = Float.floatToRawIntBits(outFloatData[i]);
					byte outByteData[] = new byte[4];
					outByteData[3] = (byte) (data >> 24);
					outByteData[2] = (byte) (data >> 16);
					outByteData[1] = (byte) (data >> 8);
					outByteData[0] = (byte) (data);
					try {
						MainActivity.out.write(outByteData);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					MainActivity.out.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}