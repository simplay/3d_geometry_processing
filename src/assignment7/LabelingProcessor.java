package assignment7;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import openGL.LabelingDisplay;

import glWrapper.GLUpdatableHEStructure;
import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.PointCloud;
import meshes.Vertex;

public class LabelingProcessor {

	private HalfEdgeStructure hs;
	private GLUpdatableHEStructure glHE;
	// colors to highlight selected region
	HEData3d colors;
	private Tuple3f selectedFaceColor = new Vector3f(1f, 1f, 0.2f);
	private Tuple3f stdFaceColor = new Vector3f(0.6f, 0.6f, 0.6f);
	private Tuple3f stdVertexColor = new Vector3f(1f, .0f, 0.f);
	private Tuple3f selectedVertexColor = new Vector3f(0.0f, 0.0f, 1.f);
	private PointCloud pc;
	private int nLabels;
	private LabelingDisplay display;
	private Vertex selectedVertex;

	// here the vertices and their labels are stored
	HashMap<Vertex, Label> vertexLabels;

	/**
	 * initializes all vertices with null labels and std color
	 * 
	 * @return
	 */
	private void initVertexLabels() {
		for (Vertex v : hs.getVertices()) {
			colors.put(v, stdFaceColor);
			vertexLabels.put(v, null);
		}
	}
	
	/**
	 * update the label name in the gui based on the selected vertex.
	 * additionally, enable/disable labeling buttons
	 * @param v
	 */
	private void updateLabelField(Vertex v) {
		if (v == null) {
			display.updateLabelField("no vertex selected");
			display.setButtonsActive(false);
		} else {
			display.setButtonsActive(true);
			if (vertexLabels.get(v) != null)
				display.updateLabelField(vertexLabels.get(v).getName());
			else
				display.updateLabelField("");
		}
	}

	/**
	 * dereferences selected vertex and updates gui
	 * 
	 * @param display
	 */
	private void deselect() {
		selectedVertex = null;

		// update gui
		updatePC();
		display.updatePC(pc);
		display.updateLabelField("");
		display.setButtonsActive(false);
		markSelectedRegion();
	}

	/**
	 * tests if the label is not already used for another vertex
	 * 
	 * @param label
	 * @param v
	 * @return
	 */
	private boolean unique(Label label, Vertex v) {
		boolean unique = true;
		for (Vertex aV : vertexLabels.keySet()) {
			Label l = vertexLabels.get(aV);
			// ignore current vertex and unlabeled vertices
			if (!aV.equals(v) && l != null) {
				// unlabeled ignored
				if (l.getName().equals(label.getName()))
					unique = false;
			}
		}

		return unique;
	}

	/**
	 * calculates distance between the two vertices
	 * 
	 * @param pos
	 * @param v
	 * @return
	 */
	private float dist(Vector3f pos, Vertex v) {
		Vector3f tmp = new Vector3f(pos);
		tmp.sub(new Vector3f(v.getPos().x, v.getPos().y, v.getPos().z));
		return tmp.length();
	}

	/**
	 * counts the number of non-null label entries in vertexLabels
	 * 
	 * @param display
	 */
	private void updateNLabels() {
		nLabels = 0;
		for (Vertex v : vertexLabels.keySet()) {
			if (vertexLabels.get(v) != null)
				nLabels++;
		}
		display.updateNLabels(nLabels);
	}

	/**
	 * updates the pointcloud instance: clear and add all labeled and also the
	 * current selected
	 */
	private void updatePC() {
		pc.points.clear();
		pc.normals.clear();
		// add all which have been labeled so far
		for (Vertex v : vertexLabels.keySet()) {
			if (vertexLabels.get(v) != null) {
				pc.points.add(v.getPos());
				pc.normals.add(new Vector3f(stdVertexColor));
			}
		}

		// add current selection too
		if (selectedVertex != null) {
			if (pc.points.contains(selectedVertex.getPos())) {
				int idx = pc.points.indexOf(selectedVertex.getPos());
				pc.normals.get(idx).x = selectedVertexColor.x;
				pc.normals.get(idx).y = selectedVertexColor.y;
				pc.normals.get(idx).z = selectedVertexColor.z;
			} else {
				pc.points.add(selectedVertex.getPos());
				pc.normals.add(new Vector3f(selectedVertexColor));
			}
		}
	}

	/**
	 * mark currently selected vertex additionally
	 */
	private void markSelectedRegion() {
		if (selectedVertex == null) {
			for (Vertex vert : hs.getVertices()) {
				colors.put(vert, stdFaceColor);
			}
		} else {
			for (Vertex vert : hs.getVertices()) {
				Vector3f distV = new Vector3f(selectedVertex.getPos());
				distV.sub(new Vector3f(vert.getPos()));
				if (distV.length() < .1f) {
					// linearly interpolate between 0 and 0.1
					float t = 1 - (distV.length()/0.1f);
					Vector3f cCenter = new Vector3f(selectedFaceColor);
					cCenter.scale(t);
					Vector3f cBorder = new Vector3f(stdFaceColor);
					cBorder.scale(1.f - t);
					cCenter.add(cBorder);
					colors.put(vert, cCenter);
				}
				else
					colors.put(vert, stdFaceColor);
			}
		}

		glHE.update("color");

	}

	public void setDisplay(LabelingDisplay display) {
		this.display = display;
	}
	
	public LabelingProcessor(HalfEdgeStructure hs, GLUpdatableHEStructure glHE,
			PointCloud pc) {
		this.nLabels = 0;
		this.hs = hs;
		this.glHE = glHE;
		this.pc = pc;

		// initialize colors and labels
		this.colors = new HEData3d(hs);
		this.vertexLabels = new HashMap<Vertex, Label>();

		initVertexLabels();

		glHE.add(colors, "color");

	}

	/**
	 * get vertex in glHES which is closest to @param v
	 * 
	 * @return
	 */
	public Vertex getClosestVertex(Vector3f v) {
		Vertex closestV = null;
		float smallestDist = Float.MAX_VALUE;
		for (Vertex vert : hs.getVertices()) {
			float dist = dist(v, vert);
			if (dist < smallestDist) {
				smallestDist = dist;
				closestV = vert;
			}
		}
		return closestV;
	}

	/**
	 * Mark @param v as selected and show its label in gui. additionally, add it
	 * to the point cloud to render it
	 * 
	 * @param display
	 */
	public void markSelected(Vertex v) {
		selectedVertex = v;
		updateLabelField(v);

		// add/remove point to point cloud and render
		updatePC();
		display.updatePC(pc);

		// mark most recently chosen vertex additionally via triangle color
		markSelectedRegion();
	}

	

	/**
	 * set label for marked vertex
	 * 
	 * @param label
	 */
	public void labelCurrentVertex(Label label) {
		// if nothing selected, ignore event
		if ((selectedVertex == null))
			return;

		// if label is null, we want to remove the label
		if (label == null) {
			// remove (add null label)
			vertexLabels.put(selectedVertex, null);
			// deselect in gui
			deselect();

			// keep track of number of vertices with a non-null label
			updateNLabels();
		} else {
			// label not null, add (or update) if valid (i. e. unique)
			if (unique(label, selectedVertex)) {
				// add/overwrite label
				vertexLabels.put(selectedVertex, label);

				// increment number of labels
				updateNLabels();
			} else {
				// deselect in gui
				deselect();
			}
		}
	}

	/**
	 * Stores labels in a text file with
	 * @param fileSaveName
	 * @throws IOException
	 */
	public void exportToTxT(String fileSaveName) throws IOException {
		System.out.println("exporting " + nLabels + " labels to "
				+ fileSaveName);
		File file = new File(fileSaveName);
		file.createNewFile();

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		for (Vertex v : vertexLabels.keySet()) {
			Label l = vertexLabels.get(v);
			if (l != null) {
				String aLine = v.index + " " + l.getName() + "\n";
				bw.write(aLine);
			}
		}

		bw.close();
	}

	/**
	 * import labels from a text file
	 * @param fileName
	 * @throws IOException
	 */
	public void importFromTxT(String fileName) throws IOException {
		// reset labels
		initVertexLabels();
		nLabels = 0;
		BufferedReader reader = new BufferedReader(new FileReader(fileName));

		String line = null;
		while ((line = reader.readLine()) != null) {
			// Read line
			String[] s = line.split("\\s+");
			vertexLabels.put(hs.getVertices().get(Integer.valueOf(s[0])),
					new Label(s[1]));
			nLabels++;

		}
		reader.close();

		selectedVertex = null;
		deselect();
		updateNLabels();
		glHE.update("color");
	}
}
