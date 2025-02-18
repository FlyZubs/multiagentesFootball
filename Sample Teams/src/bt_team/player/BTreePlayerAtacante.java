package bt_team.player;

import behavior_tree.BTNode;
import behavior_tree.Selector;
import behavior_tree.Sequence;
import easy_soccer_lib.PlayerCommander;
import easy_soccer_lib.perception.FieldPerception;
import easy_soccer_lib.perception.PlayerPerception;
import easy_soccer_lib.utils.EFieldSide;
import easy_soccer_lib.utils.Vector2D;


public class BTreePlayerAtacante extends Thread {
	final PlayerCommander commander;
	
	PlayerPerception selfPerc;
	FieldPerception  fieldPerc;
	
	Vector2D homePosition;
	Vector2D goalPosition;
	
	BTNode<BTreePlayerAtacante> btree;
	
	
	public BTreePlayerAtacante(PlayerCommander player, Vector2D home) {
		commander = player;
		homePosition = home;
		
		btree = buildTree();
	}

	private BTNode<BTreePlayerAtacante> buildTree() {
		Selector<BTreePlayerAtacante> raiz = new Selector<BTreePlayerAtacante>("RAIZ");
		
		Sequence<BTreePlayerAtacante> goleiroTree = new Sequence<BTreePlayerAtacante>("Chuta a bola pra longe");
		
		Sequence<BTreePlayerAtacante> zagueiroTree = new Sequence<BTreePlayerAtacante>("Defende e rouba a bola");
		zagueiroTree.add(new IfClosestPlayerToBall());
		zagueiroTree.add(new GoGetBall());
		
		Sequence<BTreePlayerAtacante> attackTree = new Sequence<BTreePlayerAtacante>("Avanca-para-Gol");
		attackTree.add(new IfClosestPlayerToBall());
		attackTree.add(new AdvanceWithBallToGoal());
		attackTree.add(new KickToScore());

		Sequence<BTreePlayerAtacante> deffensiveTree = new Sequence<BTreePlayerAtacante>("Rouba-Bola");
		
		
		//BTNode<BTreePlayer> defaultTree = new ReturnToHome(); //fica como EXERCICIO
		//defaultTree
		
		raiz.add(attackTree);
		raiz.add(deffensiveTree);
		//raiz.add(defaultTree);
		
		return raiz;
	}

	@Override
	public void run() {
		System.out.println(">> 1. Waiting initial perceptions...");
		selfPerc  = commander.perceiveSelfBlocking();
		fieldPerc = commander.perceiveFieldBlocking();

		System.out.println(">> 2. Moving to initial position...");
		commander.doMoveBlocking(this.homePosition);
		
		selfPerc  = commander.perceiveSelfBlocking();
		fieldPerc = commander.perceiveFieldBlocking();
		
		if (selfPerc.getSide() == EFieldSide.LEFT) {
			goalPosition = new Vector2D(52.0d, 0);
		} else {
			goalPosition = new Vector2D(-52.0d, 0);
			homePosition.setX(- homePosition.getX()); //inverte, porque somente no move as coordenadas sao espelhadas independente de lado 
			homePosition.setY(- homePosition.getY());
		}

		System.out.println(">> 3. Now starting...");
		while (commander.isActive()) {
			
			btree.tick(this);
			
			try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			updatePerceptions(); //non-blocking
		}
		
		System.out.println(">> 4. Terminated!");
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
	
	/** Algumas funcoes auxiliares que mais de um tipo de no da arvore pode precisar **/
	
	boolean closeTo(Vector2D pos) {
		return isCloseTo(pos, 1.5);
	}
	
	boolean isCloseTo(Vector2D pos, double minDistance) {
		Vector2D myPos = selfPerc.getPosition();
		return pos.distanceTo(myPos) < minDistance;
	}

	boolean isAlignedTo(Vector2D position) {
		return isAlignedTo(position, 12.0);
	}
	
	boolean isAlignedTo(Vector2D position, double minAngle) {
		if (minAngle < 0) minAngle = -minAngle;
		
		Vector2D myPos = selfPerc.getPosition();
		
		if (position == null || myPos == null) {
			return false;			
		}
		
		double angle = selfPerc.getDirection().angleFrom(position.sub(myPos));
		return angle < minAngle && angle > -minAngle;
	}	

}
