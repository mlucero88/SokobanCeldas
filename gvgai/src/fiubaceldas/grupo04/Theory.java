package fiubaceldas.grupo04;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ontology.Types;

public class Theory implements Cloneable {

	private Predicates initialConditions;
	private Types.ACTIONS action;
	private Predicates predictedEffects;

	private int usedCount = 0;
	private int successCount = 0;

	public Theory(Predicates initialConditions, Types.ACTIONS action, Predicates predictedEffects) {
		this.initialConditions = initialConditions;
		this.action = action;
		this.predictedEffects = predictedEffects;
	}
	
	// Para el parser
	public String getInitialConditionsString() {
		return this.initialConditions.toString();
	}

	// Para el parser
	public String getActonString() {
		return this.action.toString();
	}
	
	// Para el parser
	public String getPredictedEffectsString() {
		return this.predictedEffects.toString();
	}

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
		return successCount == usedCount;
	}
	
	public void setCountersAsFailedTheory() {
		usedCount = 999999999;
		successCount = 0;
	}

	public void copyCounters(Theory t) {
		this.successCount = t.getSuccessCount();
		this.usedCount = t.getUsedCount();
	}

	public Theory exclusion(Theory teoria) throws CloneNotSupportedException {
		return new Theory(this.initialConditions.exclusion(teoria.initialConditions), teoria.action, this.predictedEffects);
//		return new Theory(this.initialConditions.exclusion(teoria.initialConditions), teoria.action, this.predictedEffects.exclusion(teoria.predictedEffects));
	}

	/**
	 * <h3>Una teoría A es igual a otra B cuando:</h3>
	 * <ul>
	 * <li>Ambas tienen las mismas acciones.</li>
	 * <li>Las condiciones supuestas son iguales. Entiendase por iguales que son literalmente iguales o bien que las condiciones supuestas de A son
	 * más especificas, es decir más restrictivas que las de B. En otras palabras dada una SITUACIÓN S, puedo aplicar la teoría A o la teoría B,
	 * siendo B una teoria más generica o a lo sumo igual que A.</li>
	 * <li>Los efectos predichos por A son iguales o más especificos que los predichos por B.</li>
	 * </ul>
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		Theory other = (Theory) obj;

		return (this.action.equals(other.action) && this.initialConditions.equals(other.initialConditions)
				&& this.predictedEffects.equals(other.predictedEffects));
	}

	/**
	 * <h3>Una teoría A es similar a otra B cuando:</h3>
	 * <ul>
	 * <li>Ambas tienen las mismas acciones.</li>
	 * <li>Los efectos predichos por A son iguales o más especificos que los predichos por B.</li>
	 * </ul>
	 */
	public boolean similar(Theory other) {
		return (this.action.equals(other.action) && this.predictedEffects.equals(other.predictedEffects));
	}

	/**
	 * <h3>Una teoría A es incompatible con otra B cuando:</h3>
	 * <ul>
	 * <li>Las condiciones supuestas son iguales.</li>
	 * <li>Los efectos predichos por A son distintos.</li>
	 * </ul>
	 */
	public boolean incompatible(Theory other) {
		return (this.initialConditions.equals(other.initialConditions) && !this.predictedEffects.equals(other.predictedEffects));
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Theory cloned = (Theory) super.clone();
		cloned.initialConditions = (Predicates) this.initialConditions.clone();
		cloned.predictedEffects = (Predicates) this.predictedEffects.clone();
		cloned.action = this.action;
		cloned.usedCount = this.usedCount;
		cloned.successCount = this.successCount;
		return cloned;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		sb.append(initialConditions.toString() + " + " + action + " ===> " + predictedEffects.toString() + " (" + successCount + ", " + usedCount
				+ ")");
		return sb.toString();
	}

	public static List<Theory> returnEquals(Theory source, Set<Theory> sample) {
		List<Theory> ret = new ArrayList<Theory>();
		for (Theory t : sample) {
			if (source.equals(t) && t.isSuccesful())
				ret.add(t);
		}
		return ret;
	}

	public static List<Theory> returnSimilars(Theory source, Set<Theory> sample) {
		List<Theory> ret = new ArrayList<Theory>();
		for (Theory t : sample) {
			if (source.similar(t))
				ret.add(t);
		}
		return ret;
	}

	public static List<Theory> returnIncompatibles(Theory source, Set<Theory> sample) {
		List<Theory> ret = new ArrayList<Theory>();
		for (Theory t : sample) {
			if (source.incompatible(t)) {
				ret.add(t);
			}
		}
		return ret;
	}

	public static List<Theory> returnSuccessful(Set<Theory> set) {
		List<Theory> ret = new ArrayList<Theory>();
		for (Theory t : set) {
			if (t.isSuccesful()) {
				ret.add(t);
			}
		}
		return ret;
	}
}
