package amaanmiah.sound_engine.threads;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import amaanmiah.sound_engine.engine.SoundData;
import amaanmiah.sound_engine.objects.Source;

public class Thread2 implements Runnable{
	
	private volatile boolean active = false;
	private SourceDataLine srcDataLine = null;
	private Thread audiothread;
	private AudioInputStream ais;
	private byte[] buffer;
	private Source source;
	private float LastPan = 0.8f;
	
	public Thread2(SourceDataLine sourcedataline, AudioInputStream ais,
			int workbuffersizer, Source source) {
		this.source = source;
		this.ais = ais;
		this.buffer = new byte[workbuffersizer];
		this.srcDataLine = sourcedataline;
	}
	
	@Override
	public void run(){
		AudioFormat format = ais.getFormat();
		int framesize = format.getFrameSize();
		int channels = format.getChannels();
		
		byte ByteArray [] = new byte[4096];
		
		int buffer = ByteArray.length / framesize * channels;
		
		float samples [] = new float[buffer];
		
		
		while (active) {
			try {
				if (source.playing) {
					for (int bLen = 0; (bLen = ais.read(ByteArray)) != -1;) {
						int sLen = 0;
						sLen = SoundData.decode(ByteArray, samples, bLen, format);
						for (int c = 0; c < ByteArray.length; c++) {
							//System.out.println(ByteArray[c]);
						}
						//Do something with samples
						for (int i = 0; i < samples.length; i++) {
							double angle = source.getPlayerAngle();
							float pan = (float) Math.cos(angle);
							float DeltaPan = Math.abs(pan - LastPan);
							float average = 0f;
							
							if (pan <= 0) {//If on left-hand side
								if (i % 2 == 0)//If Left channel
									samples[i] *= 1f * (1 - source.Volume);
								else//On right channel
									samples[i] *= (pan + 1f) * (1 - source.Volume);
							}
							else {//On right-hand side
								if (i % 2 == 0)//If Left channel
									samples[i] *= (1f - pan) * (1 - source.Volume);
								else//On Right channel
									samples[i] *= 1f * (1 - source.Volume);
							}
							
							/*if (DeltaPan > 0.8 && i != 0)
								samples[i] = (samples[i-1] + samples[i]) / 2;*/
							
							LastPan = pan;
						}
						
						bLen = SoundData.encode(samples, ByteArray, sLen, format);
						
						srcDataLine.write(ByteArray, 0, bLen);
						
					}
				}
				
				if (source.playing) {
					srcDataLine.drain();
					srcDataLine.stop();
					ais.close();
					if (source.SoundPath.toString().contains("audio")) {
						ais = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("audio/"+source.SoundPath.getName()));
					} else {
						ais = AudioSystem.getAudioInputStream(source.SoundPath);
					}
					source.playing = false;
				}
				Thread.sleep(200);
			} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException | IOException | InterruptedException | UnsupportedAudioFileException e) {
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
