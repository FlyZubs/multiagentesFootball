package example.keyboard_team;

import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class MainKB {

	public static void main(String[] args) throws UnknownHostException {
		//este frame serve apenas para receber os eventos das teclas
    	JFrame frame = new JFrame("Joystick -- Deixe esta janela aberta e use o teclado para controlar");
    	frame.add(new JLabel(new ImageIcon("joystick.jpg")));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(490, 360);
		frame.setVisible(true);

		KeyboardTeam team1 = new KeyboardTeam(true);
		//KeyboardTeam team2 = new KeyboardTeam(false);
		
		team1.launchTeamAndServer();
		//team2.launchTeam();
	}
	
}
