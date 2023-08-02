package amaanmiah.sound_engine.simulator;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import amaanmiah.sound_engine.engine.SoundEngine;
import amaanmiah.sound_engine.engine.SoundData;
import amaanmiah.sound_engine.hardwareinput.*;
import amaanmiah.sound_engine.objects.*;

public class Sound_sim extends Canvas implements Runnable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 792688322876969320L;
	private BufferStrategy bs;
	private SoundEngine engine;
	private Storage storage;
	private KeyInput keyinput;
	private MouseInput mouse;
	private Player player;
	private boolean running = false;
	private Thread thread;
	double fps = 60, ticks = 60;
	public File AudioPath;
	private Map<String, String> Settings;
	public ArrayList<ArrayList<Node>> SourcePaths;
	int TotalFps;
	public static Mixer mixer;
	Source test;
	
	public Sound_sim(Map<String, String> settings) {
		this.Settings = settings;
		ApplySettings();
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds(0, 0, (int)screensize.getWidth(), (int)screensize.getHeight());
		this.setBackground(Color.GRAY);
		SourcePaths = new ArrayList<ArrayList<Node>>();
		//Create other classes
		player = new Player(200, 400);
		engine = new SoundEngine();
		storage = new Storage(engine);
		keyinput = new KeyInput(player);
		mouse = new MouseInput(storage,engine);
		engine.AcquireSound(AudioPath);
		Display display = new Display(this,storage,mouse,engine);
		keyinput.SetUpCamera(display.MainWidth,display.MainHeight);
		
		//test = new Source(500, 500, new File(getClass().getClassLoader().getResource("audio/Intense_tingsdsds.wav").toExternalForm()), "Stream");
		//storage.AddSource(test);
		
		/*try {
			edit();
		} catch (IOException e) {
			e.printStackTrace();
		}*/

		}
		
	
	private void edit() throws IOException {
		//ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		//AudioSystem.write(test.stream, AudioFileFormat.Type.WAVE, byteArrayOutputStream);
		
		int FrameSize = test.stream.getFormat().getFrameSize();
		//This details the information/format of the file
		//byte ByteArray [] = byteArrayOutputStream.toByteArray();
		byte ByteArray [] = new byte[4096];
		//System.out.println("ByteArray: " + ByteArray.length + " Bytes");
		
		//byteArrayOutputStream.flush();
		//byteArrayOutputStream.reset();
		//byteArrayOutputStream = null;
		
		
		//No. of bytes * framesize
		int buffer = ByteArray.length / FrameSize * test.stream.getFormat().getChannels();
		//System.out.println(buffer);
		
		long numberofFrames = test.stream.getFrameLength();
		
		int numberofSamples = (int) (numberofFrames * test.stream.getFormat().getChannels());
		
		//System.out.println("No. of samples: " + numberofSamples);
		
		float samples [] = new float[buffer];
		int length = ByteArray.length;
		AudioFormat format = test.stream.getFormat();
		test.sourceDataLine.start();
		
		//test.balance.s
		
		for (int bLen = 0; (bLen = test.stream.read(ByteArray)) != -1;) {
			int sLen = 0;
			sLen = SoundData.decode(ByteArray, samples, bLen, format);
			
			//Do something with samples
			for (int i = 0; i < samples.length-1; i++) {
				if (i % 2 == 0)//If Left channel
					samples[i] *= 0;
				
				if (i % 2 != 0)//If Right channel
					samples[i] = samples[i];
					
			}
			
			bLen = SoundData.encode(samples, ByteArray, sLen, format);
			
			test.sourceDataLine.write(ByteArray, 0, bLen);
			
		}
	}
	
	//updates all variables per tick
	private void tick(){
		player.PlayerMovement();//Updates player co-ords
		keyinput.updateCamera();//Updates camera position
		updateWall();//update variable of drawn walls
		CameraCollision();//Checks Camera is not out of bounds
		PlayerCollision();//Checks Player is not colliding at map limits or walls
		SetPathV2();//Calculate shortest path between Nodes
		SetAudioProperties();// Alter the audio based on Source
	}

	private void render(){
		bs = getBufferStrategy();
		if(bs == null){
			createBufferStrategy(3);
			return;
		}
		do {
			Graphics2D g2d = (Graphics2D) bs.getDrawGraphics();
			
			String playerco = "Player Co-ords: " + player.X + " ," + " " + player.Y;//Player co-ords
			String cameraco = "Camera Co-ords: " + keyinput.CamX + " ," + " " + keyinput.CamY;//Camera co-ords
			
			g2d.clearRect(0, 0, 10000, 10000);//clear screen 
			g2d.translate(-keyinput.CamX, -keyinput.CamY);//Allows camera to be moved across map
			
			storage.RenderWalls(g2d);// Draw Walls
			
			RenderRays(g2d);//render path to play from emitters
			
			//draw
			g2d.setColor(Color.BLACK);// Camera adjustment (+ CamX) so information stays in the same place on screen
			g2d.drawString(playerco, 300 + keyinput.CamX, 50 + keyinput.CamY);
			g2d.drawString(cameraco, 500 + keyinput.CamX, 50 + keyinput.CamY);
			
			//draw frames per second and number of objects
			g2d.drawString(String.valueOf(TotalFps), 50 + keyinput.CamX, 50 + keyinput.CamY);
			g2d.setColor(Color.WHITE);
			g2d.drawString("Number of Walls: " + storage.Walls.size(), 60 + keyinput.CamX, 100 + keyinput.CamY);
			g2d.setColor(Color.RED);
			g2d.drawString("Number of Nodes: " + storage.Nodes.size(), 60 + keyinput.CamX, 150 + keyinput.CamY);
			g2d.setColor(Color.BLACK);
			g2d.drawString("Number of Sources: " + storage.Emitters.size(), 60 + keyinput.CamX, 200 + keyinput.CamY);
			
			//render Nodes
			player.RenderObject(g2d);//positions stay fixed on the map
			storage.RenderEmitters(g2d);
			storage.RenderNodes(g2d);
			
			//fin
			g2d.dispose();
			bs.show();
		}while (bs.contentsLost());
	}
	
	//Tick methods
	
	private void CameraCollision() {
		//Collision bounds are from 0-10000 in both x and y axis
		//If camera screen is out of bounds then displace.
		if (keyinput.CamX > keyinput.OffsetMAX_X) {
			keyinput.CamX = keyinput.OffsetMAX_X;
			System.out.println("Collide Max X");
		}
		else if (keyinput.CamX < keyinput.OffsetMIN_X) {
			keyinput.CamX = keyinput.OffsetMIN_X;
			System.out.println("Collide Min X");
		}
		
		if (keyinput.CamY > keyinput.OffsetMAX_Y) {
			keyinput.CamY = keyinput.OffsetMAX_Y;
			System.out.println("Collide Max Y");
		}
		else if (keyinput.CamY < keyinput.OffsetMIN_Y) {
			keyinput.CamY = keyinput.OffsetMIN_Y;
			System.out.println("Collide Min Y");
		}
	}
	
	private void PlayerCollision() {
		//Player Collision against Map limits
		if (player.X < 0) {
			player.X = 0;
		}
		else if (player.X > 10000) {
			player.X = 10000;
		}
		
		if (player.Y < 0) {
			player.Y = 0;
		}
		else if (player.Y > 10000) {
			player.Y = 10000;
		}
		//Player collision against Wall objects
		for (Wall wall : storage.Walls) {
			if (!wall.built) {
				continue;
			}
			//Player co-ords
			int Px = player.X;
			int Py = player.Y;
			//Vector A co-ords
			int Ax = wall.Va.x;
			int Ay = wall.Va.y;
			//Vector B X co-ord
			int Bx = wall.Vb.x;
			//Vector C Y co-ord
			int Cy = wall.Vc.y;
			
			
			if (Px > Ax && Px < Bx && Py > Ay && Py < Cy) {//Limits displacement to within the wall and not outside it.
				//Moving up and down a wall.
				if (player.getPys() > 0) {//If moving down past AB
					player.Y -= player.getPys()*2;//displace to AB
				} else if (player.getPys() < 0){//If moving up past CD
					player.Y -= player.getPys()*2;//displace to CD
				}
				//Moving left and right across a wall.
				if (player.getPxs() > 0) {//If moving over AC
					player.X -= player.getPxs()*2;//displace to AC
				} else if (player.getPxs() < 0){//If moving over BD
					player.X -= player.getPxs()*2;//displace to BD
				}
			}
		}
	}
	//Updates wall per tick while being created
	private void updateWall() {
		for (Wall wall:storage.Walls) {
			if (!wall.exists || wall.built) {
				continue;
			}
			wall.update();
		}
	}
	//Calls Astar and forms the path from each Source to Player
	private void SetPathV2() {
		if (storage.Emitters.size() != 0){
			SourcePaths = new ArrayList<ArrayList<Node>>();
			for (int i = 0;i <= storage.Emitters.size()-1;i++) {
				ArrayList<Node> A_star_Path = new ArrayList<Node>();
				Node current;
				current = engine.AStar(storage.Emitters.get(i), player, storage.Nodes, storage.Walls);
				A_star_Path.add(current);
				while (!(current.Getparent() == null)) {
					A_star_Path.add(current.Getparent());
					current.travelled = true;
					current = current.Getparent();
				}
				SourcePaths.add(A_star_Path);
			}
		}
	}
	//Changes the volume of the clip
	private void SetAudioProperties() {
		for (int i = 0;i <= storage.Emitters.size()-1;i++) {
			float DB = engine.SetVol(storage.Emitters.get(i).Volume);
			double angle = storage.Emitters.get(i).getPlayerAngle();
			//storage.Emitters.get(i).gainControl.setValue(DB);
			//storage.Emitters.get(i).balance.setValue((float)Math.cos(angle));
		}
	}
	
	//Render methods
	
	private void RenderRays(Graphics2D g){
		g.setColor(Color.MAGENTA);
		g.setStroke(new BasicStroke(2));
		for (int i = 0; i < SourcePaths.size();i++) {//For every Path
			int w = 0;
			while (w< SourcePaths.get(i).size()-1) {//Always at least 2 Nodes.
				Node start = SourcePaths.get(i).get(w);
				Node finish = SourcePaths.get(i).get(w+1);
				//Draw line between the 2 Node's centeres
				g.drawLine(start.X, start.Y, finish.X, finish.Y);
				double DeltaX = finish.X - start.X;
				double DeltaY = finish.Y - start.Y;
				int magnitude = (int) (Math.sqrt((DeltaX * DeltaX)+(DeltaY * DeltaY)));
				String distance = ("" + magnitude);//Unit distance between Nodes.
				g.drawString(distance, ((start.X + finish.X)/2) - 10 ,((start.Y + finish.Y)/2) -5);//Draw distance integer value
				w++;//Next Node
				
			}
		}
	}
	
	@Override
	public void run() {
		addKeyListener(keyinput);
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
		//Will tick and render at the specified value per second
		long lastTime = System.nanoTime();
		final double timePerTick = 1000000000 / ticks;
		final double timePerFrame= 1000000000 / fps;
		double deltaT = 0, deltaF = 0;
		long now;
		int frames = 0, ticks = 0;
		long timer = System.nanoTime();
		while(running){
			now = System.nanoTime();
			deltaT += (now - lastTime) / timePerTick;
			deltaF += (now - lastTime) / timePerFrame;
			lastTime = now;
			//If it is time to tick
			if (deltaT >= 1){
			tick();
			ticks++;
			deltaT--;
			}
			//If it is time to render
			if (deltaF >= 1) {
				render();
				frames++;
				deltaF--;
			}
			//After 1 second has past	
			if(System.nanoTime() - timer > 1000000000){
				System.out.println("FPS " + frames + " Ticks " + ticks);
				TotalFps = frames;
				frames = 0;
				ticks = 0;
				timer += 1000000000;
			}
		}
	}
	//Applies the setting from the Launcher to the simulator
	private void ApplySettings() {
		if (!Settings.get("fps=").equals("Unlimited")) {
			fps = Integer.parseInt(Settings.get("fps="));
		}
		else {
			fps = Integer.MAX_VALUE;
		}
		ticks = Integer.parseInt(Settings.get("ticks="));
		if (ticks < 0 || fps < 0) {
			ticks = 60;
			fps = 60;
		}
		AudioPath = new File(Settings.get("audiopath="));
		
		String devicename = Settings.get("device=");
		Mixer.Info[] mixerinfo = AudioSystem.getMixerInfo();//Acquires active sound drivers from computer
		for (Info info : mixerinfo) {
			if (info.getName().equals(devicename)) {
				mixer = AudioSystem.getMixer(info);
			}
		}
	}
	
	public void SaveMap() {
		try {
			//Can only select directories to save the map data
			JFileChooser chooseFolder = new JFileChooser();
			chooseFolder.setDialogTitle("Save Location");
			chooseFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooseFolder.setAcceptAllFileFilterUsed(false);
			if (chooseFolder.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				FileOutputStream saveFile = new FileOutputStream(chooseFolder.getSelectedFile().getAbsolutePath() + "/World.sav");
				ObjectOutputStream save = new ObjectOutputStream(saveFile);
				save.writeObject(storage.Emitters);// Clip and FloatControl interface had to be transient
				save.writeObject(storage.Nodes);//Can be saved Node had to be serialised
				save.writeObject(storage.Walls);//Can be saved wall and Vector2D had to be serialised
				save.writeInt(player.X);
				save.writeInt(player.Y);
				save.close();
				saveFile.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void LoadMap() {
		try {
			//Can only select the .sav file formats
			JFileChooser chooseFile = new JFileChooser();
			chooseFile.setDialogTitle("Load File");
			chooseFile.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooseFile.setAcceptAllFileFilterUsed(false);
			chooseFile.setFileFilter(new FileNameExtensionFilter(null, "sav"));
			if (chooseFile.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				FileInputStream loadFile = new FileInputStream(chooseFile.getSelectedFile());
				ObjectInputStream load = new ObjectInputStream(loadFile);
				//Objects are loaded in the same order that they were saved in
				storage.Emitters = (ArrayList<Source>) load.readObject();
				for (Source source: storage.Emitters) {
					//source.ReloadClips();
					source.ReloadDataLine();
					source.Volume = 0;
				}
				storage.Nodes = (ArrayList<Node>) load.readObject();
				storage.Walls = (ArrayList<Wall>) load.readObject();
				player.X = load.readInt();
				player.Y = load.readInt();
				load.close();
				loadFile.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	//Starts the simulation loop
	public synchronized void start(){
		if(running)
			return;
		running = true;
		thread = new Thread(this,"Main-Loop");
		thread.start();
	}
	
	//Stops the simulation loop
	public synchronized void stop(){
		if(!running)
			return;
		running = false;
		try {
			thread.join();
			System.out.println("Stopping thread");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
