package assignment4;

import glWrapper.GLHalfedgeStructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Color3f;

import openGL.MyDisplay;
import sparse.CSRMatrix;
import sparse.SCIPYEVD;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

/**
 * You can implement the spectral smoothing application here....
 * 
 * @author Alf
 * 
 */
public class Assignment4_4_spectralSmoothing {

	private static boolean isSpectralDemo = true;

	public static void main(String[] args) throws IOException,
			MeshNotOrientedException, DanglingTriangleException {
		if (isSpectralDemo) spectralSmoothingDemo();
		else harmonicsDemo();
	}

	private static void spectralSmoothingDemo() {
		HalfEdgeStructure hs = new HalfEdgeStructure();
		WireframeMesh mesh = null;
		int evCount = 20;
		
		try {
			mesh = ObjReader.read("objs/bunny.obj", false);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			hs.init(mesh);
		} catch (MeshNotOrientedException e1) {
			e1.printStackTrace();
		} catch (DanglingTriangleException e1) {
			e1.printStackTrace();
		}

		MyDisplay d = new MyDisplay();

		try {
			SpectralSmoothing.smooth(hs, evCount);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		GLHalfedgeStructure glHs = new GLHalfedgeStructure(hs);
		glHs.configurePreferredShader("shaders/trimesh_flatColor3f.vert",
				"shaders/trimesh_flatColor3f.frag",
				"shaders/trimesh_flatColor3f.geom", "ev_"+evCount);
		d.addToDisplay(glHs);
	}

	private static void harmonicsDemo() {
		HalfEdgeStructure hes = new HalfEdgeStructure();
		WireframeMesh mesh = null;
		try {
			mesh = ObjReader.read("objs/bunny.obj", false);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try {
			hes.init(mesh);
		} catch (MeshNotOrientedException e1) {
			e1.printStackTrace();
		} catch (DanglingTriangleException e1) {
			e1.printStackTrace();
		}

		CSRMatrix m = LMatrices.symmetricCotanLaplacian(hes);
		ArrayList<Float> eigenValues = new ArrayList<Float>();
		ArrayList<ArrayList<Float>> eigenVectors = new ArrayList<ArrayList<Float>>();
		try {
			SCIPYEVD.doSVD(m, "", 20, eigenValues, eigenVectors);
		} catch (IOException e) {
			e.printStackTrace();
		}

		MyDisplay d = new MyDisplay();
		int evCounter = 0;
		for (ArrayList<Float> eigenVector : eigenVectors) {
			GLHalfedgeStructure glHs = new GLHalfedgeStructure(hes);
			ArrayList<Color3f> eigenVectorColor = new ArrayList<Color3f>();
			float minEV = Collections.min(eigenVector);
			float maxEV = Collections.max(eigenVector);
			float divisor = (Math.max(maxEV - minEV, 0.001f));

			for (Float v : eigenVector) {
				float _v = (v - minEV) / divisor;
				eigenVectorColor.add(getColor(_v));
			}

			glHs.addCol(eigenVectorColor, "color");
			glHs.configurePreferredShader("shaders/trimesh_flatColor3f.vert",
					"shaders/trimesh_flatColor3f.frag",
					"shaders/trimesh_flatColor3f.geom", "eigV_" + evCounter);
			d.addToDisplay(glHs);
			evCounter++;
		}

	}

	/**
	 * coloring described on assignment sheet
	 * 
	 * @param _v
	 * @return
	 */
	private static Color3f getColor(float _v) {
		Color3f color = new Color3f();
		color.x = Math.min(2 * Math.max(_v, 0.1f), 0.8f);
		color.z = Math.min(2 * Math.max(1 - _v, 0.1f), 0.8f);
		color.y = Math.min(color.x, color.z);
		return color;
	}
}
