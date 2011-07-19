package com.odbol.pocket.osc;

import java.net.InetSocketAddress;

import com.relivethefuture.osc.data.OscMessage;
import com.relivethefuture.osc.transport.OscClient;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import android.os.Process;
import android.preference.PreferenceManager;

/***
 * This service runs in the background and sends a test OSC message 
 * to a specified address every 2 seconds.
 * 
 * Use it to test your OSC app to make sure it can receive OSC messages
 * correctly. Do this by calling startService() (see OSCSampleServer for details)
 * 
 * @author odbol
 *
 */
public class OSCTesterClientService extends Service {
	private String oscAddress = "127.0.0.1";
	private int oscPort = OSCSampleServer.DEFAULT_OSC_PORT;
	private String oscMsgPath = "/test/count";
	
	private TesterThread thread;
	
	  private Looper mServiceLooper;
	  private ServiceHandler mServiceHandler;

	  private final class TesterThread extends HandlerThread {
			private String oscMsgPath = "/test/count";
			
			private OscClient sender;

			private int curCount = 0;
			
		public TesterThread(String name, int priority, String oscAddress, int oscPort, String oscMsgPath) {
			super(name, priority);
			
			this.oscMsgPath = oscMsgPath;
			
			 //start the osc client
			  if (sender == null) {
				  sender = new OscClient(true); 
				  InetSocketAddress addr = new InetSocketAddress(oscAddress, oscPort);
				  sender.connect(addr);
			  }

		}	
			
		public TesterThread(String name, int priority) {
			super(name, priority);
		}	
			
		  @Override
		  public void run() {

		        while (curCount++ < 100) {
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

			    	  
			          // For our sample, we just sleep for 5 seconds.
			    	  try {
						sleep(2*1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			    	  /*
			          long endTime = System.currentTimeMillis() + 2*1000;
			          while (System.currentTimeMillis() < endTime) {
			              synchronized (this) {
			                  try {
			                      wait(endTime - System.currentTimeMillis());
			                  } catch (Exception e) {
			                  }
			              }
			          }
			          */
		        }
		        
		          // Stop the service using the startId, so that we don't stop
		          // the service in the middle of handling another job
		          stopSelf();//msg.arg1);
		          
			    super.run();
			           
		  }
		  
		  @Override
		  public void destroy() {
			  if (sender != null) {
				  sender.disconnect();
				  sender = null;
			  }
			  
			  //NO such method exception?
			  //super.destroy();
		  }
	  }
	  
	  // Handler that receives messages from the thread
	  private final class ServiceHandler extends Handler {
	      public ServiceHandler(Looper looper) {
	          super(looper);
	      }
	      
	      @Override
	      public void handleMessage(Message msg) {
	    	 
	      }
	  }

	  @Override
	  public void onCreate() {
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
        		Toast.makeText(this, "Invalid port in preferences", Toast.LENGTH_LONG);
        	}
        	oscAddress = p.getString("pref_osc_addr", oscAddress);
        	oscMsgPath  = p.getString("pref_osc_msg", oscMsgPath);
        	   

	    
	    
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
	  public int onStartCommand(Intent intent, int flags, int startId) {
		  Toast.makeText(this, "OSCTester service starting", Toast.LENGTH_SHORT).show();

		  if (thread == null) {
		        
	        	thread = new TesterThread("ServiceStartArguments",
	    	            Process.THREAD_PRIORITY_AUDIO, oscAddress, oscPort, oscMsgPath);
	        	
	        	
			    // Start up the thread running the service.  Note that we create a
			    // separate thread because the service normally runs in the process's
			    // main thread, which we don't want to block.  We also make it
			    // background priority so CPU-intensive work will not disrupt our UI.
			    //HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_AUDIO);
				thread.start();
			    
			    //thread.destroy();
			    
			    // Get the HandlerThread's Looper and use it for our Handler 
			    mServiceLooper = thread.getLooper();
			    mServiceHandler = new ServiceHandler(mServiceLooper);
			  
		  }
		  
		  
		  // For each start request, send a message to start a job and deliver the
		  // start ID so we know which request we're stopping when we finish the job
		  Message msg = mServiceHandler.obtainMessage();
		  msg.arg1 = startId;
		  mServiceHandler.sendMessage(msg);

		  // If we get killed, after returning from here, restart
		  return START_STICKY;
	  }

	  @Override
	  public IBinder onBind(Intent intent) {
	      // We don't provide binding, so return null
	      return null;
	  }
	  
	  @Override
	  public void onDestroy() {
		  if (thread != null && thread.isAlive()) {
			  thread.destroy();
			  thread = null;
		  }
		  
		  Toast.makeText(this, "OSCTester service done", Toast.LENGTH_SHORT).show(); 
	  }
	}