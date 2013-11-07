package assignment4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.vecmath.Vector3f;

import com.jogamp.opengl.math.FloatUtil;

import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import utility.Monkey;

/**
 * Methods to create different flavours of the cotangent and uniform laplacian.
 * @author Alf
 *
 */
public class LMatrices {
	
	/**
	 * The uniform Laplacian
	 * @param hs half edge structure
	 * @return returns the uniform laplacian defined by hs
	 */
	public static CSRMatrix uniformLaplacian(HalfEdgeStructure hs){
		// halfedge structure vertices.
		ArrayList<Vertex> vertices = hs.getVertices();
		
		// initiall 0 rows and |N| columns (i.e. vertex count)
		CSRMatrix matrix = new CSRMatrix(0, vertices.size());
		
		// for each vertex compute a row of L,
		// since each row is computed by each vertex's neighborhood. 
		for(Vertex v : vertices){
			matrix.addRow();
			ArrayList<col_val> currentRow = matrix.lastRow();
			
			int neighborCount = v.getValence();
			float oneOverNv = (1.0f / neighborCount);
			
			
			// for each vertex neighbor of current vetex v
			Iterator<Vertex> vertexNeighborhood = v.iteratorVV();
			while(vertexNeighborhood.hasNext()){
				col_val element = new col_val(vertexNeighborhood.next().index, oneOverNv);
				currentRow.add(element);
			}
			// we are -1 (normalization, convexcombination) - see slides.
			currentRow.add(new col_val(v.index, -1.0f));
		}
		return matrix;
	}
	
	/**
	 * The cotangent Laplacian
	 * @param hs
	 * @return
	 */
	public static CSRMatrix mixedCotanLaplacian(HalfEdgeStructure hs){
		// halfedge structure vertices.
		ArrayList<Vertex> vertices = hs.getVertices();
		
		// initiall 0 rows and |N| columns (i.e. vertex count)
		CSRMatrix matrix = new CSRMatrix(0, vertices.size());
		
		for(Vertex v : vertices){
			matrix.addRow();
			ArrayList<col_val> currentRow = matrix.lastRow();
			
			float AMixed = v.getAMixed();
			
			float sum = 0.0f;
			Iterator<HalfEdge> iterVE = v.iteratorVE();
			while(iterVE.hasNext()){
				HalfEdge he = iterVE.next();
				// note: cot(a) = 1/tan(a)
				
				float cotA = Monkey.clamppedCot(he.getAlpha());
				float cotB = Monkey.clamppedCot(he.getBeta());

				
					
				float elementValue = (cotA+cotB)/(2.0f*AMixed);
				int at = he.start().index;
				col_val element = new col_val(at, elementValue);
				currentRow.add(element);
				sum += elementValue;	
			}
			currentRow.add(new col_val(v.index, -sum));
		}
		return matrix;
	}
	
	/**
	 * A symmetric cotangent Laplacian, cf Assignment 4, exercise 4.
	 * @param hs
	 * @return
	 */
    public static CSRMatrix symmetricCotanLaplacian(HalfEdgeStructure hs){
        CSRMatrix m = new CSRMatrix(0, hs.getVertices().size());
        for(Vertex v: hs.getVertices()) {
        	
			m.addRow();
			ArrayList<col_val> row = m.lastRow();
        	
        	

                if (v.isOnBoundary())
                        continue; //leave row empty
                float aMixed = v.getAMixed();
                //copy paste from vertex.getCurvature() (I'm so sorry)
                Iterator<HalfEdge> iter = v.iteratorVE();
                float sum = 0;
                while(iter.hasNext()) {
                        HalfEdge current = iter.next();
                       
                        
        				float cot_alpha = Monkey.clamppedCot(current.getAlpha());
        				float cot_beta = Monkey.clamppedCot(current.getBeta());
                        
                        
                        float scale = FloatUtil.sqrt(aMixed*current.start().getAMixed());
                        float entry = (cot_alpha + cot_beta)/(2f*scale);
                        sum += entry;
                        row.add(new col_val(current.start().index, entry));
                }                
                row.add(new col_val(v.index, -sum));
                
                Collections.sort(row);
        }
        return m;
    }
	
	
	/**
	 * helper method to multiply x,y and z coordinates of the halfedge structure at once
	 * @param m
	 * @param s
	 * @param res
	 */
	public static void mult(CSRMatrix m, HalfEdgeStructure s, ArrayList<Vector3f> res){
		ArrayList<Float> x = new ArrayList<>(), b = new ArrayList<>(s.getVertices().size());
		x.ensureCapacity(s.getVertices().size());
		
		res.clear();
		res.ensureCapacity(s.getVertices().size());
		for(Vertex v : s.getVertices()){
			x.add(0.f);
			res.add(new Vector3f());
		}
		
		for(int i = 0; i < 3; i++){
			
			//setup x
			for(Vertex v : s.getVertices()){
				switch (i) {
				case 0:
					x.set(v.index, v.getPos().x);	
					break;
				case 1:
					x.set(v.index, v.getPos().y);	
					break;
				case 2:
					x.set(v.index, v.getPos().z);	
					break;
				}
				
			}
			
			m.mult(x, b);
			
			for(Vertex v : s.getVertices()){
				switch (i) {
				case 0:
					res.get(v.index).x = b.get(v.index);	
					break;
				case 1:
					res.get(v.index).y = b.get(v.index);	
					break;
				case 2:
					res.get(v.index).z = b.get(v.index);	
					break;
				}
				
			}
		}
	}
}
