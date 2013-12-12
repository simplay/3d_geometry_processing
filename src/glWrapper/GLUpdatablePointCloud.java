package glWrapper;

import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import meshes.PointCloud;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;

public class GLUpdatablePointCloud extends GLPointCloud {
	
	/**
	 * copy point positions to float array
	 * @param points
	 * @param vals
	 */
	private void copyToArray(ArrayList<Point3f> points, float[] vals) {
		int i = 0;
		for (Tuple3f v : points) {
			vals[i++] = v.x;
			vals[i++] = v.y;
			vals[i++] = v.z;
		}
	}
	
	/**
	 * copy colors (passed as normals) to float array
	 * @param normals
	 * @param vals
	 */
	private void cta(ArrayList<Vector3f> normals, float[] vals) {
		int i = 0;
		for (Vector3f v : normals) {
			vals[i++] = v.x;
			vals[i++] = v.y;
			vals[i++] = v.z;
		}
	}

	public GLUpdatablePointCloud(PointCloud pc) {
		super(pc);
		
		this.configurePreferredShader("shaders/renderPC.vert",
				"shaders/renderPC.frag", null);
	}

	/**
	 * The position buffer will be updated in the next pass
	 * @param pc 
	 */
	public void updatePositions(PointCloud pc) {
		// positions
		float[] verts = new float[pc.points.size() * 3];
		copyToArray(pc.points, verts);
		
		// colors (using normals semantics)
		float[] norms = new float[pc.points.size() * 3];
		cta(pc.normals, norms);
		
		// indices
		int[] ind = new int[pc.points.size()];
		for (int i = 0; i < ind.length; i++) {
			ind[i] = i;
		}
		
		// clear vertexEelements list
		getElements().clear();
		this.n = pc.points.size();
//		setNumberOfVertices(pc.points.size());
		this.addElement(verts, Semantic.POSITION , 3);
		this.addElement(norms, Semantic.USERSPECIFIED , 3, "normal");
		this.addIndices(ind);
	}


	@Override
	public int glRenderFlag() {
		return GL.GL_POINTS;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		// no additional uniforms
	}
}
