package com.odbol.pocket.osc;

import java.io.IOException;

import com.relivethefuture.osc.data.BasicOscListener;
import com.relivethefuture.osc.data.OscMessage;
import com.relivethefuture.osc.transport.OscServer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class OSCSampleServer extends Activity {
	/*Defaults. Changable via preferences */
	public static final int DEFAULT_OSC_PORT = 8000;

	protected static final int EDIT_PREFS = 1;
	
	private OscServer server;

	private int oscPort = DEFAULT_OSC_PORT;

	

	/***
	 * This listens for incoming OSC messages.
	 * 
	 * For testing, it just prints the message, but you can do anything you want with it!
	 * 
	 * @author phreakhead
	 *
	 */
	public class LooperListener extends BasicOscListener {
		public Context c;
		
		@Override
		public void handleMessage(OscMessage msg) {
			String val = msg.getArguments().get(0).toString();
						
			TextView t = (TextView) findViewById(R.id.out_text);
			t.append("\nReceived: " + msg.getAddress() + " " + val);
			
			//System.out.println("Message " + msg.getAddress());
			//System.out.println("Type Tags " + msg.getTypeTags());
			
			//Toast.makeText(OSCSampleServer.this, "OSCmessage: " + msg.toString(), Toast.LENGTH_LONG);
			
		}
	}

	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
 
        
        /* This starts the test service. */
        Button twoButtonsTitle = (Button) findViewById(R.id.start_button);
        twoButtonsTitle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	startTestClient();
            	startListening();
            }
        });
        
        Button prefs = (Button) findViewById(R.id.prefs_button);
        prefs.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(OSCSampleServer.this, OSCTesterClientPreferences.class);
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
            		Toast.makeText(this, "Invalid port in preferences", Toast.LENGTH_LONG);
            	}
            	
            	
            	break;
        } 
    }
    
    private void startTestClient() {
    	Intent intent = new Intent(this, OSCTesterClientService.class);
    	startService(intent);
    }
    
    
    private void startOscServer() {
    	try {
			server = new OscServer(oscPort);
			server.start();
		}
		catch (IOException e) {
			Toast.makeText(this, "Failed to start OSC server: " + e.getMessage(), Toast.LENGTH_LONG);
			return;
		}
		server.addOscListener(new LooperListener());	
		
		TextView t = (TextView) findViewById(R.id.out_text);
		t.append("\nListening on port " + oscPort);
    }
    
    private void startListening() {		
		if (server == null)
			startOscServer();
    }
}