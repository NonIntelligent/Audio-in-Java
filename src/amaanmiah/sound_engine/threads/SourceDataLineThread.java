package amaanmiah.sound_engine.threads;

import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;

import amaanmiah.sound_engine.objects.Source;

public class SourceDataLineThread implements Runnable{
	
	private volatile boolean active = false;
	private SourceDataLine sourceDataLine = null;
	private Thread audiothread;
	private final AudioInputStream ais;
	private byte[] buffer;
	private Source source;
	
	public SourceDataLineThread(SourceDataLine sourcedataline, AudioInputStream ais,
			int workbuffersizer, Source source) {
		this.source = source;
		this.ais = ais;
		this.buffer = new byte[workbuffersizer];
		this.sourceDataLine = sourcedataline;
	}
	
	@Override
	public void run(){
		//System.out.println("buffer size: " + buffer.length);
		AudioInputStream ais = this.ais;
		SourceDataLine sourcedataline = this.sourceDataLine;
		int total = 0;
		int numBytesRead = 0;
		int numBytesToRead = buffer.length;
		//Sample rate == Frame Rate
		int framesize = ais.getFormat().getFrameSize();
		long framelength = ais.getFrameLength();
		
		float TotalToRead = framelength * framesize;
		
		while (active) {
			try {
				while (total < TotalToRead && source.playing) {
					// Read from audio source
					numBytesRead = ais.read(buffer,0,numBytesToRead);
					if (numBytesRead == -1) break;
					total += numBytesRead;
					sourcedataline.write(buffer,0,numBytesRead);
				}
				
				if (source.playing) {
					total = 0;
					sourcedataline.drain();
					sourcedataline.stop();
					source.playing = false;
				}
				Thread.sleep(200);
			} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException | IOException | InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
	
	public synchronized void start() {
		if (active)
			return;
		active = true;
		audiothread = new Thread(this);
		audiothread.setDaemon(true);
		audiothread.start();
	}
	
	public synchronized void stop() {
		if (!active)
			return;
		active = false;
		try {
			audiothread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
