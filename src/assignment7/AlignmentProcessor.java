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

		// outputting stuff
		String outBase = "./processed/";
		ObjWriter.write(abc.get(0), outBase+"aaron_disk_aligned"+".obj");
		ObjWriter.write(abc.get(1), outBase+"cedric_disk_aligned"+".obj");
		ObjWriter.write(abc.get(2), outBase+"gian_disk_aligned"+".obj");
		ObjWriter.write(abc.get(3), outBase+"michael_disk_aligned"+".obj");
		ObjWriter.write(abc.get(4), outBase+"michele_disk_aligned"+".obj");
		ObjWriter.write(abc.get(5), outBase+"stefan_disk_aligned"+".obj");
		ObjWriter.write(abc.get(6), outBase+"tiziano_disk_aligned"+".obj");
		
		// visual debugging - just in case
		MyDisplay disp = new MyDisplay();
		HalfEdgeStructure hes = new HalfEdgeStructure();
		WireframeMesh wm = ObjReader.read("processed/aaron_disk_aligned.obj", true);
		hes.init(wm);
		GLHalfedgeStructureOld aaron_a = new GLHalfedgeStructureOld(hes);
		aaron_a.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom", "aaron");
		
		HalfEdgeStructure hes2 = new HalfEdgeStructure();
		WireframeMesh wm2 = ObjReader.read("processed/cedric_disk_aligned.obj", true);
		hes2.init(wm2);
		GLHalfedgeStructureOld cedric_a = new GLHalfedgeStructureOld(hes2);
		cedric_a.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom", "cedric");
		
		HalfEdgeStructure hes3 = new HalfEdgeStructure();
		WireframeMesh wm3 = ObjReader.read("processed/gian_disk_aligned.obj", true);
		hes3.init(wm3);
		GLHalfedgeStructureOld gian_a = new GLHalfedgeStructureOld(hes3);
		gian_a.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom", "gian");
		
		HalfEdgeStructure hes4 = new HalfEdgeStructure();
		WireframeMesh wm4 = ObjReader.read("processed/michael_disk_aligned.obj", true);
		hes4.init(wm4);
		GLHalfedgeStructureOld michael_a = new GLHalfedgeStructureOld(hes4);
		michael_a.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom", "michael");
		
		HalfEdgeStructure hes5 = new HalfEdgeStructure();
		WireframeMesh wm5 = ObjReader.read("processed/michele_disk_aligned.obj", true);
		hes5.init(wm5);
		GLHalfedgeStructureOld michele_a = new GLHalfedgeStructureOld(hes5);
		michele_a.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom", "michele");
		
		HalfEdgeStructure hes6 = new HalfEdgeStructure();
		WireframeMesh wm6 = ObjReader.read("processed/stefan_disk_aligned.obj", true);
		hes6.init(wm6);
		GLHalfedgeStructureOld stefan_a = new GLHalfedgeStructureOld(hes6);
		stefan_a.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom", "stefan");
		
		HalfEdgeStructure hes7 = new HalfEdgeStructure();
		WireframeMesh wm7 = ObjReader.read("processed/tiziano_disk_aligned.obj", true);
		hes7.init(wm7);
		GLHalfedgeStructureOld tiziano_a = new GLHalfedgeStructureOld(hes7);
		tiziano_a.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom", "tiziano");
		
		
		disp.addToDisplay(aaron_a);
		disp.addToDisplay(cedric_a);
		disp.addToDisplay(gian_a);
		disp.addToDisplay(michael_a);
		disp.addToDisplay(michele_a);
		disp.addToDisplay(stefan_a);
		disp.addToDisplay(tiziano_a);
	}

}
