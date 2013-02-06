package com.example.roboarmapp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity {
	
	private static boolean buttonIsDown;
	public static boolean xAxisLocked = false;
	public static boolean yAxisLocked = false;
	public static boolean zAxisLocked = false;
	public static boolean rollLocked = false;
	public static boolean pitchLocked = false;
	public static String serverIP = "142.1.216.65";
	public static String serverPort = "888";
	public static Socket mySocket;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button myMainButton = (Button)findViewById(R.id.startStopButton);
		myMainButton.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
				{
					buttonIsDown = true;
					SeekBar gripperBar = (SeekBar)findViewById(R.id.gripperBar);
					gripperBar.setEnabled(false);
					Button homeButton = (Button)findViewById(R.id.homeButton);
					Button lockButton = (Button)findViewById(R.id.lockButton);
					homeButton.setEnabled(false);
					lockButton.setEnabled(false);
					moveRobotArm(System.currentTimeMillis());
					return true;
				}

				case MotionEvent.ACTION_UP:
				{
					buttonIsDown = false;
					SeekBar gripperBar = (SeekBar)findViewById(R.id.gripperBar);
					gripperBar.setEnabled(true);
					Button homeButton = (Button)findViewById(R.id.homeButton);
					Button lockButton = (Button)findViewById(R.id.lockButton);
					homeButton.setEnabled(true);
					lockButton.setEnabled(true);
					resetMainButton();
					return true;
				}

				default:
					return false;
				}
			}
		});
		
		Button homeButton = (Button)findViewById(R.id.homeButton);
		homeButton.setOnClickListener(new OnClickListener(){
	        public void onClick(View v) {
	        	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
	                @Override
	                public void onClick(DialogInterface dialog, int which) {
	                    if(which == Dialog.BUTTON_POSITIVE)
	                    {
	                        // TODO: HOME ROBOT POSITION
	                    }
	                }
	            };

	            new AlertDialog.Builder(v.getContext())
	            .setMessage(R.string.home_dialog_message)
	            .setPositiveButton(R.string.proceed, listener)
	            .setNegativeButton(R.string.cancel, listener)
	            .setTitle(R.string.home_dialog_title)
	            .show();
	        }
	    });
		
		SeekBar gripperBar = (SeekBar)findViewById(R.id.gripperBar);
		gripperBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
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
		Button myMainButton = (Button)findViewById(R.id.startStopButton);
		// wait two seconds before starting
		/*while ((System.currentTimeMillis() - pressTime) < 2000) {
			// DO NOTHING
		}*/
		myMainButton.setBackgroundColor(Color.RED);
		myMainButton.setText(R.string.release_to_stop);
		/*while (buttonIsDown) {
			// DO MAIN ARM MOVEMENTS
		}
		myMainButton.setBackgroundColor(Color.GREEN);*/
		//myMainButton.setText("@string/start_stop");
	}
	
	private void resetMainButton() {
		Button myMainButton = (Button)findViewById(R.id.startStopButton);
		myMainButton.setBackgroundColor(Color.GREEN);
		myMainButton.setText(R.string.start_stop);
	}
	
	/** Called when the user clicks the Lock Axis button */
    public void lockAxis(View view) {
    	Intent intent = new Intent(this, LockAxis.class);
    	startActivity(intent);
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
    	Button myMainButton = (Button)findViewById(R.id.startStopButton);
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
    		int i = 0;
    		SocketAddress sockaddr = new InetSocketAddress(serverIP, Integer.parseInt(serverPort));
    		mySocket = new Socket();
    		do {
    			try {
    				//mySocket = new Socket(serverIP, Integer.parseInt(serverPort));
    				mySocket.connect(sockaddr, 5000); //connection timeout
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    			i++;
    		} while ((!mySocket.isConnected()) && (i < 2));
    		
    		return null;
    	}
    	
    	@Override
    	protected void onPostExecute(Void result) {
    		Button myMainButton = (Button)findViewById(R.id.startStopButton);
    		if (!mySocket.isConnected()) {
    			myMainButton.setText(R.string.connect_error);
    		} else {
	    		myMainButton.setBackgroundColor(Color.GREEN);
				myMainButton.setText(R.string.start_stop);
    		}
    	}
    }
}
