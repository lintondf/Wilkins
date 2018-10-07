package org.cryptonomicon.block;

import java.io.IOException;

public interface BlockReader {

	public abstract Block read() throws IOException;

	public abstract Block readFull() throws IOException;

	public abstract Block getLast();

}