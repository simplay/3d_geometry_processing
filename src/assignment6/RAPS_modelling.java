package assignment6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import assignment4.LMatrices;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.solver.JMTSolver;
import sparse.solver.Solver;



/**
 * As rigid as possible deformations.
 * @author Alf
 *
 */
public class RAPS_modelling {

	//ArrayList containing all optimized rotations,
	//keyed by vertex.index
	ArrayList<Matrix3f> rotations;
	
	//A copy of the original half-edge structure. This is needed  to compute the correct
	//rotation matrices.
	private HalfEdgeStructure hs_originl;
	//The halfedge structure being deformed
	private HalfEdgeStructure hs_deformed;
	
	//The unnormalized cotan weight matrix, with zero rows for
	//boundary vertices.
	//It can be computed once at setup time and then be reused
	//to compute the matrix needed for position optimization
	CSRMatrix L_cotan;
	//The matrix used when solving for optimal positions
	CSRMatrix L_deform;
	
	//allocate righthand sides and x only once.	
	private ArrayList<Tuple3f> b;
	private ArrayList<Tuple3f> x;

	
	private CSRMatrix userConstraints;
	
	//sets of vertex indices that are constrained.
	private HashSet<Integer> keepFixed;
	private HashSet<Integer> deform;
	private HashMap<HalfEdge, Float> cotanWeights;

	private float weightUserConstraint = 100.0f;
	private Solver solver;	
	
	
	
	/**
	 * The mesh to be deformed
	 * @param hs
	 */
	public RAPS_modelling(HalfEdgeStructure hs){
		this.hs_originl = new HalfEdgeStructure(hs); //deep copy of the original mesh
		this.hs_deformed = hs;
		
		this.keepFixed = new HashSet<>();
		this.deform = new HashSet<>();
		this.cotanWeights = new HashMap<HalfEdge, Float>();
		
		init_b_x(hs);
		L_cotan = LMatrices.mixedCotanLaplacianOther(hs, false);
	}
	
	/**
	 * Set which vertices should be kept fixed. 
	 * @param verts_idx
	 */
	public void keep(Collection<Integer> verts_idx) {
		this.keepFixed.clear();
		this.keepFixed.addAll(verts_idx);

	}
	
	/**
	 * constrain these vertices to the new target position
	 */
	public void target(Collection<Integer> vert_idx){
		this.deform.clear();
		this.deform.addAll(vert_idx);
	}
	
	private CSRMatrix LTranspose;

	private ArrayList<Tuple3f> bNorm;
	
	/**
	 * update the linear system used to find optimal positions
	 * for the currently constrained vertices.
	 * Good place to do the cholesky decompositoin
	 */
	public void updateL() {
		
		int originalVertexCount = hs_originl.getVertices().size();
		userConstraints = new CSRMatrix(0, originalVertexCount);
		for(int k = 0; k < originalVertexCount; k++){
			
			userConstraints.addRow();
			ArrayList<col_val> row = userConstraints.lastRow();
			
			
			if(keepFixed.contains(k) ||deform.contains(k)){
				row.add(new col_val(k,weightUserConstraint*weightUserConstraint));
				Collections.sort(row);
			}else{
				row.add(new col_val(k,weightUserConstraint));
			}
			
		}
		
		for(HalfEdge edge : hs_originl.getHalfEdges()){
			cotanWeights.put(edge, edge.getCotanWeight());
		}
		
		L_deform = new CSRMatrix(0, originalVertexCount);
		CSRMatrix LTL = new CSRMatrix(0, originalVertexCount);
		LTranspose = L_cotan.transposed();
		LTranspose.multParallel(L_cotan, LTL);
		L_deform.add(LTL, userConstraints);
		
		if(deform.isEmpty()){
			solver = new JMTSolver();
		}else{
			solver = new Cholesky(L_deform);
		}
		
		rotations = new ArrayList<Matrix3f>();
		for(int k = 0; k < originalVertexCount; k++){
			Matrix3f identity = new Matrix3f();
			identity.setIdentity();
			rotations.add(identity);
		}
	}
	
	/**
	 * The RAPS modelling algorithm.
	 * @param t
	 * @param nRefinements
	 */
	public void deform(Matrix4f t, int nRefinements){
		this.transformTarget(t);
		for(int k = 0; k < nRefinements; k++){
			optimalPositions();
			optimalRotations();
			System.out.println("Iteration " + k);
		}
	}

	/**
	 * Method to transform the target positions and do nothing else.
	 * @param t
	 */
	public void transformTarget(Matrix4f t) {
		for(Vertex v : hs_deformed.getVertices()){
			if(deform.contains(v.index)){
				t.transform(v.getPos());
			}
		}
	}
	
	/**
	 * ArrayList keyed with the vertex indices.
	 * @return
	 */
	public ArrayList<Matrix3f> getRotations() {
		return rotations;
	}

	/**
	 * Getter for undeformed version of the mesh
	 * @return
	 */
	public HalfEdgeStructure getOriginalCopy() {
		return hs_originl;
	}
	

	/**
	 * initialize b and x
	 * @param hs
	 */
	private void init_b_x(HalfEdgeStructure hs) {
		b = new ArrayList<Tuple3f>();
		x = new ArrayList<Tuple3f>();
		for(int k = 0; k < hs.getVertices().size(); k++){
			b.add(new Point3f(0f,0f,0f));
			x.add(new Point3f(0f,0f,0f));
		}
	}
	
	/**
	 * Compute optimal positions for the current rotations.
	 */
	public void optimalPositions(){
		compute_b();
		solver.solveTuple(L_deform, bNorm, x);
		hs_deformed.setVerticesTo(x);
	}
	

	/**
	 * compute the righthand side for the position optimization
	 */
	private void compute_b() {
		reset_b();
		
		// foreach vertex within the original mesh
		for(Vertex v : hs_originl.getVertices()){
			Iterator<HalfEdge> edgeIterator = v.iteratorVE();
			while(edgeIterator.hasNext()){
				HalfEdge edge = edgeIterator.next();
				int idxRStart = edge.start().index;
				int idxREnd = edge.end().index;
				Matrix3f currentRotationStart = rotations.get(idxRStart);
				Matrix3f R = new Matrix3f(currentRotationStart);
				Matrix3f currentRotationEnd = rotations.get(idxREnd);
				R.add(currentRotationEnd);
				
				Vector3f edgeDir = edge.asVector();
				R.transform(edgeDir);
				
				if(v.isOnBoundary()){
					edgeDir.scale(0.0f);
				}else{
					float w = cotanWeights.get(edge)*-0.5f;
					edgeDir.scale(w);		
				}
				
				
				b.get(v.index).add(edgeDir);
			}
		}
		
		
		bNorm = new ArrayList<Tuple3f>();
		LTranspose.multTuple(b, bNorm);
		ArrayList<Point3f> newVertices = new ArrayList<Point3f>();
		userConstraints.multTuple(hs_deformed.getVerticesAsPointArray(), newVertices);
		for(int k = 0; k < b.size(); k++){
			bNorm.get(k).add(newVertices.get(k));
		}
	}



	/**
	 * helper method
	 */
	private void reset_b() {
		for(Tuple3f point : b){
			point.x = 0f; point.y = 0f; point.z = 0f;
		}
	}


	/**
	 * Compute the optimal rotations for 1-neighborhoods, given
	 * the original and deformed positions.
	 */
	public void optimalRotations() {
		//for the svd.
		Linalg3x3 l = new Linalg3x3(10);// argument controls number of iterations for ed/svd decompositions 
										//3 = very low precision but high speed. 3 seems to be good enough
			
		//Note: slightly better results are achieved when the absolute of cotangent
		//weights w_ij are used instead of plain cotangent weights.		
			
		
		for(int k = 0; k < rotations.size(); k++){
			Matrix3f S_i = new Matrix3f();
			Vertex currentDeformedVertex = hs_deformed.getVertices().get(k);
			Vertex currentOriginalVertex = hs_originl.getVertices().get(k);
			Iterator<HalfEdge> deformedEdges = currentDeformedVertex.iteratorVE();
			Iterator<HalfEdge> originalEdges = currentOriginalVertex.iteratorVE();
			
			while(deformedEdges.hasNext() || originalEdges.hasNext()){
				HalfEdge deformedEdge = deformedEdges.next();
				HalfEdge originalEdge = originalEdges.next();
				
				Matrix3f ppT = compute_ppT(originalEdge.asVector(), deformedEdge.asVector());
				
				
				float w_ij = Math.abs(cotanWeights.get(originalEdge));
				ppT.mul(w_ij);
				S_i.add(ppT);
			}
			
			Matrix3f U = new Matrix3f();
			Matrix3f V = new Matrix3f();
			Matrix3f D = new Matrix3f();
			
			l.svd(S_i, U, D, V);
			
			if(U.determinant() < 0){
				Vector3f last = new Vector3f();
				U.getColumn(2, last);
				last.negate();
				U.setColumn(2, last);
			}
			U.transpose();
			V.mul(U);
			
			
			rotations.set(k, V);
			
		}
		
	}

	


	
	

    private Matrix3f compute_ppT(Vector3f p, Vector3f p2) {
        assert(p.x*0==0);
        assert(p.y*0==0);
        assert(p.z*0==0);
        Matrix3f pp2T = new Matrix3f();
        pp2T.m00 = p.x*p2.x; pp2T.m01 = p.x*p2.y; pp2T.m02 = p.x*p2.z;
        pp2T.m10 = p.y*p2.x; pp2T.m11 = p.y*p2.y; pp2T.m12 = p.y*p2.z;
        pp2T.m20 = p.z*p2.x; pp2T.m21 = p.z*p2.y; pp2T.m22 = p.z*p2.z;
        return pp2T;
}


	
	




}
