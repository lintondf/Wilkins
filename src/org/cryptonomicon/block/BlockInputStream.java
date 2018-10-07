package org.cryptonomicon.block;

import java.io.IOException;

public interface BlockInputStream  {

	public abstract int read() throws IOException;

	public abstract int available() throws IOException;

}