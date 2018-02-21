package fiubaceldas.grupo04;

import java.util.Arrays;

import fiubaceldas.grupo04.Model.Entity;

public class Predicates implements Cloneable {

	/* Tiene la informacion de todo el mapa, como State, pero a diferencia de State, puede que en un casillero
	 * haya un wildcard ('?'), indicando que no me importa esa posicion. Tengo que ver igual si estos wildcards
	 * se generan al usar alguna heuristica... */

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
	public boolean same(Predicates other) {
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + height;
		result = prime * result + Arrays.deepHashCode(map);
		result = prime * result + width;
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
		Predicates other = (Predicates) obj;
		if (height != other.height)
			return false;
		if (!Arrays.deepEquals(map, other.map))
			return false;
		if (width != other.width)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("");
		if (map != null) {
			for (int y = 1; y < height - 1; y++) {
				for (int x = 1; x < width - 1; x++) {
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
}
