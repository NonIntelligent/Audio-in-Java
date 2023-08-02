package amaanmiah.sound_engine.simulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Launcher implements ActionListener{
	private Display Menu;
	File CustomSettings;
	String text = "";
	Map<String, String> Settings;
	
	public Launcher() {
		try {
			//Changes the design of buttons
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("UI class not found");
		} catch (InstantiationException e) {
			e.printStackTrace();
			System.out.println("UI cannot be initialised");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.out.println("No access to class");
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			System.out.println("Look and Feel not supported");
		}
		Settings = new HashMap<String, String>();
		CreateDirectory();//Create custom directory
		ReadCustomSetting();//Read from custom settings in created directory
		Menu = new Display(860,540, this);
	}
	

	public static void main(String []args) {
		@SuppressWarnings("unused")
		Launcher menu = new Launcher();
	}
	
	private void CreateDirectory() {
		String root = Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		root = root.substring(0, 4);
		File CustomSettingsPath = new File(root+"Users/"+System.getProperty("user.name")+"/Documents/My Games/SoundEngine/");
		boolean dir = CustomSettingsPath.mkdirs();//Create the directory
		if (dir) {//Directory was just created so create custom settings file
			CreateCustomSettings(CustomSettingsPath);
		}
		else {//Directory is already made
			CustomSettings = new File(CustomSettingsPath.getAbsolutePath()+"/CustomSettings.txt");
		}
	}
	
	private void CreateCustomSettings(File dir) {
		try {//Sets up the custom settings file
			InputStream in = Launcher.class.getClassLoader().getResourceAsStream("Defaultsettings.txt");
			BufferedReader read = new BufferedReader(new InputStreamReader(in));
			String line = read.readLine();
			text = "";
			while (line != null) {
				text += line + "\n";
				line = read.readLine();
			}
			read.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		CustomSettings = new File(dir.getAbsolutePath()+"/CustomSettings.txt");
		try {
			BufferedWriter write = new BufferedWriter(new FileWriter(CustomSettings));
			write.write(text);
			write.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void ReadCustomSetting() {
		try {//Reads from the custom settings and stores in Settings map
			FileInputStream in = new FileInputStream(CustomSettings);
			BufferedReader read = new BufferedReader(new InputStreamReader(in));
			String line = read.readLine();
			text = "";
			while (line != null) {
				if (line.contains("=")) {
					String[] parts = line.split("(?<==)");
					Settings.put(parts[0], parts[1]);
				}
				if ((!line.contains("=") || (line.contains("/////")))){
					text += line + "\n";
				}
				line = read.readLine();
			}
			read.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void WriteSetting() {
		try {//Writes changes made in settings menu to the CustomSetting.txt file
			BufferedWriter write = new BufferedWriter(new FileWriter(CustomSettings));
			write.write(text);
			write.write("audiopath=" + Settings.get("audiopath=") + "\n");
			write.write("resolution=" + Settings.get("resolution=") + "\n");
			write.write("ticks=" + Settings.get("ticks=") + "\n");
			write.write("fps=" + Settings.get("fps=") + "\n");
			write.write("device=" + Settings.get("device=") + "\n");
			write.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == Menu.Start) { //User clicks START
			File test = new File(Settings.get("audiopath="));
			if (!test.exists() || test.equals(null)) {//Path directory validation
				String message = "Folder path does not exist. Change directory in settings";
				JOptionPane.showMessageDialog(Menu.L_frame, message,"Path Error", JOptionPane.INFORMATION_MESSAGE);
				//Pop up error message, inform user to change audio file directory
				return;
			}
			Menu.L_frame.dispose();
			Sound_sim simul = new Sound_sim(Settings);
			simul.start();//start simulation loop
		}
		else if (e.getSource() == Menu.Settings) {
			Menu.SwitchToSettings();//Switch JPanel
		}
		else if (e.getSource() == Menu.Exit) {
			System.exit(0);//Close Program
		}
		else if (e.getSource() == Menu.MainMenu) {
			Menu.SwitchToMenu();
			Settings.put("ticks=", Menu.ticks.getSelectedItem().toString());
			Settings.put("fps=", Menu.fps.getSelectedItem().toString());
			Settings.put("device=", Menu.devices.getSelectedItem().toString());
			//write changes made
			WriteSetting();
		}
		else if (e.getSource() == Menu.AudioLoc) {
			//Only select directory to load audio files from
			JFileChooser chooseFile = new JFileChooser();
			chooseFile.setDialogTitle("Audio Location");
			chooseFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooseFile.setAcceptAllFileFilterUsed(false);
			if (chooseFile.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				File AudioPath = chooseFile.getSelectedFile();
				Settings.put("audiopath=", AudioPath.getAbsolutePath());
				Menu.AudioLoc.setText("AudioLocation: " + Settings.get("audiopath="));
			}
			
		}
	}
}
