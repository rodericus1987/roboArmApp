package com.example.roboarmapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity implements SensorEventListener {

	public static boolean xAxisLocked = false;
	public static boolean yAxisLocked = false;
	public static boolean zAxisLocked = false;
	public static boolean rollLocked = false;
	public static boolean pitchLocked = false;
	public static String serverIP = "";
	public static String serverPort = "4012";
	public static Socket mySocket;
	public static OutputStream out;
	public static InputStream in;
	public static boolean socketConnected = false;
	public static Timer myTimer;
	public static boolean doVibrate = true;
	public static boolean doSound = true;

	public static boolean sensorsStarted = false;
	public static boolean reconnectButtonSet = false;
	public static boolean serverSettingsChanged = false;

	public static SeekBar gripperBar;
	public static Button myMainButton;

	public static String period = "1000"; // # of ms between tcp/ip data send
	private static Vibrator v1;

	// Sensors
	private SensorManager mSensorManager;
	private long lastMeasurement1, lastMeasurement2;
	private Sensor mAccelerometer, mOrientation, mGyroscope, mRotation;
	private boolean reference;
	private static final float NS2S = 1.0f / 1000000000.0f;
	public static float[] rotationVector;
	public static float[] rotationMatrix;
	public static float[] acceleration;
	public static float[] previous_speed;
	public static float[] speed;
	public static float[] displacement;
	public static float rollAngle = 0;
	public static float pitchAngle = 0;
	private float referenceRoll;
	private float referencePitch;
	public static float grip = 0.0f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		displacement = new float[3];
		for (int i = 0; i < 3; i++) {
			displacement[i] = 0.0f;
		}

		speed = new float[3];
		previous_speed = new float [3];
		for (int i = 0; i < 3; i++) {
			speed[i] = 0.0f;
			previous_speed[i] = 0.0f;
		}

		v1 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		gripperBar = (SeekBar) findViewById(R.id.gripperBar);
		myMainButton = (Button) findViewById(R.id.startStopButton);		

		// check for settings file
		File fileTest = getFileStreamPath("settings.txt");
		if (fileTest.exists()) {
			try {
				String entryDelims = "[|]";
				FileInputStream in = openFileInput("settings.txt");
				InputStreamReader inputStreamReader = new InputStreamReader(in);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String rawInput = bufferedReader.readLine();
				in.close();
				String[] entries = rawInput.split(entryDelims);
				serverIP = entries[0];
				serverPort = entries[1];
				period = entries[2];
				doVibrate = true;
				if (entries[3].equals("0")) {
					doVibrate = false;
				}
				doSound = true;
				if (entries[4].equals("0")) {
					doSound = false;
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Button homeButton = (Button) findViewById(R.id.homeButton);
		homeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == Dialog.BUTTON_POSITIVE) {
							rollAngle = 0;
							pitchAngle = 0;
							grip = 300; // home signal
							gripperBar.setProgress(0);
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

		gripperBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar arg0, int progress,
					boolean arg2) {
				if (grip <= 100) {
					grip = (float) progress;
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// Vibrate for 50 milliseconds
				if (doVibrate) {
					v1.vibrate(50);
				}
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				//Log.d("CHECK:", "Current progress is " + grip);
			}
		});

		final Handler handler = new Handler();
		final Runnable r = new Runnable()
		{
			public void run() 
			{
				checkConnectionStatus();
				handler.postDelayed(this, 500);
			}
		};
		handler.post(r);

		new readFromServer().execute();
	}

	private void setOnMainButton() {
		myMainButton.setBackgroundColor(Color.RED);
		myMainButton.setText(R.string.release_to_stop);
	}

	private void resetMainButton() {
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
		if (!sensorsStarted) {
			sensorsStarted = true;
			mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			mAccelerometer = mSensorManager
					.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
			mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
			mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_NORMAL);
			mSensorManager.registerListener(this, mOrientation,
					SensorManager.SENSOR_DELAY_NORMAL);
			mSensorManager.registerListener(this, mGyroscope,
					SensorManager.SENSOR_DELAY_NORMAL);
			mSensorManager.registerListener(this, mRotation,
					SensorManager.SENSOR_DELAY_NORMAL);

			rotationVector = new float [3];
			acceleration = new float [4];
			lastMeasurement1 = System.nanoTime();
			lastMeasurement2 = System.nanoTime();
			speed[0] = 0;
			speed[1] = 0;
			speed[2] = 0;
			reference = true;
			// rollAngle = 0;
			// pitchAngle = 0;
		}
	}

	public void stopSensors() {
		if (sensorsStarted) {
			sensorsStarted = false;
			mSensorManager.unregisterListener(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Disconnect").setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				grip = 200; // disconnect signal
				return false;
			}

		});
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		}
		return true;
	}

	@Override
	public void onResume() {

		if (serverSettingsChanged) {
			serverSettingsChanged = false;
			try {
				if (socketConnected) {
					socketConnected = false;
					out.flush();
					out.close();
					in.close();
					mySocket.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (serverIP.equals("")) {
			gripperBar.setEnabled(false);
			myMainButton.setBackgroundColor(Color.YELLOW);
			myMainButton.setText(R.string.no_ip);
			myMainButton.setOnTouchListener(null);
		} else if (!socketConnected) {
			gripperBar.setEnabled(false);
			myMainButton.setBackgroundColor(Color.YELLOW);
			myMainButton.setText(R.string.connecting);
			new ConnectToServer().execute();
		}

		myTimer = new Timer();
		doSendTimerTask myTask = new doSendTimerTask();
		myTimer.schedule(myTask, 0, Integer.parseInt(period));

		super.onResume();
	}

	@Override
	public void onPause() {
		myTimer.cancel();
		myTimer.purge();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		try {
			if (socketConnected) {
				socketConnected = false;
				out.flush();
				out.close();
				in.close();
				mySocket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onDestroy();
	}

	public class readFromServer extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {
			if (socketConnected) {
				try {
					int inVal = in.read();
					if (inVal != -1) {
						v1.vibrate(500);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			new readFromServer().execute();
		}
	}

	public class ConnectToServer extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {
			Log.d("DEBUG: ", "Before connection");

			try {
				mySocket = new Socket();
				mySocket.connect(new InetSocketAddress(serverIP, Integer.parseInt(serverPort)), 2000);
				out = mySocket.getOutputStream();
				in = mySocket.getInputStream();
				socketConnected = true;
				Log.d("DEBUG: ", "Tried Connection");
			} catch (IOException e) {
				Log.e("ERR: ", e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (!socketConnected) {
				gripperBar.setEnabled(false);
				if (serverIP.equals("")) {
					myMainButton.setBackgroundColor(Color.YELLOW);
					myMainButton.setText(R.string.no_ip);
					myMainButton.setOnTouchListener(null);
				} else {
					reconnectButtonSet = false;
				}
			} else {
				gripperBar.setEnabled(true);
				reconnectButtonSet = false;
				myMainButton.setBackgroundColor(Color.GREEN);
				myMainButton.setText(R.string.start_stop);

				myMainButton = (Button) findViewById(R.id.startStopButton);
				myMainButton.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {

						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN: {

							// beep
							if (doSound) {
								/*try {
							        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
							        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
							        r.play();
							    } catch (Exception e) {}*/
								MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.robot_blip);
								//mp.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
								//mp.setVolume(0.1f, 0.1f);
								mp.start();

							}

							// Vibrate for 50 milliseconds
							if (doVibrate) {
								v1.vibrate(50);
							}

							gripperBar.setEnabled(false);
							Button homeButton = (Button) findViewById(R.id.homeButton);
							Button lockButton = (Button) findViewById(R.id.lockButton);
							homeButton.setEnabled(false);
							lockButton.setEnabled(false);
							startSensors();
							setOnMainButton();

							return true;
						}

						case MotionEvent.ACTION_UP: {

							gripperBar.setEnabled(true);
							Button homeButton = (Button) findViewById(R.id.homeButton);
							Button lockButton = (Button) findViewById(R.id.lockButton);
							homeButton.setEnabled(true);
							lockButton.setEnabled(true);
							stopSensors();
							resetMainButton();
							v1.cancel();

							return true;
						}

						default:
							return false;
						}
					}
				});
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
			if (roll < 0.005 && roll > -0.005) {
				roll = 0;
			}
			if (pitch < 0.005 & pitch > -0.005) {
				pitch = 0;
			}
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

			Log.d("CHECK: ", "The current roll value is " + rollAngle * 360 /
					2 / Math.PI);

			// float[] outFloatData = { rollAngle, pitchAngle, 0, 0, 0, grip };
			// doSend(outFloatData);
		}

		if (event.sensor.equals(mAccelerometer)) {
			float [] rawLinear = { event.values[0], event.values[1],
					event.values[2], 0 };
			for (int i = 0; i<3; i++) {
				if (rawLinear[i] < 0.2 && rawLinear[i] > -0.2) {
					rawLinear[i] = 0;
				}
			}
			float [] temp = new float [16];
			rotationMatrix = new float [16];
			SensorManager.getRotationMatrixFromVector(temp, rotationVector);
			Matrix.invertM(rotationMatrix, 0, temp, 0);
			Matrix.multiplyMV(acceleration, 0, rotationMatrix, 0, rawLinear, 0);
			//Log.d("CHECK:", "x = " + acceleration[0] + "; y = " + acceleration[1] + "; z = " + acceleration[2]);
			long timeInterval = event.timestamp - lastMeasurement1;
			lastMeasurement1 = event.timestamp;

			if (!xAxisLocked) {
				speed[0] = (float) (acceleration[0] * timeInterval * NS2S) + speed[0];
				if (speed[0] > 0 && previous_speed[0] < 0) {
					speed[0] = 0;
				}
				else if (speed[0] < 0 && previous_speed[0] > 0) {
					speed[0] = 0;
				}
				displacement[0] = (float) (speed[0] * timeInterval * NS2S) + displacement[0];
			}

			if (!yAxisLocked) {
				speed[1] = (float) (acceleration[1] * timeInterval * NS2S) + speed[1];
				if (speed[1] > 0 && previous_speed[1] < 0) {
					speed[1] = 0;
				}
				else if (speed[1] < 0 && previous_speed[1] > 0) {
					speed[1] = 0;
				}
				displacement[1] = (float) (speed[1] * timeInterval * NS2S) + displacement[1];
			}

			if (!zAxisLocked) {
				speed[2] = (float) (acceleration[2] * timeInterval * NS2S) + speed[2];
				if (speed[2] > 0 && previous_speed[2] < 0) {
					speed[2] = 0;
				}
				else if (speed[2] < 0 && previous_speed[2] > 0) {
					speed[2] = 0;
				}
				displacement[2] = (float) (speed[2] * timeInterval * NS2S) + displacement[2];
				previous_speed = speed;
			}
			//Log.d("CHECK:", "x = " + displacement[0] + "; y = " + displacement[1] + "; z = " + displacement[2]);
		}

		if (event.sensor.equals(mRotation)) {
			rotationVector = event.values;
		}

	}

	protected void checkConnectionStatus() {
		runOnUiThread(new Runnable() 
		{
			public void run() 
			{
				if ((!socketConnected) && (!reconnectButtonSet) && (!serverIP.equals(""))) {
					stopSensors();
					gripperBar.setEnabled(false);
					myMainButton.setText(R.string.connect_error);
					myMainButton.setBackgroundColor(Color.YELLOW);
					myMainButton.setOnTouchListener(new OnTouchListener() {
						public boolean onTouch(View v, MotionEvent event) {
							switch (event.getAction()) {
							case MotionEvent.ACTION_UP: {
								// Vibrate for 50 milliseconds
								if (doVibrate) {
									v1.vibrate(50);
								}
								myMainButton.setText(R.string.connecting);
								new ConnectToServer().execute();
								myMainButton.setOnTouchListener(null);
							}}
							return true;
						}});
					reconnectButtonSet = true;
				}
			}
		});
	}
}

class doSendTimerTask extends TimerTask {
	public void run() {
		if (MainActivity.socketConnected) {
			boolean disconnectCase = false;
			boolean homeCase = false;
			if (MainActivity.grip == 200) {
				disconnectCase = true;
			} else if (MainActivity.grip == 300) {
				homeCase = true;
			}
			float[] outFloatData = { MainActivity.rollAngle, MainActivity.pitchAngle, MainActivity.displacement[0], MainActivity.displacement[1], MainActivity.displacement[2], MainActivity.grip };
			//Log.d("CHECK:", "x = " + MainActivity.displacement[0] + "; y = " + MainActivity.displacement[1] + "; z = " + MainActivity.displacement[2]);

			MainActivity.displacement[0] = 0.0f;
			MainActivity.displacement[1] = 0.0f;
			MainActivity.displacement[2] = 0.0f;
			//MainActivity.speed[0] = 0.0f;
			//MainActivity.speed[1] = 0.0f;
			//MainActivity.speed[2] = 0.0f;


			for (int i = 0; i < outFloatData.length; i++) {
				int data = Float.floatToRawIntBits(outFloatData[i]);
				byte outByteData[] = new byte[4];
				outByteData[3] = (byte) (data >> 24);
				outByteData[2] = (byte) (data >> 16);
				outByteData[1] = (byte) (data >> 8);
				outByteData[0] = (byte) (data);
				try {
					MainActivity.out.write(outByteData);
				} catch (IOException e) {
					MainActivity.socketConnected = false;
					try {
						MainActivity.out.close();
						MainActivity.in.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}
			try {
				MainActivity.out.flush();
				if (disconnectCase) { // disconnect case
					try {
						if (MainActivity.socketConnected) {
							MainActivity.socketConnected = false;
							MainActivity.out.close();
							MainActivity.in.close();
							MainActivity.mySocket.close();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					MainActivity.grip = MainActivity.gripperBar.getProgress();
				}
				if (homeCase) { // home case
					MainActivity.grip = MainActivity.gripperBar.getProgress();
				}
			} catch (IOException e) {
				MainActivity.socketConnected = false;
				try {
					MainActivity.out.close();
					MainActivity.in.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}
}