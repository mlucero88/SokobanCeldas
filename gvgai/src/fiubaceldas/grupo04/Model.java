package fiubaceldas.grupo04;

public class Model {

	public enum Direction {
		NORTH("N"), SOUTH("S"), EAST("E"), WEST("W"), NORTHEAST("NE"), SOUTHEAST("SE"), NORTHWEST("NW"), SOUTHWEST("SW");

		private String value;

		private Direction(String direction) {
			value = direction;
		}

		public String toString() {
			return value;
		}
	}
	
	public enum Entity {
		WALL('w'), EMPTY('.'), BOX('1'), DESTINATION('0'), AGENT_A('A'), AGENT_B('B');

		private char value;

		private Entity(char object) {
			value = object;
		}

		public char toChar() {
			return value;
		}

		public String toString() {
			return String.valueOf(value);
		}

		public static Entity fromChar(char c) {
			for (Entity enumValue : Entity.class.getEnumConstants()) {
				if (enumValue.toChar() == c) {
					return enumValue;
				}
			}
			throw new IllegalArgumentException("There is no value with name '" + c + " in Enum " + Entity.class.getName());
		}
	}

	public static Entity playerIdToAgentName(int playerID) {
		if (playerID > 1) {
			throw new RuntimeException("Too many players");
		}
		if (playerID < 0) {
			throw new RuntimeException("Unknown player ID");
		}
		return playerID == 0 ? Entity.AGENT_A : Entity.AGENT_B;
	}

	public static <T extends Enum<T>> T valueOfIgnoreCase(Class<T> enumeration, String name) {
		for (T enumValue : enumeration.getEnumConstants()) {
			if (enumValue.toString().equalsIgnoreCase(name)) {
				return enumValue;
			}
		}
		throw new IllegalArgumentException("There is no value with name '" + name + " in Enum " + enumeration.getClass().getName());
	}
}
