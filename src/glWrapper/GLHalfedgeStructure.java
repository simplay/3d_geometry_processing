package glWrapper;

import java.util.ArrayList;
import java.util.Iterator;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import meshes.Face;
import meshes.HEData1d;
import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;
import utility.Monkey;

public class GLHalfedgeStructure extends GLDisplayable{
	private static Monkey slave = new Monkey();
	private HalfEdgeStructure halfEdgeStructure;
	private HEData1d valences1i;
	private HEData1d curveture1f;
	private HEData3d smoothedPositions3f;
	private HEData3d normals3f;
	private int minValence = Integer.MAX_VALUE;
	private int maxValence = Integer.MIN_VALUE;
	private int verticesCount;
	
	public GLHalfedgeStructure(HalfEdgeStructure halfEdgeStructure) {
		super(halfEdgeStructure.getVertices().size());
		this.halfEdgeStructure = halfEdgeStructure;
		this.valences1i = new HEData1d(halfEdgeStructure);
		this.curveture1f = new HEData1d(halfEdgeStructure);
		this.smoothedPositions3f = new HEData3d(halfEdgeStructure);
		this.normals3f = new HEData3d(halfEdgeStructure);
		
		float[] verts = new float[halfEdgeStructure.getVertices().size()*3];
		int[] ind = new int[halfEdgeStructure.getFaces().size()*3];
		this.verticesCount = verts.length / 3;

		copyToArrayP3f(halfEdgeStructure.getVertices(), verts);
		copyToArray(halfEdgeStructure.getFaces(), ind);
		
		this.addElement(verts, Semantic.POSITION , 3);
		//Here the position coordinates are passed a second time to the shader as color
		this.addElement(verts, Semantic.USERSPECIFIED , 3, "color");
		
		//pass the index array which has to be conformal to the glRenderflag returned, here GL_Triangles
		this.addIndices(ind);
		
		//1dim add data
		ArrayList<Vertex> vertices = this.halfEdgeStructure.getVertices();
		computeValence(vertices);
		computeSmoothedPositions(vertices, 10);
		computeNormals(vertices);
		computeCurveture(vertices);
		
		// pass  valence information for each vertex
		float[] valences = getValences();
		this.addElement(valences, Semantic.USERSPECIFIED , 1, "valence");
		
		float[] smoothed_positions = getSmoothedPositions();
		this.addElement(smoothed_positions, Semantic.USERSPECIFIED , 3, "smoothed_position");
		
		float[] normals = getNormals();
		this.addElement(normals, Semantic.USERSPECIFIED , 3, "normal_approx");
		
		float[] curvature = getCurvatures();
		this.addElement(curvature, Semantic.USERSPECIFIED , 1, "curvature");
	}
	
	private float[] getCurvatures() {
		Iterator<Number> iter = curveture1f.iterator();
		float[] tmp = new float[verticesCount];
		int t = 0;
		while(iter.hasNext()){
			tmp[t] = ((Float)iter.next());
			t++;
		}	
		return tmp;
	}

	private void computeCurveture(ArrayList<Vertex> vertices) {
		
		// foreach vertex v in vertices do
		for(Vertex v : vertices){
			this.curveture1f.put(v, v.getCurvature());
		}
		
	}
	
	private void computeNormals(ArrayList<Vertex> vertices) {
		for(Vertex v : vertices){
			this.normals3f.put(v, v.getWeightedAdjFacesNormal());
		}
	}

	private float[] getNormals() {
		Iterator<Tuple3f> iter = normals3f.iterator();
		float[] tmp = new float[verticesCount*3];
		int t = 0;
		
		while(iter.hasNext()){
			Tuple3f el = iter.next();
			tmp[3*t] = el.x;
			tmp[3*t+1] = el.y;
			tmp[3*t+2] = el.z;
			t++;
		}	
		return tmp;
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

	private float[] getSmoothedPositions() {
		Iterator<Tuple3f> iter = smoothedPositions3f.iterator();
		float[] tmp = new float[verticesCount*3];
		int t = 0;
		
		while(iter.hasNext()){
			Tuple3f el = iter.next();
			tmp[3*t] = el.x;
			tmp[3*t+1] = el.y;
			tmp[3*t+2] = el.z;
			t++;
		}	
		return tmp;
	}

	private float[] getValences() {
		Iterator<Number> iter = valences1i.iterator();
		float[] tmp = new float[verticesCount];
		int t = 0;
		while(iter.hasNext()){
			tmp[t] = ((Integer)iter.next());
			t++;
		}	
		return tmp;
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
	
	private void copyToArrayP3f(ArrayList<Vertex> arrayList, float[] verts) {
		int i = 0;
		for(Vertex v: arrayList){
			Point3f pos = v.getPos();
			verts[i++] = pos.x;
			verts[i++] = pos.y;
			verts[i++] = pos.z;
		}
	}
	
	/**
	 * Helper method that copies the face information to the ind array
	 * @param arrayList
	 * @param ind
	 */
	private void copyToArray(ArrayList<Face> arrayList, int[] ind) {
		int i = 0, j = 0;
		for(Face f : arrayList){
			Iterator<Vertex> iter = f.iteratorFV();
			j = 0;
			while(iter.hasNext()){
				Vertex v = iter.next();
				ind[i*3 + j] = v.index;
				j++;
			}
			i++;
		}
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
