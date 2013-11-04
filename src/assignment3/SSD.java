package assignment3;

import glWrapper.GLHashtree;
import glWrapper.GLWireframeMesh;
import java.io.IOException;
import java.util.ArrayList;
import openGL.MyDisplay;
import assignment2.HashOctree;
import sparse.LinearSystem;
import sparse.SCIPY;
import meshes.PointCloud;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import meshes.reader.PlyReader;

public class SSD {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ssdMarchingCube();
		} catch (MeshNotOrientedException e) {
			e.printStackTrace();
		} catch (DanglingTriangleException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	static private int flag = 3;
	
	public static void ssdMarchingCube() throws MeshNotOrientedException, DanglingTriangleException, IOException{
		PointCloud pc = null;
		if(flag == 0)  pc = ObjReader.readAsPointCloud("objs/oneNeighborhood.obj", true);
		else if(flag == 2) pc = ObjReader.readAsPointCloud("objs/teapot.obj", true);
		else pc = PlyReader.readPointCloud("objs/angel_points.ply", true);
		
		MyDisplay display = new MyDisplay();
		
		pc.normalizeNormals();
		HashOctree tree = new HashOctree(pc, 7, 1, 1.3f);
		tree.refineTree(3);
		
		float lambda_0 = 10f;
		float lambda_1 = 0.001f;
		float lambda_R = 100;
		
		LinearSystem linSystem = SSDMatrices.ssdSystem(tree, pc, lambda_0, lambda_1, lambda_R);
		
		ArrayList<Float> functionValues_vector = new ArrayList<Float>();
		SCIPY.solve(linSystem, "", functionValues_vector);
		
		MarchingCubes mc = new MarchingCubes(tree);
		mc.dualMC(functionValues_vector);
		WireframeMesh mesh = mc.result;
		GLWireframeMesh glMesh = new GLWireframeMesh(mesh);
		GLHashtree gltree = new GLHashtree(tree);
		
		
		glMesh.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom", "ssd");

		gltree.addFunctionValues(functionValues_vector);
		gltree.configurePreferredShader("shaders/octree_zro.vert", 
				"shaders/octree_zro.frag", "shaders/octree_zro.geom", "comp");
		
		display.addToDisplay(glMesh);
		display.addToDisplay(gltree);
	}

}
