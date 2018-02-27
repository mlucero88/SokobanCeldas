package fiubaceldas.grupo04;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import fiubaceldas.grupo04.Model.Entity;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractMultiPlayer {

	private final static int KNOWLEDGE_SAVE_ITERS = 1000;
	private final static int SLEEP_MILISEC = 100; // Para cuando juego con visuals, asi veo mejor los movimientos
	private final static int MAX_LOOP_ACTIONS = 2;

	/* Horrible pero los objetos agentes van cambiando cuando se reinicia el juego y pierdo las teorias generadas.
	 * Ademas no puedo pasarle las teorias anteriores como parametros al contructor */
	private static ArrayList<TheoryContainer> theories = new ArrayList<TheoryContainer>(2);
	private static int totalRounds = 1;

	private State lastState = null;
	private State currentState = null;
	private ACTIONS lastAction = null;
	private Entity agentName;
	private int round = 0;
	private int playerID;
	private int loopCounter = 0;
	private boolean chooseOtherTheory = false;

	public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
		this.playerID = playerID;
		agentName = Model.playerIdToAgentName(playerID);
		while (theories.size() < 2) {
			theories.add(new TheoryContainer());
		}

		if (theories.get(playerID).isEmpty()) {
			AgentKnowledge.loadFromFile("agent_" + agentName.toString() + ".json", theories.get(playerID));
			if (theories.get(playerID).isEmpty()) {
				/* Cargue un archivo vacio. Esto sucede cuando no habia ninguna corrida anterior. Aca lo que quiero hacer es
				 * harcodear la teoria de cuando la caja esta al lado del destino y el agente esta en posicion para empujar
				 * la caja para ganar, ya que esta teoria no se genera en el "act" xq el juego finaliza antes */
				theories.get(playerID).addOrReplace(Theory.getWinnerTheory(agentName));
			}
		}
	}

	@Override
	public ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
		currentState = new State(new Perception(stateObs));
		boolean isDeadlockState = currentState.isBoxDeadlock();
		boolean isEvenRound = (round++ % 2) == 0;

		if (totalRounds % KNOWLEDGE_SAVE_ITERS == 0) {
			// Harcodeo los nro de jugador para no complicarme
			AgentKnowledge.saveToFile("agent_A.json", theories.get(0));
			AgentKnowledge.saveToFile("agent_B.json", theories.get(1));
		}
		if (playerID == 0) {
			// Solo el player A incrementa rondas
			totalRounds++;
		}

		/* Aca viene la logica qué hay q hacer dependiendo del numero de ronda. Hay 2 opciones:
		 * (ronda es par): 		Player_A -> Guarda el estado actual y la accion que va a realizar. Realiza una accion distinta de null 
		 * 						Player_B -> Con el estado y accion de la ronda anterior, mas el estado actual, arma la teoria (excepto en ronda=0). Realiza accion null
		 * 
		 * (ronda es impar):	Player_A -> Con el estado y accion de la ronda anterior, mas el estado actual, arma la teoria. Realiza accion null
		 * 						Player_B -> Guarda el estado actual y la accion que va a realizar. Realiza una accion distinta de null 
		 */

		if ((isEvenRound && agentName == Entity.AGENT_A) || (!isEvenRound && agentName == Entity.AGENT_B)) {
			if (isDeadlockState) {
				return ACTIONS.ACTION_NIL; // No tiene sentido moverme
			}

			ACTIONS actionToTake = getAction(currentState);

			if ((lastAction == ACTIONS.ACTION_UP && actionToTake == ACTIONS.ACTION_DOWN)
					|| (lastAction == ACTIONS.ACTION_DOWN && actionToTake == ACTIONS.ACTION_UP)
					|| (lastAction == ACTIONS.ACTION_LEFT && actionToTake == ACTIONS.ACTION_RIGHT)
					|| (lastAction == ACTIONS.ACTION_RIGHT && actionToTake == ACTIONS.ACTION_LEFT)) {
				if (++loopCounter > MAX_LOOP_ACTIONS) {
					loopCounter = 0;
					chooseOtherTheory = true;
				}
			}
			else {
				loopCounter = 0;
			}

			lastState = currentState;
			lastAction = actionToTake;

			try {
				Thread.sleep(SLEEP_MILISEC);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}

			return actionToTake;
		}
		else {
			/* Caso excepcional que sucede al player_B en la primer ronda de cada juego nuevo */
			if (lastState == null) {
				return ACTIONS.ACTION_NIL;
			}

			Theory localTheory = new Theory(new Predicates(lastState), lastAction, new Predicates(currentState)); // Armamos una teoria local
			localTheory.utility = isDeadlockState ? 0 : State.calculateUtility(lastState, currentState, agentName);

			List<Theory> equalTheories = theories.get(playerID).returnEqualTheories(localTheory); // verificamos si existe una teoria igual
			if (!equalTheories.isEmpty()) {
				for (Theory t : equalTheories) { // Sí existen teorias iguales las ponderamos
					t.successCount++;
					t.usedCount++;
				}
			}
			else { // Si no hay teorias iguales
				List<Theory> similarTheories = theories.get(playerID).returnSimilarTheories(localTheory); // obtenemos las teorias similares
				if (!similarTheories.isEmpty()) {
					for (Theory t : similarTheories) {
						Theory mutantTheory;
						try {
							mutantTheory = t.exclusion(localTheory); // para cada teoría similar aplicamos el algoritmo de exclusión
						}
						catch (CloneNotSupportedException e) {
							e.printStackTrace();
							return ACTIONS.ACTION_NIL;
						}
						mutantTheory.copyCounters(t);
						theories.get(playerID).addOrReplace(mutantTheory); // se agrega y quita las que eran mas especificas
					}
				}
				else {
					// Ponderamos y agregamos la teoria local
					localTheory.successCount++;
					localTheory.usedCount++;
					theories.get(playerID).addOrReplace(localTheory);
				}
			}

			// Ajustamos todas las teorias que son erroneas
			List<Theory> incompatibleTheories = theories.get(playerID).returnIncompatibleTheories(localTheory);
			for (Theory t : incompatibleTheories) {
				t.usedCount++;
			}

			if (isDeadlockState) {
				/* El movimiento anterior de este jugador generó el deadlock. Ya guardamos la teoria asi que reinicio */
				throw new ExceptionRestart();
			}

			try {
				Thread.sleep(SLEEP_MILISEC);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}

			return ACTIONS.ACTION_NIL;
		}
	}

	private ACTIONS getAction(State state) {
		boolean chooseRandom = (ThreadLocalRandom.current().nextInt(theories.get(playerID).size()) % 3 == 0);
		if (!chooseRandom) {
			List<Theory> candidatesTheories = theories.get(playerID).returnApplicableTheories(state);
			if (!candidatesTheories.isEmpty()) {
				Collections.sort(candidatesTheories, new Comparator<Theory>() {
					@Override
					public int compare(Theory lhs, Theory rhs) {
						// -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
						return lhs.utility > rhs.utility ? -1 : (lhs.utility < rhs.utility) ? 1 : 0;
					}
				});

				int indexNextToLastSameUtility = 1;
				Iterator<Theory> iter = candidatesTheories.iterator();
				int bestUtility = iter.next().utility;
				while (iter.hasNext()) {
					if (iter.next().utility != bestUtility) {
						break;
					}
					else {
						indexNextToLastSameUtility++;
					}
				}

				Theory chosen = candidatesTheories.get(ThreadLocalRandom.current().nextInt(indexNextToLastSameUtility)); // Elijo una aleatoria de
				if (chooseOtherTheory && candidatesTheories.size() > indexNextToLastSameUtility) {
					// Vengo repitiendo movimientos; pruebo bajar utilidad. Si hay otra, elijo otra, si no espero al siguiente movimiento para elegir
					// otra
					chosen = candidatesTheories.get(indexNextToLastSameUtility);
					chooseOtherTheory = false;
				}

				if (chosen.utility > 0) {
					return chosen.getAction();
				}
			}
		}
		ArrayList<ACTIONS> candidates = state.getPossibleActions(agentName);
		if (candidates.isEmpty()) {
			System.out.println("No tengo acciones");
			return ACTIONS.ACTION_NIL;
		}
		return candidates.get(ThreadLocalRandom.current().nextInt(0, candidates.size()));
	}
}
