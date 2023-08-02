package amaanmiah.sound_engine.hardwareinput;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;

import amaanmiah.sound_engine.engine.SoundEngine;
import amaanmiah.sound_engine.objects.*;
import amaanmiah.sound_engine.simulator.Sound_sim;
import amaanmiah.sound_engine.vectors.Vector2D;

public class MouseInput extends MouseAdapter implements MouseMotionListener{
	private static int noderadius = 20;
	private SoundEngine engine;
	private Storage storage;
	private Wall wall;
	private boolean pressed = false;
	public int tool = 0;

	public MouseInput(Storage objects,SoundEngine Engine) {
		this.storage = objects;
		this.engine = Engine;
	}
	
	private void WithinObject(int x, int y){
		for (int i = 0; i <= storage.Emitters.size()-1;i++) {
			Source source = storage.Emitters.get(i);
			source.renderTooltip = false;
			if ((x > source.X-source.HalfR) && (x < source.X+source.HalfR) && (y > source.Y-source.HalfR) && (y < source.Y+source.HalfR)){
				source.renderTooltip = true;
			}
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		int click = e.getButton();
		pressed = true;
		//Left click
		if(click == MouseEvent.BUTTON1) {
			switch (tool) {
			case 0: //Select
				break;
			case 1: //Node
				Node node = new Node(e.getX(),e.getY(),noderadius);
				storage.AddNode(node);
				break;
			case 2: //SourceNode
				File sound = engine.to_play;
				Source source = new Source(e.getX(), e.getY(), sound, "SourceDataLine", Sound_sim.mixer);//Source
				storage.AddSource(source);
				break;
			case 3: //Wall
				wall = new Wall(new Vector2D(e.getX(),e.getY()),null);
				storage.AddWall(wall);
				break;
			}
			
		}
		//Right click
		else if (click == MouseEvent.BUTTON3) {
			switch (tool) {
			case 0: //Select
					WithinObject(e.getX(),e.getY());
				break;
			case 1: //Node
				break;
			case 2: //SourceNode
				break;
			case 3: //Wall
				break;
			} 
		}
		//Middle click
		else if (click == MouseEvent.BUTTON2) {
			
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if(tool == 3){//Dragging with Wall tool
			wall.Vd = new Vector2D(e.getX(),e.getY());
			wall.exists = true;
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if (!pressed) {
			return;
		}
		pressed = false;
		int release = e.getButton();
			//Left click
				if(release == MouseEvent.BUTTON1) {
					switch (tool) {
					case 0: //Select
						break;
					case 1: //Node
						break;
					case 2: //SourceNode
						break;
					case 3: //Wall
						//On button release the wall is built.
						if (!wall.exists) {
							storage.RemoveWall(wall);
							return;
						}
						wall.BecomeBuilt();
						Node nodeA = new Node(wall.Va.x-8, wall.Va.y-8, 20);
						Node nodeB = new Node(wall.Vb.x+8, wall.Vb.y-8, 20);
						Node nodeC = new Node(wall.Vc.x-8, wall.Vc.y+8, 20);
						Node nodeD = new Node(wall.Vd.x+8, wall.Vd.y+8, 20);
						
						storage.AddNode(nodeA);
						storage.AddNode(nodeB);
						storage.AddNode(nodeC);
						storage.AddNode(nodeD);
						break;
					}
					
				}
				//Right click
				else if (release == MouseEvent.BUTTON3) {
					switch (tool) {
					case 0: //Select
						break;
					case 1: //Node
						break;
					case 2: //SourceNode
						break;
					case 3: //Wall
						break;
					} 
				}
				//Middle click
				else if (release == MouseEvent.BUTTON2) {
					
				}
	}
		
	
	
	public void settool(int mode) {
		tool = mode;
	}
	
}
