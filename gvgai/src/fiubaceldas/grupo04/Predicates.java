package fiubaceldas.grupo04;

import fiubaceldas.grupo04.Model.Direction;
import fiubaceldas.grupo04.Model.Entity;

public class Predicates {

	public Entity positionUp = null;
	public Entity positionDown = null;
	public Entity positionLeft = null;
	public Entity positionRight = null;
	public Direction directionBox = null;
	public Direction directionDestination = null;

	public Predicates(int mapHeight, int mapWidth, char[][] map) {

	}

	/** En este caso, suponemos que un objeto A de la clase:
	 * CondicionRestaurantes, es igual (equal) a otro
	 * objeto B, de la clase CondicionRestaurantes sí A es igual o más
	 * especifico que B.
	 * ¿Qué significa que sea más especifico? que A.x == B.x o, B.x==null.
	 * Recordar que cada atributo de esta clase es uan condicion, si este es
	 * NULL, es entonces una condición
	 * que NO se contemplará. */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		Predicates other = (Predicates) obj;

		if (other.positionUp != null && this.positionUp != null) {
			if (!this.positionUp.equals(other.positionUp)) return false;
		}
		if (other.positionDown != null && this.positionDown != null) {
			if (!this.positionDown.equals(other.positionDown)) return false;
		}
		if (other.positionLeft != null && this.positionLeft != null) {
			if (!this.positionLeft.equals(other.positionLeft)) return false;
		}
		if (other.positionRight != null && this.positionRight != null) {
			if (!this.positionRight.equals(other.positionRight)) return false;
		}
		if (other.directionBox != null && this.directionBox != null) {
			if (!this.directionBox.equals(other.directionBox)) return false;
		}
		if (other.directionDestination != null && this.directionDestination != null) {
			if (!this.directionDestination.equals(other.directionDestination)) return false;
		}

		return true;
	}

	public Predicates exclusion(Predicates pred) throws CloneNotSupportedException {
		Predicates cloned = (Predicates) this.clone();

		if (pred.positionUp != null && cloned.positionUp != null) {
			if (!cloned.positionUp.equals(pred.positionUp)) {
				cloned.positionUp = null;
			}
		}
		if (pred.positionDown != null && cloned.positionDown != null) {
			if (!cloned.positionDown.equals(pred.positionDown)) {
				cloned.positionDown = null;
			}
		}
		if (pred.positionLeft != null && cloned.positionLeft != null) {
			if (!cloned.positionLeft.equals(pred.positionLeft)) {
				cloned.positionLeft = null;
			}
		}
		if (pred.positionRight != null && cloned.positionRight != null) {
			if (!cloned.positionRight.equals(pred.positionRight)) {
				cloned.positionRight = null;
			}
		}
		if (pred.directionBox != null && cloned.directionBox != null) {
			if (!cloned.directionBox.equals(pred.directionBox)) {
				cloned.directionBox = null;
			}
		}
		if (pred.directionDestination != null && cloned.directionDestination != null) {
			if (!cloned.directionDestination.equals(pred.directionDestination)) {
				cloned.directionDestination = null;
			}
		}

		return cloned;
	}
}
