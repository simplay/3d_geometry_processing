package meshes;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

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
		return new IteratorVE(anEdge);
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
	
	public float getAMixed() {
		float aMixed = 0;
		for(Iterator<Face> iter = iteratorVF(); iter.hasNext();) {
			aMixed += iter.next().getMixedVoronoiCellArea(this);
		}
		return aMixed;
	}
	
	/**
	 * get curvature of this vertex derived by its 
	 * neighborhood using the cotangent laplacian as approximation.
	 * @return approximation of curvature length
	 */
	public float getCurvature(){
		// compute face-neighborhood area
		float A_i = this.computeAMixed();
		float curvatureWeight = 1.0f / (4.0f * A_i);

		Vector3f sum = new Vector3f(0.0f, 0.0f, 0.0f);
		Iterator<HalfEdge> iterVE = this.iteratorVE();
		while(iterVE.hasNext()){
			HalfEdge he = iterVE.next();
			// note: cot(a) = 1/tan(a)
			float cotA = (float) (1.0f/Math.tan(he.getAlpha()));
			float cotB = (float) (1.0f/Math.tan(he.getBeta()));
			Vector3f heVector = he.toSEVector();
			
			heVector.scale(cotA+cotB);
			sum.add(heVector);
		}
		
		sum.scale(curvatureWeight);
		return sum.length();
	}
	
	/**
	 * computed mixed area from faces neighborhood from given vertex v.
	 * This area will be used in order to weight the curvature of the vertex v.
	 * @param v reference vertex.
	 * @return mixed area of faces from given vertex v.
	 */
	public float computeAMixed() {
		Iterator<Face> faceNeighborhood = this.iteratorVF();
		float summedArea = 0.0f;
		while(faceNeighborhood.hasNext()){
			Face neighborFace = faceNeighborhood.next();
			summedArea += neighborFace.computeObtuseFaceArea(this);
		}
		return summedArea;
	}
	
	/**
	 * quadratic error matrix of this vertex
	 * @return
	 */
	public Matrix4f getQuadricErrorMatrix(){
		Matrix4f quadricErrorMatrix = new Matrix4f();
		Iterator<Face> faces = this.iteratorVF();
		while(faces.hasNext()){
			Face face = faces.next();
			quadricErrorMatrix.add(face.getErrorQuadric());
		}
		return quadricErrorMatrix;
	}
	
	/**
	 * Get normal of this vertex by 
	 * the following approach:
	 * At every vertex sum up the normals of 
	 * the adjacent faces, weighted by the 
	 * incident angle, and normalize the result.
	 * @return normal of this vertex
	 */
	public Vector3f getWeightedAdjFacesNormal(){
		Vector3f vNormal = new Vector3f(0.0f, 0.0f, 0.0f);
		Iterator<HalfEdge> vEdgesIter = this.iteratorVE();
		
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
		vNormal.normalize();
		return vNormal;
		// normalize and write b
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
	
    public boolean isOnBoundary() {
        Iterator<HalfEdge> it = iteratorVE();
        while(it.hasNext()){
                if(it.next().isOnBorder()){
                        return true;
                }
        }
        return false;
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
	

	
	private abstract class IteratorV {
		HalfEdge start, current;
	
		public void remove() {
			//we don't support removing through the iterator.
			throw new UnsupportedOperationException();
		}
	}

	public final class IteratorVE extends IteratorV implements Iterator<HalfEdge> {	
		public IteratorVE(HalfEdge anEdge) {
			start = anEdge.opposite;
			current = null;
		}

		@Override
		public boolean hasNext() {
			return current == null || current.next.opposite != start;
		}

		@Override
		public HalfEdge next() {
			//make sure eternam iteration is impossible
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			//update what edge was returned last
			current = (current == null?
						start:
						current.next.opposite);
			return current;
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
