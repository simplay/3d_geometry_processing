package meshes;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

import meshes.Face.IteratorFE;

/**
 * Implementation of a vertex for the {@link HalfEdgeStructure}
 */
public class Vertex extends HEElement{
	
	/**position*/
	Point3f pos;
	/**adjacent edge: this vertex is startVertex of anEdge*/
	HalfEdge anEdge;
	
	/**The index of the vertex, mainly used for toString()*/
	public int index;

	public Vertex(Point3f v) {
		pos = v;
		anEdge = null;
	}
	
	
	public Point3f getPos() {
		return pos;
	}

	public void setHalfEdge(HalfEdge he) {
		anEdge = he;
	}
	
	public HalfEdge getHalfEdge() {
		return anEdge;
	}
	
	/**
	 * Get an iterator which iterates over the 1-neighbouhood
	 * @return
	 */
	public Iterator<Vertex> iteratorVV(){
		return new IteratorVV(this);
	}
	
	/**
	 * Iterate over the incident edges
	 * @return
	 */
	public Iterator<HalfEdge> iteratorVE(){
		//Implement this...
		return null;
	}
	
	/**
	 * Iterate over the neighboring faces
	 * @return
	 */
	public Iterator<Face> iteratorVF(){
		//Implement this.
		return null;
	}
	
	
	public String toString(){
		return "" + index;
	}
	
	

	public boolean isAdjascent(Vertex w) {
		boolean isAdj = false;
		Vertex v = null;
		Iterator<Vertex> it = iteratorVV();
		for( v = it.next() ; it.hasNext(); v = it.next()){
			if( v==w){
				isAdj=true;
			}
		}
		return isAdj;
	}
	
	public final class IteratorVV implements Iterator<Vertex> {
		
		Vertex baseVertex;
		Vertex actual;
		HalfEdge baseE;
		
		public IteratorVV(Vertex base){
			this.baseVertex = base;
			this.actual = null;
			
		}
		
		@Override
		public boolean hasNext() {

			boolean answer = false;
			if(actual == null){
				answer = true;
			}else{
				Vertex tmp = actual.getHalfEdge().end();
				System.out.println("helper " + tmp.index + " " + baseVertex.index);
				System.out.println();
				answer = !(actual == baseVertex);
			}
			
//			return actual == null || actual.getHalfEdge().next.start() != baseVertex;
			return answer;
		}

		@Override
		public Vertex next() {
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			actual = (actual == null?
					baseVertex.getHalfEdge().incident_v :
					actual.getHalfEdge().incident_v);
			return actual;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
			
		}
		
	}

}
