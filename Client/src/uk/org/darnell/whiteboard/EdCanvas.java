package uk.org.darnell.whiteboard;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;

public class EdCanvas extends SCanvasView {

	  public EdCanvas(Context context) {
		super(context);
		
	    paint.setAntiAlias(true);
	    paint.setColor(Color.BLACK);
	    paint.setStyle(Paint.Style.STROKE);
	    paint.setStrokeJoin(Paint.Join.ROUND);
	    paint.setStrokeWidth(STROKE_WIDTH);
		// TODO Auto-generated constructor stub
	}

	private static final float STROKE_WIDTH = 3f;

	  /** Need to track this so the dirty region can accommodate the stroke. **/
	  private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

	  private Paint paint = new Paint();
	  private Path path = new Path();
	  
	  /**
	   * Optimizes painting by invalidating the smallest possible area.
	   */
	  private float lastTouchX;
	  private float lastTouchY;
	  private final RectF dirtyRect = new RectF();

	  /**
	   * Erases the signature.
	   */
	  public void clear() {
	    path.reset();
	    // Repaints the entire view.
	    invalidate();
	  }

	  @Override
	public void onDraw(Canvas canvas) {
	    canvas.drawPath(path, paint);
	  }

	  public boolean onEdTouchEvent(MotionEvent event) {
		    boolean send = false;
		    Float eventX = event.getX();
		    Float eventY = event.getY();

		    switch (event.getAction()) {
		      case MotionEvent.ACTION_DOWN:
		        path.moveTo(eventX, eventY);
		        lastTouchX = eventX;
		        lastTouchY = eventY;
		        MainActivity.handler.sendMessage(MainActivity.handler.obtainMessage(4,eventX.intValue(),eventY.intValue(),null));
		        // There is no end point yet, so don't waste cycles invalidating.
		        return true;

		      case MotionEvent.ACTION_UP:  
		    	  send = true;
		      case MotionEvent.ACTION_MOVE:
		        // Start tracking the dirty region.
		        resetDirtyRect(eventX.intValue(), eventY.intValue());

		        // When the hardware tracks events faster than they are delivered, the
		        // event will contain a history of those skipped points.
		        int historySize = event.getHistorySize();
		        for (int i = 0; i < historySize; i++) {
		          Float historicalX = event.getHistoricalX(i);
		          Float historicalY = event.getHistoricalY(i);
		          
		          expandDirtyRect(historicalX, historicalY);
		          path.lineTo(historicalX, historicalY);
		                 
		          MainActivity.handler.sendMessage(MainActivity.handler.obtainMessage(5,historicalX.intValue(),historicalY.intValue(),null)); 
		        }

		        // After replaying history, connect the line to the touch point.
		        path.lineTo(eventX, eventY);
		        if (!send)
		        	MainActivity.handler.sendMessage(MainActivity.handler.obtainMessage(5,eventX.intValue(),eventY.intValue(),null));
		        else
		        	MainActivity.handler.sendMessage(MainActivity.handler.obtainMessage(6,eventX.intValue(),eventY.intValue(),null));
		        break;

		      default:
		        Log.d("Whiteboard","Ignored touch event: " + event.toString());
		        return false;
		    }

		    // Include half the stroke width to avoid clipping.
		    invalidate(
		        (int) (dirtyRect.left - HALF_STROKE_WIDTH),
		        (int) (dirtyRect.top - HALF_STROKE_WIDTH),
		        (int) (dirtyRect.right + HALF_STROKE_WIDTH),
		        (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));
		    
		    lastTouchX = eventX;
		    lastTouchY = eventY;

		    return true;
	  }

	  /**
	   * Called when replaying history to ensure the dirty region includes all
	   * points.
	   */
	  private void expandDirtyRect(float historicalX, float historicalY) {
	    if (historicalX < dirtyRect.left) {
	      dirtyRect.left = historicalX;
	    } else if (historicalX > dirtyRect.right) {
	      dirtyRect.right = historicalX;
	    }
	    if (historicalY < dirtyRect.top) {
	      dirtyRect.top = historicalY;
	    } else if (historicalY > dirtyRect.bottom) {
	      dirtyRect.bottom = historicalY;
	    }
	  }

	  /**
	   * Resets the dirty region when the motion event occurs.
	   */
	  private void resetDirtyRect(float eventX, float eventY) {

	    // The lastTouchX and lastTouchY were set when the ACTION_DOWN
	    // motion event occurred.
	    dirtyRect.left = Math.min(lastTouchX, eventX);
	    dirtyRect.right = Math.max(lastTouchX, eventX);
	    dirtyRect.top = Math.min(lastTouchY, eventY);
	    dirtyRect.bottom = Math.max(lastTouchY, eventY);
	  }
	}
