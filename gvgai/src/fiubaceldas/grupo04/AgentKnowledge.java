package fiubaceldas.grupo04;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import ontology.Types.ACTIONS;
import tools.Pair;

public class AgentKnowledge {

	private SetMultimap<String, Pair<ACTIONS, Double>> stateToAction = HashMultimap.create();

	private AgentKnowledge() {
	}

	public ACTIONS getActionFromState(State currentState) {
		// El problema aca es q no tiene en cuenta los '*' y los hash fallan. Deberia sacar los hash e implementar un comparador, pero pierdo performance.
		Set<Pair<ACTIONS, Double>> actions = stateToAction.get(currentState.getDescription());
		if (actions == null) {
			System.out.println(" * Elegi movimiento aleatorio *");
			final ACTIONS all[] = new ACTIONS[] { ACTIONS.ACTION_UP, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_RIGHT };
			return all[ThreadLocalRandom.current().nextInt(0, all.length)];
		}
		else {
			Pair<ACTIONS, Double> bestAction = null;
			for (Pair<ACTIONS, Double> act : actions) {
				if (bestAction == null) {
					bestAction = act;
				}
				else if (act.second.longValue() > bestAction.second.longValue()) {
					bestAction = act;
				}
			}
			return bestAction.first;
		}
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
							effect = reader.nextString();
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

					knowledge.stateToAction.put(state, new Pair<ACTIONS, Double>(action, new Double((double) success / (double) count)));
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
