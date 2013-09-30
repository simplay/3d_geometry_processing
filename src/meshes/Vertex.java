package meshes;

import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.vecmath.Point3f;

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
		this.pos = v;
		this.anEdge = null;
	}
	
	public Point3f getPos() {
		return pos;
	}

	public void setHalfEdge(HalfEdge he) {
		this.anEdge = he;
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
		return new IteratorWrapperIteratorVF(this);
	}
	
	/**
	 * pretty string representation of this Vertex
	 */
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
	
	/**
	 * Compute valence of this vertex, i.e. 
	 * get the count of all incident edges for this vertex.
	 * @return returns valence number for this vertex
	 */
	public int getValence(){
		int incidentEdgeCount = 0;
		Iterator<HalfEdge> incEdgesIter = this.iteratorVE();
		
		// count all incident edges for current v
		while(incEdgesIter.hasNext()){
			incEdgesIter.next();
			incidentEdgeCount++;
		}
		
		
		return incidentEdgeCount;
	}
	
	
	/**
	 * ***************
	 * Inner classes *
	 * ***************
	 */
	
	/**
	 * Vertex one-neighborhood face-iterator wrapper
	 * This wrapper handles all the null face-cases 
	 * which can exist for the face-iterator.
	 * @author simplay
	 *
	 */
	private final class IteratorWrapperIteratorVF implements Iterator<Face> {
		private IteratorVF iter;
		private Face nextF;
		private boolean once = true;
		public IteratorWrapperIteratorVF(Vertex base){
			this.iter = new IteratorVF(base);
		}
		
		@Override
		public boolean hasNext() {
			if(once) nextF = iter.next();
			boolean statement = iter.hasNext();
			if(!statement && once) {
				once = false;
				return nextF != null;
			}
			return statement;
		}

		@Override
		public Face next() {
			return nextF;
		}

		@Override
		public void remove() {
			iter.remove();	
		}
		
		/**
		 * Vertex one-neighborhood face-iterator
		 * @author simplay
		 *
		 */
		private final class IteratorVF implements Iterator<Face> {
			private HalfEdge actualE;
			private HalfEdge previousE = null;
			private HalfEdge baseE;
			private HalfEdge limiter;
			private boolean hasLast = true;
			
			
			public IteratorVF(Vertex base){
				this.baseE = base.getHalfEdge();
				this.actualE = baseE;
				// ccw boundary edge - for comparison
				this.limiter = baseE.getPrev().getOpposite();			
			}
			
			@Override
			public boolean hasNext() {
				boolean notLimiterReached = previousE != limiter;
				return notLimiterReached;  
			}

			boolean skip = false;
			@Override
			public Face next() {			
				if(!hasNext() && hasLast){
					throw new NoSuchElementException();
				}
				
				// update
				do{
					skip = false;
					previousE = actualE;
					actualE = actualE.getOpposite().getNext();
					Face abc = previousE.getFace();
					
					if(abc == null){
						skip = true;
					}
					
					
				}while(skip && hasNext());

				if(previousE == null) return actualE.getFace();
				return previousE.getFace();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
				
			}
		}
		
	}
	

	
	/**
	 * Vertex one-neighborhood halfEdge-iterator
	 * @author simplay
	 *
	 */
	private final class IteratorVE implements Iterator<HalfEdge> {
		
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
	
	/**
	 * Vertex one-neighborhood vertex-iterator
	 * @author simplay
	 *
	 */
	private final class IteratorVV implements Iterator<Vertex> {		
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
