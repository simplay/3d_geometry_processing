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
		WireframeMesh wf = null;
		try {
			wf = ObjReader.read("objs/buddha.obj", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		HalfEdgeStructure hs = new HalfEdgeStructure();

		try {
			hs.init(wf);
		} catch (MeshNotOrientedException e) {
			e.printStackTrace();
		} catch (DanglingTriangleException e) {
			e.printStackTrace();
		}
		float epsilon = 0.0001f;
		
		GLHalfedgeStructure glBeforeCollapse = getCollapsedStructure(hs, epsilon);
		GLHalfedgeStructure glAfterCollapse = new GLHalfedgeStructure(hs);
		
		
		glAfterCollapse.configurePreferredShader(
				"shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom", "uncollapsed");


		MyDisplay d = new MyDisplay();
		d.addToDisplay(glAfterCollapse);
		d.addToDisplay(glBeforeCollapse);
	}
	
	public static void main(String[] args) {
//		randomTest();
		stressTest();
	}
	
	private static GLHalfedgeStructure getCollapsedStructure(HalfEdgeStructure hes, float eps){
		GLHalfedgeStructure structure = new  GLHalfedgeStructure(hes);
		HalfEdgeCollapse collapse = new HalfEdgeCollapse(hes);
		int vertexCount = structure.getNumberOfVertices();
		List<Color3f> cells = Collections.nCopies(vertexCount, new Color3f(0,1,0));
		ArrayList<Color3f> colors = new ArrayList<Color3f>(cells);
		int deadCounter;
		int totalKills = 0;
		System.out.println("edges in total" + hes.getHalfEdges().size());
		System.out.println("starting collapse process ...");
		do{
			deadCounter = 0;
			System.out.println(hes.getHalfEdges().size());
			for(HalfEdge edge : hes.getHalfEdges()){
				
				if(halfEdgeIsClean(collapse, edge, eps)){
					continue;
				}else{
					colors.set(edge.start().index, new Color3f(1,1,0));
					colors.set(edge.end().index, new Color3f(1,0,0));
					collapse.collapseEdge(edge);
					deadCounter++;
				}
			}
			totalKills += deadCounter;
			System.out.println("edges killed this round:" + deadCounter + " in total killed:" + totalKills);
		}while(deadCounter > 0);
		
		collapse.finish();
		structure.configurePreferredShader("shaders/trimesh_flatColor3f.vert", 
				"shaders/trimesh_flatColor3f.frag", "shaders/trimesh_flatColor3f.geom", "collapsed");
		structure.addCol(colors, "color");

		return structure;
	}
	
	private static boolean halfEdgeIsClean(HalfEdgeCollapse collapse, HalfEdge edge, float eps){
		boolean statement = edge.toSEVector().length() > eps || 
		collapse.isEdgeDead(edge) || 
		collapse.isCollapseMeshInv(edge, edge.end().getPos()) || 
		!HalfEdgeCollapse.isEdgeCollapsable(edge);
		return statement;
	}

}
