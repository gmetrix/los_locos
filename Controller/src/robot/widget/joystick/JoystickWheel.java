package robot.widget.joystick;

import android.graphics.*;
import android.graphics.drawable.Drawable;

public class JoystickWheel extends Drawable{
	private final Point position = new Point();
	private final Paint paint = new Paint();
	private int radius = 75;
	
	private int alpha = 0x33;
	
	public JoystickWheel(){
		
		int blur = (this.radius/10);
		blur = (blur < 1)?1: blur;
		
		this.paint.setMaskFilter(new BlurMaskFilter(blur, BlurMaskFilter.Blur.INNER));
		this.paint.setStyle(Paint.Style.FILL);
		this.setColor(0xff000000);
	}
	
	public void setRadius(int radius){
		this.radius = radius;
	}
	public void setColor(int color){
		this.paint.setColor(color);
	}
	
	public void setPosition(Point position){
		this.position.set(position.x, position.y);
		
		this.setBounds(new Rect(
				this.position.x - this.radius,
				this.position.y - this.radius,
				this.position.x + this.radius,
				this.position.y + this.radius
		));
	}

	/*
	 * Gets the center position of the puck wheel, as a Point.
	 * @returns a Point, containing the center point of the puck wheel
	 * */
	public Point getPosition(){
		return new Point(this.position);
	}
	@Override
	public void draw(Canvas canvas) {
		canvas.drawCircle(this.position.x, this.position.y, this.radius, this.paint);
	}
	

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void setAlpha(int i) {
		float a = Math.abs(i & 0xff);
		a = (a/255);
		a = 0x33 *a;
		this.paint.setAlpha((int)a);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		this.paint.setColorFilter(cf);
		
	}

}
