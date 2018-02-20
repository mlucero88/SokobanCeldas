package fiubaceldas.grupo04;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import fiubaceldas.grupo04.Model.Entity;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractMultiPlayer {

	private static final int MAX_TRAINING_GAMES = 10000;
	private static final boolean TRAINING_MODE = true;

	/* Horrible pero los objetos agentes van cambiando cuando se reinicia el juego y pierdo las teorias generadas.
	 * Ademas no puedo pasarle las teorias anteriores como parametros al contructor */
	private static ArrayList<Set<Theory>> theories = new ArrayList<Set<Theory>>(2);
	private static boolean[] finished = new boolean[2];
	private static int rounds = 0;

	private State lastState = null;
	private State currentState = null;
	private ACTIONS lastAction = null;
	private Entity agentName;
	private AgentKnowledge knowledge = null;
	private int playerID;

	public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
		this.playerID = playerID;
		agentName = Model.playerIdToAgentName(playerID);
		if (!TRAINING_MODE) {
			knowledge = AgentKnowledge.loadFromFile("agent_" + agentName.toString() + ".json");
		}
		else {
			finished[playerID] = false;
			while (theories.size() < 2) {
				theories.add(new HashSet<Theory>());
			}
		}
	}

	@Override
	public ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
		currentState = new State(new Perception(stateObs));
		boolean isDeadlockState = currentState.isBoxDeadlock();

		if (!TRAINING_MODE) {
			if (!isDeadlockState) {
				return knowledge.getActionFromState(currentState);
			}
			System.out.println("*** PERDIMOS EL JUEGO !! ***");
			throw new ExceptionQuitGame();
		}

		/* A partir de aca es todo modo entrenamiento */
		if (rounds >= MAX_TRAINING_GAMES) {
			if (!finished[playerID]) {
				AgentKnowledge.saveToFile("agent_" + agentName.toString() + ".json", theories.get(playerID));
				System.out.println("*** FINALIZÓ EL ENTRENAMIENTO DEL AGENTE_" + agentName.toString() + " ***");
				finished[playerID] = true;

				if (finished[0] && finished[1]) {
					throw new ExceptionQuitGame();
				}
			}
			return ACTIONS.ACTION_NIL;
		}
		else if (playerID == 0) {
			// Solo el player A incrementa rondas
			rounds++;
		}

		if (isDeadlockState) {
			/* NO CONVIENE GUARDAR ESTA TEORIA DE FALLO XQ PUEDE SER QUE EL QUE MOVIO LA CAJA HACIA UN DEADLOCK HAYA SIDO
			 * EL OTRO JUGADOR. SIEMPRE EL act DE player_A SE EJECUTA ANTES QUE EL DEL player_B, ENTONCES SI FUE player_B
			 * QUIEN PUSO LA CAJA EN DEADLOCK, ESTOY GUARDANDO LA ULTIMA ACCION DE player_A COMO LA TEORIA QUE HIZO PERDER
			 * EL JUEGO
			 */
			// Theory failedTheory = new Theory(new Predicates(lastState), lastAction, new Predicates(currentState));
			// failedTheory.setCountersAsFailedTheory();
			// theories.add(failedTheory);

			throw new ExceptionRestart();
		}

		ACTIONS actionToTake = getRandomAction(currentState);

		/* Salteo mi primera accion. Espero al turno siguiente para armar la teoria */
		if (lastState != null) {
			Theory localTheory = new Theory(new Predicates(lastState), lastAction, new Predicates(currentState)); // Armamos una teoria local

			List<Theory> equalTheories = Theory.returnEquals(localTheory, theories.get(playerID)); // verificamos si existe una teoria igual
			if (!equalTheories.isEmpty()) {
				for (Theory t : equalTheories) { // Sí existen teorias iguales las ponderamos
					t.incSuccessCount();
					t.incUsedCount();
				}
			}
			else { // Si no hay teorias iguales
				List<Theory> similarTheories = Theory.returnSimilars(localTheory, theories.get(playerID)); // obtenemos las teorias similares
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
						if (!theories.get(playerID).contains(mutantTheory)) { // sí no existe la agregamos a la lista
							theories.get(playerID).add(mutantTheory);
							theories.get(playerID).remove(t); // olvidamos la teoria anterior
						}
					}
				}
				// Ponderamos y agregamos la teoria local
				if (!theories.get(playerID).contains(localTheory)) {
					localTheory.incSuccessCount();
					localTheory.incUsedCount();
					theories.get(playerID).add(localTheory);
				}
			}

			// Ajustamos todas las teorias que son erroneas
			List<Theory> incompatibleTheories = Theory.returnIncompatibles(localTheory, theories.get(playerID));
			for (Theory t : incompatibleTheories) {
				t.incUsedCount();
			}
		}

		lastState = currentState;
		lastAction = actionToTake;
		return actionToTake;
	}

	private ACTIONS getRandomAction(State state) {
		ArrayList<ACTIONS> candidates = state.getPossibleActions(agentName);
		candidates.add(ACTIONS.ACTION_NIL);
		return candidates.get(ThreadLocalRandom.current().nextInt(0, candidates.size()));
	}
}
