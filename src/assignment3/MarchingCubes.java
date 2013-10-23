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
	private HashMap<Point2i, Integer> createdVertices;
	
	
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
	 */
	public void primaryMC(ArrayList<Float> byVertex) {
		this.val = byVertex;
		this.createdVertices = new HashMap<Point2i, Integer>();
		this.result = new WireframeMesh();
		
		for(HashOctreeCell cell : tree.getLeafs()){
			pushCube(cell);
		}
		
	}
	
	/**
	 * Perform dual marchingCubes on the tree
	 */
	public void dualMC(ArrayList<Float> byVertex) {
		this.createdVertices = new HashMap<Point2i, Integer>();
		this.result = new WireframeMesh();
		
	    ArrayList<Float> byCell = new ArrayList<Float>();  
	    for(int k = 0; k < tree.getCells().size(); k++){  
	    	byCell.add(-1.0f);  
	    }  
		
		for(HashOctreeCell cell : tree.getLeafs()){
			float sum = 0f;
			for(int k = 0; k < 8; k++){
				MarchableCube corner = cell.getCornerElement(k, tree);
				// get value of vertex at corner cronerIndex from consodered marching cube
				sum += byVertex.get(corner.getIndex());
			}
			byCell.set(cell.getIndex(), sum/8f);
		}
		
		for(HashOctreeVertex vertex : tree.getVertices()){
			if(tree.isOnBoundary(vertex)) continue;
			else pushCube(vertex, byCell);
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
	 * @param n
	 * @param values
	 */
	private void pushCube(MarchableCube n, ArrayList<Float> values){
		float[] cornerValues = new float[8];
		Point2i[] edgefromingPoints = new Point2i[15];
		
		
		for(int k = 0; k < 15; k++){
			edgefromingPoints[k] = new Point2i(0, 0);
		}
		
		// iterate over each coner of the Marchable cube
		// get value of corner at index fur each current index.
		for(int k = 0; k < 8; k++){
			MarchableCube corner = n.getCornerElement(k, tree);
			cornerValues[k] = values.get(corner.getIndex());
		}
		
		MCTable.resolve(cornerValues, edgefromingPoints);
		
		for(Point2i point : edgefromingPoints){
			if(point.x == -1) break;
			
			else if(createdVertices.containsKey(getHashKey(n, point))){
				result.addIndex(createdVertices.get(getHashKey(n, point)));
				continue;
			}else{
				Point3f interpolatedPos = computeInterpolatedPostition(n, point, values);
				result.vertices.add(interpolatedPos);
				int index = result.vertices.size() - 1;
				
				result.addIndex(index);
				createdVertices.put(getHashKey(n, point), index);
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
