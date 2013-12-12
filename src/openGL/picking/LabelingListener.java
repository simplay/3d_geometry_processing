package openGL.picking;

import glWrapper.GLUpdatableHEStructure;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.HashMap;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import meshes.Vertex;

import assignment7.Label;
import assignment7.LabelingProcessor;

import openGL.LabelingDisplay;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.interfaces.SceneManager;
import openGL.interfaces.SceneManagerIterator;
import openGL.objects.Shape;
import openGL.objects.Transformation;

public class LabelingListener extends GLDisplayable implements MouseListener,
		KeyListener {

	// Provides access to camera frustum etc settings.
	SceneManager s;

	// provides access to the display dimensions.
	private LabelingDisplay display;

	// Provides access to the shapes transformation
	HashMap<Shape, LabelingProcessor> labables;

	/**
	 * this method does the mapping from pixel (x,y) to vertex (x, y, z);
	 */
	private Vector3f getVertexAtPixel(Vector2f p, int width, int height) {
		float d = display.getGLRenderer().getDepth((int) p.x,
				height - 1 - (int) p.y);
		Vector4f v = new Vector4f(p.x, p.y, d, 1.f);
		return unproject(v, width, height, display.getSceneManager());
	}

	/**
	 * Sets up the D matrix used for unprojection
	 * 
	 * @param w
	 * @param h
	 * @return
	 */
	private Matrix4f getD(int w, int h) {
		float wf = (float) w;
		float hf = (float) h;
		Matrix4f D = new Matrix4f();
		D.setIdentity();
		D.m00 = wf / 2;
		D.m11 = (-hf) / 2;
		D.m22 = 1.f / 2;
		D.m33 = 1;
		D.m23 = 1.f / 2;
		D.m13 = hf / 2;
		D.m03 = wf / 2;
		return D;
	}

	/**
	 * do the mapping from (x, y, d, 1) -> (x, y, z) in obj coordinates
	 * 
	 * @param v
	 * @param w
	 * @param h
	 * @param sceneManager
	 * @return
	 */
	private Vector3f unproject(Vector4f v, int w, int h,
			SceneManager sceneManager) {
		// undo projection
		Matrix4f P = new Matrix4f(sceneManager.getFrustum()
				.getProjectionMatrix());
		Matrix4f project = getD(w, h);
		project.mul(P);
		project.invert();

		project.transform(v);
		v.scale(1.f / v.w);

		// from camera back to obj space
		Matrix4f modelView = new Matrix4f(sceneManager.getCamera()
				.getCameraMatrix());
		SceneManagerIterator it = sceneManager.iterator();
		modelView.mul(it.next().getTransformation());

		modelView.invert();
		modelView.transform(v);

		return new Vector3f(v.x, v.y, v.z);
	}

	/**
	 * identify selected vertex
	 * 
	 * @param pixel
	 * @param w
	 * @param h
	 * @return
	 */
	private Vertex getclosestVertex(Vector2f pixel, int w, int h) {
		// get clicked vertex via depth buffer and unprojection
		Vector3f v = getVertexAtPixel(pixel, w, h);
		// get vertex in hs which is closest to v
		Vertex closestV = null;
		for (Shape s : labables.keySet()) {
			// only test the mesh
			if ((s.getVertexData() instanceof GLUpdatableHEStructure))
				closestV = labables.get(s).getClosestVertex(v);
		}
		return closestV;
	}

	public LabelingListener(LabelingDisplay d, SceneManager manager) {
		super(4);
		labables = new HashMap<Shape, LabelingProcessor>();
		s = manager;
		this.display = d;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// deselect if click without ctrl
		if (!e.isControlDown()) {
			for (Shape s : labables.keySet()) {
				labables.get(s).markSelected(null);
				display.updateDisplay();
			}
			return;
		}

		// select vertex
		int w = display.getRenderPanel().getCanvas().getWidth();
		int h = display.getRenderPanel().getCanvas().getHeight();
		Vector2f pixel = new Vector2f(e.getX(), e.getY());

		// get target vertex
		Vertex closestV = getclosestVertex(pixel, w, h);

		// mark selected vertex
		for (Shape s : labables.keySet()) {
			if ((s.getVertexData() instanceof GLUpdatableHEStructure)) {
				labables.get(s).markSelected(closestV);
				display.updateDisplay();
			}
		}
	}

	public void labelCurrentVertex(Label label) {
		for (Shape s : labables.keySet()) {
			if ((s.getVertexData() instanceof GLUpdatableHEStructure))
				labables.get(s).labelCurrentVertex(label);
		}
	}

	public void exportToTxT(String fileName) throws IOException {
		for (Shape s : labables.keySet()) {
			if ((s.getVertexData() instanceof GLUpdatableHEStructure))
				labables.get(s).exportToTxT(fileName);
		}
	}

	public void importFromTxT(String fileName) throws IOException {
		for (Shape s : labables.keySet()) {
			if ((s.getVertexData() instanceof GLUpdatableHEStructure))
				labables.get(s).importFromTxT(fileName);
		}
	}

	public void register(Shape s, LabelingProcessor lp) {
		lp.setDisplay(display);
		this.labables.put(s, lp);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public int glRenderFlag() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}
}
