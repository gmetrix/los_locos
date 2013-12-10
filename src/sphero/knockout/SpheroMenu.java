package sphero.knockout;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import orbotix.multiplayer.RemotePlayer;
import orbotix.robot.base.*;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;





public class SpheroMenu extends Activity  {
	
	
	private String userName = "Guest";
	
	 /**
     * ID For starting the SetNameDialog
     */
    private static final int sSetNameDialog = 0;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_sphero_menu);
		
		Button btnStart = (Button) findViewById(R.id.btnStart);
		
		
        //Listening to button event
        btnStart.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View arg0) {
                //Starting a new Intent
            	showDialog(sSetNameDialog);
                //Intent startGame = new Intent(getApplicationContext(), UiSampleActivity.class);
                //startActivity(startGame);
            }
        });
        
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
        
        Dialog d = null;
        
        if(id == sSetNameDialog){
            d = new SetNameDialog(this);
        }
        
        return d;
    }
	
	/**
     * A Dialog that asks for a username, and sets this username
     */
    public class SetNameDialog extends Dialog {

        public SetNameDialog(Context context) {
            super(context);
            
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            
            setContentView(R.layout.set_name_dialog);

            //When the user clicks "done", record the name
            findViewById(R.id.done_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userName = getNameString();
                    dismissDialog(sSetNameDialog);
                    Intent i = new Intent(view.getContext(), Controller.class);
                    i.putExtra(Controller.USER_NAME, userName);
        	        startActivity(i);
        	        finish();
                }
            });
        }
        
        private String getNameString(){
            return ((EditText)findViewById(R.id.name_field)).getText().toString();
        }
    }
}
