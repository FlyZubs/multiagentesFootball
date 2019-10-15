package team.project.myTeam;

import easy_soccer_lib.AbstractTeam;
import easy_soccer_lib.PlayerCommander;

public class CommandTeam extends AbstractTeam {
	
public CommandTeam(String prefix) {
	super("Team"+prefix, 8, true);
}

@Override
protected void launchPlayer(int ag, PlayerCommander comm) {
	System.out.println("Player lan�ado!");
	CommandPlayer p = new CommandPlayer(comm);
	p.start();
}

}

