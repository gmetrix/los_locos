package sphero.knockout;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import orbotix.robot.base.BackLEDOutputCommand;
import orbotix.robot.base.CalibrateCommand;
import orbotix.robot.base.CollisionDetectedAsyncData;
import orbotix.robot.base.ConfigureCollisionDetectionCommand;
import orbotix.robot.base.DeviceAsyncData;
import orbotix.robot.base.DeviceMessenger;
import orbotix.robot.base.FrontLEDOutputCommand;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotControl;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.CollisionDetectedAsyncData.CollisionPower;
import orbotix.robot.base.DeviceMessenger.AsyncDataListener;
import orbotix.robot.base.RobotProvider.OnRobotDisconnectedListener;
import orbotix.robot.base.RollCommand;
import orbotix.robot.base.SetHeadingCommand;
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
	
public static final String USER_NAME = "USER_NAME";
	
	private static final int MAX_PLAYER = 2;
	private static int myPlayerNumber;
	private static int myEnemyNumber;
	private static int myHealth;
	
	private Handler gamedataHandler = new Handler();
	
	// A location under GAME_LOCATION that will store the list of 
	// players who have joined the game (up to MAX_PLAYERS).
	private static final String PLAYERS_LOCATION = "player_list";
	 
	// A location under GAME_LOCATION that you will use to store data 
	// for each player (their game state, etc.)
	private static final String PLAYER_DATA_LOCATION = "player_data";
	
	private static final String FIREBASE_URL = "https://spheroko.firebaseio.com";
	private Firebase gameRef;
	private Firebase userRef;
	private Firebase playerHPRef;
	private Firebase playerListRef;
	private Firebase playerTurnRef;
	private Firebase myHPRef;
	private Firebase enemyTurnRef;
	private Firebase enemyHPRef;
	
	
	 /**
     * ID to start the StartupActivity for result to connect the Robot
     */
    private final static int  STARTUP_ACTIVITY = 0;
	private static final int  BLUETOOTH_ENABLE_REQUEST = 11;
	private static final int  BLUETOOTH_SETTINGS_REQUEST = 12;

	private final static int COLOR_PICKER_ACTIVITY = 1;
	private boolean mColorPickerShowing = false;
	private boolean wasConnected = false;
	
	/*
	 *Robot Control
	*/
	private Robot mRobot;
	/*
	 * One touch calibration
	 * */
	private CalibrationView mCalibrationView;
	
	private ProgressBar myProgressBar;
	private ProgressBar EnemyProgressBar;
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
	
	TextView myTextView;
	
	public static float startingPoint;
	
	//seek bar for calibration
	private SeekBar calibrate;
	private Handler mHandler = new Handler();
	private static int counter_touch = 0;
	
	private ValueEventListener connectedListener;
	private ChildEventListener childListener;
	
	/**
     * Attack Button
     */
    private Button attackAgain;
    
    private boolean alreadyInGame;
    
    private int playerNum;
    
    /**
     *  AttackTurn is a boolean used to keep track of the turns.
     */
    private boolean attackTurn;
    
    JoystickView stickctrl;
	
	/*Called when activity is created*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_controller);
		
		//get userName from previous setDialog
        Intent i = getIntent();
        final String userName = i.getStringExtra(USER_NAME);
        
        gameRef = new Firebase(FIREBASE_URL);
        assignPlayNum(userName, gameRef);
        
        stickctrl =(JoystickView)findViewById(R.id.joystick);
		
        myTextView= (TextView) findViewById(R.id.myTimer);
		//add the Joystick View as a controller
		addController(stickctrl);
	
		disableControllers();
		
		// Make sure you let the calibration view knows the robot it should control
//        mCalibrationView.setRobot(mRobot);
		myProgressBar = (ProgressBar) findViewById(R.id.myProgressBar);
		EnemyProgressBar = (ProgressBar) findViewById(R.id.enemybar);
		
		mainHP = (TextView) findViewById(R.id.your_pts);
		enemyHP = (TextView)findViewById(R.id.enemy_pts);
		
		//server= new Firebase(FIREBASE_URL).child("Game123");
	
		//enemy = new Firebase(FIREBASE_URL).child("Game123/Players/Player1");
		
		//set up for calibration seekbar
		calibrate =(SeekBar)findViewById(R.id.calibrateSeek);
		calibrate.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			
			@SuppressWarnings("deprecation")
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				float prog = (float)progress;
				FrontLEDOutputCommand.sendCommand(mRobot,(float)1.0);
				Log.d("progress of bar", ""+progress);
		        RollCommand.sendCommand(mRobot, prog, 0);
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				startingPoint = (float)seekBar.getProgress();
			}

			@SuppressWarnings("deprecation")
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				FrontLEDOutputCommand.sendCommand(mRobot,0);
				SetHeadingCommand.sendCommand(mRobot, 0);
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
                
                //setCalibrate(mRobot);
                
                
                // Make sure you let the calibration view knows the robot it should control
                //mCalibrationView.setRobot(mRobot);
                
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
	    	if(playerHPRef != null)
	    		playerHPRef.removeValue();
	    	if(playerListRef != null)
	    		playerListRef.removeValue();
	    	if(playerTurnRef != null)
	    		playerTurnRef.removeValue();
	    	
			DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, mCollisionListener);
	    	// Disconnect Robot properly
	    	RobotProvider.getDefaultProvider().disconnectControlledRobots();
	    }
		@Override 
		public void onStop(){
			super.onStop();
			if(playerHPRef != null)
	    		playerHPRef.removeValue();
	    	if(playerListRef != null)
	    		playerListRef.removeValue();
	    	if(playerTurnRef != null)
	    		playerTurnRef.removeValue();
		    DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, mCollisionListener);
	    	// Disconnect Robot properly
	    	RobotProvider.getDefaultProvider().disconnectControlledRobots();
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
				float speed = collisionData.getImpactSpeed();
				System.out.println(speed);
				if((power.x >70 || power.y > 70) && speed > .10){
					sendMessage();
				}

			}
		}
	};
	
	
	
	 public void assignPlayNum(String userName, final Firebase gameRef)
	    {
	    	playerListRef = gameRef.child(PLAYERS_LOCATION);
	        alreadyInGame = false;
	    	final String name = userName;
	    	
	    	playerListRef.runTransaction(new Transaction.Handler() {
	    	    @Override
	    	    public Transaction.Result doTransaction(MutableData currentData) {
	    	    	
	    	        String[] playerList = currentData.getValue(String[].class);
	    	        if(playerList == null)
	    	        	playerList = new String[]{"",""};
	    	        
	    	        //myPlayerNumber = 2; // number to show game is full & not to update playerlist
	    	        for (int i = 0; i < MAX_PLAYER; i++) {
	    	            if (playerList[i].equals(name)) {
	    	              // Player Name is already seated so abort transaction to not unnecessarily update playerList.
	    	              alreadyInGame = true;
	    	              myPlayerNumber = i; // Tell completion callback which seat we have.
	    	    	      return Transaction.abort();
	    	            }
	    	        }
	    	        for (int i = 0; i < MAX_PLAYER; i++) {
	    	            if (playerList[i].equals("") && i < MAX_PLAYER) {
	    	            // Empty seat is available so grab it and attempt to commit modified playerList.
	    	            playerList[i] = name;  // Reserve our seat.
	    	            myPlayerNumber = i; // Tell completion callback which seat we reserved.
	    	            if(myPlayerNumber == 0)
	    	            	myEnemyNumber = 1;
	    	            else
	    	            	myEnemyNumber = 0;
	    	            currentData.setValue(playerList);
	    	            return Transaction.success(currentData);
	    	          }
	    	        }
	    	        myPlayerNumber = 2;
	    	        return Transaction.abort();
	    	        
	    	    }

	    	    @Override
	    	    public void onComplete(FirebaseError error, boolean committed, DataSnapshot currentData) {
	    	    	
	    	    	
	    	    	if(myPlayerNumber < 2) //if game is not full, create data for player
	    	    	{
	    	    		playerHPRef = gameRef.child(PLAYER_DATA_LOCATION).child(String.valueOf(myPlayerNumber)).child("Health");
	    	    		playerHPRef.setValue(100);
	    	    		if(myPlayerNumber == 0)
	    	    		{
	    	    			playerTurnRef = gameRef.child(PLAYER_DATA_LOCATION).child(String.valueOf(myPlayerNumber)).child("AtkTurn");
	    	    			playerTurnRef.setValue(true);
	    	    		}
	    	    		else
	    	    		{
	    	    			playerTurnRef = gameRef.child(PLAYER_DATA_LOCATION).child(String.valueOf(myPlayerNumber)).child("AtkTurn");
	    	    			playerTurnRef.setValue(false);
	    	    		}
	    	    	}
	    	    	
	    	    }
	    	});
	    	
	    	playerListRef.addChildEventListener(new ChildEventListener() {
	    	    @Override
	    	    public void onChildAdded(DataSnapshot snapshot, String previousChildName) {

	    	    }

	    	    @Override
	    	    public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
	    	    	Toast.makeText(Controller.this, "Connected to other player. Start Game", Toast.LENGTH_SHORT).show();
	    	    	playGame();
	    	    }

	    	    @Override
	    	    public void onChildRemoved(DataSnapshot snapshot) {

	    	    }

	    	    @Override
	    	    public void onChildMoved(DataSnapshot snapshot, String previousChildName) {

	    	    }

				@Override
				public void onCancelled(FirebaseError arg0) {
					// TODO Auto-generated method stub
					
				}
	    	});
	    	
	    }
	    
	 public void playGame()
	 {
		 myHPRef = gameRef.child(PLAYER_DATA_LOCATION).child(String.valueOf(myPlayerNumber)).child("Health");
		 playerTurnRef = gameRef.child(PLAYER_DATA_LOCATION).child(String.valueOf(myPlayerNumber)).child("AtkTurn");
		 enemyTurnRef = gameRef.child(PLAYER_DATA_LOCATION).child(String.valueOf(myEnemyNumber)).child("AtkTurn");
		 enemyHPRef = gameRef.child(PLAYER_DATA_LOCATION).child(String.valueOf(myEnemyNumber)).child("Health");
		 
		 myHPRef.addValueEventListener(new ValueEventListener(){

				@Override
				public void onCancelled(FirebaseError arg0) {
					// nothing to do
					
				}

				@Override
				public void onDataChange(DataSnapshot arg0) {
				
					Object value = arg0.getValue(Integer.class);
		              if(value == null)
		              {
		            	  System.out.println("no data");
		              }
		              else
		              {
		            	  int snap = (Integer)value;
		            	  if(snap < 0)
		            		  mainHP.setText(String.valueOf(0));
		            	  else
		            		  mainHP.setText(String.valueOf(snap));
		            	  Log.d("testgetProgress","" + myProgressBar.getProgress());
							if(snap >= 0 && myProgressBar.getProgress() > 0){
								int diff;
								if(snap > myProgressBar.getProgress()){
									diff = snap - myProgressBar.getProgress();
									myProgressBar.incrementProgressBy(diff);
								}
								else{
									diff = -(myProgressBar.getProgress()- snap);
									myProgressBar.incrementProgressBy(diff);
								}
							}
							else if(snap < 0 && myProgressBar.getProgress() == 0){
								myProgressBar.incrementProgressBy(0);
							}
							else{
								Log.d("else", ""+myProgressBar.getProgress());
								myProgressBar.incrementProgressBy(snap);
							}
							
					        // Title progress is in range 0..10000
					        setProgress(100 * myProgressBar.getProgress());
		              }                 
					
					
				}
				
			});
		 
		 enemyHPRef.addValueEventListener(new ValueEventListener(){

				@Override
				public void onCancelled(FirebaseError arg0) {
					// nothing to do
					
				}

				@Override
				public void onDataChange(DataSnapshot arg0) {
					Object value = arg0.getValue(Integer.class);
		              if(value == null)
		              {
		            	  //other user disconnected, close the activity
		            	  if(wasConnected)
		            		  finish();
		            	  System.out.println("no data");
		              }
		              else
		              {
		            	  wasConnected = true;
		            	  int snap = (Integer)value;
		            	  if(snap < 0)
		            		  enemyHP.setText(String.valueOf(0));
		            	  else
		            		  enemyHP.setText(String.valueOf(snap));
		            	  Log.d("testgetProgress","" + EnemyProgressBar.getProgress());
							if(snap >= 0 && EnemyProgressBar.getProgress() > 0){
								int diff;
								if(snap > EnemyProgressBar.getProgress()){
									diff = snap - EnemyProgressBar.getProgress();
									EnemyProgressBar.incrementProgressBy(diff);
								}
								else{
									diff = -(EnemyProgressBar.getProgress()- snap);
									EnemyProgressBar.incrementProgressBy(diff);
								}
							}
							else if(snap < 0 && EnemyProgressBar.getProgress() == 0){
								EnemyProgressBar.incrementProgressBy(0);
							}
							else{
								Log.d("else", ""+EnemyProgressBar.getProgress());
								EnemyProgressBar.incrementProgressBy(snap);
							}
							
					        // Title progress is in range 0..10000
					        setProgress(100 * EnemyProgressBar.getProgress());
		              }
				 }
				
			});
		 
		 playerTurnRef.addValueEventListener(new ValueEventListener() {
	    	 @Override
	         public void onDataChange(DataSnapshot snapshot) {
	              Object value = snapshot.getValue();
	              if(value == null)
	              {
	            	  System.out.println("no data");
	              }
	              else
	              {
	            	  attackTurn = (Boolean)value;
	                  System.out.println(attackTurn);
	               	  if(attackTurn == true)
	               		  //createTimer();
	               		  enableControllers();
	              }                 
	         }

	    	 @Override
			 public void onCancelled(FirebaseError arg0) {
	    		 // TODO Auto-generated method stub
					
			 }
	     });
	        
	 }
	 
	 //handles the joystick touch click
	 public void joystickClick(View view){
		 if(attackTurn && counter_touch ==0){
			 createTimer();
			 counter_touch++;
		 }
		 else{
			 
		 }
	 }
	 public void createTimer() {
	    	//attackTurn = true;
	    	new  CountDownTimer(10000, 1000) {
	    		public void onTick(long millisUntilFinished) {
	    			if(millisUntilFinished == 10000)
	    			{
	    				//enableControllers();
	    				Toast.makeText(Controller.this, "Attack", Toast.LENGTH_SHORT).show();
	    			}
	    			myTextView.setText("Time Remaining To Attack: \n" + millisUntilFinished / 1000);
	    	   	}

	        	public void onFinish() {
	        		myTextView.setText("Turn Finished!");
	        		/*new code*/
	        		counter_touch= 0;
	        	 	disableControllers();
	        	 	//attackTurn = false;
	        	 	playerTurnRef.setValue(false);
	        	 	enemyTurnRef.setValue(true);
	         	}	
	    	}.start();
	    }
	 
	 public void sendMessage(){
			String x = (String) enemyHP.getText();
			int hp = Integer.parseInt(x) -10;

			Log.d("sendMessage",x);
//			EnemyProgressBar.incrementProgressBy(-10);
//	        // Title progress is in range 0..10000
//	        setProgress(100 * EnemyProgressBar.getProgress());
			
			enemyHPRef.setValue(hp, new Firebase.CompletionListener() {

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
	    
	 //game over dialog pop up
	 @SuppressLint({ "NewApi", "InlinedApi" })
	private void gameOverDialog(boolean whoWon){
		 AlertDialog.Builder gameOver = new AlertDialog.Builder(this, AlertDialog.THEME_TRADITIONAL);
		 gameOver.setTitle("Game Over!");
		 if(whoWon){
			 gameOver.setMessage("You Won! Your ball is made of steel! Try again?");
		 }
		 else{
			 gameOver.setMessage("You Lost! WTF?! Get back in there!");
		 }
		 gameOver.setPositiveButton("Battle Again", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		 gameOver.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Intent intent = new Intent(this, SpheroMenu.class);
				//startActivity(intent);
			}
		});
		 AlertDialog log = gameOver.create();
		 log.show();
	 }

}
