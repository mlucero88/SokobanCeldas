package fiubaceldas.grupo04;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ontology.Types;

public class Theory {

	public Predicates allegedConditions;
	public Types.ACTIONS action;
	public Boolean predictedEffects;
	
	private int usedCount = 0;
	private int successCount = 0;

	public int getSuccessCount() {
		return this.successCount;
	}

	public void incSuccessCount() {
		this.successCount++;
	}

	public int getUsedCount() {
		return this.usedCount;
	}

	public void incUsedCount() {
		this.usedCount++;
	}

	public boolean isSuccesful() {
		return this.successCount == this.usedCount;
	}

	public void copyExitosUsos(Theory t) {
		this.cantExitos = t.getSuccessCount();
		this.cantUsos = t.getUsedCount();
	}

	/**
	 * <h3>Una teoría A es igual a otra B cuando:</h3>
	 * <ul>
	 * <li>Ambas tienen las mismas acciones (en este caso no tenemos
	 * acciones).</li>
	 * <li>Las condiciones supuestas son iguales. Entiendase por iguales que son
	 * literalmente iguales o
	 * bien que las condiciones supuestas de A son más especificas, es decir más
	 * restrictivas que las de B.
	 * En otras palabras dada una SITUACIÓN S, puedo aplicar las teoría A o la
	 * toría B, siendo B
	 * una teoria más generica o a lo sumo igual que A.</li>
	 * <li>Los efectos predichos por A son iguales o más especificos que los
	 * predichos por B.
	 * En este caso todos los efectos predichos son TRUE o FALSE, por lo cual
	 * solo podemos
	 * evaluar que sean iguales.</li>
	 * </ul>
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Theory other = (Theory) obj;

		if (this.allegedConditions != null && other.allegedConditions != null) {
			if (!this.allegedConditions.equals(other.allegedConditions)) return false;
		}
		if (this.predictedEffects != null && other.predictedEffects != null) {
			if (!this.predictedEffects.equals(other.predictedEffects)) return false;
		}
		return true;
	}

	/**
	 * <h3>Una teoría A es similar a otra B cuando:</h3>
	 * <ul>
	 * <li>Ambas tienen las mismas acciones (en este caso no tenemos
	 * acciones).</li>
	 * <li>Los efectos predichos por A son iguales o más especificos que los
	 * predichos por B.
	 * En este caso todos los efectos predichos son TRUE o FALSE, por lo cual
	 * solo podemos
	 * evaluar que sean iguales.</li>
	 * </ul>
	 */
	public boolean similar(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Theory other = (Theory) obj;

		if (this.predictedEffects != null && other.predictedEffects != null) {
			if (!this.predictedEffects.equals(other.predictedEffects)) return false;
		}
		return true;
	}

	/**
	 * <h3>Una teoría A es incompatible con otra B cuando:</h3>
	 * <ul>
	 * <li>Las condiciones supuestas son iguales.</li>
	 * <li>Los efectos predichos por A son distintos.</li>
	 * </ul>
	 */
	public boolean incompatible(Theory other) {

		if (this.allegedConditions != null && other.allegedConditions != null) {
			if (this.predictedEffects != null && other.predictedEffects != null) {
				if (this.allegedConditions.equals(other.allegedConditions)) {
					if (!this.predictedEffects.equals(other.predictedEffects)) return true;
				}
			}
		}
		return false;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Theory cloned = (Theory) super.clone();
		cloned.allegedConditions = (Predicates) this.allegedConditions.clone();
		cloned.predictedEffects = new Boolean(this.predictedEffects);
		cloned.cantExitos = this.cantExitos;
		cloned.cantUsos = this.cantUsos;
		return cloned;
	}

	public static List<Theory> returnIguales(Theory source, Set<Theory> set) {
		List<Theory> teoriasIguales = new ArrayList<Theory>();
		for (Theory obj : set) {
			if (source.equals(obj) && obj.isSuccesful()) teoriasIguales.add(obj);
		}

		return teoriasIguales;
	}

	public static List<Theory> returnSimilares(Theory source, Set<Theory> set) {
		List<Theory> teoriasSimilares = new ArrayList<Theory>();
		for (Theory obj : set) {
			if (source.similar(obj)) teoriasSimilares.add(obj);
		}

		return teoriasSimilares;
	}

	public static List<Theory> returnErroneas(Theory source, Set<Theory> set) {
		List<Theory> teoriasErroneas = new ArrayList<Theory>();
		for (Theory obj : set) {
			if (source.incompatible(obj)) {
				teoriasErroneas.add(obj);
			}
		}
		return teoriasErroneas;
	}

	public static List<Theory> returnExitosas(Set<Theory> set) {
		List<Theory> teoriasExitosas = new ArrayList<Theory>();
		for (Theory obj : set) {
			if (obj.isSuccesful()) {
				teoriasExitosas.add(obj);
			}
		}
		return teoriasExitosas;
	}

	public Theory exclusion(Theory teoria) throws CloneNotSupportedException {
		Theory t = new Theory();
		t.allegedConditions = this.allegedConditions.exclusion(teoria.allegedConditions);
		t.predictedEffects = new Boolean(this.predictedEffects);
		return t;
	}

	public String toString() {
		return this.toString(true);
	}

	public String toString(boolean csv) {
		StringBuffer sb = new StringBuffer("");
		sb.append(this.allegedConditions.toString(csv) + " ===> espero=" + String.valueOf(this.predictedEffects) + "; (" + this.cantExitos + ", "
				+ this.cantUsos + ") ");
		return sb.toString();
	}

}
