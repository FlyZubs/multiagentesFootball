package bt_team.player;

import java.util.List;

import behavior_tree.BTNode;
import behavior_tree.BTStatus;
import easy_soccer_lib.perception.FieldPerception;
import easy_soccer_lib.perception.PlayerPerception;
import easy_soccer_lib.utils.Vector2D;

public class IfClosestPlayerToBall extends BTNode<BTreePlayer> {

	@Override
	public BTStatus tick(BTreePlayer agent) {
		PlayerPerception selfPerc = agent.selfPerc;
		FieldPerception fieldPerc = agent.fieldPerc;
		
		Vector2D ballPosition = fieldPerc.getBall().getPosition();
		List<PlayerPerception> myTeam = fieldPerc.getTeamPlayers(selfPerc.getSide());
		
		PlayerPerception closestPlayer = agent.selfPerc;
		double closestDistance = Double.MAX_VALUE;
		
		for (PlayerPerception player : myTeam) {
			double playerDistance = player.getPosition().distanceTo(ballPosition);
			if (playerDistance < closestDistance) {
				closestDistance = playerDistance;
				closestPlayer = player;
			}
		}
		
//		print(5000, "No ", selfPerc.getUniformNumber(), ", pos: ",
//				selfPerc.getPosition(),	", TEAM ", selfPerc.getSide(),
//				", CLOSEST? " + (closestPlayer.getUniformNumber() == selfPerc.getUniformNumber()));
		
		if (closestPlayer.getUniformNumber() == selfPerc.getUniformNumber()) {
			return BTStatus.SUCCESS;
		} else {
			return BTStatus.FAILURE;
		}
	}

}
