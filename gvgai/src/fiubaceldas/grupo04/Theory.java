package fiubaceldas.grupo04;

import fiubaceldas.grupo04.Model.Entity;
import ontology.Types;
import ontology.Types.ACTIONS;

public class Theory implements Cloneable {

	private Predicates initialConditions;
	private Types.ACTIONS action;
	private Predicates predictedEffects;

	public int usedCount = 0;
	public int successCount = 0;
	public int utility = 0;

	public Theory(Predicates initialConditions, Types.ACTIONS action, Predicates predictedEffects) {
		this.initialConditions = initialConditions;
		this.action = action;
		this.predictedEffects = predictedEffects;
	}

	public void copyCounters(Theory t) {
		this.usedCount = t.usedCount;
		this.successCount = t.successCount;
		this.utility = t.utility;
	}

	public double getSuccessRate() {
		return (double) successCount / (double) usedCount;
	}

	public Types.ACTIONS getAction() {
		return action;
	}

	public Theory exclusion(Theory teoria) throws CloneNotSupportedException {
		return new Theory(this.initialConditions.exclusion(teoria.initialConditions), teoria.action, (Predicates) this.predictedEffects.clone());
		// return new Theory(this.initialConditions.exclusion(teoria.initialConditions), teoria.action,
		// this.predictedEffects.exclusion(teoria.predictedEffects));
	}

	public boolean isApplicableToState(Predicates stateDescription) {
		// El estado actual es igual o mas especifico q esta teoria ?
		return stateDescription.equals(initialConditions);
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
	 * <li>Ambas tienen las mismas acciones.</li>
	 * <li>Las condiciones supuestas son iguales.</li>
	 * <li>Los efectos predichos por A son distintos.</li>
	 * </ul>
	 */
	public boolean incompatible(Theory other) {
		return (this.action.equals(other.action) && this.initialConditions.equals(other.initialConditions)
				&& !this.predictedEffects.equals(other.predictedEffects));
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Theory cloned = new Theory((Predicates) this.initialConditions.clone(), this.action, (Predicates) this.predictedEffects.clone());
		cloned.usedCount = this.usedCount;
		cloned.successCount = this.successCount;
		cloned.utility = this.utility;
		return cloned;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		sb.append(initialConditions.toString() + " + " + action + " ===> " + predictedEffects.toString() + " (" + successCount + ", " + usedCount
				+ ", " + utility + ")");
		return sb.toString();
	}

	// Para el parser
	public Theory(String initialConditionsDesc, Types.ACTIONS action, String predictedEffectsDesc, int usedCount, int successCount, int utility) {
		this.initialConditions = new Predicates(initialConditionsDesc);
		this.action = action;
		this.predictedEffects = new Predicates(predictedEffectsDesc);
		this.usedCount = usedCount;
		this.successCount = successCount;
		this.utility = utility;
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

	// Harcodeo de teoria que hace ganar. Dependiente del nivel a elegir
	public static Theory getWinnerTheory(Entity winnerAgent) {
		String initDesc = "******\n**" + winnerAgent.toChar() + "10*\n******\n******\n******\n******\n";
		String effectDesc = "******\n***" + winnerAgent.toChar() + "1*\n******\n******\n******\n******\n";
		Theory t = new Theory(new Predicates(initDesc), ACTIONS.ACTION_RIGHT, new Predicates(effectDesc));
		t.usedCount = 10000;
		t.successCount = 10000;
		t.utility = 100;
		return t;
	}
}
