/**
 * 
 */
package org.cryptonomicon.block.allocated;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cryptonomicon.block.Block;
import org.cryptonomicon.block.BlockList;
import org.cryptonomicon.block.BlockListIterator;
import org.cryptonomicon.block.BlockedFile;
import org.cryptonomicon.block.XorSet;
import org.cryptonomicon.block.allocated.AllocatedBlockedFile;
import org.cryptonomicon.block.allocated.AllocatedXorSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.ByteArray;

/**
 * @author lintondf
 *
 */
public class XorSetTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.allocated.AllocatedXorSet#XorSet(int, java.util.ArrayList)}.
	 */
	@Test
	public void testXorSet() {
		ArrayList<BlockedFile> files = new ArrayList<>();
		byte[] values = {(byte)0x1, (byte)0x2, (byte)0x4};
		byte[] array = new byte[Block.BLOCK_SIZE];
		ByteArray key = Jargon2.toByteArray( new byte[256/8] );
		byte all = 0;
		for (int i = 0; i < values.length; i++) {
			all = (byte) (all ^ values[i]);
			Arrays.fill(array, values[i]);
			AllocatedBlockedFile file = new AllocatedBlockedFile( array, key );
//			System.out.println( file.getBlockList().get(0).toString() );
			files.add( file );
			
		}
		XorSet set = new AllocatedXorSet( 1, files );
		assertTrue( set.getMaxBlocks() == 1 );
		BlockList allList = set.getXorOfAll();
		assertTrue( allList.size() == 1 );
		Arrays.fill( array, all );
		assertTrue( Arrays.equals(array, allList.get(0).getContents() ));
//		System.out.println( allList.get(0).toString() );
		ArrayList<BlockList> exceptLists = set.getXorExcept();
		assertTrue( exceptLists.size() == values.length );
		for (int i = 0; i < values.length; i++) {
			BlockList list = exceptLists.get(i);
			Arrays.fill(array, (byte) (all ^ values[i]) );
			assertTrue( Arrays.equals(array, list.get(0).getContents()) );
		}
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.allocated.AllocatedXorSet#getIterators()}.
	 */
	@Test
	public void testGetIterators() {
		List<BlockedFile> files = new ArrayList<>();
		byte[] values = {(byte)0x1, (byte)0x2, (byte)0x4};
		byte[] array = new byte[Block.BLOCK_SIZE];
		ByteArray key = Jargon2.toByteArray( new byte[256/8] );
		byte all = 0;
		for (int i = 0; i < values.length; i++) {
			all = (byte) (all ^ values[i]);
			Arrays.fill(array, values[i]);
			AllocatedBlockedFile file = new AllocatedBlockedFile( array, key );
//			System.out.println( file.getBlockList().get(0).toString() );
			files.add( file );
			
		}
		XorSet set = new AllocatedXorSet( 1, files );
		ArrayList<BlockListIterator> its = set.getIterators();
		for (int i = 0; i < its.size(); i++) {
			BlockListIterator it = its.get(i);
			Block block = it.next();
//			System.out.println( block.toString() );
			byte expected = (byte)  ((i < values.length) ? (all ^ values[i]) : all);
			Arrays.fill(array, expected );
			assertTrue( Arrays.equals(array, block.getContents()) );
		}
	}

}
