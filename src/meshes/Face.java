package meshes;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

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

}
