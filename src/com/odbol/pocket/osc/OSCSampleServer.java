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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/***
 * This is an example app that listens for OSC messages (i.e. an OSC server). 
 * 
 * All it needs is a port, and some defined messages that you can send it. 
 * The user will take care of the rest, including running their own OSC client to send you messages. 
 * 
 * It uses the OSCLib library from:
 * 
 * http://www.assembla.com/wiki/show/osclib
 *  
 * 
 * @author odbol
 *
 */
public class OSCSampleServer extends Activity {
	/*Defaults. Changable via preferences */
	public static final int DEFAULT_OSC_PORT = 8000;

	protected static final int EDIT_PREFS = 1;
	
	private OscServer server;

	/***
	 * The network port your application will listen to.
	 * 
	 * You should let the user set this in your own preferences.
	 * 
	 */
	private int oscPort = DEFAULT_OSC_PORT;

	

	/***
	 * This listens for incoming OSC messages.
	 * 
	 * For testing, it just prints the message, but you can do anything you want with it!
	 * 
	 * It is run in a separate thread, so if you are going to update the UI you'll need to use a Handler. 
	 * 
	 * @author odbol
	 *
	 */
	public class LooperListener extends BasicOscListener {
		public Context c;
		
		/*** 
		 * this is used to update the textview in the UI thread from the listening thread.
		 * @author odbol
		 *
		 */
		private final class TextViewUpdater implements Runnable {
			public String msg;
			
			public TextViewUpdater(String message) {
				msg = message;
			}

			@Override
		    public void run() {
		    	TextView t = (TextView) findViewById(R.id.out_text);
				t.append(msg);
				
				ScrollView sc = (ScrollView) findViewById(R.id.out_scroll);
				sc.smoothScrollTo(0, t.getBottom());
		    }
		}
		
		/***
		 * This is the main place where you will handle individual osc messages as they come in.
		 * 
		 * The message's address specifies what the user wants to change in your application: think of it as an API call.
		 * The message's arguments specify what to change those things to. You can accept multiple arguments of any primitive types.
		 */
		@Override
		public void handleMessage(OscMessage msg) {
			//get all the arguments for the message. in this case we're assuming one and only one argument.
			String val = msg.getArguments().get(0).toString();
			
			//now update the textview.
			//since it's in the UI thread, we need to access it using a handler
			Handler h = new Handler(OSCSampleServer.this.getMainLooper());
			TextViewUpdater u = new TextViewUpdater("\nReceived: " + msg.getAddress() + " " + val);	
			h.post(u);
		}
	}

    
    /***
     * This starts your app listening for OSC messages.
     * 
     * You want to call this once your user chooses to start the OSC server - it probably shouldn't be started 
     * by default since it will block that port for any other apps.
     */
    private void startListening() {
    	stopListening(); //unbind from port
    	
	    	try {
				server = new OscServer(oscPort);
				server.setUDP(true); //as of now, the TCP implementation of OSCLib is broken (getting buffer overflows!), so we have to use UDP.
				server.start();
			}
			catch (Exception e) {
				Toast.makeText(this, "Failed to start OSC server: " + e.getMessage(), Toast.LENGTH_LONG);
				return;
			}
			server.addOscListener(new LooperListener());	
			
			TextView t = (TextView) findViewById(R.id.out_text);
			t.append("\nListening on port " + oscPort);
    }
    
    private void stopListening() {
    	if (server != null) {
    		server.stop();
    		server = null;
    	}
    }
    
    @Override
    public void onDestroy() {
    	stopListening();
    	
    	super.onDestroy();
    }

    
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
    
    private boolean isServiceStarted = false;

    private boolean isListening = false;
    
    /** Called when the activity is first created. The rest of this code is just for demonstration. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
 
        
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
        
        Button l = (Button) findViewById(R.id.start_listening_button);
        l.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if (!isListening) {
            		startListening();

                	((Button)v).setText("Stop Listening");
            	}
            	else {
            		stopListening();

                	((Button)v).setText("Start Listening");
            	}

            	isListening = !isListening;
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

}