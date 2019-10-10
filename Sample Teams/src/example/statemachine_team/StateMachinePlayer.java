package example.statemachine_team;

import java.util.List;

import easy_soccer_lib.PlayerCommander;
import easy_soccer_lib.perception.FieldPerception;
import easy_soccer_lib.perception.PlayerPerception;
import easy_soccer_lib.utils.EFieldSide;
import easy_soccer_lib.utils.Vector2D;


public class StateMachinePlayer extends Thread {
	private PlayerCommander commander;
	
	private PlayerPerception selfPerc;
	private FieldPerception  fieldPerc;
	
	double homeX, homeY;
	double goalX;
	
	enum State { ATTACK, RETURN_TO_HOME, KICKOFF };
	
	State currState;
	
	public StateMachinePlayer(PlayerCommander player, double x, double y) {
		commander = player;
		homeX = x;
		homeY = y;
		currState = State.RETURN_TO_HOME;
	}

	@Override
	public void run() {
		System.out.println(">> 1. Waiting initial perceptions...");
		selfPerc  = commander.perceiveSelfBlocking();
		fieldPerc = commander.perceiveFieldBlocking();

		System.out.println(">> 2. Moving to initial position...");
		commander.doMoveBlocking(homeX, homeY);
		
		selfPerc  = commander.perceiveSelfBlocking();
		fieldPerc = commander.perceiveFieldBlocking();
		
		if (selfPerc.getSide() == EFieldSide.LEFT) {
			goalX = 52.0d;
		} else {
			goalX = -52.0d;
			homeX = -homeX; //inverte, porque somente no move as coordenadas sao espelhadas independente de lado 
			homeY = -homeY;
		}

		System.out.println(">> 3. Now starting...");
		while (commander.isActive()) {
			
			switch (this.currState) {
			case ATTACK:
				behaviorAttack();
				break;
			case RETURN_TO_HOME:
				behaviorReturn();
				break;
			//case KICKOFF:
				//	break;
			default:
				break;
			}
			
			updatePerceptions(); //non-blocking
		}
		
		System.out.println(">> 4. Terminated!");
	}

	private void behaviorReturn() {
		if (closestToTheBall()) {
			currState = State.ATTACK;
			return;
		}
		
		Vector2D homePosition = new Vector2D(homeX, homeY);
		
		if (isAlignedTo(homePosition)) {
			if (closeTo(homePosition)) {
				//fica parado
				return;
			} else {
				commander.doDashBlocking(100.0d);
			}
		} else {
			commander.doTurnToPoint(homePosition);
		}		
	}
	
	long lastPrintTime = 0; //usada para imprimir informacoes de debug em certos intervalos de tempo
	
	private boolean closestToTheBall() {
		Vector2D ballPosition = fieldPerc.getBall().getPosition();
		List<PlayerPerception> myTeam = fieldPerc.getTeamPlayers(selfPerc.getSide());
		
		PlayerPerception closestPlayer = selfPerc;
		double closestDistance = Double.MAX_VALUE;
		
		boolean print = (System.currentTimeMillis() - lastPrintTime > 5000);
		if (print) {
			lastPrintTime = System.currentTimeMillis();
			System.out.println("ME: " + currState + ", No " + selfPerc.getUniformNumber() + ", pos: " + selfPerc.getPosition());
			System.out.println("TEAM " + selfPerc.getSide());
		}
		
		for (PlayerPerception player : myTeam) {
			double playerDistance = player.getPosition().distanceTo(ballPosition);
			if (playerDistance < closestDistance) {
				closestDistance = playerDistance;
				closestPlayer = player;
			}
			if (print) {
				System.out.println(" - No " + player.getUniformNumber() + ", pos: " + player.getPosition() + ", d2ball: " + playerDistance);	
			}
		}
		
		if (print) {
			System.out.println(" - Closest? " + (closestPlayer.getUniformNumber() == selfPerc.getUniformNumber()));
		}
		return closestPlayer.getUniformNumber() == selfPerc.getUniformNumber();
	}

	private void behaviorAttack() {
		if (! closestToTheBall()) {
			currState = State.RETURN_TO_HOME;
			return;
		}
		
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		
		if (isAlignedTo(ballPos)) {
			if (closeTo(ballPos)) {
				commander.doKickToPoint(50.0d, new Vector2D(goalX, 0));
			} else {
				commander.doDashBlocking(100.0d);
			}
		} else {
			turnToBall();
		}
	}

	private boolean closeTo(Vector2D pos) {
		Vector2D myPos = selfPerc.getPosition();
		return pos.distanceTo(myPos) < 1.5;
	}

	private boolean isAlignedTo(Vector2D position) {
		Vector2D myPos = selfPerc.getPosition();
		
		if (position == null || myPos == null) {
			return false;			
		}
		
		double angle = selfPerc.getDirection().angleFrom(position.sub(myPos));
		
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
		Vector2D ballPos = fieldPerc.getBall().getPosition();
		commander.doTurnToPoint(ballPos);
	}
	

}
