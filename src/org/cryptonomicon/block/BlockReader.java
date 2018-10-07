package org.cryptonomicon.block;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Interface BlockReader.
 */
public interface BlockReader {

	/**
	 * Read.
	 *
	 * @return the block
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract Block read() throws IOException;

	/**
	 * Read full.
	 *
	 * @return the block
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract Block readFull() throws IOException;

	/**
	 * Gets the last.
	 *
	 * @return the last
	 */
	public abstract Block getLast();

}