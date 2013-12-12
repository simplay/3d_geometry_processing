package glWrapper;

import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;


import meshes.WireframeMesh;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;

public class GLWireframeMeshLines extends GLDisplayable {

	WireframeMesh myMesh;

	private void copyToArray(Vector3f color, float[] colors) {
		for (int i = 0; i < colors.length; i += 3) {
			colors[i] = color.x;
			colors[i+1] = color.y;
			colors[i+2] = color.z;
		}
	}

	/**
	 * Helper method that copies the face information to the ind array
	 * 
	 * @param faces
	 * @param ind
	 */
	private void copyToArray(ArrayList<int[]> faces, int[] ind) {
		int i = 0, j;
		for (int[] f : faces) {
			// only triangle meshes covered for now.
			assert (f.length == 3);
			for (j = 0; j < 3; j++) {
				ind[i * 3 + j] = f[j];
			}
			i++;
		}
	}

	/**
	 * Helper method that copies the vertices arraylist to the verts array
	 * 
	 * @param vertices
	 * @param verts
	 */
	private void copyToArrayP3f(ArrayList<Point3f> vertices, float[] verts) {
		int i = 0;
		for (Point3f v : vertices) {
			verts[i++] = v.x;
			verts[i++] = v.y;
			verts[i++] = v.z;
		}
	}

	public GLWireframeMeshLines(WireframeMesh m, Vector3f color) {
		super(m.vertices.size());
		myMesh = m;

		// Add Vertices
		float[] verts = new float[m.vertices.size() * 3];
		int[] ind = new int[m.faces.size() * 3];
		float[] colors = new float[m.vertices.size() * 3];

		// copy the data to the allocated arrays
		copyToArrayP3f(m.vertices, verts);
		copyToArray(m.faces, ind);
		copyToArray(color, colors);

		this.addElement(verts, Semantic.POSITION, 3);
		this.addElement(colors, Semantic.USERSPECIFIED, 3, "color");
		this.addIndices(ind);

	}

	
	/**
	 * Return the gl render flag to inform opengl that the indices/positions
	 * describe triangles
	 */
	@Override
	public int glRenderFlag() {
		return GL.GL_TRIANGLES;
	}

	/**
	 * No additional uniform variabes are passed to the shader.
	 */
	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
	}

}
