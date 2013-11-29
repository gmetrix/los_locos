package sphero.knockout.controller;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import orbotix.robot.base.CollisionDetectedAsyncData;
import orbotix.robot.base.ConfigureCollisionDetectionCommand;
import orbotix.robot.base.DeviceAsyncData;
import orbotix.robot.base.DeviceMessenger;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.CollisionDetectedAsyncData.CollisionPower;
import orbotix.robot.base.DeviceMessenger.AsyncDataListener;
import orbotix.robot.base.RobotProvider.OnRobotDisconnectedListener;
import orbotix.robot.base.SleepCommand;
import orbotix.robot.sensor.Acceleration;
//import orbotix.robot.widgets.CalibrationImageButtonView;
//import orbotix.robot.widgets.NoSpheroConnectedView;
//import orbotix.robot.widgets.NoSpheroConnectedView.OnConnectButtonClickListener;
//import orbotix.robot.widgets.SlideToSleepView;
import robot.widget.joystick.JoystickView;
import orbotix.view.calibration.CalibrationView;
import orbotix.view.calibration.widgets.ControllerActivity;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;

public class Controller extends ControllerActivity {
	
	 /**
     * ID to start the StartupActivity for result to connect the Robot
     */
    private final static int  STARTUP_ACTIVITY = 0;
	private static final int  BLUETOOTH_ENABLE_REQUEST = 11;
	private static final int  BLUETOOTH_SETTINGS_REQUEST = 12;

	private final static int COLOR_PICKER_ACTIVITY = 1;
	private boolean mColorPickerShowing = false;
	
	/*
	 *Robot Control
	*/
	private Robot mRobot;
	/*
	 * One touch calibration
	 * */
	private CalibrationView mCalibrationView;
	
	private ProgressBar mProgressBar;
	/*
	 * Sphero connection view
	 * */
	private SpheroConnectionView mSpheroConnectionView;
	
	//Default color
	private int mRed = 0xff;
	private int mGreen= 0xff;
	private int mBlue = 0xff;
	
	TextView mainHP;
	TextView enemyHP;
	Firebase server;
	Firebase enemy;
	
	private static int MAX_PTS = 100;
	private static final String FIREBASE_URL = "https://spheroknockout-123.firebaseio.com";
	
	private Handler mHandler = new Handler();

	private Handler progressHandle = new Handler();
	private static int TURN = 0;
	
	private ValueEventListener connectedListener;
	private ChildEventListener childListener;
	/*Called when activity is created*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_controller);
		
		//add the Joystick View as a controller
		addController((JoystickView)findViewById(R.id.joystick));
		
		// Make sure you let the calibration view knows the robot it should control
//        mCalibrationView.setRobot(mRobot);
		mProgressBar = (ProgressBar) findViewById(R.id.enemybar);
		
		enemyHP = (TextView)findViewById(R.id.enemy_pts);
		
		server= new Firebase(FIREBASE_URL).child("Game123");
	
		enemy = new Firebase(FIREBASE_URL).child("Game123/Players/Player1");
		
		enemy.addValueEventListener(new ValueEventListener(){

			@Override
			public void onCancelled(FirebaseError arg0) {
				// nothing to do
				
			}

			@Override
			public void onDataChange(DataSnapshot arg0) {
				enemyHP.setText(arg0.getValue(String.class));
				int snap = Integer.parseInt(arg0.getValue(String.class));
				//int pts = Math.abs(Integer.parseInt(arg0.getValue(String.class))-mProgressBar.getProgress());
				int pts1 = -10;
				Log.d("testgetProgress","" +mProgressBar.getProgress());
				if(mProgressBar.getProgress()> snap){
					mProgressBar.incrementProgressBy(pts1);
				}
				else if(mProgressBar.getProgress() == 0){
					mProgressBar.incrementProgressBy(0);
				}
				else{
					mProgressBar.incrementProgressBy(snap);
				}
		        // Title progress is in range 0..10000
		        setProgress(100 * mProgressBar.getProgress());
			}
			
		});
	
        // Set up the Sphero Connection View
        mSpheroConnectionView = (SpheroConnectionView)findViewById(R.id.sphero_connection_view);
        mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() {
			
			@Override
			public void onRobotConnectionFailed(Robot arg0) {}
			
			@Override
			public void onRobotConnected(Robot robot) {
				// Set Robot
				mRobot = robot;
                //Set connected Robot to the Controllers
                setRobot(mRobot);
                
                // Make sure you let the calibration view knows the robot it should control
                //mCalibrationView.setRobot(mRobot);
                
                // Make connect sphero pop-up invisible if it was previously up
//                mNoSpheroConnectedView.setVisibility(View.GONE);
//                mNoSpheroConnectedView.switchToConnectButton();
                // Hide Connection View since we only want to connect to one robot
                mSpheroConnectionView.setVisibility(View.GONE);
                
                mHandler.postDelayed(new Runnable(){

					@Override
					public void run() {
						
						DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mCollisionListener);
						
						ConfigureCollisionDetectionCommand.sendCommand(mRobot, ConfigureCollisionDetectionCommand.DEFAULT_DETECTION_METHOD, 
								45, 45, 100, 100, 100);
						
					}
					
				}, 1000);
			}
			
			@Override
			public void onNonePaired() {
				//mSpheroConnectionView.setVisibility(View.GONE);
//				mNoSpheroConnectedView.switchToSettingsButton();
//				mNoSpheroConnectedView.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onBluetoothNotEnabled() {
				// Bluetooth isn't enabled, so we show activity to enable bluetooth in settings
	            Intent i = RobotProvider.getDefaultProvider().getAdapterIntent();
	            startActivityForResult(i, BLUETOOTH_ENABLE_REQUEST);
	            //Toast.makeText(Controller.this, "Bluetooth Not Enabled", Toast.LENGTH_LONG).show();
			}
		});
	}

	
	 @Override
	protected void onResume() {
		super.onResume();
		mSpheroConnectionView.showSpheros();
	}

	 @Override
	    protected void onPause() {
	    	super.onPause();
			// Remove async data listener
			DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, mCollisionListener);
	    	// Disconnect Robot properly
	    	RobotProvider.getDefaultProvider().disconnectControlledRobots();
	    }
		@Override 
		public void onStop(){
			super.onStop();
			//server.getRoot().child("info/connected").removeEventListener(connectedListener);
		}
		
	@Override
	    public boolean dispatchTouchEvent(MotionEvent event) {
	    	//mCalibrationView.interpretMotionEvent(event);
	    	//mSlideToSleepView.interpretMotionEvent(event);
	    	return super.dispatchTouchEvent(event);
	    }
	
	private final AsyncDataListener mCollisionListener = new AsyncDataListener() {

		public void onDataReceived(DeviceAsyncData asyncData) {
			if (asyncData instanceof CollisionDetectedAsyncData) {
				final CollisionDetectedAsyncData collisionData = (CollisionDetectedAsyncData) asyncData;

				// Update the UI with the collision data
				Acceleration acceleration = collisionData.getImpactAcceleration();

				CollisionPower power = collisionData.getImpactPower();
				
				if(power.x >40 || power.y >40){
					sendMessage();
				}

			}
		}
	};
	
	public void sendMessage(){
		String x = (String) enemyHP.getText();
		int hp = Integer.parseInt(x) -10;
		TURN = 1;
		//enemy.setValue(hp);
		Log.d("sendMessage",x);
//		mProgressBar.incrementProgressBy(-10);
//        // Title progress is in range 0..10000
//        setProgress(100 * mProgressBar.getProgress());
		
		enemy.setValue(hp, new Firebase.CompletionListener() {

			@Override
			public void onComplete(FirebaseError arg0, Firebase arg1) {
				if (arg0 != null) {
					Log.d("Data could not be saved: " + arg0.getMessage(), "hello");
				} else {
					Log.d("Data saved successfully.", "fail to attack");
				}
			}

		});
		enemyHP.setText(String.valueOf(hp));
	}

}
