package assignment7;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

public class AlignmentProcessor {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String objPrefix = "./objs/";
		String labelPrefix = "./labels/";
		List<WireframeMesh> meshes = new LinkedList<WireframeMesh>();
		List<LinkedList<Vertex>> featureLists = new LinkedList<LinkedList<Vertex>>();
		
		WireframeMesh m = ObjReader.read(objPrefix + "peter" + ".obj", true);
		meshes.add(m);
		
		PostprocessAlignment processor = new PostprocessAlignment(null, null);
		List<WireframeMesh> alignedMeshes = processor.getAlignedMeshes();
		
	}

}
