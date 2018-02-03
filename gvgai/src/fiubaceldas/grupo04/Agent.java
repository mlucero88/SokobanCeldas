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

	private List<State> stateHistory = null;
	public Set<Theory> theories = null;
	private State currentState = null;
	private Entity agentName;
	private int playerID;

	public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
		this.playerID = playerID;
		agentName = Model.playerIdToAgentName(playerID);
		stateHistory = new ArrayList<State>();
		theories = new HashSet<Theory>();
	}

	@Override
	public ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
		Perception perception = new Perception(stateObs);

		currentState = new State(perception);
		stateHistory.add(currentState);

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
		
		
		Predicates initialConditions = new Predicates(perception); // NO DEBERIA SER A PARTIR DEL ESTADO EN VEZ DE LA PERCEPCION?
		ACTIONS actionToTake = getRandomAction();
		Predicates predictedEffects = new Predicates(perception);

		Theory teoriaLocal = new Theory(new Predicates(perception), actionToTake, ); // Armamos una teoria local

		List<Theory> teoriasIguales = Theory.returnIguales(teoriaLocal, theories); // verificamos si existe una teoria igual
		if (!teoriasIguales.isEmpty()) {
			for (Theory ti : teoriasIguales) { // Sí existen teorias iguales las ponderamos
				ti.incSuccessCount();
				ti.incUsedCount();
			}

		}
		else { // Si no hay teorias iguales
			List<Theory> teoriasSimilares = Theory.returnSimilares(teoriaLocal, theories); // obtenemos las teorias similares
			if (!teoriasSimilares.isEmpty()) {
				for (Theory ts : teoriasSimilares) {
					Theory teoriaMutante = ts.exclusion(teoriaLocal); // para cada teoría similar aplicamos el algoritmo de exclusión
					teoriaMutante.copyExitosUsos(ts);
					if (!theories.contains(teoriaMutante)) { // sí no existe la agregamos a la lista
						theories.add(teoriaMutante);
						theories.remove(ts); // olvidamos la teoria anterior
					}
				}
			}
			// Ponderamos y agregamos la teoria local
			if (!theories.contains(teoriaLocal)) {
				teoriaLocal.incSuccessCount();
				teoriaLocal.incUsedCount();
				theories.add(teoriaLocal);
			}
		}
		
		// Ajustamos todas las teorias que son erroneas
		List<Theory> teoriasErroneas = Theory.returnErroneas(teoriaLocal, theories);
		for (Theory te : teoriasErroneas) {
			te.incUsedCount();
		}

		return ACTIONS.ACTION_NIL;
	}

	private ACTIONS getRandomAction() {
		final ACTIONS all[] = new ACTIONS[] { ACTIONS.ACTION_UP, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_NIL };
		return all[ThreadLocalRandom.current().nextInt(0, all.length)];
	}
}
