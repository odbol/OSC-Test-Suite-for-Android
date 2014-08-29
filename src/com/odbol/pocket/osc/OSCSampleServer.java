package com.odbol.pocket.osc;

import com.relivethefuture.osc.data.BasicOscListener;
import com.relivethefuture.osc.data.OscMessage;
import com.relivethefuture.osc.transport.OscServer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
 * https://github.com/odbol/OSCLib
 *  
 * 
 * @author odbol
 *
 */
public class OSCSampleServer extends Activity {
	/*Defaults. Changeable via preferences */
	public static final int DEFAULT_OSC_PORT = 8000;

	protected OscServer server;

    protected boolean isListening = false;
	
	/***
	 * The network port your application will listen to.
	 * 
	 * You should let the user set this in your own preferences.
	 * 
	 */
	protected int oscPort = DEFAULT_OSC_PORT;

	

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
	protected class LooperListener extends BasicOscListener {
		public Context c;
		
		/*** 
		 * This is used to update the textview in the UI thread from the listening thread.
		 * 
		 * You do not need it in your application, but it is a good model if you need 
		 * to update your UI thread from handleMessage().
		 * 
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
			// Get the address of the message. For instance "/test/count"
			String messageAddress = msg.getAddress();
			
			// Get all the arguments for the message. 
			// In this case we're assuming one and only one argument.
			String val = msg.getArguments().get(0).toString();
			
			// Now you can do anything you want with it.
			processOSCMessage(messageAddress, val);
		}

		/***
		 * This does something with the message. 
		 * For demonstration, it just prints the message to the window.
		 * 
		 * @param messageAddress OSC address of the message.
		 * @param val The first argument of the message.
		 */
		protected void processOSCMessage(String messageAddress, String val) {
			// Now update the textview.
			// Since it's in the UI thread, we need to access it using a handler
			Handler h = new Handler(OSCSampleServer.this.getMainLooper());
			TextViewUpdater u = new TextViewUpdater("\nReceived: " + messageAddress + " " + val);	
			h.post(u);
		}
	}

    
    /***
     * This starts your app listening for OSC messages.
     * 
     * You want to call this once your user chooses to start the OSC server - it probably shouldn't be started 
     * by default since it will block that port for any other apps.
     */
	public void startListening() {
    	stopListening(); // Unbind from port
    	
    	try {
			server = new OscServer(oscPort);
			server.setUDP(true); // As of now, the TCP implementation of OSCLib is broken (getting buffer overflows!), so we have to use UDP.
			server.start();
		}
		catch (Exception e) {
			Toast
				.makeText(this, "Failed to start OSC server: " + e.getMessage(), Toast.LENGTH_LONG)
				.show();
			
			return;
		}
    	// This will add a listener to process each OSC message as it comes in.
		server.addOscListener(new LooperListener());	
		
		showMessage("\nListening on port " + oscPort);
    }

	public void stopListening() {
    	if (server != null) {
    		server.stop();
    		server = null;

    		showMessage("\nStopped listening");
    	}
    }
    
    @Override
    public void onDestroy() {
    	stopListening();
    	
    	super.onDestroy();
    }

    /** Called when the activity is first created. 
     * The rest of this code is just UI code for demonstration. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        
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
    }

	private void showMessage(String msg) {
		TextView t = (TextView) findViewById(R.id.out_text);
		t.append(msg);
	}

}