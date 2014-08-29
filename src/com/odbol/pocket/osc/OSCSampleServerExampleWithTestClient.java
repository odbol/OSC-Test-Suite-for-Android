package com.odbol.pocket.osc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/***
 * This is what we use as an example to demonstrate the OSCSampleServer working with the TestClient.
 * 
 * You do not need to use this code in your app.
 * 
 * @author tyler
 *
 */
public class OSCSampleServerExampleWithTestClient extends OSCSampleServer {

	protected static final int EDIT_PREFS = 1;
	
    private boolean isServiceStarted = false;

    
    /***
     * This just starts the OSCTesterClient service.
     * 
     * In a real application you don't need this because presumably the user has already started their own
     * OSC client either on the phone or on a separate device (e.g. a laptop connected via WiFi).
     */
    private void startTestClient() {
    	Intent intent = new Intent(this, OSCTesterClientService.class);
    	startService(intent);
    }
    
    private void stopTestClient() {
    	Intent intent = new Intent(this, OSCTesterClientService.class);
    	stopService(intent);
    }  
    
    
    /** Called when the activity is first created.  */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
 
        
        /* This starts the test service. */
        Button twoButtonsTitle = (Button) findViewById(R.id.start_button);
        twoButtonsTitle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if (!isServiceStarted) {
            		startTestClient();

                	((Button)v).setText("Stop test service");
            	}
            	else {
            		stopTestClient();

                	((Button)v).setText("Start test service");
            	}
            	
            	isServiceStarted = !isServiceStarted;
            }
        });
        
        Button prefs = (Button) findViewById(R.id.prefs_button);
        prefs.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(OSCSampleServerExampleWithTestClient.this, OSCTesterClientPreferences.class);
            	startActivityForResult(intent, EDIT_PREFS);
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // See which child activity is calling us back.
        switch (requestCode) {
            case EDIT_PREFS:
            	//reload prefs
            	SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
            	try {
            		oscPort = Integer.parseInt(p.getString("pref_osc_port", String.valueOf(DEFAULT_OSC_PORT)));
            	}
            	catch (NumberFormatException e) {
            		Toast.makeText(this, "Invalid port in preferences", Toast.LENGTH_LONG).show();
            	}
            	
            	
            	break;
        } 
    }
}
