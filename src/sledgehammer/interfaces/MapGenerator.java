package sledgehammer.interfaces;

import sledgehammer.wrapper.map.Cell;
import sledgehammer.wrapper.map.Chunk;

public interface MapGenerator {

	public Chunk generateChunk(int x, int y);

	public Cell generateCell(int wx, int wy);
	
}
