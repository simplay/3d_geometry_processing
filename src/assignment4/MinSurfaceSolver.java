package assignment4;



import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.solver.JMTSolver;
import sparse.solver.SciPySolver;
import sparse.solver.Solver;

public class MinSurfaceSolver {
	
	private static final boolean JMT = false;

	/**
	 * Threshold should be something between 0 and 1, usually 0.99 for a reasonable solution.
	 * @param hs, the halfedgeStructure you want to solve for.
	 * @param threshold, dictates, when we are close enough to the real solution.
	 */
	public static void solve(HalfEdgeStructure hs, float threshold) {
		float surfaceAreaBefore;
		float surfaceArea = hs.getSurfaceArea();
		Solver solver;
		if (JMT)
			solver = new JMTSolver();
		else 
			solver = new SciPySolver("laplacian_stuff");
		
		int iter = 0;
		do {
			surfaceAreaBefore = surfaceArea;
			ArrayList<Tuple3f> zeroCurvature = new ArrayList<Tuple3f>();
			CSRMatrix mat = LMatrices.mixedCotanLaplacian(hs);
		
			for(Vertex v: hs.getVertices()) {
				if (v.isOnBoundary()) {
					zeroCurvature.add(new Vector3f(v.getPos()));
					//add identity constraint on row
					mat.rows.get(v.index).add(new col_val(v.index, 1f));
				}
				else
					zeroCurvature.add(new Vector3f()); // vector filled with 0
			}
			
			ArrayList<Tuple3f> minifiedVertices = new ArrayList<Tuple3f>();
			solver.solveTuple(mat, zeroCurvature, minifiedVertices);
			Iterator<Vertex> hsViter = hs.iteratorV();
			Iterator<Tuple3f> minifiedVerticesIter = minifiedVertices.iterator();
			while (hsViter.hasNext()){
				hsViter.next().getPos().set(minifiedVerticesIter.next());
			}
			
			surfaceArea = hs.getSurfaceArea(); 
			System.out.println(surfaceArea/surfaceAreaBefore);
			//TODO: break if solver does not converge
			iter++;
			if(iter == 8) break;
		} while (surfaceArea/surfaceAreaBefore > threshold);
	}
}
