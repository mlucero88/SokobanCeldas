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
		map = currentState.map.clone();
	}
	
	public Predicates exclusion(Predicates pred) throws CloneNotSupportedException {
		Predicates cloned = (Predicates) this.clone();

//		if (pred.positionUp != null && cloned.positionUp != null) {
//			if (!cloned.positionUp.equals(pred.positionUp)) {
//				cloned.positionUp = null;
//			}
//		}
//		if (pred.positionDown != null && cloned.positionDown != null) {
//			if (!cloned.positionDown.equals(pred.positionDown)) {
//				cloned.positionDown = null;
//			}
//		}
//		if (pred.positionLeft != null && cloned.positionLeft != null) {
//			if (!cloned.positionLeft.equals(pred.positionLeft)) {
//				cloned.positionLeft = null;
//			}
//		}
//		if (pred.positionRight != null && cloned.positionRight != null) {
//			if (!cloned.positionRight.equals(pred.positionRight)) {
//				cloned.positionRight = null;
//			}
//		}
//		if (pred.directionBox != null && cloned.directionBox != null) {
//			if (!cloned.directionBox.equals(pred.directionBox)) {
//				cloned.directionBox = null;
//			}
//		}
//		if (pred.directionDestination != null && cloned.directionDestination != null) {
//			if (!cloned.directionDestination.equals(pred.directionDestination)) {
//				cloned.directionDestination = null;
//			}
//		}

		return cloned;
	}

	/** En este caso, suponemos que un objeto A de la clase "Predicates", es igual a otro
	 * objeto B de la clase "Predicates", si A es igual o más especifico que B.
	 * ¿Qué significa que sea más especifico? que A.x == B.x ó B.x == *.
	 * Cada posición del mapa es una condición, y un '*' representa una condición (y a su
	 * vez una posición) que no se tomará en cuenta */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		Predicates other = (Predicates) obj;

		if (this.width != other.width || this.height != other.height) {
			return false; // Capaz es mejor una excepcion. Igual esto no va a suceder nunca ...
		}
		
		for (int x = 0; x < this.width; ++x) {
			for (int y = 0; y < this.height; ++y) {
				if (other.map[x][y] != Entity.WILDCARD && this.map[x][y] != other.map[x][y]) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
	    return super.clone();
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
	
//	@Override
//	public String toString() {
//		return Arrays.toString(map);
//	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("");
		if (map != null) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					sb.append(map[x][y]);
				}
				sb.append("\\n");
			}
		}
		return sb.toString();
	}
}
