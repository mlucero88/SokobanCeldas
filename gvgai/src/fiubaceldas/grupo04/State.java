package fiubaceldas.grupo04;

import java.util.ArrayList;

import fiubaceldas.grupo04.Model.Entity;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.Vector2d;

public class State {

	/* Pongo atributos public para no estar haciendo getters y tener q cambiarlos mientras
	 * desarrollo, ya que no se si voy a estar cambiandolos mientras hago el tp */
	public int width;
	public int height;
	public Entity[][] map;

	private String description = null;

	/* Pongo esto aca xq cuando el agente se pone encima del destino, no lo puedo ubicar */
	private static Vector2d destPos = null;

	public State(Perception p) {
		width = p.getLevelWidth();
		height = p.getLevelHeight();
		map = new Entity[width][height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				map[x][y] = Entity.fromChar(p.getAt(x, y));
			}
		}

		if (destPos == null) {
			destPos = locateEntity(this, Entity.DESTINATION);
		}
	}

	public String getDescription() {
		if (description == null) {
			StringBuilder sb = new StringBuilder("");
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					sb.append(map[x][y].toChar());
				}
				sb.append("\n");
			}
			description = sb.toString();
		}

		return description;
	}

	public boolean isBoxDeadlock() {
		Vector2d boxPosition = locateEntity(this, Entity.BOX);

		boolean isWallUp = getRelativePosition(this, boxPosition, Types.UP) == Entity.WALL;
		boolean isWallDown = getRelativePosition(this, boxPosition, Types.DOWN) == Entity.WALL;
		boolean isWallLeft = getRelativePosition(this, boxPosition, Types.LEFT) == Entity.WALL;
		boolean isWallRight = getRelativePosition(this, boxPosition, Types.RIGHT) == Entity.WALL;

		// return ((isWallUp && (isWallLeft || isWallRight)) || (isWallDown && (isWallLeft || isWallRight)));
		if ((isWallUp && (isWallLeft || isWallRight)) || (isWallDown && (isWallLeft || isWallRight))) {
			return true;
		}
		return false;
	}

	/* Descarto:
	 * 			1. Moverme contra una pared
	 * 			2. Moverme contra otro jugador
	 * 			3. Empujar la caja contra un lugar que no este vacio o sea el destino (es decir, empujar contra una pared u otro jugador)
	 */
	public ArrayList<ACTIONS> getPossibleActions(Entity agent) {
		ArrayList<ACTIONS> actions = new ArrayList<ACTIONS>();

		Vector2d agentPos = locateEntity(this, agent);
		Entity up = getRelativePosition(this, agentPos, Types.UP);
		Entity upx2 = getRelativePosition(this, agentPos, Types.UP.copy().mul(2));
		Entity down = getRelativePosition(this, agentPos, Types.DOWN);
		Entity downx2 = getRelativePosition(this, agentPos, Types.DOWN.copy().mul(2));
		Entity left = getRelativePosition(this, agentPos, Types.LEFT);
		Entity leftx2 = getRelativePosition(this, agentPos, Types.LEFT.copy().mul(2));
		Entity right = getRelativePosition(this, agentPos, Types.RIGHT);
		Entity rightx2 = getRelativePosition(this, agentPos, Types.RIGHT.copy().mul(2));

		if (up == Entity.EMPTY || up == Entity.DESTINATION || (up == Entity.BOX && (upx2 == Entity.EMPTY || upx2 == Entity.DESTINATION))) {
			actions.add(ACTIONS.ACTION_UP);
		}
		if (down == Entity.EMPTY || down == Entity.DESTINATION || (down == Entity.BOX && (downx2 == Entity.EMPTY || downx2 == Entity.DESTINATION))) {
			actions.add(ACTIONS.ACTION_DOWN);
		}
		if (left == Entity.EMPTY || left == Entity.DESTINATION || (left == Entity.BOX && (leftx2 == Entity.EMPTY || leftx2 == Entity.DESTINATION))) {
			actions.add(ACTIONS.ACTION_LEFT);
		}
		if (right == Entity.EMPTY || right == Entity.DESTINATION
				|| (right == Entity.BOX && (rightx2 == Entity.EMPTY || rightx2 == Entity.DESTINATION))) {
			actions.add(ACTIONS.ACTION_RIGHT);
		}

		return actions;
	}

	/* Valores:
	 * 			Gana el juego => 100 (esta teoria esta harcodeada)
	 * 			Empuja la caja acercandola a destino && la caja la dejé contra N=0..4 paredes => (90 - 2N)
	 * 			Estaba diagonal a la caja y se puso al lado &&
	 * 				el destino esta hacia donde se posiciono && puede empujar => 80 
	 * 				el destino esta opuesto a su direccion de movimiento (intencion de rodeo?) => 75 
	 * 				el destino esta en su misma direccion de movimiento (intencion de rodeo critico?) => 70 
	 * 				otro caso => 60 
	 * 			Estaba al lado de la caja y se puso en diagonal &&
	 * 				el destino esta opuesto a donde estaba posicionado para empujar && puede empujar hacia destino cuando finalize el rodeo => 80 
	 * 				el destino esta opuesto a su direccion de movimiento (intencion de rodeo?) => 75 
	 * 				el destino esta en su misma direccion de movimiento (intencion de rodeo critico?) => 70 
	 * 				otro caso => 60 
	 * 			Empuja la caja alejandola del destino && la caja la dejé contra N=0..4 paredes => (50 - 2N)
	 * 			Me muevo a un lugar vacio acercandome a la caja => 40
	 * 			Me muevo a un lugar vacio alejandome de la caja => 30
	 * 			Me coloco al lado de la caja pero la caja no es movible para el lado apuntado => 20
	 * 			Ninguno de todos estos casos => 15
	 * 			Me quedo quieto => 10
	 * 			Caja atorada (pierde) => 0 (se detecta antes de entrar aca)
	 */
	public static int calculateUtility(State previous, State current, Entity agent) {
		Vector2d prevAgentPos = locateEntity(previous, agent);
		Vector2d currAgentPos = locateEntity(current, agent);
		if (prevAgentPos.equals(currAgentPos)) {
			return 10;
		}

		Vector2d prevBoxPos = locateEntity(previous, Entity.BOX);
		Vector2d currBoxPos = locateEntity(current, Entity.BOX);
		boolean boxWasMoved = !prevBoxPos.equals(currBoxPos);

		// Las distancias las uso al cuadrado
		if (!boxWasMoved) {
			double prevDistAgentBox = prevAgentPos.sqDist(currBoxPos);
			double currDistAgentBox = currAgentPos.sqDist(currBoxPos);

			// Ayudas para saber en que direccion esta es destino respecto de la caja
			Vector2d direction = destPos.copy().subtract(currBoxPos);
			boolean destToNorth = direction.y < 0;
			boolean destToSouth = direction.y > 0;
			boolean destToWest = direction.x < 0;
			boolean destToEast = direction.x > 0;

			// Para ver si hubo rodeo a la caja
			boolean agentWasNextToBox = prevDistAgentBox == 1;
			boolean agentIsNextToBox = currDistAgentBox == 1;
			boolean agentWasDiagonalToBox = prevDistAgentBox == 2;
			boolean agentIsDiagonalToBox = currDistAgentBox == 2;

			if (agentWasDiagonalToBox && agentIsNextToBox) {
				if (currAgentPos.copy().add(Types.UP).equals(currBoxPos)) {
					// Tengo la caja arriba
					boolean canPush = getRelativePosition(current, currBoxPos, Types.UP) != Entity.WALL;
					if (canPush && destToNorth) {
						// Me puse en una buena posicion y puedo empujar
						return 80;
					}
					else if ((destToEast && currAgentPos.equals(prevAgentPos.copy().add(Types.LEFT)))
							|| (destToWest && currAgentPos.equals(prevAgentPos.copy().add(Types.RIGHT)))) {
						// Hice un rodeo para pasar a empujar del otro lado
						return 75;
					}
					else if ((destToEast && currAgentPos.equals(prevAgentPos.copy().add(Types.RIGHT)))
							|| (destToWest && currAgentPos.equals(prevAgentPos.copy().add(Types.LEFT)))) {
						return 70;
					}
					else {
						// Rodeo pedorro
						return 60;
					}
				}
				else if (currAgentPos.copy().add(Types.DOWN).equals(currBoxPos)) {
					// Tengo la caja abajo
					boolean canPush = getRelativePosition(current, currBoxPos, Types.DOWN) != Entity.WALL;
					if (canPush && destToSouth) {
						// Me puse en una buena posicion y puedo empujar
						return 80;
					}
					else if ((destToEast && currAgentPos.equals(prevAgentPos.copy().add(Types.LEFT)))
							|| (destToWest && currAgentPos.equals(prevAgentPos.copy().add(Types.RIGHT)))) {
						// Hice un rodeo para pasar a empujar del otro lado
						return 75;
					}
					else if ((destToEast && currAgentPos.equals(prevAgentPos.copy().add(Types.RIGHT)))
							|| (destToWest && currAgentPos.equals(prevAgentPos.copy().add(Types.LEFT)))) {
						return 70;
					}
					else {
						// Rodeo pedorro
						return 60;
					}
				}
				else if (currAgentPos.copy().add(Types.LEFT).equals(currBoxPos)) {
					// Tengo la caja a la izq
					boolean canPush = getRelativePosition(current, currBoxPos, Types.LEFT) != Entity.WALL;
					if (canPush && destToWest) {
						// Me puse en una buena posicion y puedo empujar
						return 80;
					}
					else if ((destToNorth && currAgentPos.equals(prevAgentPos.copy().add(Types.DOWN)))
							|| (destToSouth && currAgentPos.equals(prevAgentPos.copy().add(Types.UP)))) {
						// Hice un rodeo para pasar a empujar del otro lado
						return 75;
					}
					else if ((destToNorth && currAgentPos.equals(prevAgentPos.copy().add(Types.UP)))
							|| (destToSouth && currAgentPos.equals(prevAgentPos.copy().add(Types.DOWN)))) {
						return 70;
					}
					else {
						// Rodeo pedorro
						return 60;
					}
				}
				else if (currAgentPos.copy().add(Types.RIGHT).equals(currBoxPos)) {
					// Tengo la caja a la der
					boolean canPush = getRelativePosition(current, currBoxPos, Types.RIGHT) != Entity.WALL;
					if (canPush && destToEast) {
						// Me puse en una buena posicion y puedo empujar
						return 80;
					}
					else if ((destToNorth && currAgentPos.equals(prevAgentPos.copy().add(Types.DOWN)))
							|| (destToSouth && currAgentPos.equals(prevAgentPos.copy().add(Types.UP)))) {
						// Hice un rodeo para pasar a empujar del otro lado
						return 75;
					}
					else if ((destToNorth && currAgentPos.equals(prevAgentPos.copy().add(Types.UP)))
							|| (destToSouth && currAgentPos.equals(prevAgentPos.copy().add(Types.DOWN)))) {
						return 70;
					}
					else {
						// Rodeo pedorro
						return 60;
					}
				}
			}
			else if (agentWasNextToBox && agentIsDiagonalToBox) {
				if (prevAgentPos.copy().add(Types.UP).equals(currBoxPos)) {
					// Tenia la caja arriba
					boolean couldPush = getRelativePosition(previous, currBoxPos, Types.UP) != Entity.WALL;
					if (couldPush && destToNorth) {
						// Podria haber empujado y la cague
						return 5;
					}
					if (!couldPush && destToSouth) {
						// Intencion de rodear la caja para empujar al lado opuesto
						return 80;
					}
					else if ((destToEast && currAgentPos.equals(prevAgentPos.copy().add(Types.LEFT)))
							|| (destToWest && currAgentPos.equals(prevAgentPos.copy().add(Types.RIGHT)))) {
						// Hice un rodeo para pasar a empujar del otro lado
						return 75;
					}
					else if ((destToEast && currAgentPos.equals(prevAgentPos.copy().add(Types.RIGHT)))
							|| (destToWest && currAgentPos.equals(prevAgentPos.copy().add(Types.LEFT)))) {
						return 70;
					}
					else {
						// Rodeo pedorro
						return 60;
					}
				}
				else if (prevAgentPos.copy().add(Types.DOWN).equals(currBoxPos)) {
					// Tenia la caja abajo
					boolean couldPush = getRelativePosition(previous, currBoxPos, Types.DOWN) != Entity.WALL;
					if (couldPush && destToSouth) {
						// Podria haber empujado y la cague
						return 5;
					}
					if (!couldPush && destToNorth) {
						// Intencion de rodear la caja para empujar al lado opuesto
						return 80;
					}
					else if ((destToEast && currAgentPos.equals(prevAgentPos.copy().add(Types.LEFT)))
							|| (destToWest && currAgentPos.equals(prevAgentPos.copy().add(Types.RIGHT)))) {
						// Hice un rodeo para pasar a empujar del otro lado
						return 75;
					}
					else if ((destToEast && currAgentPos.equals(prevAgentPos.copy().add(Types.RIGHT)))
							|| (destToWest && currAgentPos.equals(prevAgentPos.copy().add(Types.LEFT)))) {
						return 70;
					}
					else {
						// Rodeo pedorro
						return 60;
					}
				}
				else if (prevAgentPos.copy().add(Types.LEFT).equals(currBoxPos)) {
					// Tenia la caja a la izq
					boolean couldPush = getRelativePosition(previous, currBoxPos, Types.LEFT) != Entity.WALL;
					if (couldPush && destToWest) {
						// Podria haber empujado y la cague
						return 5;
					}
					if (!couldPush && destToEast) {
						// Intencion de rodear la caja para empujar al lado opuesto
						return 80;
					}
					else if ((destToNorth && currAgentPos.equals(prevAgentPos.copy().add(Types.DOWN)))
							|| (destToSouth && currAgentPos.equals(prevAgentPos.copy().add(Types.UP)))) {
						// Hice un rodeo para pasar a empujar del otro lado
						return 75;
					}
					else if ((destToNorth && currAgentPos.equals(prevAgentPos.copy().add(Types.UP)))
							|| (destToSouth && currAgentPos.equals(prevAgentPos.copy().add(Types.DOWN)))) {
						return 70;
					}
					else {
						// Rodeo pedorro
						return 60;
					}
				}
				else if (prevAgentPos.copy().add(Types.RIGHT).equals(currBoxPos)) {
					// Tenia la caja a la der
					boolean couldPush = getRelativePosition(previous, currBoxPos, Types.RIGHT) != Entity.WALL;
					if (couldPush && destToEast) {
						// Podria haber empujado y la cague
						return 5;
					}
					if (!couldPush && destToWest) {
						// Intencion de rodear la caja para empujar al lado opuesto
						return 80;
					}
					else if ((destToNorth && currAgentPos.equals(prevAgentPos.copy().add(Types.DOWN)))
							|| (destToSouth && currAgentPos.equals(prevAgentPos.copy().add(Types.UP)))) {
						// Hice un rodeo para pasar a empujar del otro lado
						return 75;
					}
					else if ((destToNorth && currAgentPos.equals(prevAgentPos.copy().add(Types.UP)))
							|| (destToSouth && currAgentPos.equals(prevAgentPos.copy().add(Types.DOWN)))) {
						return 70;
					}
					else {
						// Rodeo pedorro
						return 60;
					}
				}
			}
			else {
				// No se movio alrededor de la caja
				boolean agentGotCloserToBox = (prevDistAgentBox - currDistAgentBox > 0);
				if (currAgentPos.copy().add(Types.UP).equals(currBoxPos) && getRelativePosition(current, currBoxPos, Types.UP) == Entity.WALL) {
					// Me puse abajo de la caja y no se puede empujar para arriba
					return 20;
				}
				else if (currAgentPos.copy().add(Types.DOWN).equals(currBoxPos) && getRelativePosition(current, currBoxPos, Types.DOWN) == Entity.WALL) {
					return 20;
				}
				else if (currAgentPos.copy().add(Types.LEFT).equals(currBoxPos) && getRelativePosition(current, currBoxPos, Types.LEFT) == Entity.WALL) {
					return 20;
				}
				else if (currAgentPos.copy().add(Types.RIGHT).equals(currBoxPos) && getRelativePosition(current, currBoxPos, Types.RIGHT) == Entity.WALL) {
					return 20;
				}
				
				return agentGotCloserToBox ? 40 : 30;
			}
		}
		else {
			boolean boxGotCloser = (prevBoxPos.sqDist(destPos) - currBoxPos.sqDist(destPos) > 0);
			int value = boxGotCloser ? 90 : 50;
			int adjWallCount = 0;

			adjWallCount += (getRelativePosition(current, currBoxPos, Types.UP) == Entity.WALL) ? 1 : 0;
			adjWallCount += (getRelativePosition(current, currBoxPos, Types.DOWN) == Entity.WALL) ? 1 : 0;
			adjWallCount += (getRelativePosition(current, currBoxPos, Types.LEFT) == Entity.WALL) ? 1 : 0;
			adjWallCount += (getRelativePosition(current, currBoxPos, Types.RIGHT) == Entity.WALL) ? 1 : 0;

			return value - 2 * adjWallCount;
		}

		return 15;
	}

	@Override
	public String toString() {
		return getDescription();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		}
		else if (!description.equals(other.description))
			return false;
		return true;
	}

	private static Vector2d locateEntity(State state, Entity entity) {
		for (int x = 0; x < state.width; ++x) {
			for (int y = 0; y < state.height; ++y) {
				if (state.map[x][y] == entity) {
					return new Vector2d(x, y);
				}
			}
		}
		return null;
	}

	private static Entity getRelativePosition(State s, Vector2d origin, Vector2d offset) {
		try {
			Vector2d p = origin.copy().add(offset);
			return s.map[(int) p.x][(int) p.y];
		}
		catch (ArrayIndexOutOfBoundsException e) {
			// Para que no hinche las bolas cuando me voy fuera del mapa
			return Entity.WALL;
		}
	}
}
