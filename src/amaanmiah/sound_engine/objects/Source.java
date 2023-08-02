package amaanmiah.sound_engine.objects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import amaanmiah.sound_engine.threads.SourceDataLineThread;
import amaanmiah.sound_engine.threads.Thread2;

public class Source extends Node{
	/**
	 * The Source object of which the audio is played from
	 * 
	 * @author Amaan Miah
	 */
	private static final long serialVersionUID = 7629584503864623303L;
	public File SoundPath;
	public long clipTimePosition;
	public transient Clip clip;
	public transient SourceDataLine sourceDataLine;
	public transient FloatControl gainControl;
	public transient FloatControl pan;
	public transient FloatControl balance;
	public double Volume = 0; //Volume in linear scale
	private double PlayerAngle;
	private static final int radius = 20;
	public boolean playing = false;
	public String mode = "";
	public AudioInputStream stream;
	public Mixer mixerSett;
	
	/**
	 * Creates a Source object
	 * @param x coordinate on the x-axis
	 * @param y coordinate on the y-axis
	 * @param soundpath The file path to the audio file
	 */
	
	
	public Source(int x, int y, File soundpath, Mixer mixer) {
		//Create object with filepath of audio
		super (x, y, radius);
		this.SoundPath=soundpath;
		this.MaxRange = 1000;
		this.mixerSett = mixer;
		try {
			clip = (Clip) mixerSett.getLine(new DataLine.Info(Clip.class, null));
			
			if (soundpath.toString().contains("audio")) {
				stream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("audio/"+soundpath.getName()));
			} else {
				stream = AudioSystem.getAudioInputStream(soundpath);
			}
			clip.open(stream);//Load the clip (time taken correlates to file size)
			setupClipControls();
		} catch (UnsupportedAudioFileException | MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			System.err.println("No active audio Drivers found");
		}
		
	}
	
	public int BufferSize = 2048;
	
	public Source(int x, int y, File soundpath, String name, Mixer mixer) {
		super(x, y, radius);
		mode = "SourceDataLine";
		this.SoundPath = soundpath;
		this.MaxRange = 1000;
		this.mixerSett = mixer;
		
		try {
			
			if (soundpath.toString().contains("audio")) {
				stream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("audio/"+soundpath.getName()));
			} else {
				stream = AudioSystem.getAudioInputStream(soundpath);
			}
		
			
			sourceDataLine = (SourceDataLine) mixerSett.getLine(new DataLine.Info(SourceDataLine.class, null));
			System.out.println("Buffer size on line creation: " + sourceDataLine.getBufferSize());
			//Sets format and alters buffer size in bytes
			//Buffer size affects latency
			sourceDataLine.open(stream.getFormat(), 4096);
			Thread2 srcthread = new Thread2(sourceDataLine, stream, BufferSize, this);
			srcthread.start();
			//SourceDataLineThread srcdatathread = new SourceDataLineThread(sourceDataLine, stream, BufferSize, this);
			//srcdatathread.start();
			setupSourceDataLineControls();
		} catch(IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Overrides the Node classes render object method
	 * so that this object is differentiated from Node
	 */
	@Override
	public void RenderObject(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillOval(X-(HalfR), Y-(HalfR), Radius, Radius);
		g.setColor(Color.GREEN);
		g.fillOval(X-2, Y-2, 4, 4);
	}
	
	/**
	 * Gets the angle of the player to the last node.
	 * @return The angle as a Double in radians
	 */
	
	public double getPlayerAngle() {
		return PlayerAngle;
	}
	
	/**
	 * Sets the player angle of the Source
	 * @param PlayerAngle the angle in radians
	 */
	public void setAngleOFPlayer(double PlayerAngle) {
		this.PlayerAngle = PlayerAngle;
	}
	/**
	 * Sets the clip of the Source so that it output the correct
	 * audio to the user.
	 * @param clip The clip to set to Source
	 */
	
	private void setupClipControls() {
		this.gainControl = (FloatControl) this.clip.getControl(FloatControl.Type.MASTER_GAIN);
		if (this.clip.isControlSupported(FloatControl.Type.PAN)) {
			this.pan = (FloatControl) this.clip.getControl(FloatControl.Type.PAN);
			this.balance = (FloatControl) this.clip.getControl(FloatControl.Type.BALANCE);
		}
	}
	
	private void setupSourceDataLineControls() {
		this.gainControl = (FloatControl) this.sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
		if (this.sourceDataLine.isControlSupported(FloatControl.Type.PAN)) {
			this.pan = (FloatControl) this.sourceDataLine.getControl(FloatControl.Type.PAN);
			this.balance = (FloatControl) this.sourceDataLine.getControl(FloatControl.Type.BALANCE);
		}
	}
		
	public void ReloadClips() {
		try {
			clip = (Clip) mixerSett.getLine(new DataLine.Info(Clip.class, null));
			AudioInputStream stream;
			
			if (SoundPath.toString().contains("audio")) {
				stream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("audio/"+SoundPath.getName()));
			} else {
				stream = AudioSystem.getAudioInputStream(SoundPath);
			}
			clip.open(stream);//Load the clip (time taken correlates to file size)
			setupClipControls();
		} catch (UnsupportedAudioFileException | MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			System.err.println("No active audio Drivers found");
		}
	}
	
	public void ReloadDataLine() {
		try {
			AudioInputStream stream;
			
			if (SoundPath.toString().contains("audio")) {
				stream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("audio/"+SoundPath.getName()));
			} else {
				stream = AudioSystem.getAudioInputStream(SoundPath);
			}
			sourceDataLine = (SourceDataLine) mixerSett.getLine(new DataLine.Info(SourceDataLine.class, null));
			System.out.println("Buffer size on line creation: " + sourceDataLine.getBufferSize());
			//Sets format and alters buffer size in bytes
			//Buffer size affects latency
			sourceDataLine.open(stream.getFormat());
			SourceDataLineThread srcdatathread = new SourceDataLineThread(sourceDataLine, stream, BufferSize, this);
			srcdatathread.start();
			setupSourceDataLineControls();
			
		} catch (UnsupportedAudioFileException | MalformedURLException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			System.err.println("No Active audio Drivers found");
		}
	}
	
}
