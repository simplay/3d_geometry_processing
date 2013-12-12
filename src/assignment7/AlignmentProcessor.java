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
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String objPrefix = "./objs/";
		String labelPrefix = "./labels/";
		List<WireframeMesh> meshes = new LinkedList<WireframeMesh>();
		List<LinkedList<Vertex>> featureLists = new LinkedList<LinkedList<Vertex>>();
		
		WireframeMesh m = ObjReader.read(objPrefix + "peter" + ".obj", true);
		meshes.add(m);
		
	}

}
