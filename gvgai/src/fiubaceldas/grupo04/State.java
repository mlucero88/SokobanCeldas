package fiubaceldas.grupo04;

public class State {
	private String description;

	public State(Perception p) {
		StringBuilder sb = new StringBuilder("");
		
		for (int y = 0; y < p.getLevelHeight(); y++) {
			for (int x = 0; x < p.getLevelWidth(); x++) {
				sb.append(p.getAt(x, y));
			}
			sb.append("\\n");
		}

		description = sb.toString();
	}

	public String getDescription() {
		return description;
	}
}
