package com.odbol.pocket.osc;

import java.net.InetSocketAddress;

import com.relivethefuture.osc.data.OscMessage;
import com.relivethefuture.osc.transport.OscClient;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.preference.PreferenceManager;

/***
 * This service runs in the background and sends a test OSC message 
 * to a specified address every 2 seconds.
 * 
 * Use it to test your OSC server app to make sure it can receive OSC messages
 * correctly. Do this by calling startService() (see OSCSampleServer for details)
 * either from your app (not recommended) or by starting the service via a separate app.
 * 
 * We will assume that the user will start their own OSC Client that sends messages,
 * so you only need to implement the OSC Server and keep listening until something comes to you!
 * This is merely to test your app's response to the OSC message.
 * 
 * @author odbol
 *
 */
public class OSCTesterClientService extends IntentService {
	public OSCTesterClientService() {
		super("OSCTesterClientServiceThread");
	}

	private String oscAddress = "127.0.0.1";
	private int oscPort = OSCSampleServer.DEFAULT_OSC_PORT;
	private String oscMsgPath = "/test/count";

	private OscClient sender;

	private int curCount = 0;
	private int timeout = 100;

	@Override
	public void onCreate() {
		super.onCreate();
		/*
		 * This populates the default values from the preferences XML file. See
		 * {@link DefaultValues} for more details.
		 */
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		//reload prefs
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
		try {
			oscPort = Integer.parseInt(p.getString("pref_osc_port", String.valueOf(oscPort)));
		}
		catch (NumberFormatException e) {
			Toast.makeText(this, "Invalid port in preferences", Toast.LENGTH_LONG).show();
		}
		try {
			timeout = Integer.parseInt(p.getString("pref_timeout", String.valueOf(timeout)));
		}
		catch (NumberFormatException e) {
			Toast.makeText(this, "Invalid timeout in preferences", Toast.LENGTH_LONG).show();
		}
		oscAddress = p.getString("pref_osc_addr", oscAddress);
		oscMsgPath  = p.getString("pref_osc_msg", oscMsgPath);



		//start the osc client
		if (sender == null) {
			sender = new OscClient(true); 
			InetSocketAddress addr = new InetSocketAddress(oscAddress, oscPort);
			sender.connect(addr);
		}

		//add to foreground
		/*
	    Notification notification = new Notification(R.drawable.icon, getText(R.string.ticker_text),
	            System.currentTimeMillis());
	    Intent notificationIntent = new Intent(this, ExampleActivity.class);
	    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
	    notification.setLatestEventInfo(this, getText(R.string.notification_title),
	            getText(R.string.notification_message), pendingIntent);
	    startForeground(ONGOING_NOTIFICATION, notification);
		 */
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Toast.makeText(this, "OSCTester service starting", Toast.LENGTH_SHORT).show();

		curCount = 0;
		while (curCount++ < timeout) {
			//send a test osc message
			if (sender != null) {
				OscMessage m = new OscMessage(oscMsgPath);
				m.addArgument(curCount);
				try {
					sender.sendPacket(m);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}


			// For our sample, we just sleep for 2 seconds.
			long endTime = System.currentTimeMillis() + 2*1000;
			while (System.currentTimeMillis() < endTime) {
				synchronized (this) {
					try {
						wait(endTime - System.currentTimeMillis());
					} catch (Exception e) {
					}
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		if (sender != null) {
			sender.disconnect();
			sender = null;
		}

		Toast.makeText(this, "OSCTester service done", Toast.LENGTH_SHORT).show(); 
		
		super.onDestroy();
	}
}