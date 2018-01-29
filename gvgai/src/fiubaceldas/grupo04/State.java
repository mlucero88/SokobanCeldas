package fiubaceldas.grupo04;

import fiubaceldas.grupo04.Model.Entity;
import tools.Vector2d;

public class State {
	public Predicates stateDescription;
//	public Boolean efectosObservados;

	private Vector2d boxPosition = null;
	private Vector2d playerPosition = null;
	private Vector2d destinationPosition = null;

	private Vector2d playerToBox; // vector del jugador hacia la caja
	private Vector2d playerToDestination; // vector del jugador hacia el destino
	private Vector2d boxToDestination; // vector de la caja hacia su destino

	public State(Perception perception, Entity agentName) {
		stateDescription = new Predicates(perception.getLevelHeight(), perception.getLevelWidth(), perception.getMap());

		locateEntitiesInMap(perception, agentName);
		
		playerToBox = boxPosition.copy().subtract(playerPosition);
		playerToDestination = destinationPosition.copy().subtract(playerPosition);
		boxToDestination = destinationPosition.copy().subtract(boxPosition);

	}

	public Vector2d getBoxPosition() {
		return boxPosition;
	}

	public Vector2d getPlayerPosition() {
		return playerPosition;
	}

	public Vector2d getDestinationPosition() {
		return destinationPosition;
	}

	public Vector2d getPlayerToBox() {
		return playerToBox;
	}

	public Vector2d getPlayerToDestination() {
		return playerToDestination;
	}

	public Vector2d getBoxToDestination() {
		return boxToDestination;
	}

	private void locateEntitiesInMap(Perception map, Entity agentToLocate) {
		for (int x = 0; x < map.getLevelWidth(); ++x) {
			for (int y = 0; y < map.getLevelHeight(); ++y) {
				Entity e = Entity.fromChar(map.getAt(x, y));

				if (e == Entity.BOX) {
					boxPosition = new Vector2d(x, y);
				}
				else if (e == Entity.DESTINATION) {
					destinationPosition = new Vector2d(x, y);
				}
				else if (e == agentToLocate) {
					playerPosition = new Vector2d(x, y);
				}
				else {
					continue;
				}

				if (boxPosition != null && destinationPosition != null && playerPosition != null) {
					return;
				}
			}
		}
	}
}
