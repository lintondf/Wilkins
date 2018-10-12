/**
 * 
 */
package org.cryptonomicon.block.allocated;

import java.util.ArrayList;
import java.util.Iterator;

import org.cryptonomicon.block.BlockedFile;
import org.cryptonomicon.block.BlockedFileList;

/**
 * @author lintondf
 *
 */
public class AllocatedBlockedFileList implements BlockedFileList {
	
	ArrayList<AllocatedBlockedFile> files = new ArrayList<>();
	
	class MyIterator implements Iterator<BlockedFile> {
		
		Iterator<AllocatedBlockedFile> it;
		
		public MyIterator( Iterator<AllocatedBlockedFile> it) {
			this.it = it;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public BlockedFile next() {
			return it.next();
		}
		
	}

	@Override
	public Iterator<BlockedFile> iterator() {
		return new MyIterator( files.iterator() );
	}

	@Override
	public void add(BlockedFile file) {
		files.add((AllocatedBlockedFile) file);
	}


	@Override
	public void addAll(BlockedFileList that) {
		AllocatedBlockedFileList other = (AllocatedBlockedFileList) that;
		files.addAll( other.files );
	}

	@Override
	public int size() {
		return files.size();
	}

	@Override
	public BlockedFile get(int index) {
		return files.get(index);
	}

	/**
	 * @return the files
	 */
	public ArrayList<AllocatedBlockedFile> getFiles() {
		return files;
	}

}

