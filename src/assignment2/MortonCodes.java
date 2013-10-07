package assignment2;


/**
 * Morton Codes
 * 
 * @author simplay
 *
 */
public class MortonCodes {
	
	/** the three masks for dilated integer operations */
	public static final long 
			d100100Mask = 0b100100100100100100100100100100100100100100100100100100100100100L, 
			d010010Mask = 0b010010010010010010010010010010010010010010010010010010010010010L, 
			d001001Mask = 0b001001001001001001001001001001001001001001001001001001001001001L,
			errorCode = -1L;
	
	/**
	 * return the parent morton code
	 * @param code
	 * @return
	 */
	public static long parentCode(long code){
		return (code >> 3);
	}
	
	/**
	 * return the (negative) neighbor code at the relative position 
	 * on the level 'level' encoded by 0bxyz using dilated subtraction
	 * checks for if the dilated subtraction produced an overflow.
	 * Approach: x+y = (((x |~mask) + (y&mask)) & mask) | (x & ~mask);
	 * let y = (((x |~mask) + (y&mask)) & mask) 
	 * computes the correct value for the bits denoted by the mask
	 * (x |~mask) sets masked bits to 1
	 * (y&mask) ignores masked bits
	 * | (x & ~mask) resets other bits to original value
	 * This step is not necessary here since we do not overwrite x.
	 * @param code input position.
	 * @param level order of neighborhood we want to search around.
	 * @param Obxyz encoded difference vector, i.e. 0b101 would denote that the 
	 * caller is seeking the neighbor which lies at the relative gris position +1x,+0y,+1z
	 * @return returns neighborhood code on desired level if there 
	 * was no overflow and otherwise -1 if it does
	 */	
	public static long nbrCode(long code, int level, int Obxyz){
		// masked sums - for (x,y,z) separately
		long xAddition = ((code |~d100100Mask)+(Obxyz&d100100Mask))&d100100Mask;
		long yAddition = ((code |~d010010Mask)+(Obxyz&d010010Mask))&d010010Mask;
		long zAddition = ((code |~d001001Mask)+(Obxyz&d001001Mask))&d001001Mask;
		
		// like bitgroupwise sum
		long result = xAddition | yAddition | zAddition;
		
		// did an overflow occur?
		result = (overflowTest(result, level)) ? errorCode : result;
		
		return result;
	}

	/**
	 * return the (negative) neighbor code at the relative position 
	 * on the level 'level' encoded by 0bxyz using dilated subtraction
	 * checks for if the dilated subtraction produced an underflow.
	 * Approach: x-y = (((x & mask) - (y&mask)) & mask) | (x & ~mask);
	 * let y = (((x & mask) - (y&mask)) & mask) 
	 * computes the correct value for the bits denoted by the mask
	 * (x & mask) sets masked bits to 0
	 * (y&mask) ignores masked bits
	 * | (x & ~mask) resets other bits to original value
	 * This step is not necessary here since we do not overwrite x.
	 * @param code input position.
	 * @param level order of neighborhood we want to search around.
	 * @param Obxyz encoded difference vector, i.e. 0b101 would denote that the 
	 * caller is seeking the neighbor which lies at the relative grid position -1x,-0y,-1z
	 * @return returns neighborhood code on desired level if there 
	 * was no underflow and otherwise -1 if it does
	 */	
	public static long nbrCodeMinus(long code, int level, int Obxyz){
		// masked sums - for (x,y,z) separately
		long xSubstraction = ((code&d100100Mask)-(Obxyz&d100100Mask))&d100100Mask;
		long ySubstraction = ((code&d010010Mask)-(Obxyz&d010010Mask))&d010010Mask;
		long zSubstraction = ((code&d001001Mask)-(Obxyz&d001001Mask))&d001001Mask;
		
		// like bitgroupwise sum
		long result = xSubstraction | ySubstraction | zSubstraction;
		
		// did an underflow occur?
		result = (overflowTest(result, level)) ? errorCode : result;
		
		return result;
	}
		
	/**
	 * A test to check if an overflow/underflow has occurred. it is enough to test
	 * if the delimiter bit is untouched and is the highest bit set.
	 * is code 0b1 xyz xyz ... xyz not a level 'level' Morton code?
	 * Therefore this checks if the highest bit, the delimiter bit, 
	 * has been 'touched' or not, which only may happen if there occurred an overflow.
	 * An underflow has occurred if and only iff after code >> 3*level 
	 * is not equal 0b1 and instead is longer, i.e. 0b1a...b
	 * @param code input code we want to check if it is a Morton code
	 * @param level depth
	 * @return is current input code a valid 
	 * Morton code, i.e. did an over-/underflow occur?
	 */
	public static boolean overflowTest(long code, int level){
		// is code 0b1 xyz xyz ... xyz not a level 'level' Morton code?
		return ((code >> (3*level)) != 0b1);
	}
	
	/**
	 * Check if the cell_code is a morton code associated to the grid level
	 * given in the argument. A cell code is associated to a specific level
	 * @param cell_code
	 * @param level
	 * @return
	 */
	public static boolean isCellOnLevelXGrid(long cell_code, int level){
		return (1L == (cell_code >> (3*level)));
	}
	
	/**
	 * A test to check if the vertex_code (a morton code padded with zeros to have the length
	 * 3*tree_depth + 1) is associated to a vertex which is part of the {@param level}-grid.
	 * 
	 * This is determined by the number of trailing zeros, and if a vertex lies on some level k
	 * it will lie on the levels k+1,k+2... tree_depth too. 
	 * @param vertex_code code under investigation.
	 * @param level which cell level are we considering
	 * @param tree_depth upper bound for padding level
	 * @return is current vertex code on given level with given depth
	 */
	public static boolean isVertexOnLevelXGrid(long vertex_code, int level, int tree_depth){
		// depth of tree, i.e. 
		// the padding level: k times padded tree_depth-k
		int depth = 3*(tree_depth-level);
		
		// define masking level
		long maskLastk1 = ~(-1L << depth);
		// masked vertex code: only consider last k bits
		// vertexCode dimension: (3*tree_depth+1,...,k+1,k,...,1)
		long maskedVertexCode = (vertex_code & maskLastk1);
		
		// is vertex on this level
		return (maskedVertexCode == maskLastk1);
	}
	
	/**
	 * A test to check if a vertex code is logically describing a boundary vertex.
	 */
	public static boolean isVertexOnBoundary(long vertex_code, int tree_depth){
		boolean is = (vertex_code & (0b111 << 3*(tree_depth-1)))!= 0 || //x==1, y==1 or z==1 in a unit cube
				(vertex_code & d100100Mask) == 0 || //x==0
				(vertex_code & d010010Mask) == 0 || //y==0
				(vertex_code & d001001Mask) == (0b1 << 3*tree_depth) ; //z==0 (only the delimiter bit is set)
		
		return is;
	}
	
}
