package assignment5;

import glWrapper.GLHalfedgeStructure;
import glWrapper.GLHalfedgeStructureOld;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import openGL.MyDisplay;

import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

public class CollapseDemo {

	/**
	 * @param args
	 */
	
	private static HalfEdgeStructure hesDemo; 
	
	// ex1, task 2
	public static void randomTest(){
		WireframeMesh wf = null;
		try {
			wf = ObjReader.read("objs/bunny_ear.obj", true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HalfEdgeStructure hsToKill = new HalfEdgeStructure();
		HalfEdgeStructure hsWillLive = new HalfEdgeStructure();

		try {
			hsToKill.init(wf);
		} catch (MeshNotOrientedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DanglingTriangleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			hsWillLive.init(wf);
		} catch (MeshNotOrientedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DanglingTriangleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<Color3f> color = new ArrayList<Color3f>(Collections.nCopies(hsWillLive.getVertices().size(), new Color3f(0,0,1)));
		
		// collapse two ear edges
		HalfEdge deadEdge1 = hsToKill.getHalfEdges().get(4);
		HalfEdge deadEdge2 = hsToKill.getHalfEdges().get(5);
		//mark the halfedge on untouched object
		
		color.set(deadEdge1.end().index, new Color3f(1,0,0));
		color.set(deadEdge1.start().index, new Color3f(0,1,0));
		
		color.set(deadEdge2.end().index, new Color3f(1,0,0));
		color.set(deadEdge2.start().index, new Color3f(0,1,0));
		
		HalfEdgeCollapse collapse = new HalfEdgeCollapse(hsToKill);
		collapse.collapseEdgeAndDelete(deadEdge1);
		collapse.collapseEdgeAndDelete(deadEdge2);

		GLHalfedgeStructure glHsKill = new GLHalfedgeStructure(hsToKill);
		GLHalfedgeStructure glHsLive = new GLHalfedgeStructure(hsWillLive);
		glHsLive.addCol(color, "color");
		
		glHsKill.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom", "collapsed");
		
		glHsLive.configurePreferredShader("shaders/trimesh_flatColor3f.vert",
				"shaders/trimesh_flatColor3f.frag", "shaders/trimesh_flatColor3f.geom", "marked uncollapsed");

		MyDisplay d = new MyDisplay();
		d.addToDisplay(glHsKill);
		d.addToDisplay(glHsLive);
	}
	
	// ex1, task 3
	public static void stressTest(){
		MyDisplay display = new MyDisplay();
		WireframeMesh mesh = null;
		hesDemo = new HalfEdgeStructure();
		try {
			mesh = ObjReader.read("objs/bunny_ear.obj", true);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			hesDemo.init(mesh);
		} catch (MeshNotOrientedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DanglingTriangleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		GLHalfedgeStructure collapsedStructure = getCollapsedStructure(0.001f);
		GLHalfedgeStructure structure = new  GLHalfedgeStructure(hesDemo);
		
		structure.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom", "not collapsed");
		
		
		display.addToDisplay(structure);
		display.addToDisplay(collapsedStructure);
	}
	
	public static void main(String[] args) {
		randomTest();

	}
	
	private static GLHalfedgeStructure getCollapsedStructure(float eps){
		
		GLHalfedgeStructure structure = new  GLHalfedgeStructure(hesDemo);
		HalfEdgeCollapse collapse = new HalfEdgeCollapse(hesDemo);
		int vertexCount = structure.getNumberOfVertices();
		List<Color3f> cells = Collections.nCopies(vertexCount, new Color3f(0,1,0));
		ArrayList<Color3f> colors = new ArrayList<Color3f>(cells);
		int deadCounter = 0;
		int totalKills = 0;
		do{
			deadCounter = 0;
			for(HalfEdge he : hesDemo.getHalfEdges()){
				if(halfEdgeIsClean(collapse, he, eps)) continue;
				colors.set(he.start().index, new Color3f(1,1,0));
				colors.set(he.end().index, new Color3f(1,0,0));
				collapse.collapseEdge(he);
				deadCounter++;
			}
			totalKills += deadCounter;
		}while(deadCounter > 0);
		System.out.println("deleted this round:" + deadCounter + " in total killed:" + totalKills);
		collapse.finish();
		hesDemo.enumerateVertices();
		structure.configurePreferredShader("shaders/trimesh_flatColor3f.vert", 
				"shaders/trimesh_flatColor3f.frag", "shaders/trimesh_flatColor3f.geom", "debugger");
		structure.addCol(colors, "color");

		return structure;
	}
	
	private static boolean halfEdgeIsClean(HalfEdgeCollapse collapse, HalfEdge he, float eps){
		boolean isCollabsable = HalfEdgeCollapse.isEdgeCollapsable(he);
		boolean isCollMeshInv = collapse.isCollapseMeshInv(he, he.end().getPos());
		boolean isDeadEdge = collapse.isEdgeDead(he);
		boolean isGreaterThanEps = he.asVector().length() > eps;
		return isGreaterThanEps || isDeadEdge || isCollMeshInv || !isCollabsable;
	}

}
