package fiubaceldas.grupo04;

import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservationMulti;

public class Perception {

	/* Legend:
	 * w: WALL
	 * A: Agent A
	 * 1: Box
	 * 0: box destination
	 * B: Agent B
	 * .: empty space */
	
	/* Sistema de referencia:
	 * --------------> x+
	 * |
	 * |
	 * |
	 * |
	 * V
	 * y+
	 * 
	 */
	
	private char[][] map = null;
	private int sizeWorldWidthInPixels;
	private int sizeWorldHeightInPixels;
	private int levelWidth;
	private int levelHeight;
	private int spriteSizeWidthInPixels;
	private int spriteSizeHeightInPixels;

	public Perception(StateObservationMulti stateObs) {
		ArrayList<Observation>[][] grid = stateObs.getObservationGrid();
		ArrayList<Observation> observationList;
		Observation o;

		this.sizeWorldWidthInPixels = stateObs.getWorldDimension().width;
		this.sizeWorldHeightInPixels = stateObs.getWorldDimension().height;
		this.levelWidth = stateObs.getObservationGrid().length;
		this.levelHeight = stateObs.getObservationGrid()[0].length;
		this.spriteSizeWidthInPixels = stateObs.getWorldDimension().width / levelWidth;
		this.spriteSizeHeightInPixels = stateObs.getWorldDimension().height / levelHeight;

		this.map = new char[levelWidth][levelHeight];
		for (int x = 0; x < levelWidth; x++) {
			for (int y = 0; y < levelHeight; y++) {
				observationList = (grid[x][y]);
				if (!observationList.isEmpty()) {
					o = observationList.get(observationList.size() - 1);
					if (o.category == 4) {
						if (o.itype == 3) {
							this.map[x][y] = '0';
						}
						else if (o.itype == 0) {
							this.map[x][y] = 'w';
						}

					}
					else if (o.category == 0) {
						if (o.itype == 5) {
							this.map[x][y] = 'A';
						}
						else if (o.itype == 6) {
							this.map[x][y] = 'B';
						}
					}
					else if (o.category == 6) {
						this.map[x][y] = '1';
					}
					else {
						this.map[x][y] = '?';
					}
				}
				else {
					this.map[x][y] = '.';
				}
			}
		}
	}

	public char getAt(int x, int y) {
		return map[x][y];
	}

	public char[][] getMap() {
		return map;
	}

	public int getSizeWorldWidthInPixels() {
		return sizeWorldWidthInPixels;
	}

	public int getSizeWorldHeightInPixels() {
		return sizeWorldHeightInPixels;
	}

	public int getLevelWidth() {
		return levelWidth;
	}

	public int getLevelHeight() {
		return levelHeight;
	}

	public int getSpriteSizeWidthInPixels() {
		return spriteSizeWidthInPixels;
	}

	public int getSpriteSizeHeightInPixels() {
		return spriteSizeHeightInPixels;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("");
		if (map != null) {
			for (int y = 0; y < levelHeight; y++) {
				for (int x = 0; x < levelWidth; x++) {
					sb.append(map[x][y]);
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}
