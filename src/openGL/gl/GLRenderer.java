package openGL.gl;

import glWrapper.GLUpdatableHEStructure;
import glWrapper.GLUpdatablePointCloud;
import glWrapper.GLWireframeMeshLines;

import java.awt.image.renderable.RenderContext;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ListIterator;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Tuple3f;

import openGL.gl.interactive.GLUpdateable;
import openGL.interfaces.SceneManager;
import openGL.interfaces.SceneManagerIterator;
import openGL.interfaces.Shader;
import openGL.objects.RenderItem;
import openGL.objects.Transformation;



/**
 * This class implements a {@link RenderContext} (a renderer) using OpenGL
 * version 3 (or later) and does the rendering on the gpu.
 */
public class GLRenderer {
	// depth buffer values
	private float[][] db;

	private SceneManager sceneManager;
	private GL3 gl;

	/**
	 * The default shader for this render context, will be used for items that
	 * do not have their own shader.
	 */
	private GLShader defaultShader;

	/**
	 * The id of the currently active shader (you should always
	 * useuseShader(GLShader) and useDefaultShader() to switch between the
	 * shaders!).
	 */
	private int activeShaderID;

	/**
	 * This constructor is called by {@link GLRenderPanel}.
	 * 
	 * @param drawable
	 *            the OpenGL rendering context. All OpenGL calls are directed to
	 *            this object.
	 */
	public GLRenderer(GLAutoDrawable drawable) {
		// initialize depth buffer array (initialize large enough)
		db = new float[2000][2000];

		gl = drawable.getGL().getGL3();
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_LINE_SMOOTH);

		gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
		gl.glPointSize(20);
		gl.glLineWidth(.01f);
		gl.glDepthFunc(GL.GL_LEQUAL);

		// Load and use default shader, will be used for items that do not have
		// their own shader.
		defaultShader = (GLShader) makeShader();
		try {
			defaultShader.load("shaders/default.vert", "shaders/default.frag");
		} catch (Exception e) {
			System.out.print("Problem with shader:\n");
			System.out.print(e.getMessage());
		}
		useDefaultShader();
	}
	
	public float getDepth(int x, int y) {
		return db[x][y];
	}

	/**
	 * Set the scene manager. The scene manager contains the 3D scene that will
	 * be rendered. The scene includes geometry as well as the camera and
	 * viewing frustum.
	 */
	public void setSceneManager(SceneManager sceneManager) {
		this.sceneManager = sceneManager;
	}

	/**
	 * This method is called by the GLRenderPanel to redraw the 3D scene. The
	 * method traverses the scene using the scene manager and passes each object
	 * to the rendering method.
	 */
	public void display(GLAutoDrawable drawable) {
		gl = drawable.getGL().getGL3();

		beginFrame();

		// force to render mesh in first pass, to have only this in
		// the depth buffer
		SceneManagerIterator iterator = sceneManager.iterator();
		while (iterator.hasNext()) {
			RenderItem r = iterator.next();
			if (r.getShape() != null && r.getShape().isVisible()) {
				GLDisplayable vertexData = (GLDisplayable) r.getShape()
						.getVertexData();
				if (vertexData instanceof GLUpdatableHEStructure) {
					gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_FILL);
					gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
					gl.glPolygonOffset(15f, 15f);

					draw(r);

					gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
				}
			}
		}

		// capture depth buffer, consists only of the real geometry
		// at this point in time
		captureDepthBuffer();

		// now render everything but the mesh itself (lines, points, ...)
		iterator = sceneManager.iterator();
		while (iterator.hasNext()) {
			RenderItem r = iterator.next();
			if (r.getShape() != null && r.getShape().isVisible()) {
				GLDisplayable vertexData = (GLDisplayable) r.getShape()
						.getVertexData();
				if (!(vertexData instanceof GLUpdatableHEStructure)) {
					draw(r);
				}
			}
		}
		
		endFrame();
	}



	public void setUniform(String name, float val) {
		gl.glUniform1f(gl.glGetUniformLocation(activeShaderID, name), val);
	}

	public void setUniform(String name, Transformation mat) {
		gl.glUniformMatrix4fv(gl.glGetUniformLocation(activeShaderID, name), 1,
				false, transformationToFloat16(mat), 0);
	}

	public void setUniform(String name, Tuple3f val) {
		gl.glUniform3f(gl.glGetUniformLocation(activeShaderID, name), val.x,
				val.y, val.z);
	}

	public void useShader(GLShader s) {
		if (s != null) {
			activeShaderID = s.programId();
			s.use();
		}
	}

	public void useDefaultShader() {
		useShader(defaultShader);
	}

	public Shader makeShader() {
		return new GLShader(gl);
	}

	/**
	 * Convert a Transformation to a float array in column major ordering, as
	 * used by OpenGL.
	 */
	private static float[] transformationToFloat16(Transformation m) {
		float[] f = new float[16];
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				f[j * 4 + i] = m.getElement(i, j);
		return f;
	}
	
	/**
	 * This method is called at the beginning of each frame, i.e., before scene
	 * drawing starts.
	 */
	private void beginFrame() {

		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
	}

	/**
	 * this method is called after the mesh is rendered
	 */
	private void captureDepthBuffer() {

		// store depth buffer
		int w = 2000;
		int h = 2000;
		FloatBuffer depthBuffer = FloatBuffer.allocate(w * h);
		gl.glReadPixels(0, 0, w, h, GL3.GL_DEPTH_COMPONENT, GL3.GL_FLOAT,
				depthBuffer);
		depthBuffer.rewind();

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				float d = depthBuffer.get();
				db[x][y] = d;
			}
		}
	}

	/**
	 * This method is called at the end of each frame, i.e., after scene drawing
	 * is complete.
	 */
	private void endFrame() {
		gl.glFlush();
	}

	/**
	 * The main rendering method.
	 * 
	 * @param renderItem
	 *            the object that needs to be drawn
	 */
	private void draw(RenderItem renderItem) {

		renderItem.getShape().loadPreferredShader(this);

		GLDisplayable vertexData = (GLDisplayable) renderItem.getShape()
				.getVertexData();

		// In the first pass the object has to be given to the buffer (on the
		// GPU) and the renderItem has to store the handle, so we do not have to
		// send the object to the GPU in each pass.
		if (vertexData.getVAO() == null) {
			initArrayBuffer(vertexData);
			vertexData.getVAO().rewind();
		}

		// hack in vertex buffer refreshments
		if (vertexData instanceof GLUpdateable) {
			GLUpdateable glupd = (GLUpdateable) vertexData;
			updateArrayBuffer(glupd);
			vertexData.getVAO().rewind();
		}

		if (vertexData instanceof GLWireframeMeshLines) {
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_LINE);
			gl.glEnable(GL3.GL_POLYGON_OFFSET_LINE);
			gl.glPolygonOffset(14f, 14f);
			gl.glEnable(GL3.GL_BLEND);
			gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
		}
		if (vertexData instanceof GLUpdatablePointCloud) {
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_FILL);
			gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
			gl.glPolygonOffset(-2f, -2f);
			gl.glEnable(GL3.GL_BLEND);
			gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
		}

		// Set modelview and projection matrices in shader (has to be done in
		// every step, since they usually have changed)
		Transformation mvMat = setTransformation(renderItem.getTransformation());

		// set additional shader dependent uniforms
		renderItem.getShape().setAdditionalUniforms(this, mvMat);

		// bind the VAO of this shape (all the vertex data are already on the
		// GPU, we do not have to send them again)
		vertexData.getVAO().bind();

		// Render the vertex buffer objects
		gl.glDrawElements(renderItem.getShape().getVertexData().glRenderFlag(),
				renderItem.getShape().getVertexData().getIndices().length,
				GL.GL_UNSIGNED_INT, 0);

		// we are done with this shape, bind the default vertex array
		gl.glBindVertexArray(0);

		if (vertexData instanceof GLWireframeMeshLines)
			gl.glDisable(GL3.GL_BLEND);
		
		if (vertexData instanceof GLUpdatablePointCloud)
			gl.glDisable(GL3.GL_BLEND);

	}

	private void updateArrayBuffer(GLUpdateable data) {
		ListIterator<GLUpdateable.VertexElement> itr = data.getElements()
				.listIterator(0);

		while (itr.hasNext()) {
			GLDisplayable.VertexElement e = itr.next();
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, data.getVAO().getNextVBO());

			if (data.wantUpdate(e.glName)) {
				gl.glBufferData(GL.GL_ARRAY_BUFFER, e.getData().length * 4,
						FloatBuffer.wrap(e.getData()), GL.GL_DYNAMIC_DRAW);

				data.didUpdate(e.glName);
			}

		}

		// bind the default vertex array object
		gl.glBindVertexArray(0);
	}

	private void initArrayBuffer(GLDisplayable data) {
		// Make a vertex array object (VAO) for this shape
		data.setVAO(new GLVertexArrayObject(gl, data.getElements().size() + 1));

		// Store all vertex attributes in the buffers
		ListIterator<GLDisplayable.VertexElement> itr = data.getElements()
				.listIterator(0);
		while (itr.hasNext()) {
			GLDisplayable.VertexElement e = itr.next();
			int dim = e.getNumberOfComponents();

			// Bind the next vertex buffer object
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, data.getVAO().getNextVBO());
			// Upload vertex data
			gl.glBufferData(GL.GL_ARRAY_BUFFER, e.getData().length * 4,
					FloatBuffer.wrap(e.getData()), GL.GL_DYNAMIC_DRAW);

			// Tell OpenGL which "in" variable in the vertex shader corresponds
			// to the current vertex buffer object.
			// We use our own convention to name the variables, i.e.,
			// "position", "normal", "color", "texcoord", or others if
			// necessary.
			int attribIndex = -1;
			switch (e.getSemantic()) {
			case POSITION:
				attribIndex = gl
						.glGetAttribLocation(activeShaderID, "position");
				break;
			case USERSPECIFIED:
				attribIndex = gl.glGetAttribLocation(activeShaderID,
						e.getGLName());
				break;

			}

			gl.glVertexAttribPointer(attribIndex, dim, GL.GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(attribIndex);
		}

		// bind the default vertex buffer objects
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

		// store the indices into the last buffer
		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, data.getVAO().getNextVBO());
		gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER,
				data.getIndices().length * 4,
				IntBuffer.wrap(data.getIndices()), GL.GL_DYNAMIC_DRAW);

		// bind the default vertex array object
		gl.glBindVertexArray(0);
	}

	/**
	 * Set modelview and projection matrices in shader. modelview will be
	 * scenemanager.cameraMatrix*transformation and the proj matrix
	 * sceneManager.getFrustum().ProjectionMatrix
	 * 
	 * @param transformation
	 * @return
	 */
	private Transformation setTransformation(Transformation transformation) {

		// Compute the modelview matrix by multiplying the camera matrix and
		// the transformation matrix of the object
		Transformation modelview = new Transformation(sceneManager.getCamera()
				.getCameraMatrix());
		modelview.mul(transformation);

		// Set modelview and projection matrices in shader
		gl.glUniformMatrix4fv(
				gl.glGetUniformLocation(activeShaderID, "modelview"), 1, false,
				transformationToFloat16(modelview), 0);
		gl.glUniformMatrix4fv(gl.glGetUniformLocation(activeShaderID,
				"projection"), 1, false, transformationToFloat16(sceneManager
				.getFrustum().getProjectionMatrix()), 0);

		return modelview;

	}
}
