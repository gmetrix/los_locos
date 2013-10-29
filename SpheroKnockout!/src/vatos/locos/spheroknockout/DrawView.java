package vatos.locos.spheroknockout;


import android.util.AttributeSet;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.MotionEvent;
import android.view.View;

public class DrawView extends View {

	float myX, myY;
	private static float TOLERANCE = 0;
	private Paint paint,bitPaint;
	private Path myPath;
	private Bitmap myBitMap;
	private Canvas myCanvas;
	float x,y;

	
	public DrawView(Context context) {
		super(context);
		initialize();
		setFocusable(true);
	}
	
	public DrawView(Context context, AttributeSet attrs){
		super(context, attrs);
		initialize();
		setFocusable(true);
	}
	
	
	 
	public void initialize(){
		paint = new Paint();
		paint.setDither(true);
		paint.setAntiAlias(true);
		paint.setColor(0xFFFF0D00);
		paint.setStrokeWidth(9);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		
		x=y=0;
		
		bitPaint = new Paint(Paint.DITHER_FLAG);
		myPath = new Path();
		//init();
	}
	
	//change the color
	public void colorChanged(int color) {
	        paint.setColor(color);
	}
	  @Override
      protected void onSizeChanged(int w, int h, int oldw, int oldh) {
          super.onSizeChanged(w, h, oldw, oldh);
          myBitMap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
          myCanvas= new Canvas(myBitMap);
      }


	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		//fills the canvas with black
		canvas.drawColor(0xFF000000);
		
		canvas.drawBitmap(myBitMap, 0, 0, null);
		canvas.drawPath(myPath, paint);
		
	}

	public void touchDown(float x, float y){
		myPath.reset();
		myPath.moveTo(x,y);
		myX= x;
		myY = y;
		myBitMap.eraseColor(Color.TRANSPARENT);
	}
	
	public void touchUp(float x, float y){
		myPath.lineTo(myX, myY);
        // commit the path to our offscreen
        myCanvas.drawPath(myPath, paint);
        // kill this so we don't double draw
        myPath.reset();
	}
	
	public void touchMove(float x, float y){
		float abX = Math.abs(x- myX);
		float abY = Math.abs(x-myY);
		if(abX <= TOLERANCE||abY <=TOLERANCE){
			myPath.quadTo(myX, myY, (x + myX)/2, (y + myY)/2);
			myX = x;
			myY = y;
			
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		x = event.getX();
		y = event.getY();

		switch(event.getAction()){
		
		case MotionEvent.ACTION_DOWN:
			x = event.getX();
			y = event.getY();
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			x= event.getX();
			y= event.getY();
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			x = event.getX();
			y= event.getY();
			invalidate();
			break;
			
		}
		return true;
	}

	
	//touch event that captures the x/y position
//	public boolean onTouch(View v, MotionEvent touch){
//		int sensor = touch.getAction();
//		float touchX=  touch.getX();
//		float touchY = touch.getY();
//		switch(sensor){
//		case MotionEvent.ACTION_DOWN:
//			touchX = touch.getX();
//			touchY= touch.getY();
//			touchDown(myX, myY);
//			invalidate();
//			break;
//		case MotionEvent.ACTION_UP:
//			touchX = touch.getX();
//			touchY= touch.getY();
//			invalidate();
//			break;
//		case MotionEvent.ACTION_MOVE:
//			touchX = touch.getX();
//			touchY= touch.getY();
//			touchMove(touchX, touchY);
//			invalidate();
//			break;
//		}
//		return true;
//	}

}
