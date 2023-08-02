package amaanmiah.sound_engine.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.Clip;

import amaanmiah.sound_engine.objects.Node;
import amaanmiah.sound_engine.objects.Player;
import amaanmiah.sound_engine.objects.Source;
import amaanmiah.sound_engine.objects.Wall;
import amaanmiah.sound_engine.vectors.Vector2D;

/**
 * This class handles the sound propagation algorithms and the altering of audio to the user.
 * <p>
 * On creation the AcquireSound and LoadSound methods should be called so that
 * the clips can be created and stored.
 * 
 * @author Amaan Miah
 * 
 */

public class SoundEngine{
	public int No_Walls = 0;
	public int No_Nodes = 0;
	private Comparator<Node> comparator;
	public ArrayList<File> AudioFiles = new ArrayList<File>();
	public Map<File, Clip> Clips = new HashMap<File, Clip>();
	public File to_play;
	
	/**
	 * This constructor will setup the comparator
	 * of which the A-star algorithm will use to sort its lists.
	 * <p>
	 * You will have to invoke the 'AcquireSound' method to add a
	 * list of .wav files from a directory path, so that they can be loaded
	 * by the 'LoadSound' method (again manually called by you).
	 */
	
	public SoundEngine() {
		SetUpComparator();
	}
	
	/**
	 * This will acquire every .wav file in the directory and add them to the
	 * 'AudioFiles' ArrayList.
	 * 
	 * @param audiopath The full directory of which .wav files are contained in.
	 * (Should be acquired via user input i.e. JFileChooser)
	 * @see LoadSound
	 */
	
	public void AcquireSound(File audiopath) {
		//Acquires audio files from the selected directory
		for (final File file : audiopath.listFiles()) {
			if (file.isDirectory()) {
				continue;
			}
			else if (file.toString().endsWith(".wav")){
				AudioFiles.add(file);
			}
		}
	}
	
	/**
	 * This will play the audio file of the chosen Source object
	 * @param source an object of the Source class, which also needs to have an opened clip stored.
	 * @throws IOException 
	 * @see Stop
	 * @see Pause
	 * @see Source.setClip
	 */
	
	public void Play(Source source) throws IOException{
		if (source.clip == null) {
			source.sourceDataLine.start();
			source.playing = true;
			return;
		}
		//Set playback at starting point
		source.clip.setMicrosecondPosition(0);
		source.clip.start();
		source.playing = true;
	}
	
	/**
	 * This will stop the audio of the chosen Source object.
	 * @param source an object of the Source class, which also needs to have an opened clip stored.
	 * @see Play
	 * @see Pause
	 */
	
	public void Stop(Source source){
		if (source.mode.contains("SourceDataLine")) {
			source.sourceDataLine.stop();
			source.playing = false;
			return;
		}
		//Stops audio when playing
		  source.clip.stop();
		  source.playing = false;
	}
	
	/**
	 * This will pause the audio of the chosen Source object.
	 * @param source an object of the Source class, which also needs to have an opened clip stored.
	 */
	
	public void Pause(Source source) {
		//Stores current time in audio
		source.clipTimePosition = source.clip.getMicrosecondPosition();
		source.clip.stop();
	}
	
	/**
	 * This converts a percentage into its decibel equivalent as the loudness
	 * of a decibel is a logarithmic scale instead of a linear one.
	 * @param percentage The percentage of loudness i.e 0.5 is half.
	 * @return The volume in decibels
	 */
	
	public float SetVol(double percentage) {
		//Converts a percentage into a decibel equivalent
		float VolPercent = (float) (1 - percentage);
		float DB = 0;
		if(VolPercent<=0) {
			return -80;
		}
		//Does not let volume decrease below -80 or error ocurrs
		DB = Math.max(20 * (float) Math.log10(VolPercent),-80);
		return DB;
	}
	
	/**
	 * Calculates the euclidean distance between two Node Objects (The hypotenuse of a triangle)
	 * and converts it into a percentage based on distance/maximum range.
	 * @param Current The 'Current' Node object
	 * @param Target The 'Target' Node object
	 * @return The percentage of distance/MaxRange
	 */
	
	public double CalcDist(Node Current, Node Target) {
 		double vol = 0; // A percentage
		double deltaX = Target.X - Current.X;
		double deltaY = Target.Y - Current.Y;
		//Calculate hypotenuse (magnitude) of line
		double magnitude = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));  
		//Calculate volume percentage
		vol = ((magnitude / Current.MaxRange));
		return vol;
		}
	
	/**
	 * This algorithm checks for every Wall, if the current Node and the target Node are
	 * across/over a wall.
	 * @param current The current Node
	 * @param target The target Node
	 * @param walls An ArrayList of Wall objects containing every wall on the map(obtained from the Storage class). 
	 * @return The total penalty incurred by each side of a wall crossed by an imaginary straight line.
	 */
	
	public double interfere(Node current, Node target, ArrayList<Wall> walls) {
		Vector2D currentPos = new Vector2D(current.X, current.Y);
		Vector2D targetPos = new Vector2D(target.X, target.Y);
		Vector2D NodeToTarget = Vector2D.Subtract(targetPos, currentPos);
//		Angles are against unit vector (1,0) X-axis
//		Except in case where the current Node is to the right side of the wall
//		In this case the unit vector is (-1,0)
		double AngleT = Math.atan2(-NodeToTarget.y, NodeToTarget.x); // Range of -PI to PI atan2(det,dot)
		
		double totalpenalty = 0;//Reset penalty
		
		for(Wall wall: walls) {
			if (!wall.built) {
				continue;
			}
			
//			Wall diagram
//			A------------B
//			|////////////|
//			|////////////|
//			|////////////|
//			C------------D
			
			
			AngleT = Math.atan2(-NodeToTarget.y, NodeToTarget.x);//Returns angle to normal if current Node was to the right
			
			//for line A to B
			double CurrentNormal = Vector2D.Dot(currentPos, wall.nTop);
			double CurrentSide = CurrentNormal - wall.adotn;//-ve if current Node above AB. +ve if below AB
			double TargetNormal = Vector2D.Dot(targetPos, wall.nTop);
			double TargetSide = TargetNormal - wall.adotn;//-ve if target Node above AB. +ve if below AB
			
			Vector2D NodeToBig = Vector2D.Subtract(wall.Va, currentPos);//Current node is below AB
			Vector2D NodeToSmall = Vector2D.Subtract(wall.Vb, currentPos);
			
			if (CurrentSide < 0) {//If CurrentNode is above AB
				NodeToBig = Vector2D.Subtract(wall.Vb, currentPos);
				NodeToSmall = Vector2D.Subtract(wall.Va, currentPos);
			}
			
			double AngleBig = Math.atan2(-NodeToBig.y, NodeToBig.x);
			double AngleSmall = Math.atan2(-NodeToSmall.y, NodeToSmall.x);
			//If current Node and target Node are negative (over line AB)
			//AND the angle between current and target are in between the current Node's angle to A AND B
			if(CurrentSide/TargetSide <= 0 && AngleT >= AngleSmall && AngleT <= AngleBig) {
				totalpenalty += wall.penalty;
			}
			
			//for line C to D
			CurrentNormal = Vector2D.Dot(currentPos, wall.nBottom);
			CurrentSide = CurrentNormal - wall.ddotn;//-ve if current Node above CD. +ve if below CD.
			TargetNormal = Vector2D.Dot(targetPos, wall.nBottom);
			TargetSide = TargetNormal - wall.ddotn;
			
			NodeToBig = Vector2D.Subtract(wall.Vc, currentPos);//Current node is below CD.
			NodeToSmall = Vector2D.Subtract(wall.Vd, currentPos);
			
			if (CurrentSide < 0) {//If CurrentNode is above CD
				NodeToBig = Vector2D.Subtract(wall.Vd, currentPos);
				NodeToSmall = Vector2D.Subtract(wall.Vc, currentPos);
			}
			
			AngleBig = Math.atan2(-NodeToBig.y, NodeToBig.x);
			AngleSmall = Math.atan2(-NodeToSmall.y, NodeToSmall.x);
			
			if(CurrentSide/TargetSide <= 0 && AngleT >= AngleSmall && AngleT <= AngleBig) {
				totalpenalty += wall.penalty;
			}
			
			//for line A to C
			CurrentNormal = Vector2D.Dot(currentPos, wall.nLeft);
			CurrentSide = CurrentNormal - wall.cdotn;//-ve if current Node right of AC. +ve if left of AC.
			TargetNormal = Vector2D.Dot(targetPos, wall.nLeft);
			TargetSide = TargetNormal - wall.cdotn;
			
			NodeToBig = Vector2D.Subtract(wall.Va, currentPos);//Current node is left of AC.
			NodeToSmall = Vector2D.Subtract(wall.Vc, currentPos);
			
			AngleBig = Math.atan2(-NodeToBig.y, NodeToBig.x);
			AngleSmall = Math.atan2(-NodeToSmall.y, NodeToSmall.x);
			
			if (CurrentSide < 0) {//If CurrentNode is right of AC
				AngleT = - Math.atan2(NodeToTarget.y, -NodeToTarget.x);
				AngleBig = - Math.atan2(NodeToBig.y, -NodeToBig.x);
				AngleSmall = - Math.atan2(NodeToSmall.y, -NodeToSmall.x);
			}
			
			if(CurrentSide/TargetSide <= 0 && AngleT >= AngleSmall && AngleT <= AngleBig) {
				totalpenalty += wall.penalty;
			}
			
			//for line B to D
			CurrentNormal = Vector2D.Dot(currentPos, wall.nRight);
			CurrentSide = CurrentNormal - wall.bdotn;//-ve if current Node is right of BD.+ve if left of BD.
			TargetNormal = Vector2D.Dot(targetPos, wall.nRight);
			TargetSide = TargetNormal - wall.bdotn;
			
			NodeToBig = Vector2D.Subtract(wall.Vb, currentPos);//Current node is left of BD.
			NodeToSmall = Vector2D.Subtract(wall.Vd, currentPos);
			
			AngleBig = Math.atan2(-NodeToBig.y, NodeToBig.x);
			AngleSmall = Math.atan2(-NodeToSmall.y, NodeToSmall.x);
			
			if (CurrentSide < 0) {//If CurrentNode is right of BD
				AngleBig = - Math.atan2(NodeToBig.y, -NodeToBig.x);
				AngleSmall = - Math.atan2(NodeToSmall.y, -NodeToSmall.x);
			}
			
			if(CurrentSide/TargetSide <= 0 && AngleT >= AngleSmall && AngleT <= AngleBig) {
				totalpenalty += wall.penalty;
			}
		}
		//total penalty incurred between the Nodes
		return totalpenalty;
	}
	
	/**
	 * Calculates the Euclidean distance between 2 Nodes
	 * @param start Starting Node object
	 * @param Target Target Node object
	 * @return The distance between the nodes as a double
	 */
	
	public double CalcDistance(Node start, Node Target) {
		double deltaX = Target.X - start.X;
		double deltaY = Target.Y - start.Y;
		double magnitude = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
		return magnitude;
	}
	
	/**
	 * Sets the angle of the audio based on the position of the player's parent Node.
	 * <p>
	 * This is automatically called in the A_Star algorithm.
	 * @param player The Player object
	 * @param start The Source object
	 */
	
	public void SetAngle(Node player,Source start) {
		Node Node = player.Getparent();
		Vector2D PlayerToNode = new Vector2D(Node.X-player.X, Node.Y-player.Y);
		double Angle = Math.atan2(-PlayerToNode.y, PlayerToNode.x);
		start.setAngleOFPlayer(Angle);
	}
	
	/**
	 * This is used to sort the A_Star's open list with the lowest F_cost and H_cost
	 * being first in the queue, effectively acting as a priority queue.
	 * <p>
	 * Called on creating an object of SoundEngine.
	 */
	// Comparator sorts F and H cost by lowest being first.
	private void SetUpComparator() {
		comparator = new Comparator<Node>() {
		public int compare(Node x, Node y) {
			// x is second node
			// y is first node
			if(x.getF_cost() < y.getF_cost()) {
				return -1; //Moves x forward in list if lower
			}
			
			if (x.getF_cost() == y.getF_cost()) {
				if (x.getH_cost() < y.getH_cost()) {
					return -1;
				}
			}
			
			if (x.getF_cost() > y.getF_cost()) {
				return +1;//Moves x backwards in list if greater
			}
			
			if (x.getF_cost() == y.getF_cost()) {
				if (x.getH_cost() > y.getH_cost()) {
					return +1;
				}
			}
			
			return 0; //If same F and H cost then do not change
			}
		};
	}
	
	private double Optimise(Node current, Node neighbour, ArrayList<Wall> walls2, ArrayList<Node> nodes2){
		Map<Node, Double> map = current.getLinks();
		double penalty = 0;
		
		if (map.get(neighbour) != null) {
			penalty = map.get(neighbour);
		}
		
		else if (current.getClass() == Source.class || neighbour.getClass() == Player.class) {
			penalty = interfere(current,neighbour,walls2);
		}
		
		return penalty;
	}
	
	/**
	 * The A_Star algorithm that finds the shortest path between the Source and Player.
	 * @param start Source object where the audio is played from.
	 * @param finish Player object where the goal of the algorithm is.
	 * @param nodes The list of all Node objects that the algorithm can travel through.
	 * @param walls The list of all Wall objects that interact with the path.
	 * @return The Player object of which its parent can be traced to reveal the shortest path.
	 */
	
	public Node AStar(Source start,Player finish, ArrayList<Node> nodes, ArrayList<Wall> walls) { 
		ArrayList<Wall> walls2 = new ArrayList<Wall>();//Copy of walls ArrayList to fix concurrent modification error
		ArrayList<Node> Closed = new ArrayList<Node>();// Nodes are seen and evaluated
		ArrayList<Node> Open = new ArrayList<Node>(); // Nodes are seen but not evaluated
		ArrayList<Node> nodes2 = new ArrayList<Node>();// To check every node without affecting global storage (avoid concurrent modification error)
		walls2.addAll(walls);
		nodes2.addAll(nodes);
		nodes2.add(start);
		nodes2.add(finish);
		
		for (Node node: nodes2) {//Assumes that all other nodes are worst case.
			node.setG_cost(100000);//Positive as Lower values are better.
			node.setH_cost(100000);
			node.setF_cost(100000);
			node.MaxRange = start.getMaxRange(); //To calculate the volume based on Source audio range.
			node.Setparent(null);//To reset the nodes from last Astar calculation
			node.checked = false;
			node.travelled = false;
		}
		
		if ((No_Walls != walls2.size()) || (No_Nodes != nodes2.size())){
			for (int i = 0; i <= nodes.size()-1; i++) {
				Map<Node, Double> map = nodes.get(i).getLinks();
				for (int j = 0; j <= nodes.size()-1; j++) {
					double penalty = interfere(nodes.get(i),nodes.get(j),walls2);
					map.put(nodes.get(j), penalty);
				}				
			}
			No_Walls = walls2.size();
			No_Nodes = nodes2.size();
		}
		
//		Euclidean distances to nodes
		start.setG_cost(0);//Distance from start to start
		start.setH_cost(CalcDist(start, finish));//Cost to finish as a percentage
		start.setF_cost(start.getG_cost() + start.getH_cost());
		start.Volume = 0;//Reset volume
		Open.add(start);//To expand
		
		while(!Open.isEmpty()) {//While there is a path/solution to problem
			Collections.sort(Open,comparator);//Sort by lowest F-cost and H-Cost
			Node current = Open.get(0);
			//Finish check
			if (current.equals(finish)) {
				start.Volume = Math.min(1, current.getG_cost());
				SetAngle(current,start);
				return (current);
			}
			Open.remove(current);//Node is now considered to be evaluated
			
			for (Node neighbour:nodes2) {
//				Does not check itself or its parent
				if ((neighbour == current) || (current.Getparent() == neighbour)) {
					continue;
				}
				
				boolean IsOpen = Open.contains(neighbour);//Is neighbour in the open list?
				boolean IsClosed = Closed.contains(neighbour);//Is neighbour in the closed list?
				
//				 Distance From start to neighbour.
				
				//double penalty = interfere(current,neighbour,walls2);//Reduce number of walls checked
				double penalty = Optimise(current, neighbour, walls2, nodes2);
				double costFromStart = current.getG_cost() + CalcDist(current, neighbour) + penalty;
				
//				The node is not in the open or closed list OR a better path has been found
				if ((!IsOpen && !IsClosed) || costFromStart < neighbour.getG_cost()) {
					neighbour.Setparent(current);
					neighbour.setG_cost(costFromStart);
					neighbour.setH_cost(CalcDist(neighbour, finish));
					neighbour.setF_cost(neighbour.getG_cost() + neighbour.getH_cost());
					if (IsClosed) {
						Closed.remove(neighbour);
					}
					if (!IsOpen) {
						Open.add(neighbour);
					}
				}
			}
//			Node has actually been seen & expanded
			Closed.add(current);
			current.checked = true;
		}
		return null;
	}	
}
