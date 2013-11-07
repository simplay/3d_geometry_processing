package assignment4;

import glWrapper.GLHalfedgeStructure;
import glWrapper.GLHalfedgeStructureOld;

import java.io.IOException;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import openGL.gl.GLDisplayable;
import sparse.CSRMatrix;

public class Assignment4_2_smoothing {

	public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException {
		MyDisplay d = new MyDisplay();
		WireframeMesh mesh1 = ObjReader.read("objs/bunny5k.obj", true);
		WireframeMesh mesh2 = ObjReader.read("objs/bunny5k.obj", true);
		WireframeMesh mesh3 = ObjReader.read("objs/bunny5k.obj", true);
		
		HalfEdgeStructure hs1 = new HalfEdgeStructure();
		HalfEdgeStructure hs2 = new HalfEdgeStructure();
		HalfEdgeStructure hs3 = new HalfEdgeStructure();
		
		hs1.init(mesh1);
		hs2.init(mesh2);
		hs3.init(mesh3);
		
		CSRMatrix mat1 = LMatrices.mixedCotanLaplacian(hs1);
		CSRMatrix mat2 = LMatrices.mixedCotanLaplacian(hs2);
		
		ImplicitSmoother.smooth(hs1, mat1, 0.1f);
		ImplicitSmoother.unsharpMasking(hs2, mat2, 0.1f, 2.5f);
		
		GLDisplayable glHeSSmooth = new GLHalfedgeStructure(hs1);
		GLDisplayable glHeSSharp = new GLHalfedgeStructure(hs2);
		GLHalfedgeStructureOld glHeS = new GLHalfedgeStructureOld(hs3);
		
		glHeSSmooth.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom", "smooth");
			
		glHeSSharp.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom", "unsharp");
			
		glHeS.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom", "original");
						
		d.addToDisplay(glHeSSmooth);
		d.addToDisplay(glHeSSharp);
		d.addToDisplay(glHeS);
	}
}
