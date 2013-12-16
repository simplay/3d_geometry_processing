package assignment7;

import javax.vecmath.Vector3f;

import glWrapper.GLUpdatableHEStructure;
import glWrapper.GLUpdatablePointCloud;
import glWrapper.GLWireframeMeshLines;
import meshes.HalfEdgeStructure;
import meshes.PointCloud;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.LabelingDisplay;

public class LabelingDemo {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String objPrefix = "./objs/";
		String modelName = "aaron_disk_remeshed";

		WireframeMesh m = ObjReader.read(objPrefix + modelName + ".obj", true);

		labelingDemo(m);

	}

	/**
	 * Open a labeling display with a mesh loaded.
	 * 
	 * @param m
	 * 
	 * @throws Exception
	 */
	private static void labelingDemo(WireframeMesh m)
			throws Exception {
		/**
		 * empty point cloud. used to mark currently selected and already
		 * labeled vertices
		 */
		PointCloud pc = new PointCloud();
		GLUpdatablePointCloud glPC = new GLUpdatablePointCloud(pc);
		

		/**
		 * This is used to render the triangle edges
		 */
		GLWireframeMeshLines glLines = new GLWireframeMeshLines(m,
				new Vector3f(.0f, .0f, .0f));
		glLines.configurePreferredShader("shaders/line.vert",
				"shaders/line.frag", null);

		/**
		 * HES, the labeling is done directly on the HS vertices
		 */
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(m);
		GLUpdatableHEStructure glHE = new GLUpdatableHEStructure(hs);

		/**
		 * Labeling processor instance, cares about the labeling
		 */
		LabelingProcessor lp = new LabelingProcessor(hs, glHE, pc);

		/**
		 * add everything to the labeling display
		 */
		LabelingDisplay disp = new LabelingDisplay();

		disp.add(glHE, lp);
		disp.add(glLines);
		disp.add(glPC);
	}
}
