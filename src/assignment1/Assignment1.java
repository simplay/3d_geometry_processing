package assignment1;

import glWrapper.GLHalfedgeStructure;
import glWrapper.GLWireframeMesh;

import java.io.IOException;
import java.util.ArrayList;
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
 * @author Alf
 *
 */
public class Assignment1 {

	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/oneNeighborhood.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		
		//create a half-edge structure out of the wireframe description.
		//As not every mesh can be represented as a half-edge structure
		//exceptions could occur.
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		MyDisplay disp = new MyDisplay();
		GLHalfedgeStructure teapot = new GLHalfedgeStructure(hs);
		//choose the shader for the data
		teapot.configurePreferredShader("shaders/default.vert", 
				"shaders/default.frag", 
				null);
		
		//add the data to the display
		disp.addToDisplay(teapot);
		
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
		
		
		
		
		
//		Vertex v0 = hs.getVertices().get(0);
//		HalfEdge he = v0.getHalfEdge();
		
//		System.out.println("vert_"+ v0.index);
//		
//		he = he.getNext().getOpposite();
//		System.out.println(he.start().index);
//		
//		he = he.getNext().getOpposite();
//		System.out.println(he.start().index);
//		
//		he = he.getNext().getOpposite();
//		System.out.println(he.start().index);
//		
//		he = he.getNext().getOpposite();
//		System.out.println(he.start().index);
//		
//		he = he.getNext().getOpposite();
//		System.out.println(he.start().index);
//		
//		he = he.getNext().getOpposite();
//		System.out.println(he.start().index);
	
		Vertex v0 = hs.getVertices().get(0);
		System.out.println("vertex_"+v0.index);
		Vertex tmp = null;
		HalfEdge he = v0.getHalfEdge();
		
		System.out.println(he.end().index);
		do{
			he = he.getOpposite().getNext();
			tmp = he.end();
			System.out.println(tmp.index);
		}while(v0 != tmp.getHalfEdge().end() );
		
//		while(v0 != tmp.getHalfEdge().start()){
//			he = he.getOpposite().getNext();
//			tmp = he.end();
//			System.out.println(tmp.index);
//		}
		

//		System.out.println("\n");
//		System.out.println("Iterate over vertices");
//		for(Vertex v : hs.getVertices()){
//			System.out.println("neighb vertex_"+v.index);
//			Iterator<Vertex> vVVIter = v.iteratorVV();
//			while(vVVIter.hasNext()){
//				Vertex _v = vVVIter.next();
//				System.out.println("vertex_" + _v.index + " " + _v.getPos());
//			}
//		}
		
	}
	

}
