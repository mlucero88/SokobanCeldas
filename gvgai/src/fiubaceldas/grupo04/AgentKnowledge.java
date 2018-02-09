package fiubaceldas.grupo04;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import ontology.Types.ACTIONS;

public class AgentKnowledge {

	private HashMap<String, ACTIONS> stateToAction = new HashMap<>();

	private AgentKnowledge() {
	}
	
	public ACTIONS getActionFromState(State currentState) {
		ACTIONS action = stateToAction.get(currentState.getDescription());
		if (action == null) {
			final ACTIONS all[] = new ACTIONS[] { ACTIONS.ACTION_UP, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_NIL };
			action = all[ThreadLocalRandom.current().nextInt(0, all.length)];
		}
		return action;
	}

	static public AgentKnowledge loadFromFile(String filename) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			String jsonString = new String(Files.readAllBytes(Paths.get(filename)));
			Map<String, QState> json = gson.fromJson(jsonString, new TypeToken<Map<String, QState>>() {
			}.getType());
			HashMap<String, QState> table = new HashMap<String, QState>(json);
			Knowledge k = new Knowledge();
			k.setQTable(table);
			return k;
		}
		catch (Exception e) {
			System.err.println("Error loading serialized knowledge from " + filename + ": " + e.getMessage());
		}
		return null;
	}

	static public void saveToFile(String filename, Set<Theory> theories) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			String text = gson.toJson(k.QTable());
			out.write(text);
			out.close();
		}
		catch (Exception e) {
			System.err.println("Error storing serialized knowledge to " + filename + ": " + e.getMessage());
		}
	}
}
