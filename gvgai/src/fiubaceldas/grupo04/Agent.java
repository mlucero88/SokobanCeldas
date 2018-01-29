package fiubaceldas.grupo04;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import fiubaceldas.grupo04.Model.Entity;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractMultiPlayer {
	
	private List<State> stateHistory = null;
	public Set<Theory> theories = null;	
	private State currentState = null;	
	private int playerID;
	private Entity agentName;

	public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
		this.playerID = playerID;
		agentName = Model.playerIdToAgentName(playerID);
		stateHistory = new ArrayList<State>();
		theories = new HashSet<Theory>();	
	}
	
	@Override
	public ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
        Perception perception = new Perception(stateObs);
        
		List<Theory> teoriasIguales=null;
		List<Theory> teoriasSimilares=null;
		List<Theory> teoriasErroneas=null;

		while(true){
			currentState = new State(perception, agentName);
			stateHistory.add(currentState);
			
			Theory teoriaLocal = buildTheory(currentState);  //Armamos una teoria local
			
			teoriasIguales = Theory.returnIguales(teoriaLocal, teorias); //verificamos si existe una teoria igual
			if(!teoriasIguales.isEmpty()){
				for (Theory ti : teoriasIguales) { //Sí existen teorias iguales las ponderamos
					ti.incSuccessCount();
					ti.incUsedCount();					
				}
				
			}else{ //Si no hay teorias iguales
				teoriasSimilares = Theory.returnSimilares(teoriaLocal, teorias); //obtenemos las teorias similares
				if(!teoriasSimilares.isEmpty()){
					for (Theory ts : teoriasSimilares) {
						Theory teoriaMutante = ts.exclusion(teoriaLocal); //para cada teoría similar aplicamos el algoritmo de exclusión
						teoriaMutante.copyExitosUsos(ts);						
						if(!this.teorias.contains(teoriaMutante)){ //sí no existe la agregamos a la lista
							this.teorias.add(teoriaMutante);
							this.teorias.remove(ts); //olvidamos la teoria anterior
						}
					}
				}
				//Ponderamos y agregamos la teoria local
				if(!this.teorias.contains(teoriaLocal)){
					teoriaLocal.incSuccessCount();
					teoriaLocal.incUsedCount();
					this.teorias.add(teoriaLocal);
				}
			}			 
			//Ajustamos todas las teorias que son erroneas
			teoriasErroneas = Theory.returnErroneas(teoriaLocal, teorias);
			for (Theory te : teoriasErroneas) {
				te.incUsedCount();
			}
		}
        
		return ACTIONS.ACTION_NIL;
	}

	private Theory buildTheory(State state) {
		Theory teoriaLocal = new Theory();
		teoriaLocal.allegedConditions = state.currentState;
		teoriaLocal.predictedEffects = state.efectosObservados;
		return teoriaLocal;
		
	}
}
