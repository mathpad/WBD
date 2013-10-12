package uk.org.darnell.whiteboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samsung.samm.common.SObjectStroke;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;
import com.samsung.spensdk.applistener.SPenTouchListener;
//import com.samsung.spensdk.example.R;
//import com.samsung.spensdk.example.tools.SPenSDKUtils;
import uk.org.darnell.socket.*;

public class MainActivity extends Activity {
	
	private final static String TAG = "Whiteboard";
	
	//==============================
	// Application Identifier Setting
	// "SDK Sample Application 1.0"
	//==============================
	private final String APPLICATION_ID_NAME = "SDK Sample Application";
	private final int APPLICATION_ID_VERSION_MAJOR = 2;
	private final int APPLICATION_ID_VERSION_MINOR = 2;
	private final String APPLICATION_ID_VERSION_PATCHNAME = "Debug";
	
	
	//==============================
	// Variables
	//==============================
	Context mContext = null;
	
	private RelativeLayout	mCanvasContainer;
	private static EdCanvas	mSCanvas;
	final Server server = new Server();
	Thread th;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.editor_startup);
	
		mContext = this;
		
		//------------------------------------
		// Create SCanvasView
		//------------------------------------
		mCanvasContainer = (RelativeLayout) findViewById(R.id.canvas_container);		
		mSCanvas = new EdCanvas(mContext);        
		mCanvasContainer.addView(mSCanvas);
		
		//------------------------------------------------
		// Set SCanvas Initialize Listener
		//------------------------------------------------
		mSCanvas.setSCanvasInitializeListener(new SCanvasInitializeListener() {
			@Override
			public void onInitialized() { 
				//--------------------------------------------
				// Start SCanvasView/CanvasView Task Here
				//--------------------------------------------
				// Application Identifier Setting
				if(!mSCanvas.setAppID(APPLICATION_ID_NAME, APPLICATION_ID_VERSION_MAJOR, APPLICATION_ID_VERSION_MINOR,APPLICATION_ID_VERSION_PATCHNAME))
					Toast.makeText(mContext, "Fail to set App ID.", Toast.LENGTH_LONG).show();

				// Set Title
				//if(!mSCanvas.setTitle("Whiteboard"))
				//	Toast.makeText(mContext, "Fail to set Title.", Toast.LENGTH_LONG).show();
				
				// mSCanvas.setBackgroundImage(arg0);
				
				// Set Background as white
				if(!mSCanvas.setBGColor(0xFFFFFFFF))
					Toast.makeText(mContext, "Fail to set Background color.", Toast.LENGTH_LONG).show();
			
				Toast.makeText(mContext, "mSCanvas Done.", Toast.LENGTH_SHORT).show();
			}
		});
		
		th = new Thread (new Runnable() 
		{public void run() {server.run(handler);}});
		th.start();
		
		// Caution:
		// Do NOT load file or start animation here because we don't know canvas size here.
		// Start such SCanvasView Task at onInitialized() of SCanvasInitializeListener

	
	
		//--------------------------------------------
		// Set S pen Touch Listener
		//--------------------------------------------
		mSCanvas.setSPenTouchListener(new SPenTouchListener(){

			
			@Override
			public boolean onTouchFinger(View view, MotionEvent event) {
				//mSCanvas.onEdTouchEvent(event);
				return true;	// dispatch event to SCanvasView for drawing
			}
			

			@Override
			public boolean onTouchPen(View view, MotionEvent event) {
				mSCanvas.onEdTouchEvent(event);
				return true;	// dispatch event to SCanvasView for drawing
			}

			@Override
			public boolean onTouchPenEraser(View view, MotionEvent event) {
				return false;	// dispatch event to SCanvasView for drawing
			}		

			@Override
			public void onTouchButtonDown(View view, MotionEvent event) {
			   Toast.makeText(mContext, "S Pen Button Down on Touch", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onTouchButtonUp(View view, MotionEvent event) {
				Toast.makeText(mContext, "S Pen Button Up on Touch", Toast.LENGTH_SHORT).show();
			}			

		});
		
	}
		
	protected static Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			
			Log.d(TAG, "Handler " + msg.what + " " + (String)msg.obj);
			if (msg.what == 1)
			{
				Toast.makeText(mSCanvas.getContext(), (String)msg.obj, Toast.LENGTH_SHORT).show();
				Log.d(TAG, "Made Toast");
			}
			else if (msg.what == 2)
			{
				Toast.makeText(mSCanvas.getContext(), (String)msg.obj, Toast.LENGTH_SHORT).show();
				mSCanvas.setBackgroundImage(Server.screen);
			}
			else if (msg.what == 4) Server.sendMove(msg.arg1,msg.arg2);
			else if (msg.what == 5) Server.addPoint(msg.arg1,msg.arg2);
			else if (msg.what == 6) Server.sendPoints(msg.arg1,msg.arg2);
		}
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Release SCanvasView resources
		//server.getHandler().sendEmptyMessage(1);
		th.interrupt();
		
		if(!mSCanvas.closeSCanvasView())
			Log.e(TAG, "Fail to close SCanvasView");
	}
	


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// th.interrupt();
		server.getHandler().sendEmptyMessage(1);
		// th.interrupt();
		//SPenSDKUtils.alertActivityFinish(this, "Exit");
	} 

}
	
	
