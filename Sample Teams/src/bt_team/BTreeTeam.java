package bt_team;

import bt_team.player.BTreePlayerGoleiro;
import bt_team.player.BTreePlayerArmador;
import bt_team.player.BTreePlayerAtacante;
import bt_team.player.BTreePlayerZagueiro;
import easy_soccer_lib.AbstractTeam;
import easy_soccer_lib.PlayerCommander;
import easy_soccer_lib.utils.Vector2D;


/**
 * Time simples, demonstrado em sala.
 */
public class BTreeTeam extends AbstractTeam {

	public BTreeTeam(String suffix) {
		super("BT-Team-" + suffix, 8, false);
	}

	@Override
	protected void launchPlayer(int ag, PlayerCommander commander) {
		double x, y;

		switch (ag) {
		case 0:
			x = -48.0d;
			y = 0.0d;
			break;
		case 1:
			x = -30.0d;
			y = -8.0d;
			break;
		case 2:
			x = -30.0d;
			y = 8.0d;
			break;
		case 3:
			x = -20.0d;
			y = -8.0d;
			break;
		case 4:
			x = -20.0d;
			y = 0.0d;
			break;
		case 5:
			x = -20.0d;
			y = 8.0d;
			break;
		case 6:
			x = -10.0d;
			y = -8.0d;
			break;
		case 7:
			x = -10.0d;
			y = 8.0d;
			break;
		default:
			x = -30.0d;
			y = 0;
		}
		
		BTreePlayerGoleiro pl = new BTreePlayerGoleiro(commander, new Vector2D(x, y));
		pl.start();
	}

}
