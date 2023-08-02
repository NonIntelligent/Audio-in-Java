package amaanmiah.sound_engine.objects;

import java.awt.Color;
import java.awt.Graphics2D;

public class Player extends Node{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2925055633423911748L;
	private int pxs,pys;
	private final static int radius = 30;
	
	public Player(int x, int y) {
		super (x,y,radius);
	}
	
	public void PlayerMovement(){
		X += pxs;
		Y += pys;
	}
	
	@Override
	public void RenderObject(Graphics2D g) {
		g.setColor(Color.BLUE);
		g.fillOval(X-HalfR, Y-HalfR, 30, 30);
		g.setColor(Color.GREEN);
		g.fillOval(X-2, Y-2, 4, 4);
	}

	public int getPxs() {
		return pxs;
	}

	public int getPys() {
		return pys;
	}

	public void setPxs(int pxs) {
		this.pxs = pxs;
	}

	public void setPys(int pys) {
		this.pys = pys;
	}
}
