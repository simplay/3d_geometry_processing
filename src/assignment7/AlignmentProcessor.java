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
		LinkedList<Features> featureLists = new LinkedList<Features>();
		
		WireframeMesh m = ObjReader.read(objPrefix + "aaron_disk_remeshed" + ".obj", true);
		meshes.add(m);
		m = ObjReader.read(objPrefix + "cedric_disk_remeshed" + ".obj", true);
		meshes.add(m);
		m = ObjReader.read(objPrefix + "gian_disk_remeshed" + ".obj", true);
		meshes.add(m);
		m = ObjReader.read(objPrefix + "michael_disk_remeshed" + ".obj", true);
		meshes.add(m);
		m = ObjReader.read(objPrefix + "michele_disk_remeshed" + ".obj", true);
		meshes.add(m);
		m = ObjReader.read(objPrefix + "stefan_disk_remeshed" + ".obj", true);
		meshes.add(m);
		m = ObjReader.read(objPrefix + "tiziano_disk_remeshed" + ".obj", true);
		meshes.add(m);
		
		Features f = new Features(labelPrefix + "aaron_disk_remeshed" + ".lab");
		featureLists.add(f);
		f = new Features(labelPrefix + "cedric_disk_remeshed" + ".lab");
		featureLists.add(f);
		f = new Features(labelPrefix + "gian_disk_remeshed" + ".lab");
		featureLists.add(f);
		f = new Features(labelPrefix + "michael_disk_remeshed" + ".lab");
		featureLists.add(f);
		f = new Features(labelPrefix + "michele_disk_remeshed" + ".lab");
		featureLists.add(f);
		f = new Features(labelPrefix + "stefan_disk_remeshed" + ".lab");
		featureLists.add(f);
		f = new Features(labelPrefix + "tiziano_disk_remeshed" + ".lab");
		featureLists.add(f);
		
		PostprocessAlignment processor = new PostprocessAlignment(meshes, featureLists);
//		List<WireframeMesh> alignedMeshes = processor.getAlignedMeshes();
		
	}

}
