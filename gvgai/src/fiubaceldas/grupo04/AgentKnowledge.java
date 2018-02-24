package fiubaceldas.grupo04;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import ontology.Types.ACTIONS;

public class AgentKnowledge {

	private AgentKnowledge() {
	}

	static public void loadFromFile(String filename, TheoryContainer theories) {
		try {
			JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));

			try {
				reader.beginArray();
				while (reader.hasNext()) {
					String state = null;
					ACTIONS action = null;
					String effect = null;
					int count = 0, success = 0, utility = 0;

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
						else if (name.equals("utility")) {
							utility = reader.nextInt();
						}
						else {
							reader.skipValue();
						}
					}
					reader.endObject();

					theories.addOrReplace(new Theory(state, action, effect, count, success, utility));
				}
				reader.endArray();
			}
			finally {
				reader.close();
			}
		}
		catch (FileNotFoundException e) {
			try {
				JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
				writer.beginArray();
				writer.endArray();
				writer.close();
			}
			catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		catch (Exception e) {
			System.err.println("Error loading serialized knowledge from " + filename + ": " + e.getMessage());
		}
	}

	static public void saveToFile(String filename, TheoryContainer theories) {
		try {
			JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
			writer.setIndent("    ");
			writer.beginArray();
			for (Theory t : theories) {
				writer.beginObject();
				writer.name("state").value(t.getInitialConditionsString());
				writer.name("action").value(t.getActonString());
				writer.name("effect").value(t.getPredictedEffectsString());
				writer.name("count").value(t.usedCount);
				writer.name("success").value(t.successCount);
				writer.name("utility").value(t.utility);
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
