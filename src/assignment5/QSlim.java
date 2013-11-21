package assignment5;

import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import meshes.Face;
import meshes.HEData;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.objects.Transformation;


/** 
 * Implement the QSlim algorithm here
 * 
 * @author Alf
 *
 */
public class QSlim {
	
	private HalfEdgeStructure hs;
	
	// assigned error to each vertex
	private HashMap<Vertex, Matrix4f> vertexQuadricError = new HashMap<Vertex, Matrix4f>();
	private HashMap<HalfEdge, PotentialCollapse> candidateCollapses = new HashMap<HalfEdge, PotentialCollapse>();
	private HalfEdgeCollapse collapse;
	private PriorityQueue<PotentialCollapse> collapses = new  PriorityQueue<>();
	
	/********************************************
	 * Use or discard the skeletton, as you like.
	 * @return 
	 ********************************************/
	
	public Set<Vertex> getQSlimVertices(){
		return this.vertexQuadricError.keySet();
	}
	
	public Matrix4f getVertexMatrixAt(Vertex key){
		return this.vertexQuadricError.get(key);
	}
	
	public QSlim(HalfEdgeStructure hs){
		this.hs = hs;
		
		// map quadric error matrices to vertices
		for(Vertex vertex : hs.getVertices()){
			Matrix4f quadErrMat = vertex.getQuadricErrorMatrix();
			vertexQuadricError.put(vertex, quadErrMat);
		}
		
		// find all candidate collapss
		for(HalfEdge edge : hs.getHalfEdges()){
			if(!candidateCollapses.containsKey(edge.getOpposite())){
				PotentialCollapse candidate = new PotentialCollapse(edge, 0.0f);
				candidateCollapses.put(edge, candidate);
			}
		}
		
		this.collapse = new HalfEdgeCollapse(hs);
	}
	
	
	/**
	 * The actual QSlim algorithm, collapse edges until
	 * the target number of vertices is reached.
	 * @param target
	 */
	public void simplify(int target){
		
		// iterate until target size has been reached
		while(target < deltaDeathTotal()){
			collapsWorstEdge();
		}
		collapse.finish();
	}
	
	private int deltaDeathTotal(){
		int hsVertexCount = hs.getVertices().size();
		int deadVertxCount = collapse.deadVertices.size();
		return (hsVertexCount-deadVertxCount);
	}
	
	
	/**
	 * Collapse the next cheapest eligible edge. ; this method can be called
	 * until some target number of vertices is reached.
	 */
	public int collapsWorstEdge(){

		PotentialCollapse potColl = collapses.poll();
		
		if(potColl.isDirty || collapse.isEdgeDead(potColl.he)){
			return 0;
		}
		
		HalfEdge he = potColl.he;
		
		if(collapse.isCollapseMeshInv(he, potColl.targetPosition) ||
				!HalfEdgeCollapse.isEdgeCollapsable(he)){
			float cost = (potColl.cost + 0.1f)*10.0f;
			PotentialCollapse candidate = new PotentialCollapse(he, cost);
			candidateCollapses.put(he, candidate);
			return 0;
		}
		collapse.collapseEdge(he, potColl.targetPosition);
		
		// update vertices
		Vertex targetVertex = he.end();
		vertexQuadricError.put(targetVertex, potColl.qem);
		Iterator<HalfEdge> vertexAdjEdges = targetVertex.iteratorVE();
		while(vertexAdjEdges.hasNext()){
			HalfEdge edge = vertexAdjEdges.next();
			
			PotentialCollapse candidate = new PotentialCollapse(edge, 0.0f);
			candidateCollapses.put(edge, candidate);
			
			PotentialCollapse candidate2 = new PotentialCollapse(edge.getOpposite(), 0.0f);
			candidateCollapses.put(edge.getOpposite(), candidate2);
			
		}
		
		return 1;
	}
	
	/**
	 * Represent a potential collapse
	 * @author Alf
	 *
	 */
	protected class PotentialCollapse implements Comparable<PotentialCollapse>{
		private HalfEdge he;
		private float cost;
		// dirty potential collapses my be deleted
		private boolean isDirty; 
		private Matrix4f qem;
		private final boolean optimal = true;
		private Point3f targetPosition;
		
		
		private Point3f computeOptTargetPosition(Matrix4f qem){
			Matrix4f Q = new Matrix4f(qem);
			// Homogeneous matrix, i.e. last element is one
			Q.setRow(3, 0f, 0f, 0f, 1.0f);
			
			// is matrix invertable
			if(optimal && Q.determinant() != 0){
				Q.invert();
				Point3f optimalPosition = new Point3f();
				Q.transform(optimalPosition);
				targetPosition = optimalPosition;
			}else{
				targetPosition = new Point3f();
				Point3f posEnd = this.he.end().getPos();
				Point3f posStart = this.he.start().getPos();
				targetPosition.add(posEnd, posStart);
				targetPosition.scale(0.5f);
			}
			
			return targetPosition;
		}
		
		
		public PotentialCollapse(HalfEdge he, float cost){
			this.he = he;
			this.cost = cost;
			
			if(cost == 0.0f){
				qem = new Matrix4f();
				Matrix4f Q_v = vertexQuadricError.get(this.he.start());
				Matrix4f Q_w = vertexQuadricError.get(this.he.end());
				qem.add(Q_v,Q_w);
				Point3f target = computeOptTargetPosition(qem);
				Vector4f Qp = new Vector4f(target);
				Qp.w = 1.0f;
				qem.transform(Qp);
				Vector4f t = new Vector4f(target);
				t.w = 1.0f;
				this.cost = Qp.dot(t);
			}
			
			if(candidateCollapses.containsKey(this.he)){
				
				PotentialCollapse candidate = candidateCollapses.get(this.he);
				candidate.isDirty = true;
			}
			
			collapses.add(this);		
		}
		
		@Override
		public int compareTo(PotentialCollapse other) {
			return (int) Math.signum(this.cost - other.cost);
		}
		
		public boolean isDirty(){
			return this.isDirty;
		}
	}

}
