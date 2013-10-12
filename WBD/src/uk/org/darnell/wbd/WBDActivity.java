package uk.org.darnell.wbd;

import uk.org.darnell.socket.Server;
import uk.org.darnell.wbd.util.SystemUiHider;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
 	final Server server = new Server();
 	Thread th;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );

        gv = new GraphicsView(this);
        setContentView(gv);
        gv.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        
		th = new Thread (new Runnable() 
		{public void run() {server.run(handler);}});
		th.start();
        
    }
    
	public class GraphicsView extends View {
		
		private Path path = new Path();
		private Paint paint = new Paint();
		private static final float STROKE_WIDTH = 3f;
		/** Need to track this so the dirty region can accommodate the stroke. **/
		private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

		public GraphicsView(Context context) {
			super(context);
		    paint.setAntiAlias(true);
		    paint.setColor(Color.WHITE);
		    paint.setStyle(Paint.Style.STROKE);
		    paint.setStrokeJoin(Paint.Join.ROUND);
		    paint.setStrokeWidth(STROKE_WIDTH);
			// TODO Auto-generated constructor stub
		}
		
		  /**
		   * Optimizes painting by invalidating the smallest possible area.
		   */
		  private float lastX;
		  private float lastY;
		  private final RectF dirtyRect = new RectF();

		  /**
		   * Erases the signature.
		   */
		  public void clear() {
		    path.reset();
		    // Repaints the entire view.
		    invalidate();
		  }

		protected void reset()
		{	
			Log.d(TAG,"reset()");
			clear();
		}

		  @Override
			public void onDraw(Canvas canvas) {
			    canvas.drawPath(path, paint);
			  }

			  public boolean drawLine(int type,int X,int Y) {
				    switch (type) {
				      case 4:
				        resetDirtyRect(X, Y);
				        path.moveTo(X, Y);
				        break;
				      case 5:
				        // When the hardware tracks events faster than they are delivered, the
				        // event will contain a history of those skipped points.
				        path.lineTo(X, Y);
				        resetDirtyRect(X,Y);
				        // Include half the stroke width to avoid clipping.
					    invalidate(
						        (int) (dirtyRect.left - HALF_STROKE_WIDTH),
						        (int) (dirtyRect.top - HALF_STROKE_WIDTH),
						        (int) (dirtyRect.right + HALF_STROKE_WIDTH),
						        (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));
				        break;
				      default:
				        Log.d(TAG,"Ignored touch event: " + X + "," + Y);
				        return false;
				    }
				    lastX = X;
				    lastY = Y;

				    
				    return true;
			  }

			  /**
			   * Resets the dirty region when the motion event occurs.
			   */
			  private void resetDirtyRect(float eventX, float eventY) {

			    // The lastTouchX and lastTouchY were set when the ACTION_DOWN
			    // motion event occurred.
			    dirtyRect.left = Math.min(lastX, eventX);
			    dirtyRect.right = Math.max(lastX, eventX);
			    dirtyRect.top = Math.min(lastY, eventY);
			    dirtyRect.bottom = Math.max(lastY, eventY);
			  }

			  /*
		private void cls()
		{
			Paint paint = new Paint();
			Bitmap bMap;
			Bitmap.Config conf = Bitmap.Config.ARGB_8888;
			bMap = Bitmap.createBitmap(1920, 1080, conf); 
			canvas.drawBitmap(bMap, 0, 0, paint);
		}
		*/
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
			else if (msg.what == 4) gv.drawLine(4,msg.arg1,msg.arg2);
			else if (msg.what == 5) gv.drawLine(5,msg.arg1,msg.arg2);
		}
	};    
}
