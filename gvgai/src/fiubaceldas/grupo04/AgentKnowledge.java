package fiubaceldas.grupo04;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import ontology.Types.ACTIONS;

public class AgentKnowledge {

	private HashMap<String, ACTIONS> stateToAction = new HashMap<>();

	private AgentKnowledge() {
	}

	public ACTIONS getActionFromState(State currentState) {
		ACTIONS action = stateToAction.get(currentState.getDescription());
		if (action == null) {
			final ACTIONS all[] = new ACTIONS[] { ACTIONS.ACTION_UP, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_RIGHT,
					ACTIONS.ACTION_NIL };
			action = all[ThreadLocalRandom.current().nextInt(0, all.length)];
		}
		return action;
	}

	static public AgentKnowledge loadFromFile(String filename) {
		try {
			AgentKnowledge knowledge = new AgentKnowledge();
			JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));

			try {
				reader.beginArray();
				while (reader.hasNext()) {
					String state = null;
					ACTIONS action = null;
					String effect = null;
					int count = 0, success = 0;

					reader.beginObject();
					while (reader.hasNext()) {
						String name = reader.nextName();
						if (name.equals("state")) {
							state = reader.nextString();
						}
						else if (name.equals("action")) {
							action = ACTIONS.fromString(reader.nextString());
						}
						else if (name.equals("effect")) {
							action = ACTIONS.fromString(reader.nextString());
						}
						else if (name.equals("count")) {
							count = reader.nextInt();
						}
						else if (name.equals("success")) {
							success = reader.nextInt();
						}
						else {
							reader.skipValue();
						}
					}
					reader.endObject();
					
					knowledge.stateToAction.put(state, action);
				}
				reader.endArray();
			}
			finally {
				reader.close();
			}

			return knowledge;
		}
		catch (Exception e) {
			System.err.println("Error loading serialized knowledge from " + filename + ": " + e.getMessage());
		}

		return null;
	}

	static public void saveToFile(String filename, Set<Theory> theories) {
		try {
			JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
			writer.setIndent("    ");
			writer.beginArray();
			for (Theory t : theories) {
				writer.beginObject();
				writer.name("state").value(t.getInitialConditionsString());
				writer.name("action").value(t.getActonString());
				writer.name("effect").value(t.getPredictedEffectsString());
				writer.name("count").value(t.getUsedCount());
				writer.name("success").value(t.getSuccessCount());
				writer.endObject();
			}
			writer.endArray();
			writer.close();
		}
		catch (Exception e) {
			System.err.println("Error storing serialized knowledge to " + filename + ": " + e.getMessage());
		}
	}
}
