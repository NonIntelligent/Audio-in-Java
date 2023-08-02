package amaanmiah.sound_engine.objects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;

import amaanmiah.sound_engine.vectors.Vector2D;

public class Wall implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7568493520240741912L;
	public Vector2D Va, Vb, Vc, Vd;
	public Vector2D nTop, nBottom, nLeft, nRight;
	public double adotn, bdotn, cdotn, ddotn;
	private int xa,ya,xd,yd,width,height;
	public double penalty = 1000; // Penalty applied when sound travels through a side of a wall.
	public boolean exists = false; // The user can see the wall if it 'exists'
	public boolean built = false; // Once the wall is built, (user releases mouse click)
	
	public Wall(Vector2D Va, Vector2D Vd) {
		this.Va = Va;
		this.Vd = Vd;
	}
	
	public void update() {
		xa = Va.x;
		ya = Va.y;
		xd = Vd.x;
		yd = Vd.y;
		width = xd - xa;
		height = yd - ya;
		
		if (width < 0) {
			xa = xa + width;
			width = width*-1;
		}
		if (height < 0) {
			ya = ya + height;
			height = height*-1;
		}
		
		this.Vb = new Vector2D(xa+width ,ya);
		this.Vc = new Vector2D(xa, ya+height);
	}
	
	public void BecomeBuilt() {
		
		this.Va = new Vector2D(xa,ya);
		this.Vb = new Vector2D(xa+width,ya);
		this.Vc = new Vector2D(xa,ya+height);
		this.Vd = new Vector2D(xa+width,ya+height);
		
		//A to B 
		Vector2D vtemp = Vector2D.Subtract(Va,Vb);
		nTop = new Vector2D(vtemp.y, -vtemp.x);
		adotn = Vector2D.Dot(nTop, Va);
		
		//C to D
		vtemp = Vector2D.Subtract(Vc,Vd);
		nBottom = new Vector2D(vtemp.y, -vtemp.x);
		ddotn = Vector2D.Dot(nBottom, Vd);
		
		//A to C
		vtemp = Vector2D.Subtract(Va,Vc);
		nLeft = new Vector2D(vtemp.y, -vtemp.x);
		cdotn = Vector2D.Dot(nLeft, Vc);
		
		//B to D
		vtemp = Vector2D.Subtract(Vb,Vd);
		nRight = new Vector2D(vtemp.y, -vtemp.x);
		bdotn = Vector2D.Dot(nRight, Vb);
		
		this.built = true;
	}
	
	public void DrawWall(Graphics2D g) {
		if (exists) {
			g.setColor(Color.WHITE);
			g.fillRect(xa, ya, width, height);
			g.setColor(Color.BLACK);
			g.setStroke(new BasicStroke(2));
			g.drawRect(xa, ya, width, height);
		}
	}
	
}
