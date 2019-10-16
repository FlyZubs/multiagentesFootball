package team.project.myTeam;

import java.awt.List;
import java.awt.Rectangle;
import java.util.ArrayList;

import easy_soccer_lib.PlayerCommander;
import easy_soccer_lib.perception.FieldPerception;
import easy_soccer_lib.perception.MatchPerception;
import easy_soccer_lib.perception.PlayerPerception;
import easy_soccer_lib.utils.EFieldSide;
import easy_soccer_lib.utils.Vector2D;

public class CommandPlayer extends Thread {
	
private int LOOP_INTERVAL = 100; //0.1s
private PlayerCommander commander;
private PlayerPerception selfPerc;
private FieldPerception fieldPerc;
private MatchPerception matchPerc;

public CommandPlayer(PlayerCommander player) {
	commander = player;
}
@Override
public void run() {
	System.out.println(">> Executando...");
	long nextIteration = System.currentTimeMillis() + LOOP_INTERVAL;
	updatePerceptions();
	switch (selfPerc.getUniformNumber()) {
		case 1:
			acaoGoleiro(nextIteration);
			break;
		case 2:
			acaoZagueiro(nextIteration, -1); // cima
			break;
		case 3:
			acaoZagueiro(nextIteration, 1); // baixo
			break;
		case 4:
			acaoArmador(nextIteration, -1); // cima
			break;
		case 5:
			acaoArmador(nextIteration, 0); // meio
			break;
		case 6:
			acaoArmador(nextIteration, 1); // baixo
			break;
		case 7:
			acaoAtacante(nextIteration, -1); // cima
			break;
		case 8:
			acaoAtacante(nextIteration, 1); // baixo
			break;
		default: break;
	}
}

private void updatePerceptions() {
	PlayerPerception newSelf = commander.perceiveSelfBlocking();
	FieldPerception newField = commander.perceiveFieldBlocking();
	MatchPerception newMatch = commander.perceiveMatchBlocking();
	if (newSelf != null) this.selfPerc = newSelf;
	if (newField != null) this.fieldPerc = newField;
	if (newMatch != null) this.matchPerc = newMatch;
}

private void turnToPoint(Vector2D point){
	Vector2D newDirection = point.sub(selfPerc.getPosition());
	commander.doTurnToDirectionBlocking(newDirection);
}

private boolean isAlignToPoint(Vector2D point, double margin){
	double angle = point.sub(selfPerc.getPosition()).angleFrom(selfPerc.getDirection());
	return angle < margin && angle > margin*(-1);
}


private void dash(Vector2D point){
	if(selfPerc.getPosition().distanceTo(point) <= 1) return;
	if(!isAlignToPoint(point, 15)) turnToPoint(point);
	commander.doDashBlocking(70);
}

private void kickToPoint(Vector2D point, double intensity){
	Vector2D newDirection = point.sub(selfPerc.getPosition());
	double angle = newDirection.angleFrom(selfPerc.getDirection());
	if(angle > 90 || angle < -90){
		commander.doTurnToDirectionBlocking(newDirection);
		angle = 0;
	}
	commander.doKickBlocking(intensity, angle);
}

private boolean isPointsAreClose(Vector2D reference,Vector2D point, double margin){
	return reference.distanceTo(point) <= margin;
}

private PlayerPerception getClosestPlayerPoint(Vector2D point,EFieldSide side, double margin){
	ArrayList<PlayerPerception> lp = fieldPerc.getTeamPlayers(side);
	PlayerPerception np = null;
	if(lp != null && !lp.isEmpty()){
		double dist,temp;
		dist = lp.get(0).getPosition().distanceTo(point);
		np = lp.get(0);
		if(isPointsAreClose(np.getPosition(), point, margin))
			return np;
		for (PlayerPerception p : lp) {
			if(p.getPosition() == null) break;
			if(isPointsAreClose(p.getPosition(),point, margin))
				return p;
			temp = p.getPosition().distanceTo(point);
			if(temp < dist){
				dist = temp;
				np = p;
			}
		}
	}
	return np;
}

private void acaoGoleiro(long nextIteration) {
	double xInit=-48, yInit=0, ballX=0, ballY=0;
	EFieldSide side = selfPerc.getSide();
	Vector2D initPos = new Vector2D(xInit*side.value(), yInit*side.value());
	Vector2D ballPos;
	Rectangle area = side == EFieldSide.LEFT?new Rectangle(-52, -20, 16, 40):new Rectangle(36, -20, 16, 40);
	while (true) {
		updatePerceptions();
		ballPos = fieldPerc.getBall().getPosition();
		switch (matchPerc.getState()) {
			case BEFORE_KICK_OFF:
				// posiciona
				commander.doMoveBlocking(xInit, yInit);
				break;
			case KICK_OFF_LEFT:
			case KICK_OFF_RIGHT:
				dash(initPos);
			case PLAY_ON:
				ballX=fieldPerc.getBall().getPosition().getX();
				ballY=fieldPerc.getBall().getPosition().getY();
				if(isPointsAreClose(selfPerc.getPosition(),ballPos, 1)){
					// chutar
					kickToPoint(new Vector2D(0,0), 100);
				}else if(area.contains(ballX, ballY)){
					// defender
					dash(ballPos);
				}else if(!isPointsAreClose(selfPerc.getPosition(),initPos, 3)){
					// recuar
					dash(initPos);
				}else {
					// olhar para a bola
					turnToPoint(ballPos);
				}
				break;
				/* Todos os estados da partida */
			default: break;
		}
	}
}

private void acaoZagueiro(long nextIteration, int pos) {
	double xInit=-30, yInit=8*pos;
	EFieldSide side = selfPerc.getSide();
	EFieldSide enemySide = side.invert(side);
	Vector2D initPos = new Vector2D(xInit*side.value(), yInit*side.value());
	Vector2D ballPos, vTemp;
	PlayerPerception pTemp;
	while (true) {
		updatePerceptions();
		ballPos = fieldPerc.getBall().getPosition();
		ArrayList<PlayerPerception> myTeam = fieldPerc.getTeamPlayers(selfPerc.getSide());
		switch (matchPerc.getState()) {
			case BEFORE_KICK_OFF:
				commander.doMoveBlocking(xInit, yInit);
				break;
			case KICK_OFF_LEFT:
				dash(initPos);
				break;
			case KICK_OFF_RIGHT:
				dash(initPos);
				break;
			case PLAY_ON:
				if(isPointsAreClose(selfPerc.getPosition(),ballPos, 1)){
					PlayerPerception closestPlayer = selfPerc;
					double closestDistance = Double.MAX_VALUE;
					
					for (PlayerPerception player : myTeam) {
						double playerDistance = player.getPosition().distanceTo(ballPos);
						if(playerDistance > 1) {
							if (playerDistance < closestDistance && player.getUniformNumber() > 3) {
								closestDistance = playerDistance;
								closestPlayer = player;
							}							
						}
					}
					
					vTemp = closestPlayer.getPosition();
					System.out.println(vTemp);
					
					Vector2D vTempF = vTemp.sub(selfPerc.getPosition());
					double intensity = ((vTempF.magnitude()*100)/40);
					if(intensity > 50) intensity = 10;
					kickToPoint(vTemp, intensity);
				}else{
					pTemp = getClosestPlayerPoint(ballPos,side, 3);
					if(pTemp != null && pTemp.getUniformNumber() == selfPerc.getUniformNumber()){
						// pega a bola
						dash(ballPos);
					}else if(isPointsAreClose(selfPerc.getPosition(), getClosestPlayerPoint(selfPerc.getPosition(), enemySide, 10).getPosition(), 8)) {
						if(selfPerc.getPosition().distanceTo(initPos) > 10) {
							dash(initPos);
						}else {
							dash(getClosestPlayerPoint(selfPerc.getPosition(), enemySide, 8).getPosition());					
						}
					}else if(!isPointsAreClose(selfPerc.getPosition(), initPos, 3)){
						// recua
						dash(initPos);
					}else{
						// olha para a bola
						turnToPoint(ballPos);
					}
				}
				break;
				/* Todos os estados da partida */
			default:
				break;
		}
	}
}


private void acaoArmador(long nextIteration, int pos) {
	double xInit=-15, yInit=15*pos;
	EFieldSide side = selfPerc.getSide();
	EFieldSide enemySide = side.invert(side);
	Vector2D initPos = new Vector2D(xInit*side.value(), yInit*side.value());
	Vector2D ballPos, vTemp;
	PlayerPerception pTemp;
	while (true) {
		updatePerceptions();
		ballPos = fieldPerc.getBall().getPosition();
		ArrayList<PlayerPerception> myTeam = fieldPerc.getTeamPlayers(side);
		ArrayList<PlayerPerception> enemyTeam = fieldPerc.getTeamPlayers(enemySide);
		switch (matchPerc.getState()) {
			case BEFORE_KICK_OFF:
				commander.doMoveBlocking(xInit, yInit);
				break;
			case KICK_OFF_LEFT:
				dash(initPos);
				break;
			case KICK_OFF_RIGHT:
				dash(initPos);
				break;
			case PLAY_ON:
				if(isPointsAreClose(selfPerc.getPosition(),ballPos, 1)){
					PlayerPerception closestPlayer = selfPerc;
					double closestDistance = Double.MAX_VALUE;
					
					for (PlayerPerception player : myTeam) {
						double playerDistance = player.getPosition().distanceTo(ballPos);
						if(playerDistance > 1) {
							if (playerDistance < closestDistance && player.getUniformNumber() > 6) {
								closestDistance = playerDistance;
								closestPlayer = player;
							}							
						}
					}
					
					vTemp = closestPlayer.getPosition();
					System.out.println(vTemp);
					
					Vector2D vTempF = vTemp.sub(selfPerc.getPosition());
					double intensity = ((vTempF.magnitude()*100)/40);
					if(intensity > 50) intensity = 15;
					kickToPoint(vTemp, intensity);
				}else{
					pTemp = getClosestPlayerPoint(ballPos,side, 3);
					if(pTemp != null && pTemp.getUniformNumber() == selfPerc.getUniformNumber()){
						// pega a bola
						dash(ballPos);
					}else if(isPointsAreClose(selfPerc.getPosition(), getClosestPlayerPoint(selfPerc.getPosition(), enemySide, 10).getPosition(), 8)) {
						if(selfPerc.getPosition().distanceTo(initPos) > 10) {
							dash(initPos);
						}else {
							dash(getClosestPlayerPoint(selfPerc.getPosition(), enemySide, 8).getPosition());					
						}
					}else if(!isPointsAreClose(selfPerc.getPosition(), initPos, 5)){
						// recua
						dash(initPos);
					}else{
						// olha para a bola
						turnToPoint(ballPos);
					}
				}
				break;
				/* Todos os estados da partida */
			default:
				break;
		}
	}
}

private void acaoAtacante(long nextIteration, int pos) {
	double xInit=-7, yInit=8*pos;
	EFieldSide side = selfPerc.getSide();
	Vector2D initPos = new Vector2D(xInit*side.value(), yInit);
	Vector2D goalPos = new Vector2D(50*side.value(), 0);
	Vector2D ballPos;
	Vector2D centerPos = new Vector2D(0,0);
	PlayerPerception pTemp;
	while (true) {
		updatePerceptions();
		ballPos = fieldPerc.getBall().getPosition();
		switch (matchPerc.getState()) {
			case BEFORE_KICK_OFF:
				commander.doMoveBlocking(xInit, yInit);
				break;
			case KICK_OFF_LEFT:
				if(side == EFieldSide.LEFT) {
					pTemp = getClosestPlayerPoint(centerPos, side, 3);
					if(pTemp.getUniformNumber() == 8) {
						dash(centerPos);
						kickToPoint(goalPos.multiply(-1), 40);						
					}
				}else {
					dash(initPos);					
				}
				break;
			case KICK_OFF_RIGHT:
				if(side == EFieldSide.RIGHT) {
					pTemp = getClosestPlayerPoint(centerPos, side, 3);
					if(pTemp.getUniformNumber() == 8) {
						dash(centerPos);
						kickToPoint(goalPos.multiply(-1), 40);						
					}
				}else {
					dash(initPos);					
				}
				break;
			case PLAY_ON:
				if(isPointsAreClose(selfPerc.getPosition(),ballPos, 1)){
					if(isPointsAreClose(ballPos, goalPos, 30)){
						// chuta para o gol
						kickToPoint(goalPos, 100);
					}else{
						// conduz para o gol
						kickToPoint(goalPos, 15);
					}
				}else{
					pTemp = getClosestPlayerPoint(ballPos,side, 3);
					if(pTemp != null && pTemp.getUniformNumber() == selfPerc.getUniformNumber() ){
						// pega a bola
						dash(ballPos);
					}else if(pTemp != null && ((pTemp.getUniformNumber() == 7 )||pTemp.getUniformNumber() == 8 )){
						if(isPointsAreClose(selfPerc.getPosition(),pTemp.getPosition(), 8)) {
							dash(pTemp.getPosition().multiply(-1));								
						}else if(isPointsAreClose(selfPerc.getPosition(),goalPos, 25)) {
							turnToPoint(ballPos);
						}else {
							dash(goalPos);							
						}
					}
					else if(!isPointsAreClose(selfPerc.getPosition(),initPos, 3)){
						// recua
						dash(initPos);
					}else{
						// olha para a bola
						
						turnToPoint(ballPos);
					}
				}
				break;
				/* Todos os estados da partida */
			default:
				break;
		}
	}
}



}

