package glWrapper;

import java.util.ArrayList;
import java.util.Iterator;
import javax.media.opengl.GL;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;
import meshes.HEData1d;
import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;
import utility.Monkey;

public class GLHalfedgeStructure extends GLDisplayable{
	private Monkey slave;
	private HalfEdgeStructure halfEdgeStructure;
	private HEData1d valences1i;
	private HEData1d curveture1f;
	private HEData3d smoothedPositions3f;
	private HEData3d normals3f;
	private int minValence = Integer.MAX_VALUE;
	private int maxValence = Integer.MIN_VALUE;
	private int rounds = 10;
	
	public GLHalfedgeStructure(HalfEdgeStructure halfEdgeStructure) {
		super(halfEdgeStructure.getVertices().size());
		this.slave = new Monkey();
		this.halfEdgeStructure = halfEdgeStructure;
		this.valences1i = new HEData1d(halfEdgeStructure);
		this.curveture1f = new HEData1d(halfEdgeStructure);
		this.smoothedPositions3f = new HEData3d(halfEdgeStructure);
		this.normals3f = new HEData3d(halfEdgeStructure);
		
		ArrayList<Vertex> vertices = this.halfEdgeStructure.getVertices();
		computeValence(vertices);
		computeSmoothedPositions(vertices, rounds);
		computeNormals(vertices);
		computeCurveture(vertices);
		
		float[] verts = new float[halfEdgeStructure.getVertices().size()*3];
		int[] ind = new int[halfEdgeStructure.getFaces().size()*3];
		float[] smoothed_positions = slave.copyHead3dToArray3f(smoothedPositions3f);
		float[] normals = slave.copyHead3dToArray3f(normals3f);
		float[] valences = slave.copyHead1dToArray1i(valences1i);
		float[] curvature = slave.copyHead1dToArray1f(curveture1f);

		slave.copyToArrayP3f(halfEdgeStructure.getVertices(), verts);
		slave.copyToArray(halfEdgeStructure.getFaces(), ind);
		

		this.addElement(verts, Semantic.POSITION , 3);
		this.addElement(verts, Semantic.USERSPECIFIED , 3, "color");
		this.addIndices(ind);
		
		//1dim and 3dim data addition
		this.addElement(valences, Semantic.USERSPECIFIED , 1, "valence");
		this.addElement(smoothed_positions, Semantic.USERSPECIFIED , 3, "smoothed_position");
		this.addElement(normals, Semantic.USERSPECIFIED , 3, "normal_approx");
		this.addElement(curvature, Semantic.USERSPECIFIED , 1, "curvature");
	}
	

	private void computeCurveture(ArrayList<Vertex> vertices) {
		for(Vertex v : vertices){
			this.curveture1f.put(v, v.getCurvature());
		}	
	}
	
	private void computeNormals(ArrayList<Vertex> vertices) {
		for(Vertex v : vertices){
			this.normals3f.put(v, v.getWeightedAdjFacesNormal());
		}
	}
	
	/**
	 * computed k-smoothed position for all vertices 
	 * in this halfEdge structure.  
	 * @param vertices vertices of this halfEdge structure.
	 * @param rounds number of rounds for smoothing.
	 */
	private void computeSmoothedPositions(ArrayList<Vertex> vertices, int rounds) {
		boolean firstRound = true;
		Tuple3f p = new Point3f();
		for(int k = 0; k < rounds; k++){
			
			for(Vertex v : vertices){
				Iterator<Vertex> iter = v.iteratorVV();
			
				Vector3f avgPos = new Vector3f(0.0f, 0.0f, 0.0f);
				int posCount = 0;
				while(iter.hasNext()){
					Vertex _v = iter.next();
		
					if(firstRound) p = _v.getPos();
					else p = this.smoothedPositions3f.get(_v);
					
					avgPos.add(p);
					posCount++;
				}
				
				// weight average position and update
				avgPos.scale((float) (1.0f/posCount));
				this.smoothedPositions3f.put(v, avgPos);
			}
			// close barrier
			if(firstRound) firstRound = !firstRound;
		}	
	}

	
	/**
	 * Foreach vertex : vertices get their valence.
	 * @param vertices list of vertices.
	 */
	private void computeValence(ArrayList<Vertex> vertices){
		for(Vertex v : vertices){
			int valence = v.getValence();
			lazeUpdateValenceExtreme(valence);
			valences1i.put(v, valence);
		}
	}
	
	/**
	 * updates min and max valence for this haldedge structure.
	 * @param candidateValence candidate valence for new extreme values.
	 */
	private void lazeUpdateValenceExtreme(int candidateValence){
		if(candidateValence >= maxValence) this.maxValence = candidateValence;
		if(candidateValence <= minValence) this.minValence = candidateValence;
	}
	
	@Override
	public int glRenderFlag() {
		// TODO Auto-generated method stub
		return GL.GL_TRIANGLES;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		// TODO Auto-generated method stub
	}

}
