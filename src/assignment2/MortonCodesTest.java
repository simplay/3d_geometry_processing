package assignment2;

import static org.junit.Assert.*;
import org.junit.Test;


public class MortonCodesTest {
	
	long hash = 		0b1000101000100;
	
	//the hashes of its parent and neighbors
	// level 3 code
	long parent = 		0b1000101000; 
	// level 4 codes
	long nbr_plus_x = 	0b1000101100000; 
	long nbr_plus_y =   0b1000101000110;
	long nbr_plus_z =   0b1000101000101;
	
	long nbr_minus_x = 	0b1000101000000;
	//invalid: the vertex lies on the boundary and an underflow should occur
	long nbr_minus_y =  -1; 
	long nbr_minus_z =  0b1000100001101;
	
	//example of a vertex morton code in a multigrid of
	//depth 4. It lies on the level 3 and 4 grids
	long vertexHash = 	0b1000110100000;
	long level0 = 		0b1;
	int lvlUpperBound = 20; // due to 64 bit
	long overflowCode = MortonCodes.errorCode;
	
	
	@Test
	public void testParentCode() {
		assertFalse((parent == MortonCodes.parentCode(nbr_plus_x)));
		assertTrue((parent == MortonCodes.parentCode(nbr_plus_y)));
		assertTrue((parent == MortonCodes.parentCode(nbr_plus_z)));
		assertTrue((parent == MortonCodes.parentCode(nbr_minus_x)));
		assertFalse((parent == MortonCodes.parentCode(nbr_minus_y)));
		assertFalse((parent == MortonCodes.parentCode(nbr_minus_z)));
	}
	
	@Test
	public void testOverflowTest(){
		// base level
		long currentCode = level0;
		
		// for each level from 0 to upper bound check
		for(int lvl = 0; lvl <= lvlUpperBound; lvl++){
			// valid case: code is at level lvl
			assertFalse(MortonCodes.overflowTest(currentCode, lvl));
			
			// all overflow/underflow cases:
			for(int otherlvl = 0; otherlvl <= lvlUpperBound; otherlvl++){
				if(lvl != otherlvl){
					assertTrue(MortonCodes.overflowTest(currentCode, otherlvl));
				}
			}
			// get next level
			currentCode = level0 << 3*(lvl+1);
		}
	}
	
	@Test
	public void testNbrCode(){
		// regular cases:
		assertEquals(nbr_plus_x, MortonCodes.nbrCode(hash, 4, 0b100));
		assertEquals(nbr_plus_y, MortonCodes.nbrCode(hash, 4, 0b010));
		assertEquals(nbr_plus_z, MortonCodes.nbrCode(hash, 4, 0b001));
		
		// symmetric negative cases:
		assertEquals(hash, MortonCodes.nbrCode(nbr_minus_x, 4, 0b100));
		assertFalse((hash == MortonCodes.nbrCode(nbr_minus_y, 4, 0b010)));
		assertEquals(hash, MortonCodes.nbrCode(nbr_minus_z, 4, 0b001));
		
		// overflow cases
		assertEquals(overflowCode, MortonCodes.nbrCode(0b1100, 1, 0b100));
		assertEquals(overflowCode, MortonCodes.nbrCode(0b1010, 1, 0b010));
		assertEquals(overflowCode, MortonCodes.nbrCode(0b1001, 1, 0b001));
	}
	
	@Test
	public void testNbrCodeMinus(){
		// regular cases:
		assertEquals(nbr_minus_x, MortonCodes.nbrCodeMinus(hash, 4, 0b100));
		assertEquals(nbr_minus_y, MortonCodes.nbrCodeMinus(hash, 4, 0b010));
		assertEquals(nbr_minus_z, MortonCodes.nbrCodeMinus(hash, 4, 0b001));
		
		// symmetric negative cases:
		assertEquals(hash, MortonCodes.nbrCodeMinus(nbr_plus_x, 4, 0b100));
		assertEquals(hash, MortonCodes.nbrCodeMinus(nbr_plus_y, 4, 0b010));
		assertEquals(hash, MortonCodes.nbrCodeMinus(nbr_plus_z, 4, 0b001));
		
		assertEquals(overflowCode, MortonCodes.nbrCodeMinus(0b1000000, 2, 0b100));
		assertEquals(overflowCode, MortonCodes.nbrCodeMinus(0b1000000, 2, 0b010));
		assertEquals(overflowCode, MortonCodes.nbrCodeMinus(0b1000000, 2, 0b001));
	}
	
	@Test
	public void testIsCellOnLevelXGrid(){
		long currentCode = level0;
		for(int lvl = 0; lvl <= lvlUpperBound; lvl++){
			// valid case: is cell code at level lvl
			assertTrue(MortonCodes.isCellOnLevelXGrid(currentCode, lvl));
			
			// all other cell levels:
			for(int otherlvl = 0; otherlvl <= lvlUpperBound; otherlvl++){
				if(lvl != otherlvl){
					assertFalse(MortonCodes.isCellOnLevelXGrid(currentCode, otherlvl));
				}
			}
			currentCode = level0 << 3*(lvl+1);
		}
		
		// random other tests
		assertTrue(MortonCodes.isCellOnLevelXGrid(parent, 3));
		assertFalse(MortonCodes.isCellOnLevelXGrid(parent, 2));
		assertFalse(MortonCodes.isCellOnLevelXGrid(parent, 4));
		assertTrue(MortonCodes.isCellOnLevelXGrid(hash, 4));
		assertFalse(MortonCodes.isCellOnLevelXGrid(hash, 3));
		assertFalse(MortonCodes.isCellOnLevelXGrid(hash, 5));
	}
	
	@Test
	public void testIsVertexOnLevelXGrid(){
		assertTrue(MortonCodes.isVertexOnLevelXGrid(vertexHash, 4, 4));
		assertFalse(MortonCodes.isVertexOnLevelXGrid(vertexHash, 3, 4));
		assertFalse(MortonCodes.isVertexOnLevelXGrid(vertexHash, 2, 4));
		assertFalse(MortonCodes.isVertexOnLevelXGrid(vertexHash, 1, 4));
		assertFalse(MortonCodes.isVertexOnLevelXGrid(vertexHash, 0, 4));
	}

}
