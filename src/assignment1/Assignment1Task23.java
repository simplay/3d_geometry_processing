package assignment1;

import glWrapper.GLHalfedgeStructure;
import java.io.IOException;
import openGL.MyDisplay;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

/**
 * 
 * @author simplay
 *
 */
public class Assignment1Task23 {
	public static boolean checkIterators = false;
	
	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
//		WireframeMesh m = ObjReader.read("./objs/oneNeighborhood.obj", true);
//		WireframeMesh m = ObjReader.read("./objs/cat.obj", true);
		WireframeMesh m = ObjReader.read("./objs/dragon.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();

		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		MyDisplay disp = new MyDisplay();
		GLHalfedgeStructure teapot1 = new GLHalfedgeStructure(hs);
		GLHalfedgeStructure teapot2 = new GLHalfedgeStructure(hs);
		GLHalfedgeStructure teapot3 = new GLHalfedgeStructure(hs);
		GLHalfedgeStructure teapot4 = new GLHalfedgeStructure(hs);
		GLHalfedgeStructure teapot5 = new GLHalfedgeStructure(hs);
		
		//choose the shader for the data
		teapot1.configurePreferredShader("shaders/default.vert", 
				"shaders/default.frag", 
				null, "default");
		
		teapot2.configurePreferredShader("shaders/valence.vert", 
				"shaders/valence.frag", 
				null, "valence");
		
		teapot3.configurePreferredShader("shaders/smoothing.vert", 
				"shaders/smoothing.frag", 
				null, "smoothing");
		
		teapot4.configurePreferredShader("shaders/normalsShader.vert", 
				"shaders/normalsShader.frag", 
				null, "normals");
		
		teapot5.configurePreferredShader("shaders/curvature.vert", 
				"shaders/curvature.frag", 
				null, "curvature");
		
		//add the data to the display
		disp.addToDisplay(teapot1);
		disp.addToDisplay(teapot2);
		disp.addToDisplay(teapot3);
		disp.addToDisplay(teapot4);
		disp.addToDisplay(teapot5);
		
	}
}
