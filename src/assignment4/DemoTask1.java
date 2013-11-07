package assignment4;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import openGL.MyDisplay;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import sparse.CSRMatrix;
import glWrapper.GLHalfedgeStructure;
import glWrapper.GLHalfedgeStructureOld;

public class DemoTask1 {

	public static void main(String[] args) {
		WireframeMesh m = null;
		try {
			m = ObjReader.read("objs/dragon.obj", false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		HalfEdgeStructure hs1 = new HalfEdgeStructure();
		try {
			hs1.init(m);
		} catch (MeshNotOrientedException e1) {
			e1.printStackTrace();
		} catch (DanglingTriangleException e1) {
			e1.printStackTrace();
		}

		try {
			m = ObjReader.read("objs/sphere.obj", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		HalfEdgeStructure hs2 = new HalfEdgeStructure();
		try {
			hs2.init(m);
		} catch (MeshNotOrientedException e) {
			e.printStackTrace();
		} catch (DanglingTriangleException e) {
			e.printStackTrace();
		}
		
		MyDisplay d = new MyDisplay();
		
		ArrayList<Vector3f> curvatures11 = new ArrayList<Vector3f>();
		ArrayList<Vector3f> curvatures12 = new ArrayList<Vector3f>();
		ArrayList<Vector3f> curvatures21 = new ArrayList<Vector3f>();
		ArrayList<Vector3f> curvatures22 = new ArrayList<Vector3f>();
		ArrayList<Tuple3f> curvaturesTuple11 = new ArrayList<Tuple3f>();
		ArrayList<Tuple3f> curvaturesTuple12 = new ArrayList<Tuple3f>();
		ArrayList<Tuple3f> curvaturesTuple21 = new ArrayList<Tuple3f>();
		ArrayList<Tuple3f> curvaturesTuple22 = new ArrayList<Tuple3f>();
		
		GLHalfedgeStructure glHs11 = new GLHalfedgeStructure(hs1);
		GLHalfedgeStructure glHs12 = new GLHalfedgeStructure(hs1);
		GLHalfedgeStructure glHs21 = new GLHalfedgeStructure(hs2);
		GLHalfedgeStructure glHs22 = new GLHalfedgeStructure(hs2);
		GLHalfedgeStructureOld glMesh1 = new GLHalfedgeStructureOld(hs1);
		GLHalfedgeStructureOld glMesh2 = new GLHalfedgeStructureOld(hs2);
		
		CSRMatrix mMixed1 = LMatrices.mixedCotanLaplacian(hs1);
		CSRMatrix mUniform1 = LMatrices.uniformLaplacian(hs1);
		CSRMatrix mMixed2 = LMatrices.mixedCotanLaplacian(hs2);
		CSRMatrix mUniform2 = LMatrices.uniformLaplacian(hs2);
		
		LMatrices.mult(mMixed1, hs1, curvatures11);
		LMatrices.mult(mUniform1, hs1, curvatures12);
		LMatrices.mult(mMixed2, hs2, curvatures21);
		LMatrices.mult(mUniform2, hs2, curvatures22);
		
		for (Vector3f t : curvatures11) curvaturesTuple11.add(t);
		for (Vector3f t : curvatures12) curvaturesTuple12.add(t);
		for (Vector3f t : curvatures21) curvaturesTuple21.add(t);		
		for (Vector3f t : curvatures22) curvaturesTuple22.add(t);
		
		glHs11.add(curvaturesTuple11, "curvature");
		glHs12.add(curvaturesTuple12, "curvature");
		glHs21.add(curvaturesTuple21, "curvature");
		glHs22.add(curvaturesTuple22, "curvature");
		
		
		glHs11.configurePreferredShader("shaders/curvNew.vert", 
				"shaders/curvNew.frag", 
				"shaders/curvNew.geom", "curvature11");
		
		glHs12.configurePreferredShader("shaders/curvNew.vert", 
				"shaders/curvNew.frag", 
				"shaders/curvNew.geom", "curvature12");
		
		glHs21.configurePreferredShader("shaders/curvNew.vert", 
				"shaders/curvNew.frag", 
				"shaders/curvNew.geom", "curvature21");
		
		glHs22.configurePreferredShader("shaders/curvNew.vert", 
				"shaders/curvNew.frag", 
				"shaders/curvNew.geom", "curvature22");
		
		glMesh1.configurePreferredShader("shaders/curvNew.vert", 
				"shaders/curvNew.frag", 
				"shaders/curvNew.geom", "default1");
		
		glMesh2.configurePreferredShader("shaders/curvNew.vert", 
				"shaders/curvNew.frag", 
				"shaders/curvNew.geom", "default2");
		
		d.addToDisplay(glHs11);
		d.addToDisplay(glHs12);
		d.addToDisplay(glHs21);
		d.addToDisplay(glHs22);
		d.addToDisplay(glMesh1);
		d.addToDisplay(glMesh2);
	}

}
