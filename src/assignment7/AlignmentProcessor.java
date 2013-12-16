package assignment7;

import glWrapper.GLHalfedgeStructureOld;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import openGL.MyDisplay;

import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

public class AlignmentProcessor {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
	    int mb = 1024*1024;
	         
	    //Getting the runtime reference from system
	    Runtime runtime = Runtime.getRuntime();
	    System.out.println("Total Memory:" + runtime.totalMemory() / mb);
	    System.out.println("Max Memory:" + runtime.maxMemory() / mb);
	    
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
		List<WireframeMesh> abc = processor.getAlignedMeshes();
		
		
		
		HalfEdgeStructure hs1 = new HalfEdgeStructure();
		WireframeMesh mm = ObjReader.read(objPrefix + "tiziano_disk_remeshed" + ".obj", true);
		hs1.init(mm);

		GLHalfedgeStructureOld teapot11 = new GLHalfedgeStructureOld(hs1);
		teapot11.configurePreferredShader("shaders/default.vert", 
				"shaders/default.frag", 
				null, "before");
		
		
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(abc.get(abc.size()-1));
		MyDisplay disp = new MyDisplay();
		GLHalfedgeStructureOld teapot1 = new GLHalfedgeStructureOld(hs);
		teapot1.configurePreferredShader("shaders/default.vert", 
				"shaders/default.frag", 
				null, "after");
		
		
//		disp.addToDisplay(teapot1);
//		disp.addToDisplay(teapot11);
		
		String outBase = "./processed/";
		ObjWriter.write(abc.get(0), outBase+"aaron_disk_aligned"+".obj");
		ObjWriter.write(abc.get(1), outBase+"cedric_disk_aligned"+".obj");
		ObjWriter.write(abc.get(2), outBase+"gian_disk_aligned"+".obj");
		ObjWriter.write(abc.get(3), outBase+"michael_disk_aligned"+".obj");
		ObjWriter.write(abc.get(4), outBase+"michele_disk_aligned"+".obj");
		ObjWriter.write(abc.get(5), outBase+"stefan_disk_aligned"+".obj");
		ObjWriter.write(abc.get(6), outBase+"tiziano_disk_aligned"+".obj");
	}

}
