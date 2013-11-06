package assignment4;


import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;

import assignment2.MortonCodes;

public class Assignment4_1_Tests {
	
	// A sphere of radius 2.
	private HalfEdgeStructure hs; 
	// An ugly sphere of radius 1, don't expect the Laplacians 
	//to perform accurately on this mesh.
	private HalfEdgeStructure hs2; 
	@Before
	public void setUp(){
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
	public void testEachUniformLaplacianRowSummsUpToZero(){
		CSRMatrix matrix = LMatrices.uniformLaplacian(hs);
		for(ArrayList<col_val> row : matrix.rows){
			float sum = 0.0f;
			for(col_val element : row){
				sum += element.val;
			}
			assertEquals(0.0f, sum, eps);		
		}
	}
	
	@Test
	public void testEachMixedCotangentLaplacianRowSummsUpToZero(){
		CSRMatrix matrix = LMatrices.mixedCotanLaplacian(hs);
		// for each row of Laplacian matrix 
		for(ArrayList<col_val> row : matrix.rows){
			float sum = 0.0f;
			
			// for each element within row sum elements up
			for(col_val element : row){
				sum += element.val;
			}
			// is sum smaller given threshold eps
			assertEquals(0.0f, sum, eps);		
		}
	}
	
	@Test
	public void testMeanCurvaturePropertyCotangentLaplacian(){
		CSRMatrix matrix = LMatrices.mixedCotanLaplacian(hs);
		ArrayList<Vector3f> rightHandSide = new ArrayList<Vector3f>();
		
		LMatrices.mult(matrix, hs, rightHandSide);
		for(Vector3f b_k : rightHandSide){
			float H_k = Math.abs(b_k.length()/2.0f);
			assertEquals(0.5f, H_k, 0.001f);
		}
		
	}
	
    @Test
    public void testSphereCurvatureDirectionCotan() {
            CSRMatrix m = LMatrices.mixedCotanLaplacian(hs);
            ArrayList<Vector3f> res = new ArrayList<Vector3f>();
            LMatrices.mult(m, hs, res);
            for (int i = 0; i < res.size(); i++) {
                    Vector3f curv = new Vector3f(res.get(i));
                    curv.normalize();
                    curv.scale(-1f);
                    //asserts, that origin is the center of sphere
                    Vector3f normal = new Vector3f(hs.getVertices().get(i).getPos());
                    normal.normalize();
                    assertTrue("expected " + normal + ", but was" + curv, curv.epsilonEquals(normal, 0.01f));
            }
    }
	

}
