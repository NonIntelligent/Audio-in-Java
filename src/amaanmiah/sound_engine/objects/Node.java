package amaanmiah.sound_engine.objects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Node implements Serializable{
	
	private static final long serialVersionUID = 782536227405932734L;
	public int X,Y, Radius, HalfR;
	private double g_cost = 0, h_cost = 0, f_cost = 0;
	private Map<Node,Double> paths = new HashMap<>();
	public boolean renderTooltip = false;
	private Node parent = null;
	public boolean checked = false;
	public boolean travelled = false;
	public float MaxRange;

	public Node(int x, int y, int Radius) {
		this.X = x;
		this.Y = y;
		this.Radius = Radius;
		this.HalfR = Radius/2;
	}
	
	public void RenderObject(Graphics2D g) {
		g.setColor(Color.RED);
		if (checked) {
			g.setColor(Color.GREEN);
		}
		if (travelled) {
			g.setColor(Color.ORANGE);
		}
		g.fillOval(X-(HalfR), Y-(HalfR), Radius, Radius);
		g.setColor(Color.GREEN);
		g.fillOval(X-2, Y-2, 4, 4);
	}
	
	public double getG_cost() {
		return g_cost;
	}

	public void setG_cost(double g_cost) {
		this.g_cost = g_cost;
	}

	public double getH_cost() {
		return h_cost;
	}

	public void setH_cost(double h_cost) {
		this.h_cost = h_cost;
	}

	public double getF_cost() {
		return f_cost;
	}

	public void setF_cost(double f_cost) {
		this.f_cost = f_cost;
	}
	
	public Map<Node,Double> getLinks() {
		return paths;
	}

	public void setLinks(Map<Node,Double> paths) {
		this.paths = paths;
	}

	public Node Getparent() {
		return parent;
	}
	
	public void Setparent(Node parent) {
		this.parent = parent;
	}
	public float getMaxRange() {
		return this.MaxRange;
	}

	public void setMaxRange(float maxRange) {
		MaxRange = maxRange;
	}
}
