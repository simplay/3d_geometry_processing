package assignment1;

import glWrapper.GLHalfedgeStructureOld;

import java.io.IOException;
import java.util.Iterator;
import openGL.MyDisplay;
import meshes.Face;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

/**
 * 
 * @author simplay
 *
 */
public class Assignment1Task1 {
	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/oneNeighborhood.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		MyDisplay disp = new MyDisplay();
		GLHalfedgeStructureOld teapot1 = new GLHalfedgeStructureOld(hs);

		
		//choose the shader for the data
		teapot1.configurePreferredShader("shaders/default.vert", 
				"shaders/default.frag", 
				null, "default");

		
		//add the data to the display
		disp.addToDisplay(teapot1);

		checkIterators(hs);
		
		
	}
	
	public static void checkIterators(HalfEdgeStructure hs){
		System.out.println("Iterate over faces");
		int t = 0;
		for(Face f : hs.getFaces()){
			System.out.println("face_"+t);
			Iterator<Vertex> fFVIter = f.iteratorFV();
			while(fFVIter.hasNext() ){
				Vertex v = fFVIter.next();
				System.out.println("vertex_" + v.index + " " + v.getPos());
			}
			System.out.println();
			
			Iterator<HalfEdge> fFEIter = f.iteratorFE();
			while(fFEIter.hasNext() ){
				HalfEdge he = fFEIter.next();
				System.out.println("edge_" + he.toString());
			}
			
			System.out.println();
			
			t++;
		}
	
		System.out.println("\n");
		System.out.println("Iterate over vertices");
		
		for(Vertex v : hs.getVertices()){
			System.out.println("vertex neighborhood around vertex_"+v.index);
			Iterator<Vertex> vVVIter = v.iteratorVV();
			while(vVVIter.hasNext()){
				Vertex _v = vVVIter.next();
				System.out.println("vertex_" + _v.index + " " + _v.getPos());
			}
			
			System.out.println();
			System.out.println("halfedge neighborhood around vertex_"+v.index);
			Iterator<HalfEdge> vVEIter = v.iteratorVE();
			while(vVEIter.hasNext()){
				HalfEdge _he = vVEIter.next();
				System.out.println("edge_" + _he);
			}
			System.out.println();
			System.out.println("face neighborhood around vertex_"+v.index);
			Iterator<Face> vVFIter = v.iteratorVF();
			while(vVFIter.hasNext()){
				Face _f = vVFIter.next();
				System.out.println("face_" + _f);
			}
			System.out.println();
		}
	}
	

}
