package glWrapper;

import java.util.ArrayList;
import javax.media.opengl.GL;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;
import assignment2.HashOctree;
import assignment2.HashOctreeVertex;

/**
 * GLWrapper which will send the HashOctree vertex positions to the GPU
 * @author Alf
 *
 */
public class GLHashtreeVertexAdjacencies extends GLDisplayable {

	private HashOctree myTree;
	public GLHashtreeVertexAdjacencies(HashOctree tree) {
		super(6*tree.numberofVertices());
		this.addElement(tree.getAdjVertPositions(), Semantic.POSITION , 3);
		this.addElement(tree.getAdjVertices(), Semantic.USERSPECIFIED , 3, "parent");
		this.addIndices(tree.getAdjVertInd());
	}
	
	/**
	 * values are given by OctreeVertex
	 * @param values
	 */
	public void addFunctionValues(ArrayList<Float> values){
		float[] vals = new float[myTree.numberofVertices()];
		
		for(HashOctreeVertex v: myTree.getVertices()){
			vals[v.index] = values.get(v.index);//*/Math.signum(values.get(myTree.getVertex(n, i).index));
		}
		
		this.addElement(vals, Semantic.USERSPECIFIED , 1, "func");
	}

	@Override
	public int glRenderFlag() {
		return GL.GL_POINTS;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		// TODO Auto-generated method stub
		
	}
}
