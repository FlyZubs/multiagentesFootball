package bt_team.player;

import java.awt.Rectangle;

import behavior_tree.BTNode;
import behavior_tree.BTStatus;
import easy_soccer_lib.perception.FieldPerception;
import easy_soccer_lib.perception.PlayerPerception;
import easy_soccer_lib.utils.EFieldSide;
import easy_soccer_lib.utils.Vector2D;


public class DetectBallInArea extends BTNode<BTreePlayerGoleiro>{
	
	private PlayerPerception selfPerc;
	public FieldPerception fieldPerc;
	EFieldSide side = selfPerc.getSide();
	
	public BTStatus tick(BTreePlayerGoleiro agent) {
		Vector2D ballPos = agent.fieldPerc.getBall().getPosition();
		Rectangle area = side == EFieldSide.LEFT?new Rectangle(-52, -20, 16, 40):new Rectangle(36, -20, 16, 40);
		
		double ballX = fieldPerc.getBall().getPosition().getX();
		double ballY = fieldPerc.getBall().getPosition().getY();
		
		if(area.contains(ballX,ballY)) {
			if (agent.isAlignedTo(ballPos)) {
				agent.commander.doDashBlocking(100.0d);
				if (agent.isCloseTo(ballPos, 1.0)) {
					agent.commander.doKickToPoint(100.0d, agent.goalPosition);
					return BTStatus.SUCCESS;
				}
			} else {
				agent.commander.doTurnToPoint(ballPos);
			}
		}else {
			return BTStatus.FAILURE;
		}
		return BTStatus.RUNNING;
	}
}
