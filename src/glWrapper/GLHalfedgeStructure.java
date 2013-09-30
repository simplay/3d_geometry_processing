package glWrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import meshes.Face;
import meshes.Face.IteratorFE;
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
	private HEData1d curveture1f;
	private HEData3d smoothedPositions3f;
	private HEData3d normals3f;
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
	}
	


	private void computeCurveture(ArrayList<Vertex> vertices) {
		
		// foreach vertex v in vertices do
		for(Vertex v : vertices){

			// compute face-neighborhood area
			float A_i = computeAMixed(v);
			float curvatureWeight = 1.0f / (2.0f * A_i);

			Vector3f sum = new Vector3f(0.0f, 0.0f, 0.0f);
			Iterator<HalfEdge> iterVE = v.iteratorVE();
			while(iterVE.hasNext()){
				HalfEdge he = iterVE.next();
				float cotA = (float) (1.0f/Math.tan(he.getAlpha()));
				float cotB = (float) (1.0f/Math.tan(he.getBeta()));
				Vector3f heVector = he.toSEVector();
				
				heVector.scale(cotA+cotB);
				sum.add(heVector);
			}
			
			sum.scale(curvatureWeight);
			
			this.curveture1f.put(v, sum.length());
		}
		
	}


	/**
	 * computed mixed area from faces neighborhood from given vertex v.
	 * This area will be used in order to weight the curvature of the vertex v.
	 * @param v reference vertex.
	 * @return mixed area of faces from given vertex v.
	 */
	private float computeAMixed(Vertex v) {
		Iterator<Face> faceNeighborhood = v.iteratorVF();
		float summedArea = 0.0f;
		while(faceNeighborhood.hasNext()){
			Face neighborF = faceNeighborhood.next();
			summedArea += computeFaceArea(neighborF, v);
		}
		return summedArea;
	}

	private float computeFaceArea(Face neighborF, Vertex v) {
		IteratorFE iter = neighborF.iteratorFE();
		HalfEdge toV = null;
		while(iter.hasNext()){
			toV = iter.next();
			if(toV.incident_v == v) break;
		}
		
		// get angle spanned by edges intersection v
		float angleV = toV.toSEVector().angle(toV.getNext().toSEVector());
		float area = 0.0f;
		
		if(!neighborF.isObtuse()){
			
			HalfEdge PR = toV.getOpposite();
			float areaPR = (float) (Math.pow(PR.getLength(), 2.0)*(1.0 / Math.tan(PR.getIncidentAngle())));
			
			HalfEdge PQ = toV.getNext();
			float areaPQ = (float) (Math.pow(PQ.getLength(), 2.0)*(1.0 / Math.tan(PQ.getIncidentAngle())));
			
			area = (areaPR + areaPQ)/8.0f;
			
		}else if(angleV > Math.PI / 2.0f){
			area = neighborF.getArea() / 2.0f;
		}else{
			area = neighborF.getArea() / 4.0f;
		}
		
		return area;
	}



	private void computeNormals(ArrayList<Vertex> vertices) {
		
		// foreach vertex of our HE structure
		for(Vertex v : vertices){
			Vector3f vNormal = new Vector3f(0.0f, 0.0f, 0.0f);
			Iterator<HalfEdge> vEdgesIter = v.iteratorVE();
			
			// get reference vector defined by current vertex
			HalfEdge refHE = vEdgesIter.next().getOpposite();			
			Vector3f refV = refHE.toSEVector();
			
			//for each edge of current vertex
			while(vEdgesIter.hasNext()){
				Vector3f tmpNormal = new Vector3f();
				
				// other vector
				HalfEdge otherE = vEdgesIter.next().getOpposite();				
				Vector3f otherV = otherE.toSEVector();
				
				// weighted normal formed by those two vectors
				tmpNormal.cross(refV, otherV);
				float angleW = refV.angle(otherV);
				tmpNormal.scale(angleW);
				
				// update normal and referece vector for next iteration
				vNormal.add(tmpNormal);
				refV = otherV;
			}
			
			// normalize and write back
			vNormal.normalize();
			this.normals3f.put(v, vNormal);
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
