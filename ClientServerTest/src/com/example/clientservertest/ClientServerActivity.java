package com.example.clientservertest;

import orbotix.robot.base.CollisionDetectedAsyncData;
import orbotix.robot.base.CollisionDetectedAsyncData.CollisionPower;
import orbotix.robot.base.ConfigureCollisionDetectionCommand;
import orbotix.robot.base.DeviceAsyncData;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.CollisionDetectedAsyncData.CollisionPower;
import orbotix.robot.base.DeviceMessenger;
import orbotix.robot.base.DeviceMessenger.AsyncDataListener;
import orbotix.robot.base.Robot;
import orbotix.robot.sensor.Acceleration;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.ChildEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


public class ClientServerActivity extends Activity {

	TextView mainHP;
	TextView enemyHP;
	Firebase server;
	Firebase enemy;
	
	private static final String FIREBASE_URL = "https://spheroknockout-123.firebaseio.com";
	private Robot mRobot;
	
	private Handler mHandler = new Handler();

	
	private static int TURN = 0;
	
	private SpheroConnectionView mSpheroConnectionView;
	
	private ValueEventListener connectedListener;
	private ChildEventListener childListener;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client_server);
		
		mainHP = (TextView)findViewById(R.id.your_pts);
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
				
			}
			
		});
		
		//mainHP.setText("server1223");
		
//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		
//		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
//		server.addChildEventListener(new ChildEventListener(){
//
//			@Override
//			public void onCancelled(FirebaseError arg0) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void onChildAdded(DataSnapshot arg0, String arg1) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void onChildChanged(DataSnapshot arg0, String arg1) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void onChildMoved(DataSnapshot arg0, String arg1) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void onChildRemoved(DataSnapshot arg0) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//		});
//	
		mSpheroConnectionView = (SpheroConnectionView)findViewById(R.id.sphero_connection_view);
		
		mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener(){

			@Override
			public void onBluetoothNotEnabled() {
				Toast.makeText(ClientServerActivity.this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
				
			}

			@Override
			public void onNonePaired() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onRobotConnected(Robot robot) {
				mRobot= robot;
				//hide the connection view
				mSpheroConnectionView.setVisibility(View.GONE);
				
				//calling configure collision detection command right after the robot connects, will not work
				//you need to wait a sec for the robot to initilize 
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
			public void onRobotConnectionFailed(Robot arg0) {
				
			}
			
		});
		
	}
//	@Override
//	public void onStart(){
//		super.onStart();
//		//mSpheroConnectionView.setVisibility(View.GONE);
//		connectedListener = server.getRoot().child("info/connected").addValueEventListener(new ValueEventListener(){
//
//			@Override
//			public void onCancelled(FirebaseError arg0) {
//				// No operation here
//				
//			}
//
//			@Override
//			public void onDataChange(DataSnapshot arg0) {
//				boolean connected = (Boolean) arg0.getValue();
//				if(connected){
//					Toast.makeText(ClientServerActivity.this, "Connected to game", Toast.LENGTH_SHORT).show();
//				}
//				else{
//					Toast.makeText(ClientServerActivity.this, "Disconnected from game", Toast.LENGTH_SHORT).show();
//				}
//				
//			}
//			
//		});
//	}
//	
	 /**
     * Called when the user comes back to this app
     */
    @Override
    protected void onResume() {
    	super.onResume();
        // Refresh list of Spheros
        mSpheroConnectionView.showSpheros();

//      		connectedListener = server.getRoot().child("info/connected").addValueEventListener(new ValueEventListener(){
//
//      			@Override
//      			public void onCancelled(FirebaseError arg0) {
//      				// No operation here
//      				
//      			}
//
//      			@Override
//      			public void onDataChange(DataSnapshot arg0) {
//      				boolean connected = (Boolean) arg0.getValue();
//      				if(connected){
//      					Toast.makeText(ClientServerActivity.this, "Connected to game", Toast.LENGTH_SHORT).show();
//      				}
//      				else{
//      					Toast.makeText(ClientServerActivity.this, "Disconnected from game", Toast.LENGTH_SHORT).show();
//      				}
//      				
//      			}
//      			
//      		});
    }
    
    
    /**
     * Called when the user presses the back or home button
     */
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

	private final AsyncDataListener mCollisionListener = new AsyncDataListener() {

		public void onDataReceived(DeviceAsyncData asyncData) {
			if (asyncData instanceof CollisionDetectedAsyncData) {
				final CollisionDetectedAsyncData collisionData = (CollisionDetectedAsyncData) asyncData;

				// Update the UI with the collision data
				Acceleration acceleration = collisionData.getImpactAcceleration();
//				mAccelXValueLabel = (TextView) findViewById(R.id.accel_x_value);
//				mAccelXValueLabel.setText("" + acceleration.x);
//
//				mAccelYValueLabel = (TextView) findViewById(R.id.accel_y_value);
//				mAccelYValueLabel.setText("" + acceleration.y);
//
//				mAccelZValueLabel = (TextView) findViewById(R.id.accel_z_value);
//				mAccelZValueLabel.setText("" + acceleration.z);
//
//				mXAxisCheckBox = (CheckBox) findViewById(R.id.axis_x_checkbox);
//				mXAxisCheckBox.setChecked(collisionData.hasImpactXAxis());
//
//				mYAxisCheckBox = (CheckBox) findViewById(R.id.axis_y_checkbox);
//				mYAxisCheckBox.setChecked(collisionData.hasImpactYAxis());

				CollisionPower power = collisionData.getImpactPower();
				
				if(power.x >40 || power.y >40){
					sendMessage();
				}
//				mPowerXValueLabel = (TextView) findViewById(R.id.power_x_value);
//				mPowerXValueLabel.setText("" + power.x);
//
//				
//				mPowerYValueLabel = (TextView) findViewById(R.id.power_y_value);
//				mPowerYValueLabel.setText("" + power.y);
//
//				mSpeedValueLabel = (TextView) findViewById(R.id.speed_value);
//				mSpeedValueLabel.setText("" + collisionData.getImpactSpeed());
//
//				mTimestampLabel = (TextView) findViewById(R.id.time_stamp_value);
//				mTimestampLabel.setText(collisionData.getImpactTimeStamp() + " ms");
			}
		}
	};
	
	private void checkTurn(){
		
	}
	public void sendMessage(){
		String x = (String) enemyHP.getText();
		int hp = Integer.parseInt(x) -10;
		TURN = 1;
		//enemy.setValue(hp);
		Log.d("sendMessage",x);
		
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
