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
import sparse.solver.SciPySolver;
import sparse.solver.Solver;
import assignment3.SSDMatrices;

public class ImplicitSmoother {
	
	public static void smooth(HalfEdgeStructure hs, CSRMatrix m, float lambda) {
		int vertexCount = hs.getVertices().size();
		float volumeBefore = hs.getVolume();
		ArrayList<Tuple3f> smoothedVertices = new ArrayList<Tuple3f>(vertexCount);
		ArrayList<Tuple3f> vertices = new ArrayList<Tuple3f>(vertexCount);
		getVertices(hs, m, lambda, vertices, smoothedVertices);
		hs.setVerticesTo(smoothedVertices);

		rescale(hs, volumeBefore);
	}

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

	private static void getVertices(HalfEdgeStructure hes, CSRMatrix matrix,
			float lambda, ArrayList<Tuple3f> vertices,
			ArrayList<Tuple3f> smoothedVertices) {
		
		int vertexCount = hes.getVertices().size();
		CSRMatrix I = SSDMatrices.eye(vertexCount, vertexCount);
		matrix.scale(-lambda);
		
		// initialize smooth matrix
		CSRMatrix smoothMat = new CSRMatrix(0, 0);
		smoothMat.add(I, matrix);
		
		// update position
		for (Vertex vertex : hes.getVertices()) {
			vertices.add(new Point3f(vertex.getPos()));
		}

		Solver solver = new SciPySolver("");
		solver.solveTuple(smoothMat, vertices, smoothedVertices);
	}

	public static void unsharpMasking(HalfEdgeStructure hes, CSRMatrix matrix,
			float lambda, float s) {
		
		ArrayList<Vertex> hesVertices = hes.getVertices();
		int vertexCount = hesVertices.size();
		float originalVolume = hes.getVolume();

		ArrayList<Tuple3f> smoothedVertices = new ArrayList<Tuple3f>(vertexCount);
		ArrayList<Tuple3f> vertices = new ArrayList<Tuple3f>(vertexCount);
		getVertices(hes, matrix, lambda, vertices, smoothedVertices);
		
		// update each vertex in HeS
		for(int k = 0; k < vertexCount; k++){
			Vector3f updatedVertex = new Vector3f(vertices.get(k));
			updatedVertex.sub(smoothedVertices.get(k));
			updatedVertex.scale(s);
			updatedVertex.add(smoothedVertices.get(k));
			
			// update current vertex
			Vertex toBeUpdateVertex = hesVertices.get(k);
			Point3f position = toBeUpdateVertex.getPos();
			position.set(updatedVertex);
		}

		rescale(hes, originalVolume);
	}

}
