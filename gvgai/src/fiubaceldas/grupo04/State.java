package fiubaceldas.grupo04;

import fiubaceldas.grupo04.Model.Entity;
import ontology.Types;
import tools.Vector2d;

public class State {
	
	/* Pongo atributos public para no estar haciendo getters y tener q cambiarlos mientras
	 * desarrollo, ya que no se si voy a estar cambiandolos mientras hago el tp */
	public int width;
	public int height;
	public Entity[][] map;
	
	private String description;

	public State(Perception p) {
		width = p.getLevelWidth();
		height = p.getLevelHeight();
		map = new Entity[width][height];

		StringBuilder sb = new StringBuilder("");
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				char c = p.getAt(x, y);
				map[x][y] = Entity.fromChar(c);
				sb.append(c);
			}
			sb.append("\\n");
		}

		description = sb.toString();
	}

	public String getDescription() {
		return description;
	}

	public boolean isBoxDeadlock() {
		Vector2d boxPosition = locateBox();
		Vector2d posUp = boxPosition.copy().add(Types.UP);
		Vector2d posDown = boxPosition.copy().add(Types.DOWN);
		Vector2d posLeft = boxPosition.copy().add(Types.LEFT);
		Vector2d posRight = boxPosition.copy().add(Types.RIGHT);
		
		boolean isWallUp = map[(int)posUp.x][(int)posUp.y] == Entity.WALL;
		boolean isWallDown = map[(int)posDown.x][(int)posDown.y] == Entity.WALL;
		boolean isWallLeft = map[(int)posLeft.x][(int)posLeft.y] == Entity.WALL;
		boolean isWallRight = map[(int)posRight.x][(int)posRight.y] == Entity.WALL;
		
		if ((isWallUp && (isWallLeft || isWallRight)) || (isWallDown && (isWallLeft || isWallRight))) {
			return true;
		}
		
		return false;
	}

	@Override
	public String toString() {
		return getDescription();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		return result;
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

	private Vector2d locateBox() {
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (map[x][y] == Entity.BOX) {
					return new Vector2d(x, y);
				}
			}
		}
		return null;
	}
}
