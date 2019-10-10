package example.statemachine_team;

import java.net.UnknownHostException;


public class MainSM {

	public static void main(String[] args) throws UnknownHostException {
		StateMachineTeam team1 = new StateMachineTeam("A");
		StateMachineTeam team2 = new StateMachineTeam("B");
		
		team1.launchTeamAndServer();
		team2.launchTeam();
	}
	
}

