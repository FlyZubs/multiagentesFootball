package example.ballfollower_team;

import easy_soccer_lib.PlayerCommander;
import easy_soccer_lib.perception.FieldPerception;
import easy_soccer_lib.perception.PlayerPerception;
import easy_soccer_lib.utils.Vector2D;


public class BallFollowerPlayer extends Thread {
	private PlayerCommander commander;
	
	private PlayerPerception selfPerc;
	private FieldPerception  fieldPerc;
	
	
	public BallFollowerPlayer(PlayerCommander player) {
		commander = player;
	}

	@Override
	public void run() {
		System.out.println(">> 1. Waiting initial perceptions...");
		selfPerc  = commander.perceiveSelfBlocking();
		fieldPerc = commander.perceiveFieldBlocking();
		
		System.out.println(">> 2. Moving to initial position...");
		commander.doMoveBlocking(-25.0d, 0.0d);
		
		selfPerc  = commander.perceiveSelfBlocking();
		fieldPerc = commander.perceiveFieldBlocking();
		
		System.out.println(">> 3. Now starting...");
		while (commander.isActive()) {
			
			if (isAlignedToBall()) {
				if (closeToBall()) {
					commander.doKick(50.0d, 0.0d);
				} else {
					runToBall();
				}
			} else {
				turnToBall();
			}

			updatePerceptions(); //non-blocking
		}
		
		System.out.println(">> 4. Terminated!");
	}

	private boolean closeToBall() {
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		Vector2D myPos = selfPerc.getPosition();
		
		return ballPos.distanceTo(myPos) < 1.5;
	}

	private boolean isAlignedToBall() {
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		Vector2D myPos = selfPerc.getPosition();
		Vector2D directionToBall = ballPos.sub(myPos);

		double angle = selfPerc.getDirection().angleFrom(directionToBall);
		//System.out.println("Vetores: " + directionToBall + "  " + selfPerc.getDirection());
		//System.out.println(" => Angulo agente-bola: " + angle);
		
		return angle < 15.0d && angle > -15.0d;
	}
	
	private void updatePerceptions() {
		PlayerPerception newSelf = commander.perceiveSelf();
		FieldPerception newField = commander.perceiveField();
		
		if (newSelf != null) {
			this.selfPerc = newSelf;
		}
		if (newField != null) {
			this.fieldPerc = newField;
		}
	}

	private void turnToBall() {
		System.out.println("TURN");
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		commander.doTurnToPoint(ballPos);	
	}
	
	private void runToBall() {
		System.out.println("RUN");
		commander.doDashBlocking(100.0d);
	}

}
