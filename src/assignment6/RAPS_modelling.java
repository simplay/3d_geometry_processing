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
	public ArrayList<Matrix3f> rotations;
	
	//A copy of the original half-edge structure. This is needed  to compute the correct
	//rotation matrices.
	private HalfEdgeStructure hs_originl;
	//The halfedge structure being deformed
	private HalfEdgeStructure hs_deformed;
	
	//The unnormalized cotan weight matrix, with zero rows for
	//boundary vertices.
	//It can be computed once at setup time and then be reused
	//to compute the matrix needed for position optimization
	public CSRMatrix L_cotan;
	//The matrix used when solving for optimal positions
	CSRMatrix L_deform;
	
	//allocate righthand sides and x only once.
	public ArrayList<Point3f> b;
	public ArrayList<Point3f> x;

	//sets of vertex indices that are constrained.
	private HashSet<Integer> keepFixed;
	private HashSet<Integer> deform;
	private HashMap<HalfEdge, Float> cotanWeights;
	
	private Solver solver;

	private CSRMatrix LTranspose;

	private CSRMatrix M_constraints;
	Linalg3x3 l = new Linalg3x3(3);
	private ArrayList<Point3f> bNormed;
	
	private final static float userconstraintW = 100f;

	
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
	
	
	/**
	 * update the linear system used to find optimal positions
	 * for the currently constrained vertices.
	 * Good place to do the cholesky decompositoin
	 */
	public void updateL() {
		int vertecCount = hs_originl.getVertices().size();
		updateConstraints();
		
		for (HalfEdge he: hs_originl.getHalfEdges()) {
			cotanWeights.put(he, he.getCotanWeight());
		}
		
		L_deform = new CSRMatrix(0, vertecCount);
		CSRMatrix LTLt = new CSRMatrix(0, vertecCount);
		LTranspose = L_cotan.transposed();
		LTranspose.multParallel(L_cotan, LTLt);
		L_deform.add(LTLt, M_constraints);
		
		if (deform.isEmpty()){
			solver = new JMTSolver();
		}else{
			solver = new Cholesky(L_deform);
		}  
		
		//fill rotations with id
		rotations = new ArrayList<Matrix3f>();
		for (int k = 0; k < vertecCount; k++) {
			Matrix3f identity = new Matrix3f();
			identity.setIdentity();
			rotations.add(identity);
		}
	}
	
	private void updateConstraints() {
		int vertecCount = hs_originl.getVertices().size();
		M_constraints = new CSRMatrix(0, vertecCount);
		for (int k = 0; k < vertecCount; k++) {

			M_constraints.addRow();
			ArrayList<col_val> row = M_constraints.lastRow();
			
			if (keepFixed.contains(k) || deform.contains(k)) {
				row.add(new col_val(k, userconstraintW*userconstraintW)); 
				Collections.sort(row);
			} else if (hs_originl.getVertices().get(k).isOnBoundary()) {
				row.add(new col_val(k, userconstraintW));
			}
		}
	}
	
	/**
	 * The RAPS modelling algorithm.
	 * @param t
	 * @param nRefinements
	 */
	public void deform(Matrix4f t, int nRefinements){
		this.transformTarget(t);
		for(int k = 0; k < nRefinements; k++) {
			optimalPositions();
			optimalRotations();	
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
	public ArrayList<Matrix3f> getRotations() {
		return rotations;
	}
	public HalfEdgeStructure getOriginalCopy() {
		return hs_originl;
	}
	private void init_b_x(HalfEdgeStructure hs) {
		b = new ArrayList<Point3f>();
		x = new ArrayList<Point3f>();
		for(int i = 0; i < hs.getVertices().size(); i++){
			b.add(new Point3f(0,0,0));
			x.add(new Point3f(1,1,1));
		}
	}	
	
	/**
	 * Compute optimal positions for the current rotations.
	 */
	public void optimalPositions(){
		compute_b();
		solver.solveTuple(L_deform, bNormed, x);
		hs_deformed.setVerticesTo(x);
	}
	

	/**
	 * compute the right hand side for the position optimization
	 */
	private void compute_b() {
		reset_b();
		
		// foreach vertex : original mesh compute coresponding b value
		for (Vertex v: hs_originl.getVertices()) {
			Iterator<HalfEdge> edges = v.iteratorVE();
			
			// foreach incident edge of current vertex
			while (edges.hasNext()) {
				HalfEdge edge = edges.next();
				
				// compute rotation for current edge
				int rotStartIdx = edge.start().index;
				int rotEndIdx = edge.end().index;
				Matrix3f R = new Matrix3f(rotations.get(rotStartIdx));
				R.add(rotations.get(rotEndIdx));
				Vector3f edgeDir = edge.asVector();
				
				// rotate edge
				R.transform(edgeDir);
				
				// case distinction for boundary
				if (v.isOnBoundary())
					edgeDir.scale(0);
				else {
					// apply scaling with cotan weight
					float w = cotanWeights.get(edge)*(-0.5f);
					edgeDir.scale(w);
				}
				// update current b with new edge values
				b.get(v.index).add(edgeDir);
			}
		}
		
		// compute normalzed version of b
		bNormed = new ArrayList<Point3f>();
		LTranspose.multTuple(b, bNormed);
		ArrayList<Point3f> verticesNew = new ArrayList<Point3f>();
		M_constraints.multTuple(hs_deformed.getVerticesAsPointArray(), verticesNew);
		
		// foreach b compute its normalized version
		for (int k = 0; k < b.size(); k++){
			bNormed.get(k).add(verticesNew.get(k));
		}
	}



	/**
	 * helper method
	 */
	private void reset_b() {
		for(Point3f p: b){
			p.x = 0; p.y = 0; p.z = 0;
		}
	}


	/**
	 * Compute the optimal rotations for 1-neighborhoods, given
	 * the original and deformed positions.
	 */
	public void optimalRotations() {			
		for (int i = 0; i < rotations.size(); i++) {
			Matrix3f S_i = new Matrix3f();
			Vertex v_deformed = hs_deformed.getVertices().get(i);
			Vertex v_orig = hs_originl.getVertices().get(i);
			Iterator<HalfEdge> iter_deformed = v_deformed.iteratorVE();
			Iterator<HalfEdge> iter_orig = v_orig.iteratorVE();
			
			while (iter_deformed.hasNext() || iter_orig.hasNext()) { 
				HalfEdge heOrig = iter_orig.next();
				HalfEdge heDeformed = iter_deformed.next();
				Matrix3f ppT = compute_ppT(heOrig.asVector(), heDeformed.asVector());
				float w_ij = Math.abs(cotanWeights.get(heOrig));
				ppT.mul(w_ij); 
				S_i.add(ppT);
			}
			rotations.set(i, makeNewRotationFor(S_i));
		}
		
	}
	private Matrix3f makeNewRotationFor(Matrix3f S_i) {
		Matrix3f U = new Matrix3f();
		Matrix3f V = new Matrix3f();
		Matrix3f D = new Matrix3f();
		l.svd(S_i, U, D, V);

		if (U.determinant() < 0) {
			Vector3f lastCol = new Vector3f();
			U.getColumn(2, lastCol);
			lastCol.negate();
			U.setColumn(2, lastCol);
		}
		U.transpose();
		V.mul(U);
		return V;
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
