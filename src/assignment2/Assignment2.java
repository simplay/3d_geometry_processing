package assignment2;

import glWrapper.GLHashtree;
import glWrapper.GLHashtreeCellAdjacencies;
import glWrapper.GLHashtreeVertexAdjacencies;
import glWrapper.GLHashtree_Vertices;
import glWrapper.GLPointCloud;
import java.io.IOException;
import openGL.MyDisplay;
import openGL.gl.GLDisplayable;
import meshes.PointCloud;
import meshes.reader.ObjReader;
import meshes.reader.PlyReader;

public class Assignment2 {

	public static void main(String[] args) throws IOException {

//		hashTreeDemo(ObjReader.readAsPointCloud("./objs/dragon.obj", true));
		hashTreeDemo(PlyReader.readPointCloud("./objs/octreeTest2.ply", true));

	}

	public static void hashTreeDemo(PointCloud pc) {
		HashOctree tree = new HashOctree(pc, 4, 1, 1f);
		MyDisplay display = new MyDisplay();
		
		GLDisplayable glPC = new GLPointCloud(pc);
		GLDisplayable glOT = new GLHashtree(tree);
		GLDisplayable hot = new GLHashtree(tree);
		GLDisplayable glOTv = new GLHashtree_Vertices(tree);
		GLDisplayable vertexAdj = new GLHashtreeVertexAdjacencies(tree);
		GLDisplayable cellAdj = new GLHashtreeCellAdjacencies(tree);
		
		glOT.configurePreferredShader("shaders/hashoctree/octree.vert",
				"shaders/hashoctree/octree.frag", "shaders/hashoctree/octree.geom", "default");

		hot.configurePreferredShader("shaders/hashoctree/octree.vert",
				"shaders/hashoctree/octreeAdj.frag", "shaders/hashoctree/octree_parent.geom", "parents");
		
		vertexAdj.configurePreferredShader("shaders/hashoctree/octree.vert",
				"shaders/hashoctree/octreeAdj.frag", "shaders/hashoctree/octree_neighbor.geom", "vert adj");
		
		cellAdj.configurePreferredShader("shaders/hashoctree/octree.vert",
				"shaders/hashoctree/octreeAdj.frag", "shaders/hashoctree/octree_neighbor.geom", "cell adj");
		
		
		display.addToDisplay(glOT);
		display.addToDisplay(glOTv);  
		display.addToDisplay(glPC);
		
		display.addToDisplay(cellAdj);
		display.addToDisplay(vertexAdj);
		display.addToDisplay(hot);
	}

}
