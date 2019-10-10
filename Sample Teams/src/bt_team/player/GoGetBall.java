package bt_team.player;

import behavior_tree.BTNode;
import behavior_tree.BTStatus;
import easy_soccer_lib.utils.Vector2D;


public class GoGetBall extends BTNode<BTreePlayer> {

	@Override
	public BTStatus tick(BTreePlayer agent) {
		Vector2D ballPos = agent.fieldPerc.getBall().getPosition();
		
		//condicao desejada: perto da bola (dist < 3) 
		if (agent.isCloseTo(ballPos, 3.0)) {
			print("PERTO!");
			return BTStatus.SUCCESS;
		}

		//corre atras da bola e da um pequeno toque
		if (agent.isAlignedTo(ballPos)) {
			agent.commander.doDashBlocking(100.0d);
		} else {
			agent.commander.doTurnToPoint(ballPos);
		}
		
		return BTStatus.RUNNING;
	}
	
}
