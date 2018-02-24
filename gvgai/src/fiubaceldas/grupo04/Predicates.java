package fiubaceldas.grupo04;

import fiubaceldas.grupo04.Model.Entity;

public class Predicates implements Cloneable {

	/* Tiene la informacion de todo el mapa, como State, pero a diferencia de State, puede que en un casillero
	 * haya un wildcard ('*'), indicando que no me importa esa posicion */

	private int width;
	private int height;
	private Entity[][] map;

	public Predicates(State currentState) {
		width = currentState.width;
		height = currentState.height;
		map = mapClone(currentState.map, width, height);
	}

	public Predicates exclusion(Predicates other) throws CloneNotSupportedException {
		Predicates cloned = (Predicates) this.clone();

		for (int x = 0; x < cloned.width; ++x) {
			for (int y = 0; y < cloned.height; ++y) {
				if (cloned.map[x][y] != other.map[x][y]) {
					cloned.map[x][y] = Entity.WILDCARD;
				}
			}
		}
		return cloned;
	}

	/**
	 * En este caso, suponemos que un objeto A de la clase "Predicates", es igual a otro objeto B de la clase "Predicates", si A es igual o más
	 * especifico que B. ¿Qué significa que sea más especifico? que A.x == B.x ó B.x == *. Cada posición del mapa es una condición, y un '*'
	 * representa una condición (y a su vez una posición) que no se tomará en cuenta
	 */
	@Override
	public boolean equals(Object obj) {
		Predicates other = (Predicates) obj;
		for (int x = 0; x < this.width; ++x) {
			for (int y = 0; y < this.height; ++y) {
				if (other.map[x][y] != Entity.WILDCARD && this.map[x][y] != other.map[x][y]) {
					return false;
				}
			}
		}
		return true;
	}

	protected Predicates() {
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Predicates cloned = new Predicates();
		cloned.height = this.height;
		cloned.width = this.width;
		cloned.map = mapClone(this.map, this.width, this.height);
		return cloned;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("");
		if (map != null) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					sb.append(map[x][y].toChar());
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	private Entity[][] mapClone(Entity[][] map, int w, int h) {
		Entity[][] cloned = new Entity[w][h];

		for (int x = 0; x < w; ++x) {
			for (int y = 0; y < h; ++y) {
				cloned[x][y] = map[x][y];
			}
		}
		return cloned;
	}

	// Para el parser
	public Predicates(String description) {
		this.width = description.indexOf('\n');
		this.height = description.length() / (width + 1);
		this.map = new Entity[width][height];

		int i = 0;
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				map[x][y] = Entity.fromChar(description.charAt(i));
				++i;
			}
			++i; // skip \n
		}
	}
}
