package assignment2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;
import javax.vecmath.Point3f;
import meshes.PointCloud;


/**
 * This is an implementation of a hashtable based Octree.
 * The Morton Codes used are described in detail in the slides accompanying
 * this exercise, the bitwise operations on them will be implemented in the class
 * {@link MortonCodes}.
 * <p>
 * To navigate through the adjacencies of the octree, the methods getNbr_*2* can be
 * used (once implemented).
 * Their arguments Oxyz encode relative difference vectors, which usually
 * will be 3-bit integers. For example the argument 0b011 in the method
 * getNbr_c2c(cell, 0b011) would encode that the cell at the relative grid position
 * +0x, +1y , +1z is sought (relative to the cell passed as parameter).
 * 
 * @author Alf
 *
 */
public class HashOctree {
	
	
	/** The root of the tree*/
	public HashOctreeCell root;
	
	/** Abort criterion for the hashoctree construction: stop refining at depth maxDepth 
	 * MaxDepth should never be  more than 20, as longs have 64 bit, and every level needs 3 bits 
	 * and 4 additional bits are needed to discover overflows.*/
	private int maxDepth;
	/** Abort criterion for the hashoctree refinement: stop refining if a cell has less than
	 * pointsPerCell points. */
	private int pointsPerCell;
	
	/** Number of points stored in the octree */
	private int nrPoints;
	/** depth of this octree */
	private int depth;
	
	/** The bounding box of this tree. */
	private Point3f bboxMax, bboxMin;
	
	/** The hashmap to store the octree cells */
	private HashMap<Long, HashOctreeCell> cellMap;
	/** The hashmap to store the octree vertices */
	private HashMap<Long, HashOctreeVertex> vertexMap;
	/** For convenience, all leafs of the octree (cells with no sub-cells) are stored here. This
	 * allows to enumerate them and access them by an index (useful for matrix construcion)*/
	private ArrayList<HashOctreeCell> leafs;
	/** For convenience, all vertices of the octree are stored here. This
	 * allows to enumerate them and access them by an index (useful for matrix construcion)*/
	private ArrayList<HashOctreeVertex> vertices;

	private HashTreeData hashtreeData;
	
	/**
	 * 
	 * <p>Construct a hashbased octree for the PointCloud pc. depth denotes the maximal depth the octree can have,
	 * and pointsPerCell controls at what number of points a cell is not split any further.
	 * </p>
	 * <p>
	 * The factor parameter controls how many times as big as the boundingbox of the pointcloud
	 * the hashtree will be; if factor is < 1 the argument is ignored and 1 is used instead.
	 * </p>
	 * @param pc
	 * @param depth
	 * @param pointsPerCell
	 * @param factor
	 */
	public HashOctree(PointCloud pc, int depth, int pointsPerCell, float factor) {
		
		
		this.hashtreeData = new HashTreeData();
		this.maxDepth= depth;
		this.pointsPerCell = pointsPerCell;
		
		this.cellMap = new HashMap<>();
		this.leafs = new ArrayList<>();
				
		this.nrPoints = 0;
		this.depth = 0;
		
		this.root = HashOctreeCell.Root(pc.points, Math.max(factor,1));
		
		bboxMax = new Point3f();
		bboxMin = new Point3f();
		root.computeVertexPos(0b111, bboxMax);
		root.computeVertexPos(0b000, bboxMin);

		//build the octree
		//this creates all octree cells.
		Stack<HashOctreeCell> stack = new Stack<>();
		stack.push(root);
		buildTree(stack);
		
		//this  creates a hashmap of all octree vertices. This method makes sure that every 
		//vertex is registered  exactly once, even if it is used on multiple levels by multiple cells
		buildVertexMap();
		
		//lastly: enumerate the vertices and leafs.
		// the vertices and leafs are stored a second time in two separate arrays.
		//Leafs and Vertices have an 'index' field, this method assign to every vertex and every leaf its unique integer index.
		//This index will be used during the Reconstruction assignment to build and debug linear systems.
		enumerateVertices();
		enumerateLeafs();
		
		assert(nrPoints == pc.points.size());
		
		computeCellAdjacencies();
		computeVertexAdjacencies();
	}


	/**
	 * First step int the hashoctree creation. This initializes the 
	 * cells, the hashmap containing the cells and the list containing all Leafs
	 * The method operates in a recursive fashion on the stack.
	 * @param stack
	 */
	private void buildTree(Stack<HashOctreeCell> stack) {
		HashOctreeCell node = null;
		
		//split cells until there is no cell left to split:
		while(!stack.isEmpty()){
			node = stack.pop();
			
			if(!this.cellMap.containsKey(node.code)){
				this.cellMap.put(node.code, node);
			}
			else{
				assert(false); //sanity check no two cells should have the same morton code!!!!
			}
			
			
			if(node.points.size() > pointsPerCell && node.lvl <maxDepth){
				//creates 8 children,
				//the points stored in node are split between the 8 children
				//and  morton codes are assigned to the children
				splitNode(node, stack);
				
			}else{
				leafs.add(node);
				nrPoints += node.points.size();
				depth = (node.lvl > depth? node.lvl: depth);
			}
		}
	}

	/**
	 * Second step in the hashoctree creation: Initialize the vertex hashmap and
	 * computes all vertex codes.
	 */
	private void buildVertexMap() {
		this.vertexMap = new HashMap<Long, HashOctreeVertex>();
		
		long vertKey;
		//every vertex is the corner of some leaf. This method makes sure that every vertex is registered once only.
		HashOctreeVertex v;
 
		for(HashOctreeCell c : leafs){
			for(int i = 0b000; i <= 0b111; i++){
				//compute the hash corresponding to the vertex 0bxyz: this is the nbr code + padding
				vertKey = MortonCodes.nbrCode(c.code, c.lvl, i) << (3*(depth - c.lvl)); //getVertexHash(c, i);
				if(vertKey >= 0){
					//retrieve the vertex associated to the hash if existing
					if(vertexMap.containsKey(vertKey)){
						v= vertexMap.get(vertKey);
						
						//update
						v.maxLvl = (v.maxLvl < c.lvl ? c.lvl : v.maxLvl);
						v.minLvl = (v.minLvl > c.lvl ? c.lvl : v.minLvl);
						
						assert(v.code == vertKey);
					}
					//or create a new vertex for the given hash
					else{
						v = new HashOctreeVertex();
						v.maxLvl = c.lvl;
						v.minLvl = c.lvl;
						v.code = vertKey;
						c.computeVertexPos(i, v.position);
						vertexMap.put(v.code,v);
					}
				}
				else{
					//sanity check.
					assert(false);
				}
			}

		}
	}


	/**
	 * This helper method creates the child octree cells, computes their morton codes and
	 * splits the points contained in node between the children,   
	 * @param node
	 * @param stack
	 */
	private void splitNode(HashOctreeCell node, Stack<HashOctreeCell> stack) {
		HashOctreeCell[] children = new HashOctreeCell[8];
		for(int i = 0b0; i <= 0b111; i++){
			//Constructor takes care of the following code generation
			//children[i].code = node.code << 3;
			//children[i].code = children[i].code | i;
			children[i] = new HashOctreeCell(node, i);
		}
		
		int pointCode;
		//reassign all points
		for(Point3f p : node.points){
			//to which child should this point be assigned?
			pointCode = 0;
			if(p.x > node.center.x){
				pointCode = pointCode | 0b100;
			}
			if(p.y > node.center.y){
				pointCode = pointCode | 0b010;
			}
			if(p.z > node.center.z){
				pointCode = pointCode | 0b001;
			}
			
			//...to the one computed.
			children[pointCode].points.add(p);
		}
		node.points = null;
		for(HashOctreeCell n: children){
			stack.push(n);
		}
	}

	/** Enumerate all vertices */
	private void enumerateVertices(){
		int i = 0;
		this.vertices = new ArrayList<HashOctreeVertex>(this.vertexMap.values().size());
		for( HashOctreeVertex v: vertexMap.values()){
			this.vertices.add(v);
			v.index = i;
			i++;
		}
		
	}
	
	/** Enumerate all Leafs */
	private void enumerateLeafs() {
		int idx = 0;
		for(HashOctreeCell n : leafs){
			n.leafIndex = idx++;
		}
	}


	public ArrayList<HashOctreeCell> getLeafs() {
		return leafs;
	}

	public ArrayList<HashOctreeVertex> getVertices() {
		return this.vertices;
	}

	public Collection<HashOctreeCell> getCells() {
		return cellMap.values();
	}
	

	public HashOctreeVertex getVertexbyIndex(int index){
		return vertices.get(index);
	}
	
	
	public int getDepth() {
		return this.depth;
	}
	

	public int numberofVertices() {
		return vertexMap.size();
	}

	public int numberOfPoints() {
		return nrPoints;
	}
	
	public int numberOfCells() {
		return this.cellMap.size();
	}

	public int numberOfLeafs() {
		return leafs.size();
	}
	

	/**
	 * Retrieve the cell which corresponds to the morton code 
	 * in the argument. This will return null if no such cell exists.
	 * @param hash
	 * @return
	 */
	public HashOctreeCell getCell(long hash) {
		return this.cellMap.get(hash);
	}

	/**
	 * Retrieve the vertex which corresponds to the morton code 
	 * in the argument. This will return null if no such cell exists.
	 * @param hash
	 * @return
	 */
	public HashOctreeVertex getVertex(long hash) {
		return this.vertexMap.get(hash);
	}
	
	
	
	/**
	 * Retrieve a vertex neighboring this cell i.e. a corner vertex of the cell
	 * 
	 *    011------111
	 *   /         /|
	 *  /         / |
	 * 010------101 |
	 * |         |  |
	 * |  001----|-101
	 * | /       | /
	 * |/        |/
	 * 000------100
	 */
	public HashOctreeVertex getNbr_c2v(HashOctreeCell cell, int vertex_Obxyz) {
		
		//get the neighbor code
		long code = MortonCodes.nbrCode(cell.code, cell.lvl, vertex_Obxyz);
		
		//and padd it to get the vertex code.
		code = code << 3*(depth - cell.lvl);
		return getVertex(code);
	}
	
	/**
	 * Find and return a leaf cell that is adjacent to v.
	 * 
	 * 
	 * @param v
	 * @param nbr_Obxyz
	 * @return
	 */
	public HashOctreeCell getNbr_v2c(HashOctreeVertex v, int nbr_Obxyz) {
		long code = v.code;
		
		//first compute the code from the top left cell ($$), 
		// which is simply the unpadded morton code.
		// -------
		// |  |$$|
		// ---v---
		// |  |  |
		// -------
		code = code >> 3* (depth - v.maxLvl);
	
		// as $$ corresponds to the parameter nbr_0bxyz = 0b111
		// the seeked cell can be computed by nbrCodeMinus.
		//0b111 & (~ nbr_Obxyz) computes the appropriate 'difference vector'
		code = MortonCodes.nbrCodeMinus(code, v.maxLvl, 0b111 & (~ nbr_Obxyz));
		
		//finally, iterate through the multigrid layers 
		//to find the smallest existing cell. 
		while(getCell(code) == null && code > 0){
			code = code >>3;
		}
		
		return getCell(code);
		
	}
	
	
	/**
	 * Return the parent cell.
	 * if we know the code of the parent, we easily can find its cell
	 * by invoking the getCell method of this class.
	 * since we have a cell provided as input and each cell has a code
	 * we can find its parent code by using the MortonCodes method 
	 * 'parentCode' which returns a code's parent code. Hence,
	 * enter the cell's code as paramter for the method parentCode.
	 * @param cell target cell from which we want to find its parent.
	 * @return returns parent cell of input cell
	 */
	public HashOctreeCell getParent(HashOctreeCell cell){
		return this.getCell(MortonCodes.parentCode(cell.code));
	}
	
	/**
	 * Return an adjacent cell on the same or on a lower level, which lies in the
	 * direction Obxyz. If there is no such cell, null is returned
	 * 
	 * @param cell target cell we are working with
	 * @param relative grid position on the paramter's cell level-
	 * @return returns the neighbor cell at the relative grid position denoted by Obxxyz
	 */
	public HashOctreeCell getNbr_c2c(HashOctreeCell cell, int Obxyz){
		long neighborCode = MortonCodes.nbrCode(cell.code, cell.lvl, Obxyz);
		return findCellInNeighborhood(neighborCode);		
	}
	
	/**
	 * 
	 * @param neighborCode
	 * @return
	 */
	private HashOctreeCell findCellInNeighborhood(long neighborCode){
		HashOctreeCell candidateNeighborCell = null;
		long candidateNeighborCode = neighborCode;
		// iterate over neighborhood
		while (hasNotFoundNeighborCell(candidateNeighborCell, candidateNeighborCode)){ 
			candidateNeighborCell = this.getCell(candidateNeighborCode);
			candidateNeighborCode = MortonCodes.parentCode(candidateNeighborCode);
		}	
		return candidateNeighborCell;
	}
	
	/**
	 * 
	 * @param neighborCell
	 * @param neighborCode
	 * @return
	 */
	private boolean hasNotFoundNeighborCell(HashOctreeCell neighborCell, long neighborCode){
		boolean neighborNotFound = neighborCell == null;
		boolean rootNotReached = neighborCode > 0b1000;
		boolean isNotOverflow = neighborCode != -1L;
		boolean statment = neighborNotFound && rootNotReached && isNotOverflow;
		return statment;
	}
	
	/**
	 * Return the cell on the same or on a lower level, which lies in the
	 * direction Obxyz. If there is no such cell, null is returned
	 * @param cell
	 * @param Obxyz
	 * @return
	 */
	public HashOctreeCell getNbr_c2cMinus(HashOctreeCell cell, int Obxyz){
		long neighborCode = MortonCodes.nbrCodeMinus(cell.code, cell.lvl, Obxyz);
		return findCellInNeighborhood(neighborCode);
	}
	
	
	
	/** find and return a vertex on the finest grid possible, that shares an edge of some octreecell
	 * with the vertex v and lies in direction nbr_0bxyz (0b100 = +x direction, 0b010 = +y direction
	 * 0b001 = +z direction). If no neighbor exists, null is returned.
	 * @param v
	 * @param nbr_0bxyz
	 * @return
	 */
	public HashOctreeVertex getNbr_v2v(HashOctreeVertex v, int nbr_0bxyz){	
		long neighborCode = 0;
		
		HashOctreeVertex neighborVertex = null;
		for(int lvl = v.maxLvl; v.minLvl <= lvl; lvl--){
			// have we found a neighbor or was there an overflow?
			if(neighborVertex != null || neighborCode == -1L) break;
			
			// current cell level
			int depth = 3*(this.depth - lvl);
			
			// visit neighbors around target at level (depth-lvl).
			long targetAnchestorCode = v.code >> depth;
			neighborCode = MortonCodes.nbrCode(targetAnchestorCode, lvl, nbr_0bxyz);
			neighborVertex = this.getVertex(neighborCode << depth);
		}
		
		return neighborVertex;
	}
	
	
	
	
	/** find and return maximal depth vertex, that shares an edge of some octreecell
	 * with vertex and lies in direction nbr_0bxyz (0b100 = +x direction, 0b010 = +y direction
	 * 0b001 = +z direction). If no neighbor exists, null is returned.
	 * @param vertex
	 * @param nbr_0bxyz
	 * @return
	 */
	public HashOctreeVertex getNbr_v2vMinus(HashOctreeVertex v, int nbr_0bxyz){
		long neighborCode = 0;
		
		HashOctreeVertex neighborVertex = null;
		for(int lvl = v.maxLvl; v.minLvl <= lvl; lvl--){
			// have we found a neighbor or was there an overflow?
			if(neighborVertex != null || neighborCode == -1L) break;
			
			// current cell level
			int depth = 3*(this.depth - lvl);
			
			// visit neighbors around target at level (depth-lvl).
			long targetAnchestorCode = v.code >> depth;
			neighborCode = MortonCodes.nbrCodeMinus(targetAnchestorCode, lvl, nbr_0bxyz);
			neighborVertex = this.getVertex(neighborCode << depth);
		}
		
		return neighborVertex;
	}
	
	/**
	 * This method checks if the vertex code lies on the bounday of the
	 * hashoctree
	 */
	public boolean isOnBoundary(HashOctreeVertex v) {
		return MortonCodes.isVertexOnBoundary(v.code, this.depth);
	}

	/**
	 * Find and return the Leaf cell in which p lies. This method
	 * will return null if the point is outside of the volume
	 * covered by this octree.
	 * @param p
	 * @return
	 */
	public HashOctreeCell getCell(Point3f p){
		
		if(p.x >bboxMax.x + 10e-5 || 
				p.y > bboxMax.y+ 10e-5 || 
				p.z > bboxMax.z + 10e-5 ||
				p.x < bboxMin.x - 10e-5 ||
				p.y < bboxMin.y - 10e-5 ||
				p.z < bboxMin.z - 10e-5){
			return null;
		}
		long code = root.code;
		
		HashOctreeCell child, cell;
		cell = child = root;
		
		do{
			cell = child;
			
			//compute the code of the correct child
			code = code << 3;
			if(p.x > cell.center.x){
				code = code | 0b100;
			}
			if(p.y > cell.center.y){
				code = code | 0b010;
			}
			if(p.z > cell.center.z){
				code = code | 0b001;
			}
			
			child = getCell(code);
			
		}while (child!= null);//iterate while there is a child.
		
		return cell;
		
	}
	
	public float[] getVerticesPostions(){
//		List<Point3f> vertexList = new LinkedList<Point3f>();
//		for(HashOctreeVertex v : this.getVertices()){
//			this.lazyInsertVertex(vertexList, v);
//		}
//		return getArray3fFromList3f(vertexList);
		getNeighborPositions();
		return getArray3fFromList3f(vertexList);
	}
	
	
	List<Point3f> NeighborList;
	List<Point3f> vertexList;
	
	public float[] getNeighborPositions(){
		NeighborList = new LinkedList<Point3f>();
		vertexList = new LinkedList<Point3f>();
		this.counter = 0;
		for(HashOctreeVertex v : this.getVertices()){
			// over all neighbors
			for(int mask = 0b100; mask > 0; mask >>= 1){
				this.lazyInsertVertex(NeighborList, vertexList, getNbr_v2v(v, mask), v);
				this.lazyInsertVertex(NeighborList, vertexList, getNbr_v2vMinus(v, mask), v);
			}
		}
		return getArray3fFromList3f(NeighborList);
		
	}
	
	public int counter; 
	
	private void lazyInsertVertex(List<Point3f> targetDS,List<Point3f> vertexList, HashOctreeVertex vertex, HashOctreeVertex baseVertex){
		Point3f basePos = baseVertex.position;
		Point3f pos = null;
		if(vertex != null) pos = vertex.position;
		else pos = new Point3f(basePos.x, basePos.y, basePos.z);
		targetDS.add(pos);
		vertexList.add(basePos);
	}
	
	private float[] getArray3fFromList3f(List<Point3f> points){
		float[] tmp = new float[points.size()*3];
		int index = 0;
		for(Point3f point : points){
			tmp[3*index] = point.x;
			tmp[3*index+1] = point.y;
			tmp[3*index+2] = point.z;
			index++;
		}		
		return tmp;
	}
	
	/**
	 * Method to refine the tree in proximity of the point cloud. The maximal
	 * tree depth is not changed. 
	 * 
	 * <p>
	 * This method can be used to get rid of artefacts arising due to sudden large level changes
	 * close to the pointcloud, which arise in the SSD variant we implement in the 3rd assignment.
	 * <b> One or two refinement steps should suffice.</b>
	 * </p>
	 * @param steps
	 */
	public int[] getIndices(){
		// each cell has 6 faces, 3 values, 
		int upperBound = 6*numberofVertices()*3;
		int[] tmp = new int[upperBound];
		for(int k = 0; k < upperBound; k++) tmp[k] = k;
		return tmp;
	}
	
	public void refineTree(int steps){
		for(int k = 0; k < steps; k++){
			refine();
			buildVertexMap();
			enumerateVertices();
			enumerateLeafs();
		}
	}
	
	/**
	 * This method iterates over all leaf cells. For every leaf that contains
	 * at least a point, the neighbors of the cell are visited; if they are on a coarser level,
	 * they are split once. After running this method the vertex map and the vertex enumerations 
	 * are outdated and need to be recomupted.
	 */
	private void refine(){
		Stack<HashOctreeCell> stack = new Stack<>();
		
		for(HashOctreeCell c : this.leafs){
						
			if(! c.isLeaf() || c.points.size() == 0){
				continue;
			}
			for(int i = 0b100; i != 0; i= i>>1){
				HashOctreeCell temp = getNbr_c2c(c, i);
				if(temp!= null && temp.isLeaf() && temp.lvl < c.lvl){
					splitNode(temp, stack);

					
					while(!stack.isEmpty()){
						this.cellMap.put(stack.peek().code, stack.pop());
					}
				}
			}
		}
		
		this.leafs.clear();
		for(HashOctreeCell c : this.getCells()){
			if(c.isLeaf()){
				this.leafs.add(c);
			}
		}
	}
	
	/**
	 * helper data structure containing 
	 * all hash tree meta information
	 * @author simplay
	 *
	 */
	public class HashTreeData{
		private float[] adjVertNeighborVertices;
		private float[] adjVertVertices;
		private int[] adjVertInd;
		
		private float[] adjCellNeighborVertices;
		private float[] adjCellVertices;
		private int[] adjCellInd;
		
	}
	
	public float[] getAdjVertices(){
		return this.hashtreeData.adjVertNeighborVertices;
	}
	
	public float[] getAdjVertPositions(){
		return this.hashtreeData.adjVertVertices;
	}
	
	public int[] getAdjVertInd(){
		return this.hashtreeData.adjVertInd;
	}
	
	public float[] getAdjCellNeighborVertices(){
		return this.hashtreeData.adjCellNeighborVertices;
	}
	
	public float[] getAdjCellVertices(){
		return this.hashtreeData.adjCellVertices;
	}
	
	public int[] getAdjCellInd(){
		return this.hashtreeData.adjCellInd;
	}
	
	/**
	 * compute adjacency cell centers for this tree.
	 */
	private void computeCellAdjacencies(){
		float[] verts = new float[6*this.numberOfLeafs()*3];
		float[] parentCellCenters = new float[6*this.numberOfLeafs()*3];
		
		for(HashOctreeCell n : this.getLeafs()){
			Iterator<HashOctreeCell> iter = this.getAdjCellIterator(n);

			while(iter.hasNext()) {
				this.lazyAdd(n, verts, parentCellCenters, iter.next());
			}
		}
		
		int[] ind = new int[6*this.numberOfLeafs()];
		for(int i = 0; i < ind.length; i++)	{
			ind[i]=i;
		}
		
		this.hashtreeData.adjCellNeighborVertices = parentCellCenters;
		this.hashtreeData.adjCellVertices = verts;
		this.hashtreeData.adjCellInd = ind;
		adjVertInt = 0;
	}
	
	/**
	 * Associate parent with a neighbor if its neighbor exists.
	 * note that i-th parent : parents has its neighbor at i-th index : neighbors.
	 * @param parent among this vertex we are asking for its neighborhood
	 * @param parents list of parents with a neighbor
	 * @param neighbors list of neighbors having a parent
	 * @param neighbor vertex cell of parent
	 */
	private void lazyAdd(HashOctreeVertex parent , float[] parents, float[] neighbors, HashOctreeVertex neighbor){
		if (neighbor != null) {
			insertPoint(parent.position, parents, neighbors, neighbor.position);
		}
		
	}
	
	/**
	 * Associate parent with a neighbor if its neighbor exists.
	 * note that i-th parent : parents has its neighbor at i-th index : neighbors.
	 * @param parent among this cell we are asking for its neighborhood
	 * @param parents list of parents with a neighbor
	 * @param neighbors list of neighbors having a parent
	 * @param neighbor cell cell of parent
	 */
	private void lazyAdd(HashOctreeCell parent , float[] parents, float[] neighbors, HashOctreeCell neighbor){
		if (neighbor != null){
			insertPoint(parent.center, parents, neighbors, neighbor.center);
		}
	}
	
	/**
	 * Insert point3f into its collection.
	 * @param parent parent point
	 * @param parents parent point list
	 * @param neighbors neighbor point list
	 * @param neighbor neighbor point
	 */
	private void insertPoint(Point3f parent , float[] parents, float[] neighbors, Point3f neighbor){
		parents[3*adjVertInt] = parent.x;
		parents[3*adjVertInt + 1] = parent.y;
		parents[3*adjVertInt + 2] = parent.z;
		neighbors[3*adjVertInt] = neighbor.x;
		neighbors[3*adjVertInt + 1] = neighbor.y;
		neighbors[3*adjVertInt + 2] = neighbor.z;
		adjVertInt++;
	}
	
	/**
	 * global index
	 */
	private int adjVertInt = 0; 

	/**
	 * helper method which computes this tree's vertices' adjacent vertices.
	 */
	private void computeVertexAdjacencies(){
		float[] verts = new float[6*this.numberofVertices()*3];
		float[] adjVerts = new float[6*this.numberofVertices()*3];
		int[] ind = new int[6*this.numberofVertices()];

		// for each vertex in this hashtree
		for(HashOctreeVertex v : this.getVertices()) {
			// for each direction
			for (int mask = 0b100; mask > 0; mask >>= 1) {
				// lazy add left and right neighbor
				lazyAdd(v , verts, adjVerts, this.getNbr_v2v(v, mask));
				lazyAdd(v , verts, adjVerts, this.getNbr_v2vMinus(v, mask));
			}
		}
		
		// find corresponding indices
		for(int i = 0; i < ind.length; i++)	{
			ind[i]=i;
		}
		
		// write back
		this.hashtreeData.adjVertVertices = verts;
		this.hashtreeData.adjVertNeighborVertices = adjVerts;
		this.hashtreeData.adjVertInd = ind;
		adjVertInt = 0;
	}
	
	/**
	 * Get number of vertices of this hashoctree.
	 * @return number of vertices.
	 */
    public int numberOfVertices() {
        return vertexMap.size();
    }
	
	/**
	 * 
	 * @param cell
	 * @return
	 */
	public Iterator<HashOctreeCell> getAdjCellIterator(HashOctreeCell cell) {
		return new AdjacentCellIterator(cell);
	}
	
	/**
	 * Neighborhood cell iterator for a given cell.
	 */
	public class AdjacentCellIterator implements Iterator<HashOctreeCell> {
		
		private HashOctreeCell[] neighbors;
		private int globalIndex = 0;
		
		public AdjacentCellIterator(HashOctreeCell cell) {
			this.globalIndex = 0;
			this.neighbors = new HashOctreeCell[8];
			int k = 0;
			
			// get each potential neighbor
			for (int mask = 0b100; mask > 0; mask >>= 1) {
				neighbors[2*k] = getNbr_c2c(cell, mask);
				neighbors[2*k+1] = getNbr_c2cMinus(cell, mask);
				k++;
			}
			
		}
		
		public boolean hasNext() {
			HashOctreeCell candidate = null;
			for(int k = globalIndex; k < 8; k++){
				candidate = this.neighbors[k];
				if(candidate != null){
					return true;
				}
			}
			return false;
		}
		
		@Override
		public HashOctreeCell next() {
			if (!hasNext())
				throw new NoSuchElementException();
			
			HashOctreeCell candidate = null;
			for(int k = globalIndex; k < 8; k++){
				candidate = this.neighbors[k];
				if(candidate != null){
					globalIndex = k;
					globalIndex++;
					break;
				}
			}
	
			return candidate;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}

