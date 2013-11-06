package meshes;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.vecmath.Vector3f;

import myutils.MyMath;

/**
 * Implementation of a face for the {@link HalfEdgeStructure}
 *
 */
public class Face extends HEElement {
	
	//an adjacent edge, which is positively oriented with respect to the face.
	private HalfEdge anEdge;
	
	public Face(){
		anEdge = null;
	}

	public void setHalfEdge(HalfEdge he) {
		this.anEdge = he;
	}

	public HalfEdge getHalfEdge() {
		return anEdge;
	}
	
	public float getMixedVoronoiCellArea(Vertex p) {
		Iterator<HalfEdge> iter = new IteratorFE(anEdge.getFace());
		HalfEdge pointingToP = iter.next();
		while(pointingToP.incident_v != p){
			pointingToP = iter.next();
		}
		float angleAtP = pointingToP.getIncidentAngle();
		float voronoiCellArea;
		if (!isObtuse()) { // non-obtuse
			HalfEdge PR = pointingToP.getOpposite();
			HalfEdge PQ = pointingToP.getNext();
			float areaPR = PR.lengthSquared()*MyMath.cot(PQ.getIncidentAngle());
			float areaPQ = PQ.lengthSquared() * MyMath.cot(PQ.getNext().getIncidentAngle());
			voronoiCellArea = 1/8f * ( areaPR + areaPQ ); 
		} else if (angleAtP > Math.PI/2) { // obtuse at P
			voronoiCellArea = getArea()/2;
		} else { // else
			voronoiCellArea = getArea()/4;
		}
		return voronoiCellArea;
	}
	
	
	/**
	 * Compute this face's obtuse area
	 * @param v origin neighborhood vertex of this face
	 * @return returns obtuse face area.
	 */
	public float computeObtuseFaceArea(Vertex v) {
		IteratorFE iter = this.iteratorFE();
		HalfEdge toV = null;
		while(iter.hasNext()){
			toV = iter.next();
			if(toV.incident_v == v) break;
		}
		
		// get angle spanned by edges intersection v
		float angleV = toV.toSEVector().angle(toV.getNext().toSEVector());
		float area = 0.0f;
		
		if(!this.isObtuse()){
			
			HalfEdge PR = toV.getOpposite();
			float areaPR = (float) (Math.pow(PR.getLength(), 2.0)*(1.0 / Math.tan(PR.getIncidentAngle())));
			
			HalfEdge PQ = toV.getNext();
			float areaPQ = (float) (Math.pow(PQ.getLength(), 2.0)*(1.0 / Math.tan(PQ.getIncidentAngle())));
			
			area = (areaPR + areaPQ)/8.0f;
			
		}else if(angleV > Math.PI / 2.0f){
			area = this.getArea() / 2.0f;
		}else{
			area = this.getArea() / 4.0f;
		}
		
		return area;
	}
	
	
	/**
	 * is this face obuse?
	 * @return
	 */
	public boolean isObtuse(){
		IteratorFE iter = this.iteratorFE();
		while(iter.hasNext()){
			float angle  = iter.next().getIncidentAngle();
			if(angle > Math.PI/2 && angle < Math.PI) return true;
		}
		return false;
	}
	
	public List<Vertex> getCorners(){
		List<Vertex> cornerList = new LinkedList<Vertex>();
		Iterator<Vertex> spanVertices = this.iteratorFV();
		while(spanVertices.hasNext()){
			cornerList.add(spanVertices.next());
		}
		return cornerList;
	}
	
	/**
	 * Iterate over the vertices on the face.
	 * @return
	 */
	public Iterator<Vertex> iteratorFV(){
		return new IteratorFV(anEdge);
	}
	
	/**
	 * Iterate over the adjacent edges
	 * @return
	 */
	public IteratorFE iteratorFE(){
		//Implement this

		return new IteratorFE(this);
	}
	
	public String toString(){
		if(anEdge == null){
			return "f: not initialized";
		}
		String s = "f: [";
		Iterator<Vertex> it = this.iteratorFV();
		while(it.hasNext()){
			s += it.next().toString() + " , ";
		}
		s+= "]";
		return s;
		
	}
	
	
	public final class IteratorFE implements Iterator<HalfEdge>{
		
		private Face baseFace;
		private HalfEdge baseEdge;
		private HalfEdge actual;

		public IteratorFE(Face face) {
			this.baseFace = face;
			this.actual = null;
			this.baseEdge = baseFace.getHalfEdge();
		}
		
		@Override
		public boolean hasNext() {
			return actual == null || actual.next != baseEdge;
		}

		@Override
		public HalfEdge next() {
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			
			actual = (actual == null?
					baseEdge:
					actual.next);
			return actual;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	
	/**
	 * Iterator to iterate over the vertices on a face
	 * @author Alf
	 *
	 */
	public final class IteratorFV implements Iterator<Vertex> {
		
		
		private HalfEdge first, actual;

		public IteratorFV(HalfEdge anEdge) {
			first = anEdge;
			actual = null;
		}

		@Override
		public boolean hasNext() {
			return actual == null || actual.next != first;
		}

		@Override
		public Vertex next() {
			//make sure eternam iteration is impossible
			if(!hasNext()){
				throw new NoSuchElementException();
			}

			//update what edge was returned last
			actual = (actual == null?
						first:
						actual.next);
			return actual.incident_v;
		}

		
		@Override
		public void remove() {
			//we don't support removing through the iterator.
			throw new UnsupportedOperationException();
		}

		/**
		 * return the face this iterator iterates around
		 * @return
		 */
		public Face face() {
			return first.incident_f;
		}
		

	}
	/**
	 * Compute spanned area of this face by its vertices
	 * @return area of this face.
	 */
	public float getArea(){
		Vector3f normal = getFaceNormal();
		return (normal.length() / 2.0f);
	}
	
	/**
	 * Compute normal vector on this face defined by its vertices
	 * @return normal vector perpendicular to this face
	 */
	public Vector3f getFaceNormal(){
		Vector3f normal = new Vector3f();
		normal.cross(anEdge.toSEVector(), anEdge.next.toSEVector());
		return normal;
	}
	
	
	
	
	
	
	
}
