package org.cryptonomicon.block;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Interface BlockInputStream.
 */
public interface BlockInputStream  {

	/**
	 * Read.
	 *
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract int read() throws IOException;

	/**
	 * Available.
	 *
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract int available() throws IOException;

}