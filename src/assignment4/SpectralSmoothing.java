package assignment4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.vecmath.Point3f;

import meshes.HalfEdgeStructure;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.SCIPYEVD;

public class SpectralSmoothing {
	public static void smooth(HalfEdgeStructure hes, int evCount)
			throws IOException {
		
		CSRMatrix L = LMatrices.symmetricCotanLaplacian(hes);
		ArrayList<Float> eigenValues = new ArrayList<Float>();
		ArrayList<ArrayList<Float>> eigenVectors = new ArrayList<ArrayList<Float>>();
		
		// svd decomposition
		SCIPYEVD.doSVD(L, "", evCount, eigenValues,
				eigenVectors);
		
		// matrix built by chosen eigenvectors
		CSRMatrix evMat = new CSRMatrix(0, hes.getVertices().size());
		for (int i = 0; i < evCount; i++) {
			evMat.addRow();
			ArrayList<col_val> row = evMat.lastRow();

			Iterator<Float> iter = eigenVectors.get(i).iterator();
			// update each vertex
			for (int j = 0; j < hes.getVertices().size(); j++) {
				
				row.add(new col_val(j, iter.next()));
			}
			Collections.sort(row);
		}
		
		CSRMatrix evMatT = evMat.transposed();
		CSRMatrix result = new CSRMatrix(0, 0);
		
		// [e1,...,e_n]*diag(f(freq(e_1),...,f(freq(e_n))*[e1,...,e_n]'
		evMatT.multParallel(evMat, result);
		ArrayList<Point3f> smoothedVertices = new ArrayList<Point3f>();
		result.multTuple(hes.getVerticesAsPointArray(), smoothedVertices);
		hes.setVerticesTo(smoothedVertices);
	}
}
