package robot.widget.joystick;

import sphero.knockout.utilities.ColorTools;
import android.graphics.*;
import android.graphics.drawable.Drawable;


public class JoystickPuck extends Drawable{

	private int radius =25;
	private final Point position = new Point();
	
	//Pucker part
	private final Shadow shadow;
	private final Surface surface;
	private final Shading shading;
	
	//Constructs a new joystick puck
	public JoystickPuck(){
		this.shadow = new Shadow();
		this.surface = new Surface();
		this.shading = new Shading();
		
		this.setShadowColor(0xff000000);
		this.setSurfaceColor(0xffcccccc);
		this.setShadingColor(0xff000000);
	}
	
	/*
	 * Sets the radius of the Joystick puck, in pixels
	 * @param radius
	 * */
	public void setRadius(int radius){
		if(radius <1){
			throw new IllegalArgumentException("Radius must be greater than 0.");
		}
		this.radius =radius;
		
		this.shadow.setRadius(this.radius);
		this.surface.setRadius(this.radius);
		this.shading.setRadius(this.radius - (this.radius/8));
	}
	/*
	 * set the color of the pucks shadow to the specified color
	 * */
	public void setShadowColor(int color){
		this.shadow.setColor(color);
	}
	/*
	 * Sets the color of the pucks surface to the specified colors. Is a gradient that goes from one
	 * color to the other
	 * @param color
	 * */
	public void setSurfaceColor(int color){
		
		this.surface.setColor(color);
	}
	/*
	 * Sets the color of the shading on the puck's surface.
	 * @param color*/
	public void setShadingColor(int color){
		this.shading.setColor(color);
	}
	/*
	 * Sets the position of the puck to the specified position
	 * */
	public void setPosition(Point position){
		this.position.set(position.x, position.y);
		
		this.shadow.setPosition(position);
		this.shading.setPosition(position);
		this.surface.setPosition(position);
		
		this.setBounds(new Rect(
				this.position.x- this.radius,
				this.position.y- this.radius,
				this.position.x +this.radius,
				this.position.y +this.radius
		));
	}
	/*
	 * Gets a copy of this pucks position.
	 * @returns an index containing the position of this puck.*/
	public Point getPosition(){
		return new Point(this.position);
	}
	@Override
	public void draw(Canvas canvas) {
		this.shadow.draw(canvas);
		this.surface.draw(canvas);
		this.shading.draw(canvas);
		
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void setAlpha(int alpha) {
		this.shadow.setAlpha(alpha);
		this.surface.setAlpha(alpha);
		this.shading.setAlpha(alpha);
		
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		this.shadow.setColorFilter(cf);
		this.surface.setColorFilter(cf);
		this.shading.setColorFilter(cf);
	}
	/*
	 * Part of the puck
	 * */
	private interface PuckPart{
		/*
		 * Draw this part to the provided canvas.
		 * */
		public void draw(Canvas canvas);
		/*
		 * set the color of this puckpart to the provided color, as an int.
		 * @param color
		 */
		public void setColor(int color);
		 /**
         * Set this PuckPart to the provided position.
         * @param position
         */
        public void setPosition(Point position);

        /**
         * Sets this PuckPart's radius to the provided radius.
         * @param radius
         */
        public void setRadius(int radius);
    }
	/*The puck's shadow*/
	private class Shadow extends Drawable implements PuckPart{
		private final Paint paint = new Paint();
		private int radius =25;
		private final Point position = new Point();
		
		private int alpha = 0x55;
		
		private Shadow(){
			//Set blur to 1/5 the radius
			final int blur = (this.radius/5);
			this.paint.setMaskFilter(new BlurMaskFilter(((blur < 1)?1:blur), BlurMaskFilter.Blur.OUTER));
			paint.setStyle(Paint.Style.FILL);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
			this.setColor(0xff000000);
		}
		@Override
		public void setColor(int color) {
			this.paint.setColor(color);
			final int i = (color >>24);
			this.setAlpha(i);
			
		}

		@Override
		public void setPosition(Point position) {
			this.position.set(position.x, position.y);
			
		}

		@Override
		public void setRadius(int radius) {
			if(radius <1){
				throw new IllegalArgumentException("Radius must be greater than 0.");
			}
			
			this.radius = radius;
			
		}

		@Override
		public void draw(Canvas canvas){
			canvas.drawCircle(this.position.x, this.position.y, this.radius, this.paint);
			
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}

		@Override
		public void setAlpha(int i) {
			final float a = 0x55 * (Math.abs(i & 0xff)/255f);
			this.paint.setAlpha((int)a);
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			this.paint.setColorFilter(cf);
		}
		
		
	}
	/*The puck's surface*/
	private class Surface extends Drawable implements PuckPart{
		
		private final Paint paint = new Paint();
		private final Point position = new Point();
		private final Point gradient_pos_1 = new Point();
		private final Point gradient_pos_2 = new Point();
		private int radius =25;
		
		private int color_1 = 0xff888888;
		private int color_2 = 0xffdddddd;
		
		public Surface(){
			paint.setStyle(Paint.Style.FILL);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
			paint.setAntiAlias(true);
		}

		@Override
		public void setColor(int color) {
			this.color_1 =ColorTools.ColorSum(color, 0x00333333, true);
			this.color_2 = color;
			
		}

		@Override
		public void setPosition(Point position) {
			this.position.set(position.x, position.y);
			
			this.gradient_pos_1.set(this.position.x, this.position.y + this.radius);
			this.gradient_pos_2.set(this.position.x, this.position.y - this.radius);
			
		}

		@Override
		public void setRadius(int radius) {
			
			if(radius <1){
				throw new IllegalArgumentException("Radius must be greater than 0");			
			}
			this.radius = radius;
		}

		@Override
		public void draw(Canvas canvas) {
			
			LinearGradient gradient =new LinearGradient(
					this.gradient_pos_1.x,
					this.gradient_pos_1.y,
					this.gradient_pos_2.x,
					this.gradient_pos_2.y,
					this.color_1,
					this.color_2,
					Shader.TileMode.MIRROR);
			this.paint.setShader(gradient);
			
			canvas.drawCircle(this.position.x, this.position.y, this.radius, this.paint);
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}

		@Override
		public void setAlpha(int i) {
			this.paint.setAlpha(i);
			
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			this.paint.setColorFilter(cf);
			
		}
	}
	public class Shading extends Drawable implements PuckPart{

		private final Point position = new Point();
		private final Paint paint = new Paint();
		
		private int alpha = 0x44;
		private int color_1 =  0xffffffff;
		private int color_2 = 0xff000000;
		
		private int radius = 22;
		
		public Shading(){
			
			paint.setStyle(Paint.Style.FILL);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
			paint.setAntiAlias(true);
			
		}
		@Override
		public void setColor(int color) {
			this.color_1 = (color & 0xffffff);
			final int a = (color >> 24);
			this.setAlpha(a);
			this.color_2= (this.color_1) + (this.alpha<<24);
			
			
		}

		@Override
		public void setPosition(Point position) {
			this.position.set(position.x, position.y);
			
		}

		@Override
		public void setRadius(int radius) {
			if(radius <1){
				throw new IllegalArgumentException("Radius must be greater than 0.");
			}
			this.radius= radius;
		}

		@Override
		public void draw(Canvas canvas) {
			RadialGradient gradient = new RadialGradient(
					this.position.x, 
					this.position.y, 
					this.radius, 
					this.color_1,
					this.color_2, 
					Shader.TileMode.MIRROR);
			this.paint.setShader(gradient);
			
			canvas.drawCircle(this.position.x, this.position.y, this.radius, this.paint);
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}

		@Override
		public void setAlpha(int alpha) {

			float a = Math.abs(alpha & 0xff);
			a = a/255f;
			a = 0x44 * a;
			this.alpha =(int)a;
			this.color_2 = (this.color_2 & 0xffffff) + (this.alpha<< 24);
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			this.paint.setColorFilter(cf);
			
		}
		
	}
}
