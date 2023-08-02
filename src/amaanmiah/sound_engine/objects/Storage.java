package amaanmiah.sound_engine.objects;

import java.awt.Graphics2D;
import java.util.ArrayList;

import amaanmiah.sound_engine.engine.SoundEngine;

public class Storage {
	public ArrayList<Source> Emitters = new ArrayList<Source>();
	public ArrayList<Node> Nodes = new ArrayList<Node>();
	public ArrayList<Wall> Walls = new ArrayList<Wall>();
	SoundEngine engine;
	
	public Storage(SoundEngine engine) {
		this.engine = engine;
	}
	
	public void AddSource(Source source) {
		Emitters.add(source);
	}
	
	public void AddNode(Node node) {
		Nodes.add(node);
	}
	
	public void AddWall(Wall wall) {
		Walls.add(wall);
	}
	
	public void RemoveSource(Source source) {
		Emitters.remove(source);
	}
	
	public void RemoveNode(Node node) {
		Nodes.remove(node);
	}
	
	public void RemoveWall(Wall wall) {
		Walls.remove(wall);
	}
	
	public ArrayList<Source> GetObjects() {
		return Emitters;
	}
	
	public void RenderEmitters(Graphics2D g) {
		if (Emitters.size() == 0) {
			return;
		}
		for (int i= 0; i<= Emitters.size()-1; i++) {
			Emitters.get(i).RenderObject(g);
		}
	}
	
	public void RenderNodes(Graphics2D g) {
		if (Nodes.size() == 0) {
			return;
			}
		for (int i= 0; i<= Nodes.size()-1; i++) {
			Nodes.get(i).RenderObject(g);
		}
	}
	
	public void RenderWalls(Graphics2D g) {
		if (Walls.size() == 0) {
			return;
		}
		for (int i= 0; i<= Walls.size()-1; i++) {
			Walls.get(i).DrawWall(g);
		}
	}                       
	
	public void StopEmitters() {
		
		long starttime = System.nanoTime();
		for (int i= 0;i<= Emitters.size()-1; i++) {
			engine.Stop(Emitters.get(i));
		}
		long endtime = System.nanoTime();
		System.out.println((double)(endtime-starttime)/1000000000);
	}
	
}
