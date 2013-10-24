package assignment3;

import java.util.ArrayList;
import java.util.HashMap;
import javax.vecmath.Point3f;
import meshes.Point2i;
import meshes.WireframeMesh;
import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import assignment2.HashOctreeVertex;


/**
 * Implwmwnr your Marching cubes algorithms here.
 * @author bertholet
 *
 */
public class MarchingCubes {
	
	//the reconstructed surface
	public WireframeMesh result;
	

	//the tree to march
	private HashOctree tree;
	//per marchable cube values
	private ArrayList<Float> val;
	private HashMap<Point2i, Integer> vertexList;
	
	
	/**
	 * Implementation of the marching cube algorithm. pass the tree
	 * and either the primary values associated to the trees edges
	 * @param tree
	 * @param byLeaf
	 */
	public MarchingCubes(HashOctree tree){
		this.tree = tree;
		
		
	}
	
	/**
	 * Perform primary Marching cubes on the tree.
	 * Iterate over all tree cells and decide for 
	 * every cell what triangle to create.
	 */
	public void primaryMC(ArrayList<Float> byVertex) {
		this.val = byVertex;
		this.vertexList = new HashMap<Point2i, Integer>();
		this.result = new WireframeMesh();
		
		for(HashOctreeCell cell : tree.getLeafs()){
			pushCube(cell);
		}
		
	}
	
	/**
	 * Perform dual marchingCubes on the tree
	 * dual cube form a vertex, spanned by adjacent cell centers.
	 * thread dual marching cubes as they had 8 corners, i.e. 
	 * counting corners multiple times is okay
	 * 
	 * Note: Marching cubes will generate at most one vertex per cube
	 * use a hashtable in order to 
	 * keep track of already created vertices.
	 */
	public void dualMC(ArrayList<Float> byVertex) {
		this.vertexList = new HashMap<Point2i, Integer>();
		this.result = new WireframeMesh();
		
		// initialize arrayList - is there another neat way to initializ
		// such an arraylist?
	    ArrayList<Float> dualVerticesValues = new ArrayList<Float>();  
	    for(int k = 0; k < tree.getCells().size(); k++){  
	    	dualVerticesValues.add(-1.0f);  
	    }  
		
	    // dual vertex <=> cell
		for(HashOctreeCell cell : tree.getLeafs()){
			float sum = 0f;
			// iterate over each coner of current cell and compute average.
			for(int k = 0; k < 8; k++){
				MarchableCube corner = cell.getCornerElement(k, tree);
				// get value of vertex at corner cronerIndex from consodered marching cube
				sum += byVertex.get(corner.getIndex());
			}
			sum = sum / 8f;
			dualVerticesValues.set(cell.getIndex(), sum);
		}
		
		for(HashOctreeVertex vertex : tree.getVertices()){
			if(tree.isOnBoundary(vertex)) continue;
			else pushCube(vertex, dualVerticesValues);
		}
		
	}

	/**
	 * Overloaded version of pushCube without requiring any value list.
	 * @param n
	 */
	private void pushCube(MarchableCube n){
		pushCube(n, this.val);
	}
	
	/**
	 * March a single cube: compute the triangles and add them to the wireframe model
	 * Given a cube with associtated values
	 * we have to resolve corners of given cube
	 * 
	 * before creating a vertex, i.e. storing it into the vertexList,
	 * check the hashtable if this vertex is already contained in the 
	 * wireframe-mesh, i.e. is contained in the hashtable. 
	 * Recycle it if already in hashtable.
	 * 
	 * Note: Degenerated triangles can occur using dual marching - since 
	 * we thread dual marching cubes as they had 8 corners even when they don't.
	 * 
	 * Hence, add a check if forming face (candidate) is degenerated and 
	 * add it only if it is not.
	 * 
	 * @param n marching cube - current cell representing a marching vertex 
	 * @param values - since each cell is representing a marching verte
	 * this list of values is representing the actual vertex value, i.e.
	 * the cell value.
	 */
	private void pushCube(MarchableCube n, ArrayList<Float> values){
		float[] cornerValues = new float[8];
		// triangulation cases: reduced due to symmetries to 15 cases
		// for cell with 8 corners with signed values, there are 2^8 cases
		// for the ordered sign configuration.
		// represents a cube edge
		Point2i[] signChangeCasesCubeEdges = new Point2i[15];
		
		
		for(int k = 0; k < 15; k++){
			signChangeCasesCubeEdges[k] = new Point2i(0, 0);
		}
		
		// iterate over each corner of the Marchable cube
		// get value of corner at index fur each current index.
		for(int k = 0; k < 8; k++){
			MarchableCube corner = n.getCornerElement(k, tree);
			cornerValues[k] = values.get(corner.getIndex());
		}
		
		// perfrom look-up - returns list of edges
		MCTable.resolve(cornerValues, signChangeCasesCubeEdges);
		
		for(Point2i edge : signChangeCasesCubeEdges){
			if(edge.x == -1) break;
			
			else if(vertexList.containsKey(getHashKey(n, edge))){
				result.addIndex(vertexList.get(getHashKey(n, edge)));
				continue;
			}else{
				Point3f interpolatedPos = computeInterpolatedPostition(n, edge, values);
				result.vertices.add(interpolatedPos);
				int index = result.vertices.size() - 1;
				
				result.addIndex(index);
				vertexList.put(getHashKey(n, edge), index);
			}
		}
	}
	
	/**
	 * 
	 * @param cube
	 * @param point
	 * @return
	 */
	private Point3f computeInterpolatedPostition(MarchableCube cube, Point2i point, ArrayList<Float> values){
		MarchableCube cube_a = cube.getCornerElement(point.x, tree);
		MarchableCube cube_b = cube.getCornerElement(point.y, tree);
		
		float a = values.get(cube_a.getIndex());
		float b = values.get(cube_b.getIndex());
		
		Point3f pos_a = new Point3f(cube_a.getPosition());
		Point3f pos_b = new Point3f(cube_b.getPosition());
		
		float frac_a_ab = a / (a-b);
		pos_a.scale(1.0f - frac_a_ab);
		pos_b.scale(frac_a_ab);
		
		Point3f pos = new Point3f();
		pos.add(pos_a, pos_b);
		return pos;	
	}

	
	/**
	 * Get a nicely marched wireframe mesh...
	 * @return
	 */
	public WireframeMesh getResult() {
		return this.result;
	}


	/**
	 * compute a key from the edge description e, that can be used to
	 * uniquely identify the edge e of the cube n. See Assignment 3 Exerise 1-5
	 * @param n
	 * @param e
	 * @return
	 */
	private Point2i getHashKey(MarchableCube n, Point2i e) {
		Point2i p = new Point2i(n.getCornerElement(e.x, tree).getIndex(),
				n.getCornerElement(e.y, tree).getIndex());
		if(p.x > p.y) {
			int temp = p.x;
			p.x= p.y; p.y = temp;
		}
		return p;
	}

}
