package example.cmdline_team;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import easy_soccer_lib.PlayerCommander;
import easy_soccer_lib.perception.FieldPerception;
import easy_soccer_lib.perception.MatchPerception;
import easy_soccer_lib.perception.PlayerPerception;
import easy_soccer_lib.utils.EFieldSide;
import easy_soccer_lib.utils.Vector2D;

public class CommandLinePlayer extends Thread {
	private int LOOP_INTERVAL = 10;  //0.02s
	
	private PlayerCommander commander;
	private PlayerPerception selfPerc;
	private FieldPerception  fieldPerc;
	private MatchPerception matchPerc;
	
	private String command;
	private boolean hasNewCommand;
	
	/**
	 * Abaixo, segue o comando seguido de exemplo e descri��o dos par�metros.
	 * 
	 * MOVE:							M -25 0			-> X e Y do ponto
	 * KICK:							K 100 20		-> Intensidade e angulo relativo
	 * RUN:								R (ou D) 100	-> Intensidade
	 * TURN:							T 90			-> Angulo
	 * TURN TO DIRECTION:               TD  0 1         -> Vetor de direcao X, Y
	 * KICK TO POINT:					KTP -25 0		-> X e Y do ponto
	 * TURN TO POINT: 					TTP	-10 5		-> X e Y do ponto
	 * TURN TO BALL: 					TTB
	 * RUN TO POINT:					RTP -10 5 3		-> X, Y do ponto e taxa de erro
	 * RUN TO BALL: 					RTB 3			-> Taxa de erro
	 * SHOW (CURRENT) PLAYER INFO:		SP
	 * SHOW PLAYER INFO:				SPI A 3			-> time A (lado esquerdo) ou B (lado direto) e numero do jogador
	 * SHOW TIME (tempo):				TM
	 * SHOW BALL POSITION:				SB
	 *
	 * Observacoes:
	 * - As a��es s�o imprecisas. Assim, Turn 50 seguido de -50 nem sempre retorna a posic�o.
	 * - Run 100 � andar aproximadamente 1 unidade de distancia, run 50 � andar aproximadamente metade e assim por diante
	 * - Kick 100 � chutar aproximadamente 40 unidades de distancia, kick 50 � chutar aproximadamente metade e assim por diante.
	 */
	
	public CommandLinePlayer(PlayerCommander player) {
		commander = player;
	}

	@Override
	public void run() {
		System.out.println(">> Main loop ...");
		
		initGUI();
		
		commander.doMoveBlocking(-25d, 0d);
		
		String[] action = null;
		while (true) {
			try{
				updatePerceptions();
				
				if(hasNewCommand){
					action = command.trim().toUpperCase().split(" ");
					doAction(action);
					hasNewCommand = false;
				}
				
			}catch(Exception e){
				guiPrintln("Erro ao executar comando \"" + command + "\"" );
				guiPrintln(e.toString());
				e.printStackTrace();
				hasNewCommand = false;
			}
			
			sleepFor(LOOP_INTERVAL);
		}
		
	}

	private void doAction(String[] action) throws Exception {
		guiClear();
		
		if (action == null || action.length == 0){
			return;
		}
		
		if(action[0].equalsIgnoreCase("M")){
			commander.doMoveBlocking(Double.parseDouble(action[1]), Double.parseDouble(action[2]));
			System.out.println(command);
			guiPrintln("Ok");
			
		}else if(action[0].equalsIgnoreCase("K")){
			commander.doKick(Double.parseDouble(action[1]), Double.parseDouble(action[2]));
			System.out.println(command);
			guiPrintln("Ok");
			
		}else if(action[0].equalsIgnoreCase("R") || action[0].equalsIgnoreCase("D")){
			commander.doDash(Double.parseDouble(action[1]));
			System.out.println(command);
			guiPrintln("Ok");
			
		}else if(action[0].equalsIgnoreCase("T")){
			commander.doTurn(Double.parseDouble(action[1]));
			guiPrintln(command);
			guiPrintln("Ok");
			
		}else if(action[0].equalsIgnoreCase("KTP")){
			kickToPoint(Double.parseDouble(action[1]), Double.parseDouble(action[2]));
			System.out.println(command);
			guiPrintln("Ok");
			
		}else if(action[0].equalsIgnoreCase("TD")){
			Vector2D direction = new Vector2D(Double.parseDouble(action[1]), Double.parseDouble(action[2]));
			commander.doTurnToDirection(direction);
			System.out.println(command);
			guiPrintln("Ok");
			
		}else if(action[0].equalsIgnoreCase("TTP")){
			commander.doTurnToPoint(new Vector2D(Double.parseDouble(action[1]), Double.parseDouble(action[2])));
			System.out.println(command);
			guiPrintln("Ok");
			
		}else if(action[0].equalsIgnoreCase("TTB")){
			turnToBall();
			System.out.println(command);
			guiPrintln("Ok");
			
		}else if(action[0].equalsIgnoreCase("RTP")){
			runToPoint(Double.parseDouble(action[1]), Double.parseDouble(action[2]), Double.parseDouble(action[3]));
			System.out.println(command);
			guiPrintln("Ok");
			
		}else if(action[0].equalsIgnoreCase("RTB")){
			runToBall(Double.parseDouble(action[1]));
			System.out.println(command);
			guiPrintln("Ok");
			
		}else if(action[0].equalsIgnoreCase("SP")){
			guiPrintln(action[0]+" position: " + selfPerc.getPosition());
			guiPrintln(action[0]+" direction: " + selfPerc.getDirection());
			guiPrintln(action[0]+" team: " + selfPerc.getTeam());
			guiPrintln(action[0]+" uniform: " + selfPerc.getUniformNumber());
			
		}else if(action[0].equalsIgnoreCase("SPI")){
			if(action[1].equalsIgnoreCase("A") || action[1].equalsIgnoreCase("B")){
				guiPrintln(action[0]+":\n"+fieldPerc.getTeamPlayer(action[1].equalsIgnoreCase("A")? EFieldSide.LEFT : EFieldSide.RIGHT, Integer.parseInt(action[2])));
			}else{
				throw new Exception("Wrong team -- use \"A\" or \"B\"");
			}
			
		}else if(action[0].equalsIgnoreCase("TM")){
			guiPrintln(action[0]+": "+matchPerc.getTime());
			
		}else if(action[0].equalsIgnoreCase("SB")){
			guiPrintln(action[0]+": "+fieldPerc.getBall().getPosition());
			
		}else{
			throw new Exception("Invalid command.");
		}
	}
	
	private void kickToPoint(double x, double y){
		Vector2D myPos = selfPerc.getPosition();
		Vector2D point = new Vector2D(x, y);
		Vector2D newDirection = point.sub(myPos);
		//System.out.println(" => Point = " + point + " -- Player = " + myPos + " -- New Direction = " + newDirection + " -- Magnitude = "+ newDirection.magnitude());
		
		commander.doTurnToDirectionBlocking(newDirection);
		
		double intensity = (newDirection.magnitude() * 100) / 40;
		if(intensity > 100){
			intensity = 100;
		}
		commander.doKickBlocking(intensity, 0);
	}
	
	private void turnToBall() {
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		commander.doTurnToPoint(ballPos);		
	}
	
	private void runToBall(double distError) {
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		runToPoint(ballPos.getX(), ballPos.getY(), distError);
	}
	
	private void runToPoint(double x, double y, double distanceError) {
		Vector2D point = new Vector2D(x, y);
		
		while (selfPerc.getPosition().distanceTo(point) > distanceError) {
			if (isAlignedTo(point, 15)) {
				commander.doDashBlocking(100.0d);
			} else {
				commander.doTurnToPointBlocking(point);
			}			
			updatePerceptions();
		}
	}
	
	private boolean isAlignedTo(Vector2D position, double angleError) {
		Vector2D desiredDirection = position.sub(selfPerc.getPosition());
		double angle = selfPerc.getDirection().angleFrom(desiredDirection);
		return angle >= -angleError && angle <= angleError;
	}
	
	private boolean updatePerceptions() {
		boolean newPerception = false;
		PlayerPerception newSelf = commander.perceiveSelf();
		FieldPerception newField = commander.perceiveField();
		MatchPerception newMatch = commander.perceiveMatch();
		
		if (newSelf != null) {
			this.selfPerc = newSelf;
			newPerception = true;
		}
		if (newField != null) {
			this.fieldPerc = newField;
			newPerception = true;
		}
		if (newMatch != null){
			this.matchPerc = newMatch;
			newPerception = true;
		}
		
		return newPerception;
	}

	private void sleepFor(long timeMillis) {
		try {
			Thread.sleep(timeMillis);
		} catch (InterruptedException e) {
			//e.printStackTrace();
			System.out.println("Sleep interrupted, but going on...");
		}
	}
	
	JTextArea textArea;
	
	private void initGUI() {
		JFrame frame = new JFrame();
		frame.setTitle("COMM");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setLayout(new BorderLayout());
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setSize(250, 170);
		
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout());
		upperPanel.setBorder(new TitledBorder(new EtchedBorder(), "Command", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION));
		
		TextField tf = new TextField();
		tf.addActionListener(new ActionListener() { 
			@Override public void actionPerformed(ActionEvent ev) { 
				command = tf.getText();
				hasNewCommand = true;
			}
		});
		upperPanel.add(tf, BorderLayout.CENTER);
		
		Button btn = new Button("DO");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				command = tf.getText();
				hasNewCommand = true;
			}
		});
		upperPanel.add(btn, BorderLayout.LINE_END);
		frame.add(upperPanel, BorderLayout.PAGE_START);
		
		textArea = new JTextArea(5, 25);
		textArea.setEditable(false);
		textArea.setBorder(new TitledBorder(new EtchedBorder(), "Output", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION));
		frame.add(textArea, BorderLayout.CENTER);
		textArea.setText("\nType a command in the line above,\nthen press button \"do\".");
	}
	
	void guiClear() {
		textArea.setText("");
	}
	
	void guiPrintln(String msg) {
		textArea.append(msg + "\n");
	}

	
}
