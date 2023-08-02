package amaanmiah.sound_engine.hardwareinput;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import amaanmiah.sound_engine.objects.Player;

public class KeyInput extends KeyAdapter{
	Player player;
	private int mode = 0; // 0 is player, 1 is camera
	private static int Pspeed = 2;
	private static int Cspeed = 5;
	public int CamX = 0,CamY = 0;
	public int CamXSpeed = 0,CamYSpeed = 0;
	public int ViewPortX, ViewPortY;
	public int OffsetMAX_X, OffsetMAX_Y;
	public int OffsetMIN_X = 0, OffsetMIN_Y = 0;
	
	public KeyInput(Player player) {
		this.player = player;
	}
	
	
	public void SetUpCamera(int x, int y) {
		ViewPortX = x;
		ViewPortY = y;
		OffsetMAX_X = 10000 - ViewPortX;
		OffsetMAX_Y = 10000 - ViewPortY;
	}
	
	public void updateCamera() {
		CamX += CamXSpeed;
		CamY += CamYSpeed;
	}
	
	
	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		int modifier = e.getModifiers();
		// Player or Camera does not move while using shortcuts
		if (modifier == KeyEvent.CTRL_MASK && key == KeyEvent.VK_S) {
			return;
		}
		
		if (modifier == KeyEvent.CTRL_MASK && key == KeyEvent.VK_D) {
			return;
		}
		
		// Change controlling mode (Player or Camera)
		if (key == KeyEvent.VK_SPACE){
        	if (mode == 0) {
        		mode = 1;
        		player.setPxs(0);
        		player.setPys(0);
        	}else {
        		mode = 0;	
        	}
        }
		        
		if (mode == 0) {// Controlling player
			if (key == KeyEvent.VK_A) {//Left Player
				player.setPxs(-Pspeed);
			}

			if (key == KeyEvent.VK_D) {//Right Player
        		player.setPxs(Pspeed);
        	}

        	if (key == KeyEvent.VK_W) {//Up Player
        		player.setPys(-Pspeed);
        	}

        	if (key == KeyEvent.VK_S) {//Down Player
        		player.setPys(Pspeed);
        	}
		}

        if (mode == 1) {// Controlling Camera
	        if (key == KeyEvent.VK_A) {//Left Camera
	            CamXSpeed = -Cspeed;
	        }
	
	        if (key == KeyEvent.VK_D) {//Right Camera
	        	CamXSpeed = Cspeed;
	        }
	
	        if (key == KeyEvent.VK_W) {//Up Camera
	        	CamYSpeed = -Cspeed;
	        }
	
	        if (key == KeyEvent.VK_S) {//Down Camera
	        	CamYSpeed = Cspeed;
	        }
        }
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		//Resets speed to 0 on key release
		if (mode == 0) {//Controlling Player
			if (key == KeyEvent.VK_A) {//Left
				player.setPxs(0);
	        }
	
	        if (key == KeyEvent.VK_D) {//Right
	        	player.setPxs(0);
	        }
	
	        if (key == KeyEvent.VK_W) {//Up
	        	player.setPys(0);
	        }
	
	        if (key == KeyEvent.VK_S) {//Down
	        	player.setPys(0);
	        }
		}
		
		if (mode == 1) {//Controlling Camera
			if (key == KeyEvent.VK_A) {//Left
				CamXSpeed = 0;
	        }
	
	        if (key == KeyEvent.VK_D) {//Right
	        	CamXSpeed = 0;
	        }
	
	        if (key == KeyEvent.VK_W) {//Up
	        	CamYSpeed = 0;
	        }
	
	        if (key == KeyEvent.VK_S) {//Down
	        	CamYSpeed = 0;
	        }
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		//nothing here
	}
}
