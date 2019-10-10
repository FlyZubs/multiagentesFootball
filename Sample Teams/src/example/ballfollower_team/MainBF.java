package example.ballfollower_team;

import java.net.UnknownHostException;


public class MainBF {

	public static void main(String[] args) throws UnknownHostException {
		BallFollowerTeam team1 = new BallFollowerTeam("A");
		BallFollowerTeam team2 = new BallFollowerTeam("B");
		
		team1.launchTeamAndServer();
		team2.launchTeam();
	}
	
}

