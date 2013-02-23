package uk.org.darnell.wbd;

import java.util.ArrayList;

import uk.org.darnell.socket.Server;
import uk.org.darnell.wbd.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class WBDActivity extends Activity {
	
	 private final static String TAG = "WBD";
     private static GraphicsView gv; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );

        gv = new GraphicsView(this);
        setContentView(gv);
        gv.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
    
	public class GraphicsView extends View {

		private ArrayList<Integer> redraw;
		private Canvas canvas = null;
		private Bitmap bitMap;

		public GraphicsView(Context context) {
			super(context);
		}

		protected void reset()
		{	
			Log.d(TAG,"reset()");
			redraw = new ArrayList<Integer>();
			// drawAll();
		}

		@Override
		protected void onDraw(Canvas canvas1){ 
			if (canvas == null)
			{
				Bitmap.Config conf = Bitmap.Config.ARGB_8888;
				bitMap = Bitmap.createBitmap(1920, 1080, conf); 
				canvas = new Canvas(bitMap);
				reset();
			}
			// drawList();
			Paint paint = new Paint();
			canvas1.drawBitmap(bitMap, 0, 0, paint);	
			redraw = new ArrayList<Integer>();
		}

		private void cls()
		{
			Paint paint = new Paint();
			Bitmap bMap;
			Bitmap.Config conf = Bitmap.Config.ARGB_8888;
			bMap = Bitmap.createBitmap(1920, 1080, conf); 
			canvas.drawBitmap(bMap, 0, 0, paint);
		}
	}
	
	protected static Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			
			Log.d(TAG, "Handler " + msg.what + " " + (String)msg.obj);
			if (msg.what == 1)
			{
				Toast.makeText(gv.getContext(), (String)msg.obj, Toast.LENGTH_SHORT).show();
				Log.d(TAG, "Made Toast");
			}
			else if (msg.what == 2)
			{
				Toast.makeText(gv.getContext(), (String)msg.obj, Toast.LENGTH_SHORT).show();
				//gv.setBackgroundImage(Server.screen);
			}
			else if (msg.what == 4) Server.sendMove(msg.arg1,msg.arg2);
			else if (msg.what == 5) Server.addPoint(msg.arg1,msg.arg2);
			else if (msg.what == 6) Server.sendPoints(msg.arg1,msg.arg2);
		}
	};
	
    
}
