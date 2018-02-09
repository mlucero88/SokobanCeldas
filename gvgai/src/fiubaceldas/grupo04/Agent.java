package fiubaceldas.grupo04;

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

	private static final int TRAINING_ROUNDS = 1000;
	private static final boolean TRAINING_MODE = true;

	private State lastState = null;
	private State currentState = null;
	private ACTIONS lastAction = null;
	private Entity agentName;
	private AgentKnowledge knowledge = null;
	private int playerID;
	private int rounds = 0;

	public Set<Theory> theories = new HashSet<Theory>();

	public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
		this.playerID = playerID;
		agentName = Model.playerIdToAgentName(playerID);
		if (!TRAINING_MODE) {
			knowledge = AgentKnowledge.loadFromFile("agent_" + agentName.toString() + ".json");
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
		if (rounds++ >= TRAINING_ROUNDS) {
			AgentKnowledge.saveToFile("agent_" + agentName.toString() + ".json", theories);
			System.out.println("*** FINALIZÓ EL ENTRENAMIENTO DEL AGENTE_" + agentName.toString() + " ***");

			/* TODO Creo que solo va a guardar a disco el conocimiento del agente que primero llegue a esta parte
			 * del codigo y el otro agente no va a guardar por arrojar la excepcion. Revisar al hacer la corrida */
			throw new ExceptionQuitGame();
		}
		
		if (isDeadlockState) {
			// TODO Armo una teoria antes de reiniciar?
			lastState = null;
			currentState = null;
			lastAction = null;

			System.out.println("*** LLEGAMOS A UN DEADLOCK !! ***");
			throw new ExceptionRestart();
		}
		

		ACTIONS actionToTake = getRandomAction();

		if (lastState == null) {
			/* Es mi primera accion. Espero al turno siguiente para armar la teoria */
			lastState = currentState;
			lastAction = actionToTake;
			return actionToTake;
		}

		/* AGARRAR LA PERCEPCION, ARMAR EL ESTADO A PARTIR DE LA PERCEPCION (ESTO SERIA EL MAPA SIN WILDCARDS), AGREGAR
		 * EL ESTADO AL HISTORIAL. CHEQUEAR SI ESTAMOS EN UN ESTADO DE "FIN DE JUEGO" (CAJA CONTRA 2 PAREDES). 
		 * 
		 * [ENTRENAMIENTO!] CON CADA ACCION POSIBLE DEL JUGADOR, ARMAR UNA TEORIA CON EL ESTADO ACTUAL COMO CONDICION INICIAL (ESTO NO SE..DONDE
		 * ENTRARIAN LOS WILDCARDS?) + LA ACCION + EL ESTADO ESPERADO, Q LO CALCULO TOMANDO COMO SUPOSICION Q EL OTRO JUGADOR NO SE MUEVE.
		 * A ESTA TEORIA HAY Q ASIGNARLE UNA UTILIDAD, QUE PUEDO TOMAR COMO CRITERIO LA DISTANCIA DE LA CAJA A LA META PONDERADO CON LA DISTANCIA
		 * DEL JUGADOR A LA CAJA (O ALGO ASI). UNA VEZ HECHO TODAS LAS TEORIAS, LAS GUARDO. LA PREGUNTA ES.. COMO LAS REFINO? SUPONGO QUE LA UTILIDAD
		 * INCREMENTARA/DECREMENTARA A MEDIDA QUE LA TEORIA SE USE MAS VECES, O SEA NO DEBERIA "REINICIAR" EL MEDIDOR DE UTILIDAD ENTRE LAS 1000+ CORRIDAS
		 * QUE HAGA PARA EL ENTRENAMIENTO.
		 * UNA VEZ Q HAGA LAS 1000+ CORRIDAS, VUELVO TODO LO APRENDIDO A JSON
		 * 
		 * 
		 * [YA ENTRENADO!] BUSCAR EN EL CONOCIMIENTO EL ESTADO ACTUAL Y ELEGIR LA ACCION QUE TIENE MAYOR UTILIDAD
		 */

		Theory localTheory = new Theory(new Predicates(lastState), lastAction, new Predicates(currentState)); // Armamos una teoria local

		List<Theory> equalTheories = Theory.returnEquals(localTheory, theories); // verificamos si existe una teoria igual
		if (!equalTheories.isEmpty()) {
			for (Theory t : equalTheories) { // Sí existen teorias iguales las ponderamos
				t.incSuccessCount();
				t.incUsedCount();
			}

		}
		else { // Si no hay teorias iguales
			List<Theory> similarTheories = Theory.returnSimilars(localTheory, theories); // obtenemos las teorias similares
			if (!similarTheories.isEmpty()) {
				for (Theory t : similarTheories) {
					Theory teoriaMutante = t.exclusion(localTheory); // para cada teoría similar aplicamos el algoritmo de exclusión
					teoriaMutante.copyExitosUsos(t);
					if (!theories.contains(teoriaMutante)) { // sí no existe la agregamos a la lista
						theories.add(teoriaMutante);
						theories.remove(t); // olvidamos la teoria anterior
					}
				}
			}
			// Ponderamos y agregamos la teoria local
			if (!theories.contains(localTheory)) {
				localTheory.incSuccessCount();
				localTheory.incUsedCount();
				theories.add(localTheory);
			}
		}

		// Ajustamos todas las teorias que son erroneas
		List<Theory> incompatibleTheories = Theory.returnIncompatibles(localTheory, theories);
		for (Theory t : incompatibleTheories) {
			t.incUsedCount();
		}

		return ACTIONS.ACTION_NIL;
	}

	private ACTIONS getRandomAction() {
		final ACTIONS all[] = new ACTIONS[] { ACTIONS.ACTION_UP, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_NIL };
		return all[ThreadLocalRandom.current().nextInt(0, all.length)];
	}
}
