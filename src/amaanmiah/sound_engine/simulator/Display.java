package amaanmiah.sound_engine.simulator;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;

import amaanmiah.sound_engine.engine.SoundEngine;
import amaanmiah.sound_engine.hardwareinput.MouseInput;
import amaanmiah.sound_engine.objects.Source;
import amaanmiah.sound_engine.objects.Storage;



public class Display implements ActionListener{
	private Sound_sim soundsim;
	JFrame L_frame;
	CardLayout CL;
	JPanel Cards, L_panel, S_panel;
	public JButton Start, Settings, Exit, MainMenu;
	public JButton AudioLoc;
	public String title = "Sound Engine";
	public JComboBox<String> fps,ticks, devices;
	public int MainWidth, MainHeight;
	private JButton Select, Node, Source, Wall, Play, Stop;
	private JButton LastTool = null;
	private JMenuBar MenuBar;
	private JMenuItem save,load,newFile;
	private Storage storage;
	private SoundEngine engine;
	private MouseInput M_Input;
	private final URL Textures = getClass().getClassLoader().getResource("Textures/Toolbar.fw.png");
	JComboBox<String> AudioList;
	Map<String,File> FileMap = new HashMap<String, File>();
	BufferedImage a = null;
	
	public Display(Sound_sim sim,Storage objects,MouseInput MInput, SoundEngine sEngine) {
		this.soundsim = sim;
		this.storage = objects;
		this.M_Input = MInput;
		this.engine = sEngine;
		SetupTools();
		MenuBar();
		createMainDisplay();
	}
	
	public Display(int width, int height,Launcher menu){//Creates Launch Menu
		CL = new CardLayout();
		Cards = new JPanel(CL);
		LauncherButtons(menu);//Method to create buttons
		SettingsComponents(menu);//Method to create all Components in Settings menu
		LauncherDisplay(width,height);//Method to create Display
	}
	
	private void LauncherButtons(Launcher menu) {
		//Create Buttons
		Start = new JButton("Start");
		Settings = new JButton("Settings");
		Exit = new JButton("Exit");
		MainMenu = new JButton("Main-Menu");
		//Set Bounds
		Start.setBounds(345, 240, 150, 75);
		Settings.setBounds(345, 325, 150, 75);
		Exit.setBounds(345, 410, 150, 75);
		MainMenu.setBounds(345, 410, 150, 75);
		//Add Action Listener from Launcher class
		Start.addActionListener(menu);
		Settings.addActionListener(menu);
		Exit.addActionListener(menu);
		MainMenu.addActionListener(menu);
		//Set Focusable to avoid taking focus from the Launcher when created
		Start.setFocusable(false);
		Settings.setFocusable(false);
		Exit.setFocusable(false);
		MainMenu.setFocusable(false);
	}
	
	private void SettingsComponents(Launcher menu) {
		//Create Components
		//Audio location is just a button
		AudioLoc = new JButton("AudioLocation: " + menu.Settings.get("audiopath="));
		fps = new JComboBox<String>();
		fps.setToolTipText("Max Fps");
		fps.addItem(menu.Settings.get("fps="));//adds the saved setting at index 0 to display
		//Add other value options
		fps.addItem("10");
		fps.addItem("40");
		fps.addItem("60");
		fps.addItem("80");
		fps.addItem("Unlimited");
		fps.setSelectedIndex(0);
		//ticks drop-down menu
		ticks = new JComboBox<String>();
		ticks.setToolTipText("Ticks per Second");
		ticks.addItem(menu.Settings.get("ticks="));
		ticks.addItem("10");
		ticks.addItem("40");
		ticks.addItem("60");
		ticks.addItem("80");
		ticks.setSelectedIndex(0);
		// Select audio device
		Mixer.Info[] mixerinfo = AudioSystem.getMixerInfo();//Acquires active sound drivers from computer
		devices = new JComboBox<String>();
		devices.setToolTipText("Choose audio device");
		devices.addItem(menu.Settings.get("device="));
		for (Info info : mixerinfo) {
			devices.addItem(info.getName());
		}
		
		//Set Bounds
		AudioLoc.setBounds(300, 20, 300, 50);
		fps.setBounds(300, 100, 300, 50);
		ticks.setBounds(300, 180, 300, 50);
		devices.setBounds(300, 260, 300, 50);
		//Add Action Listener from Launcher class
		AudioLoc.addActionListener(menu);
	}

	private void LauncherDisplay(int width, int height) {
	//Launcher main panel where start,setting and exit
	//buttons are added
	L_panel = new JPanel();
	L_panel.setLayout(null);
	L_panel.add(Start);
	L_panel.add(Settings);
	L_panel.add(Exit);
	L_panel.setVisible(true);
	L_panel.setBounds(0, 0, width, height);
	L_panel.setBackground(Color.DARK_GRAY);
	
	//Settings panel where all of the
	//settings components are added
	S_panel = new JPanel();
	S_panel.setLayout(null);
	S_panel.add(MainMenu);
	S_panel.add(AudioLoc);
	S_panel.add(fps);
	S_panel.add(ticks);
	S_panel.add(devices);
	S_panel.setVisible(false);
	S_panel.setBounds(0, 0, width, height);
	S_panel.setBackground(Color.DARK_GRAY);
	
	//This allows the panels to be swapped more easily
	
	Cards.add(L_panel,"Menu");
	Cards.add(S_panel,"Settings");
	
	//Launcher window is created
	L_frame = new JFrame("Launcher");
	L_frame.setSize(width,height);
	L_frame.add(Cards);
	L_frame.setBackground(Color.GREEN);
	L_frame.setResizable(false);
	L_frame.setLocationRelativeTo(null);
	L_frame.setVisible(true);
	L_frame.setFocusable(true);
	L_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void createMainDisplay() {
		JFrame frame;
		frame = new JFrame(title);
		frame.getContentPane().setBackground(Color.DARK_GRAY);
		frame.setLayout(null);
		frame.add(AudioList);
		frame.add(Select);
		frame.add(Node);
		frame.add(Source);
		frame.add(Wall);
		frame.add(Play);
		frame.add(Stop);
		frame.setJMenuBar(MenuBar);
		frame.setSize((int) (soundsim.getWidth()*.75), (int) (soundsim.getHeight()*.75));
		this.MainWidth = frame.getWidth();
		this.MainHeight = frame.getHeight();
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
		frame.add(soundsim);
		frame.setVisible(true);
		frame.setFocusable(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.validate();
	}
	
	private void MenuBar() {
		MenuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_J);
		menu.getAccessibleContext().setAccessibleDescription("the menu items");
		//Menu items
		save = new JMenuItem("Save");
		load = new JMenuItem("Load");
		newFile = new JMenuItem("New");
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK));
		newFile.addActionListener(this);
		save.addActionListener(this);
		load.addActionListener(this);
		menu.add(newFile);
		menu.add(save);
		menu.add(load);
		MenuBar.add(menu);
	}
	
	private void SetupTools() {
		//Sets up ComboBox to change the output sound of the Source
		//FileMap is used for access to the file by using the name
		//This makes the drop-down menu (AudioList) readable
		String [] names = new String[engine.AudioFiles.size()+4];
		int  i = 0;
		for (File file:engine.AudioFiles) {
			FileMap.put(file.getName(), file);
			names[i] = file.getName();
			i++;
		}
		//Load files from resource
		File doh = new File(getClass().getClassLoader().getResource("audio/doh.wav").toExternalForm());
		File t1 = new File(getClass().getClassLoader().getResource("audio/t1_be_back.wav").toExternalForm());
		File Wilhelm = new File(getClass().getClassLoader().getResource("audio/WilhelmScream.wav").toExternalForm());
		File type = new File(getClass().getClassLoader().getResource("audio/type-writing.wav").toExternalForm());
		
		names [i] = doh.getName();
		names [i+1] = t1.getName();
		names [i+2] = Wilhelm.getName();
		names [i+3] = type.getName();
		
		FileMap.put(doh.getName(), doh);
		FileMap.put(t1.getName(), t1);
		FileMap.put(Wilhelm.getName(), Wilhelm);
		FileMap.put(type.getName(), type);
		//Add to drop-down menu
		AudioList = new JComboBox<String>(names);
		AudioList.setBounds(300, 100, 200, 75);
		AudioList.setSelectedIndex(-1);
		AudioList.addActionListener(this);
		//Create tool buttons and borders
		
		LastTool = new JButton("");
		//Setup buttons for tools
		Select = SetupButton(0);
		Node = SetupButton(40);
		Source = SetupButton(80);
		Wall = SetupButton(120);
		Play = SetupButton(160);
		Stop = SetupButton(200);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == AudioList) {
		engine.to_play = FileMap.get(AudioList.getSelectedItem());
			AudioList.setFocusable(false);
			}
		//Sets the current tool to whichever button was clicked
		else if (e.getSource() == Select) {
			M_Input.settool(0);
			LastTool.setSelected(false);
			Select.setSelected(true);
			LastTool = Select;
		}
		else if (e.getSource() == Node) {
			M_Input.settool(1);
			LastTool.setSelected(false);
			Node.setSelected(true);
			LastTool = Node;
		}
		else if (e.getSource() == Source) {
			M_Input.settool(2);
			LastTool.setSelected(false);
			Source.setSelected(true);
			LastTool = Source;
		}
		else if (e.getSource() == Wall) {
			M_Input.settool(3);
			LastTool.setSelected(false);
			Wall.setSelected(true);
			LastTool = Wall;
		}
		//play audio
		else if ((e.getSource() == Play) && (storage.Emitters.size() != 0)){
			for (Source source :storage.Emitters) {
				try {
					engine.Play(source);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			LastTool.setSelected(false);
			Play.setSelected(true);
			LastTool = Play;
			}
		//Stop audio
		else if ((e.getSource() == Stop) && (storage.Emitters.size() != 0)){
			storage.StopEmitters();
			LastTool.setSelected(false);
			Stop.setSelected(true);
			LastTool = Stop;
		}
		//Reset map
		else if (e.getSource() == newFile) {
			storage.Emitters.clear();
			storage.Nodes.clear();
			storage.Walls.clear();
			soundsim.SourcePaths.clear();
			newFile.setFocusable(false);
		}
		//Save map
		else if (e.getSource() == save) {
			soundsim.SaveMap();
			save.setFocusable(false);
		}
		//Load Map
		else if (e.getSource() == load) {
			soundsim.LoadMap();
			load.setFocusable(false);
		}
			
	}
	
	public void SwitchToSettings() {
		CL.show(Cards, "Settings");
	}
	
	public void SwitchToMenu() {
		CL.show(Cards, "Menu");
	}
	// Reads the Texture file and aqcuires a subimage depending on parameters
	//y is the starting pixel in y-axis, n is to denote alternate icon 
	private void ButtonImage(int y,int n) {
		try {
			a = ImageIO.read(Textures);
			if (n==1) {// Get the alternate icon
				a = a.getSubimage(40, y, 40, 40);
				return;
			}
			//Acquires a 40x40 pixel size icon, y specifying the icon
			a = a.getSubimage(0, y, 40, 40);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// Creates button and sets the correct icon for that button
	private JButton SetupButton(int p) {// parameter used to get the correct icon
		ButtonImage(p,0);//Sets current buffered image to an icon specified by parameter 'p'
		JButton button = new JButton(new ImageIcon(a));
		button.setBorder(new LineBorder(Color.BLACK));
		ButtonImage(p,1);//Sets current buffered image to an alternate icon
		button.setSelectedIcon(new ImageIcon(a));
		//set size and position of buttons
		button.setBounds(10, p+60, 40, 40);
		//Allow class to listen to user input and run methods as result
		button.addActionListener(this);
		//So that user does not lose keyboard control while clicking
		button.setFocusable(false);
		return button;
	}
}
	
