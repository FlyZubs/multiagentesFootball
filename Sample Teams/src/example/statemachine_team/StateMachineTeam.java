package example.statemachine_team;

import easy_soccer_lib.AbstractTeam;
import easy_soccer_lib.PlayerCommander;


/**
 * Time simples, criado em sala a partir do time BallFollower.
 */
public class StateMachineTeam extends AbstractTeam {

	public StateMachineTeam(String suffix) {
		super("StateMachine-" + suffix, 2, false);
	}

	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		double x, y;

		switch (ag) {
		case 0:
			x = -37.0d;
			y = -20.0d;
			break;
		case 1:
			x = -37.0d;
			y = 20.0d;
			break;
		default:
			x = -37.0d;
			y = 0;
		}
		
		StateMachinePlayer pl = new StateMachinePlayer(commander, x, y);
		pl.start();
	}

}
