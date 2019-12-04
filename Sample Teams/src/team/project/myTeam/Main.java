package team.project.myTeam;

import java.net.InetAddress;
import java.net.UnknownHostException;

import bt_team.BTreeTeam;
import example.ballfollower_team.BallFollowerTeam;
import team.legacy.Krislet.Krislet;

public class Main {

public static void main(String[] args) {
	try {
		CommandTeam teamA = new CommandTeam("A");
		//CommandTeam teamB = new CommandTeam("B");
		teamA.launchTeamAndServer();
		//teamB.launchTeam();
	} catch (UnknownHostException e) {
		System.out.println("Falha ao conectar.");
	}
}

}
