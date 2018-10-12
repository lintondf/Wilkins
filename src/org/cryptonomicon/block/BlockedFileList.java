/**
 * 
 */
package org.cryptonomicon.block;

import java.util.Iterator;
import java.util.List;

/**
 * @author lintondf
 *
 */
public interface BlockedFileList extends Iterable<BlockedFile> {
	
	void add( BlockedFile file );
	void addAll( BlockedFileList that );
	BlockedFile get(int index);
	int size();

}
