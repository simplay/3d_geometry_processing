package assignment4;

import static org.junit.Assert.*;
import java.util.ArrayList;
import javax.vecmath.Vector3f;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import org.junit.Before;
import org.junit.Test;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;

public class Assignment4_1_Tests {

	// A sphere of radius 2.
	private HalfEdgeStructure hs;
	// An ugly sphere of radius 1, don't expect the Laplacians
	// to perform accurately on this mesh.
	private HalfEdgeStructure hs2;

	@Before
	public void setUp() {
		try {
			WireframeMesh m = ObjReader.read("objs/sphere.obj", false);
			hs = new HalfEdgeStructure();
			hs.init(m);

			m = ObjReader.read("objs/uglySphere.obj", false);
			hs2 = new HalfEdgeStructure();
			hs2.init(m);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	float eps = 0.0001f;

	@Test
	public void testEachUniformLaplacianRowSummsUpToZero() {
		CSRMatrix matrix = LMatrices.uniformLaplacian(hs);
		for (ArrayList<col_val> row : matrix.rows) {
			float sum = 0.0f;
			for (col_val element : row) {
				sum += element.val;
			}
			assertEquals(0.0f, sum, eps);
		}
	}
	
	@Test
	public void testEachSymmRowSummsUpToZero() {
		CSRMatrix matrix = LMatrices.symmetricCotanLaplacian(hs);
		for (ArrayList<col_val> row : matrix.rows) {
			float sum = 0.0f;
			for (col_val element : row) {
				sum += element.val;
			}
			assertEquals(0.0f, sum, eps);
		}
	}

	@Test
	public void testEachMixedCotangentLaplacianRowSummsUpToZero() {
		CSRMatrix matrix = LMatrices.mixedCotanLaplacian(hs);
		// for each row of Laplacian matrix
		for (ArrayList<col_val> row : matrix.rows) {
			float sum = 0.0f;

			// for each element within row sum elements up
			for (col_val element : row) {
				sum += element.val;
			}
			// is sum smaller given threshold eps
			assertEquals(0.0f, sum, eps);
		}
	}

	@Test
	public void testMeanCurvaturePropertyCotangentLaplacian() {
		CSRMatrix matrix = LMatrices.mixedCotanLaplacian(hs);
		ArrayList<Vector3f> rightHandSide = new ArrayList<Vector3f>();

		LMatrices.mult(matrix, hs, rightHandSide);
		for (Vector3f b_k : rightHandSide) {
			float H_k = Math.abs(b_k.length() / 2.0f);
			assertEquals(0.5f, H_k, 0.001f);
		}
	}
	

	@Test
	public void testSphereCurvatureDirectionCotan() {
		CSRMatrix matrix = LMatrices.mixedCotanLaplacian(hs);
		ArrayList<Vector3f> x = new ArrayList<Vector3f>();
		LMatrices.mult(matrix, hs, x);
		for (int i = 0; i < x.size(); i++) {
			Vector3f curv = new Vector3f(x.get(i));
			curv.normalize();
			curv.scale(-1f);
			Vector3f normal = new Vector3f(hs.getVertices().get(i).getPos());
			normal.normalize();
			assertTrue(curv.epsilonEquals(normal, 0.01f));
		}
	}
	
	@Test
	public void testSymmetricProperty(){
		CSRMatrix matrix = LMatrices.symmetricCotanLaplacian(hs);
		for(int k = 0; k < matrix.nRows; k++){
			ArrayList<col_val> row_k = matrix.rows.get(k);
			for(col_val tElement : row_k){
				int t = tElement.col;
				float tk = matrix.getElement(t,k);
				float kt = tElement.val;
				assertEquals(kt, tk, eps);
			}
		}
	}

}
