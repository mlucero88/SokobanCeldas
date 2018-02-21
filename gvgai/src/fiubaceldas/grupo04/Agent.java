package fiubaceldas.grupo04;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import fiubaceldas.grupo04.Model.Entity;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractMultiPlayer {

	private static final int MAX_TRAINING_ROUNDS = 1000;
	private static final boolean TRAINING_MODE = true;

	/* Horrible pero los objetos agentes van cambiando cuando se reinicia el juego y pierdo las teorias generadas.
	 * Ademas no puedo pasarle las teorias anteriores como parametros al contructor */
	private static ArrayList<HashSet<Theory>> theories = new ArrayList<HashSet<Theory>>(2);
	private static boolean[] finished = new boolean[2];
	private static int totalRounds = 0;

	private State lastState = null;
	private State currentState = null;
	private ACTIONS lastAction = null;
	private AgentKnowledge knowledge = null;
	private Entity agentName;
	private int round = 0;
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
				theories.add(new HashSet<Theory>(4096));
			}
		}
	}

	@Override
	public ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
		currentState = new State(new Perception(stateObs));
		boolean isDeadlockState = currentState.isBoxDeadlock();
		boolean isEvenRound = (round++ % 2) == 0;

		if (!TRAINING_MODE) {
			/* En rondas pares mueve player_A, en impares mueve player_B */
			if (!isDeadlockState) {
				if ((isEvenRound && agentName == Entity.AGENT_A) || (!isEvenRound && agentName == Entity.AGENT_B)) {
					return knowledge.getActionFromState(currentState);
				}
				else {
					return ACTIONS.ACTION_NIL;
				}
			}
			System.out.println("*** PERDIMOS EL JUEGO !! ***");
			throw new ExceptionQuitGame();
		}

		/* A partir de aca es todo modo entrenamiento */
		if (totalRounds >= MAX_TRAINING_ROUNDS) {
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
			totalRounds++;
		}

		if (isDeadlockState) {
			/* Veo quien fue el ultimo que movio y le guardo la teoria como fallida */
			if ((isEvenRound && agentName == Entity.AGENT_B) || (!isEvenRound && agentName == Entity.AGENT_A)) {
				/* Este jugador fue quien movio mal */
				Theory failedTheory = new Theory(new Predicates(lastState), lastAction, new Predicates(currentState));
				failedTheory.setCountersAsFailedTheory();
				theories.get(playerID).add(failedTheory);

				throw new ExceptionRestart();
			}
			else {
				/* El otro jugador fue quien movio mal. No hago nada asi se ejecuta el "act" del otro agente */
				return ACTIONS.ACTION_NIL;
			}
		}

		/* Aca viene la logica qué hay q hacer dependiendo del numero de ronda. Hay 2 opciones:
		 * (ronda es par): 		Player_A -> Guarda el estado actual y la accion que va a realizar. Realiza una accion distinta de null 
		 * 						Player_B -> Con el estado y accion de la ronda anterior, mas el estado actual, arma la teoria (excepto en ronda=0). Realiza accion null
		 * 
		 * (ronda es impar):	Player_A -> Con el estado y accion de la ronda anterior, mas el estado actual, arma la teoria. Realiza accion null
		 * 						Player_B -> Guarda el estado actual y la accion que va a realizar. Realiza una accion distinta de null 
		 * 
		 * El diseño es pobre, pero no importa */

		if ((isEvenRound && agentName == Entity.AGENT_A) || (!isEvenRound && agentName == Entity.AGENT_B)) {
			ACTIONS actionToTake = getRandomAction(currentState);
			lastState = currentState;
			lastAction = actionToTake;

			return actionToTake;
		}
		else {
			/* Caso excepcional que sucede al player_B en la primer ronda de cada juego nuevo */
			if (lastState == null) {
				return ACTIONS.ACTION_NIL;
			}

			Theory localTheory = new Theory(new Predicates(lastState), lastAction, new Predicates(currentState)); // Armamos una teoria local

			List<Theory> equalTheories = Theory.returnSame(localTheory, theories.get(playerID)); // verificamos si existe una teoria igual
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
							theories.get(playerID).remove(t); // olvidamos la teoria anterior
							theories.get(playerID).add(mutantTheory);
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

			return ACTIONS.ACTION_NIL;
		}
	}

	private ACTIONS getRandomAction(State state) {
		ArrayList<ACTIONS> candidates = state.getPossibleActions(agentName);
		return candidates.get(ThreadLocalRandom.current().nextInt(0, candidates.size()));
	}
}
