package assignment4;

import java.util.ArrayList;
import java.util.Iterator;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;
import com.jogamp.opengl.math.FloatUtil;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.solver.SciPySolver;
import sparse.solver.Solver;

public class MinSurfaceSolver {
	
	private static void rescale(HalfEdgeStructure hes, float previousVolume) {
		float relativeVolume = previousVolume/hes.getVolume();
		float volumeRatio = FloatUtil.pow(relativeVolume, 1.0f/3.0f);
		Iterator<Vertex> vertices = hes.iteratorV();
		
		// For each vertex in HalfEdgeStructure do rescale
		while (vertices.hasNext()){
			Point3f vPosition = vertices.next().getPos();
			vPosition.scale(volumeRatio);
		}	
	}
	
	
	/**
	 * @param hes
	 * @param threshold how close is approx solution to reality
	 */
	public static void solve(HalfEdgeStructure hs, float threshold) {
		float surfaceAreaBefore;
		float surfaceArea = hs.getSurfaceArea();
		Solver solver = new SciPySolver("");
		float ration = 0.0f;
		int abordCounter = 0;
		do {
			surfaceAreaBefore = surfaceArea;
			ArrayList<Tuple3f> zeroCurvature = new ArrayList<Tuple3f>();
			CSRMatrix mat = LMatrices.mixedCotanLaplacian(hs);
			
			// for each vertex
			for(Vertex v: hs.getVertices()) {
				Vector3f value = new Vector3f(0.0f, 0.0f, 0.0f);
				if (v.isOnBoundary()) {
					value = new Vector3f(v.getPos());
					zeroCurvature.add(value);
					int index = v.index;
					ArrayList<col_val> indexRow = mat.rows.get(index);
					indexRow.add(new col_val(index, 1.0f));
				}
				else{
					zeroCurvature.add(value);
				}
			}
			
			ArrayList<Tuple3f> narrowVertices = new ArrayList<Tuple3f>();
			solver.solveTuple(mat, zeroCurvature, narrowVertices);
			
			
			float previous = hs.getVolume();

			Iterator<Tuple3f> newVertices = narrowVertices.iterator();
			Iterator<Vertex> hsViter = hs.iteratorV();
			
			// update hes vertices with new value
			while (hsViter.hasNext() && newVertices.hasNext()){
				Tuple3f newV = newVertices.next();
				newV.scale(-1f);
				hsViter.next().getPos().set(newV);
			}

			rescale(hs, previous);
			
			surfaceArea = hs.getSurfaceArea(); 
			ration = (surfaceArea/surfaceAreaBefore);
			abordCounter++;
		} while (ration < threshold && abordCounter <= 8);

	}
}
