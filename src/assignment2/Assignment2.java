package assignment2;

import glWrapper.GLHashOctree;
import glWrapper.GLHashtree;
import glWrapper.GLHashtree_Vertices;
import glWrapper.GLPointCloud;

import java.io.IOException;

import openGL.MyDisplay;

import meshes.PointCloud;
import meshes.reader.ObjReader;
import meshes.reader.PlyReader;

public class Assignment2 {

	public static void main(String[] args) throws IOException {

		// these demos will run once all methods in the MortonCodes class are
		// implemented.
		hashTreeDemo(ObjReader.readAsPointCloud("./objs/dragon.obj", true));
//		hashTreeDemo(PlyReader.readPointCloud("./objs/octreeTest2.ply", true));

	}

	public static void hashTreeDemo(PointCloud pc) {

		HashOctree tree = new HashOctree(pc, 4, 1, 1f);
		MyDisplay display = new MyDisplay();
		GLPointCloud glPC = new GLPointCloud(pc);
		GLHashtree glOT = new GLHashtree(tree);

		glOT.configurePreferredShader("shaders/hashoctree/octree.vert",
				"shaders/hashoctree/octree.frag", "shaders/hashoctree/octree.geom");
		

		GLHashtree_Vertices glOTv = new GLHashtree_Vertices(tree);
		
		GLHashOctree hot = new GLHashOctree(tree);
		hot.configurePreferredShader("shaders/hashoctree/octree.vert",
				"shaders/hashoctree/octree.frag", "shaders/hashoctree/octree_parent.geom", "parents");
		
		display.addToDisplay(glOT);
		display.addToDisplay(glOTv);  
		display.addToDisplay(glPC);
		display.addToDisplay(hot);

	}

	/**
	 * Prints binary representation of given long input
	 * 
	 * @param code
	 *            long which should be printed in its binary representation
	 */
	public static void displayToBin(long code) {
		System.out.println(Long.toString(code, 2));
	}
}
