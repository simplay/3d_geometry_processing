package assignment5;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Color3f;

import glWrapper.GLHalfedgeStructure;
import openGL.MyDisplay;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

public class QSlimDemo {

	public static void main(String[] args) throws Exception{
		int vertexCount = 50;
		WireframeMesh wf = ObjReader.read("objs/dragon.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);
		GLHalfedgeStructure originalMesh = new GLHalfedgeStructure(hs);
		QSlim qs = new QSlim(hs);
		qs.simplify(vertexCount);
		
		MyDisplay d = new MyDisplay();
		GLHalfedgeStructure collapsedStructure = new GLHalfedgeStructure(hs);
		collapsedStructure.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom", "reduced");

		d.addToDisplay(collapsedStructure);
		
		Color3f defaultColor = new Color3f(0,0,1);
		int vertexCounts = originalMesh.getNumberOfVertices();
		List<Color3f> container = Collections.nCopies(vertexCounts, defaultColor);
		ArrayList<Color3f> color = new ArrayList<Color3f>(container);
		originalMesh.configurePreferredShader("shaders/trimesh_flatColor3f.vert",
				"shaders/trimesh_flatColor3f.frag", 
				"shaders/trimesh_flatColor3f.geom", "original");
		originalMesh.addCol(color, "color");
		d.addToDisplay(originalMesh);
	}
}
