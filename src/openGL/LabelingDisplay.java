package openGL;

import glWrapper.GLUpdatableHEStructure;
import glWrapper.GLUpdatablePointCloud;
import glWrapper.GLWireframeMeshLines;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import meshes.PointCloud;

import assignment7.LabelingProcessor;

import openGL.gl.GLRenderer;
import openGL.interfaces.RenderPanel;
import openGL.interfaces.SceneManager;
import openGL.objects.Shape;
import openGL.picking.LabelingListener;
import openGL.picking.LabelingPanelNorth;
import openGL.picking.LabelingPanelSouth;

/**
 * GUI
 */
public class LabelingDisplay extends MyDisplay implements ActionListener {
	LabelingListener l;
	private GLUpdatablePointCloud glPC;
	private Shape pcShape;

	private LabelingPanelNorth labelPanelTop;
	private LabelingPanelSouth labelPanelBottom;

	private static final long serialVersionUID = 1L;

	public LabelingDisplay() {
		super();

		// labeling listener
		l = new LabelingListener(this, sceneManager);
		renderPanel.getCanvas().addMouseListener(l);
		renderPanel.getCanvas().addKeyListener(l);
		renderPanel.getCanvas().setFocusable(true);

		// Labeling part of the GUI (including listener for the buttons)
		this.labelPanelTop = new LabelingPanelNorth();
		labelPanelTop.addPickingListener(l);
		this.labelPanelBottom = new LabelingPanelSouth();
		labelPanelBottom.addPickingListener(l);

		this.getContentPane().add(labelPanelTop, BorderLayout.NORTH);
		this.getContentPane().add(labelPanelBottom, BorderLayout.SOUTH);
	}

	public void add(GLUpdatableHEStructure glHE, LabelingProcessor lp) {
		Shape s = new Shape(glHE);
		sceneManager.addShape(s);
		trackball.register(s);
		l.register(s, lp);
		this.updateWhatsOnDisplay();
	}

	public void add(GLUpdatablePointCloud glPC) {
		this.glPC = glPC;
		pcShape = new Shape(this.glPC);
		sceneManager.addShape(pcShape);
		trackball.register(pcShape);
		this.updateWhatsOnDisplay();
		synchronized (this) {
			this.updateDisplay();
		}
		this.invalidate();
	}

	public void add(GLWireframeMeshLines glD) {
		Shape s = new Shape(glD);
		sceneManager.addShape(s);
		trackball.register(s);
		this.updateWhatsOnDisplay();
		this.updateDisplay();
		this.invalidate();
	}

	public GLRenderer getGLRenderer() {
		return glRenderer;
	}

	public RenderPanel getRenderPanel() {
		return renderPanel;
	}

	public void updatePC(PointCloud pc) {
		pcShape.getVertexData().setVAO(null);
		glPC.updatePositions(pc);

		this.updateDisplay();
	}

	public void updateLabelField(String string) {
		labelPanelTop.updateLabelField(string);
	}

	public void updateNLabels(int nLabels) {
		labelPanelBottom.updateNLabels(nLabels);

	}

	public SceneManager getSceneManager() {
		return sceneManager;
	}

	public void setButtonsActive(boolean b) {
		labelPanelTop.setButtonsActive(b);
	}
}
