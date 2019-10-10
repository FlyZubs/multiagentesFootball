package example.cmdline_team;

import easy_soccer_lib.AbstractTeam;
import easy_soccer_lib.PlayerCommander;

public class CommandLineTeam extends AbstractTeam {

	public CommandLineTeam() {
		super("COMM", 1, false);
	}

	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		System.out.println("Player lan�ado");
		CommandLinePlayer p = new CommandLinePlayer(commander);
		p.start();
	}

}
