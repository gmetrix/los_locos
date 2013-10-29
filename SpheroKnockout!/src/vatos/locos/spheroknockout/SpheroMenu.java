package vatos.locos.spheroknockout;



import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;
import orbotix.robot.base.*;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;


public class SpheroMenu extends Activity  {
	DrawView spheroPath;
	private SpheroConnectionView robotConnection;
	
	/*
	 * Robot that we are controlling
	 */
	private Robot mySphero = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_sphero_menu);
		
		//Find Sphero Connection View from the layout file
		robotConnection = (SpheroConnectionView) findViewById(R.id.sphero_connection_view);
		//This event listener will notify you when these events occur, it is up to you what you want to do during them
		robotConnection.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener(){
			
			@Override
			public void onRobotConnectionFailed(Robot arg1){
				
			}
			@Override
			public void onNonePaired(){}
			
			@Override
			public void onRobotConnected(Robot arg1){
				//set the robot
				mySphero = arg1;
				//Hide the connection view. Comment this code if you want to connect to multiple robots
				robotConnection.setVisibility(View.GONE);
				//set the AsyncDataListener that will process self level
				//DeviceMessenger.getInstance().addAsyncDataListener(mySphero, asyncDataListener)
				
			}
			@Override
			public void onBluetoothNotEnabled(){
				//See UISample Sample on how to show BT settings screen, for now just notify user
				Toast.makeText(SpheroMenu.this, "Bluetooth Not Enabled", Toast.LENGTH_LONG).show();
			}
		});
		
		spheroPath = (DrawView) findViewById(R.id.drawView1);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sphero_menu, menu);
		return true;
	}
	@Override
	protected void onResume(){
		super.onResume();
		robotConnection.showSpheros();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		RobotProvider.getDefaultProvider().disconnectControlledRobots();
	}
	
//	public void setPlayButton(){
//		playButton.setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//				Intent i = new Intent("vatos.locos.spheroknockout.ATTACKSPHERO");
//				startActivity(i);
//				
//			}
//			
//		});
//		
//	}

//	@Override
//	public boolean onTouch(View arg0, MotionEvent arg1) {
//		// TODO Auto-generated method stub
//		return false;
//	}
	//@Override
//    protected void onPause() {
//        super.onPause();
//        // Pause the game along with the activity
//        mSnakeView.setMode(SnakeView.PAUSE);
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        // Store the game state
//        outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
//    }
}
