package sphero.knockout.controller;

import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.view.calibration.CalibrationView;
import orbotix.view.calibration.widgets.ControllerActivity;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;
import robot.widget.joystick.JoystickView;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
//import orbotix.robot.app.ColorPickerActivity;
//import orbotix.robot.widgets.CalibrationImageButtonView;
//import orbotix.robot.widgets.NoSpheroConnectedView;
//import orbotix.robot.widgets.NoSpheroConnectedView.OnConnectButtonClickListener;
//import orbotix.robot.widgets.SlideToSleepView;

//import android.app.Activity;
//import android.view.Menu;

public class Controller extends ControllerActivity {
	
	 /**
     * ID to start the StartupActivity for result to connect the Robot
     */
    private final static int  STARTUP_ACTIVITY            = 0;
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
	
	/*
	 * Sphero connection view
	 * */
	private SpheroConnectionView mSpheroConnectionView;
	
	//Default color
	private int mRed = 0xff;
	private int mGreen= 0xff;
	private int mBlue = 0xff;
	/*Called when activity is created*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_controller);
		
		//add the Joystick View as a controller
		addController((JoystickView)findViewById(R.id.joystick));
		
		 
        // Set up the Sphero Connection View
        mSpheroConnectionView = (SpheroConnectionView)findViewById(R.id.sphero_connection_view);
        mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() {
			
			@Override
			public void onRobotConnectionFailed(Robot arg0) {}
			
			@Override
			public void onRobotConnected(Robot arg0) {
				// Set Robot
				mRobot = arg0;
                //Set connected Robot to the Controllers
                setRobot(mRobot);
                
                // Make sure you let the calibration view knows the robot it should control
                mCalibrationView.setRobot(mRobot);
                
                // Make connect sphero pop-up invisible if it was previously up
//                mNoSpheroConnectedView.setVisibility(View.GONE);
//                mNoSpheroConnectedView.switchToConnectButton();
                // Hide Connection View since we only want to connect to one robot
                mSpheroConnectionView.setVisibility(View.GONE);
			}
			
			@Override
			public void onNonePaired() {
				mSpheroConnectionView.setVisibility(View.GONE);
//				mNoSpheroConnectedView.switchToSettingsButton();
//				mNoSpheroConnectedView.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onBluetoothNotEnabled() {
				// Bluetooth isn't enabled, so we show activity to enable bluetooth in settings
	            Intent i = RobotProvider.getDefaultProvider().getAdapterIntent();
	            startActivityForResult(i, BLUETOOTH_ENABLE_REQUEST);
			}
		});
	}

	
	 @Override
	protected void onResume() {
		
		super.onResume();
		mSpheroConnectionView.showSpheros();
	}

	 @Override
	 protected void onPause(){
		 super.onPause();
		 RobotProvider.getDefaultProvider().disconnectControlledRobots();
	 }

	@Override
	    public boolean dispatchTouchEvent(MotionEvent event) {
	    	mCalibrationView.interpretMotionEvent(event);
	    	//mSlideToSleepView.interpretMotionEvent(event);
	    	return super.dispatchTouchEvent(event);
	    }

}
