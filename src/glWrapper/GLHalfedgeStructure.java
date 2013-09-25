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
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.gl.GLDisplayable.Semantic;
import openGL.objects.Transformation;

public class GLHalfedgeStructure extends GLDisplayable{
	private HalfEdgeStructure halfEdgeStructure;
	private HEData1d valences1i;
	private HEData3d smoothedPositions3f;
	private int verticesCount;
	
	public GLHalfedgeStructure(HalfEdgeStructure halfEdgeStructure) {
		super(halfEdgeStructure.getVertices().size());
		this.halfEdgeStructure = halfEdgeStructure;
		this.valences1i = new HEData1d(halfEdgeStructure);
		this.smoothedPositions3f = new HEData3d(halfEdgeStructure);
		
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
//		computeNormals(vertices);
		
		// pass  valence information for each vertex
		float[] valences = getValences();
		this.addElement(valences, Semantic.USERSPECIFIED , 1, "valence");
		
		float[] smoothed_positions = getSmoothedPositions();
		this.addElement(smoothed_positions, Semantic.USERSPECIFIED , 3, "smoothed_position");
		
//		float[] normals = getNormals();
//		this.addElement(normals, Semantic.USERSPECIFIED , 3, "normal");
	}
	
	private void computeNormals(ArrayList<Vertex> vertices) {
		
		Point3f delta = new Point3f();
		Point3f otherPosition = null;
		HalfEdge item = null;
		Point3f myPosition = null;
		float weight = 0.0f;
		
		// foreach vertex
		for(Vertex v : vertices){
			Iterator<Face> iter = v.iteratorVF();
			
			myPosition = v.getPos();
			
			while(iter.hasNext()){
				Face f = iter.next();
//				f.
				HalfEdge t = f.getHalfEdge();
				System.out.println("v_"+v + " e_" + t.toString());
			}
		}
		
	}

	private float[] getNormals() {
		// TODO Auto-generated method stub
		return null;
	}

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
			System.out.println((k+1) + "th iteration proceeded");
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

	private void computeValence(ArrayList<Vertex> vertices){
		int incEdgeCount = 0;
		for(Vertex v : vertices){
			incEdgeCount = 0;
			Iterator<HalfEdge> incEdgesIter = v.iteratorVE();
			// count all incident edges for current v
			while(incEdgesIter.hasNext()){
				incEdgesIter.next();
				incEdgeCount++;
			}
			//write current valence back into v's HEData1D
			valences1i.put(v, incEdgeCount);
		}
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
