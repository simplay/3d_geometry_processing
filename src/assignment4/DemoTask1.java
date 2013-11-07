package assignment4;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import openGL.MyDisplay;
import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import sparse.CSRMatrix;
import glWrapper.GLHalfedgeStructure;

public class DemoTask1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WireframeMesh m = null;
		try {
			m = ObjReader.read("objs/dragon.obj", false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HalfEdgeStructure hs1 = new HalfEdgeStructure();
		try {
			hs1.init(m);
		} catch (MeshNotOrientedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DanglingTriangleException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			m = ObjReader.read("objs/sphere.obj", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HalfEdgeStructure hs2 = new HalfEdgeStructure();
		try {
			hs2.init(m);
		} catch (MeshNotOrientedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DanglingTriangleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HalfEdgeStructure[] hsArray = new HalfEdgeStructure[] { hs1, hs2 };
		MyDisplay d = new MyDisplay();

		for (HalfEdgeStructure hs : hsArray) {
			CSRMatrix mMixed = LMatrices.mixedCotanLaplacian(hs);
			CSRMatrix mUniform = LMatrices.uniformLaplacian(hs);
			CSRMatrix[] laplacians = new CSRMatrix[] { mUniform, mMixed };
			for (CSRMatrix laplacian : laplacians) {
				ArrayList<Vector3f> curvatures = new ArrayList<Vector3f>();
				LMatrices.mult(laplacian, hs, curvatures);

				// ArrayList<Vector3f> curvatures = new ArrayList<Vector3f>();

				ArrayList<Tuple3f> curvaturesTuple = new ArrayList<Tuple3f>();

				for (Vector3f t : curvatures) {

					curvaturesTuple.add(t);

				}

				GLHalfedgeStructure glHs = new GLHalfedgeStructure(hs);
				// glHs.add(curvatures, "curvature");

				glHs.add(curvaturesTuple, "curvature");

				// And show off...

//				glHs.configurePreferredShader("shaders/curvature_arrows.vert",
//						"shaders/curvature_arrows.frag",
//						"shaders/curvature_arrows.geom");
//				
				
				glHs.configurePreferredShader("shaders/curvNew.vert", 
						"shaders/curvNew.frag", 
						"shaders/curvNew.geom", "curvature");
				
				
				d.addToDisplay(glHs);
			}
			// And show off...
			GLHalfedgeStructure glMesh = new GLHalfedgeStructure(hs);
			glMesh.configurePreferredShader("shaders/trimesh_flat.vert",
					"shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
			d.addToDisplay(glMesh);
		}

	}

}
