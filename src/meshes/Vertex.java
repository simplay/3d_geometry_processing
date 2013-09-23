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
		return new IteratorVE(this);
	}
	
	/**
	 * Iterate over the neighboring faces
	 * @return
	 */
	public Iterator<Face> iteratorVF(){
		return new IteratorVF(this);
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
	
	public final class IteratorVF implements Iterator<Face> {
		
		private HalfEdge actualE;
		private HalfEdge baseE;
		private HalfEdge limiter;
		// first iterator item processed?
		private boolean once = false;
		
		public IteratorVF(Vertex base){
			this.baseE = base.getHalfEdge();
			this.actualE = null;
			// ccw boundary edge - for comparison
			this.limiter = baseE.getPrev().getOpposite();			
		}
		
		@Override
		public boolean hasNext() {
			return actualE == null || limiter != actualE;  
		}

		@Override
		public Face next() {			
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			
			// iterate until either a non-null face has been 
			// found or or there is no next element
			do{
				if(actualE == null){
					once = true;
					actualE = baseE;
				}else{
					HalfEdge he = actualE;
					he = he.getOpposite().getNext();
					actualE = he;
				}
			}while((once && actualE.getFace() == null) && hasNext());
			return actualE.getFace();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
			
		}
	}
	
	public final class IteratorVE implements Iterator<HalfEdge> {
		
		private HalfEdge actualE;
		private HalfEdge baseE;
		private HalfEdge limiter;
		
		public IteratorVE(Vertex base){
			this.baseE = base.getHalfEdge();
			this.actualE = null;
			this.limiter = baseE.getPrev().getOpposite();			
		}
		
		@Override
		public boolean hasNext() {
			return actualE == null || limiter != actualE;  
		}

		@Override
		public HalfEdge next() {			
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			
			if(actualE == null){
				actualE = baseE;
			}else{
				HalfEdge he = actualE;
				he = he.getOpposite().getNext();
				actualE = he;
			}
			return actualE;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
			
		}
	}
	
	public final class IteratorVV implements Iterator<Vertex> {
		
		private HalfEdge actualE;
		private HalfEdge baseE;
		private HalfEdge limiter;
		
		public IteratorVV(Vertex base){
			this.baseE = base.getHalfEdge();
			this.actualE = null;
			this.limiter = baseE.getPrev().getOpposite();			
		}
		
		@Override
		public boolean hasNext() {
			return actualE == null || limiter != actualE;  
		}

		@Override
		public Vertex next() {			
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			
			if(actualE == null){
				actualE = baseE;
			}else{
				HalfEdge he = actualE;
				he = he.getOpposite().getNext();
				actualE = he;
			}
			return actualE.end();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
			
		}
	}

}
