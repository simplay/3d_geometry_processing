package glWrapper;

import java.util.ArrayList;
import javax.media.opengl.GL;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;
import assignment2.HashOctree;
import assignment2.HashOctreeCell;

/**
 * Simple GLWrapper for the {@link HashOctree}.
 * The octree is sent to the Gpu as set of cell-center points and
 * side lengths. 
 * @author Alf
 *
 */
public class GLHashtreeCellAdjacencies extends GLDisplayable {

	private HashOctree myTree;
	public GLHashtreeCellAdjacencies(HashOctree tree) {
		// upper bound
		super(6*tree.numberOfLeafs());
		this.addElement(tree.getAdjCellVertices(), Semantic.POSITION , 3);
		this.addElement(tree.getAdjCellNeighborVertices(), Semantic.USERSPECIFIED , 3, "parent");		
		this.addIndices(tree.getAdjCellInd());
	}
	
	/**
	 * values are given by OctreeVertex
	 * @param values
	 */
	public void addFunctionValues(ArrayList<Float> values){
		float[] vals = new float[myTree.numberOfLeafs()];
		
		for(HashOctreeCell n: myTree.getLeafs()){
			for(int i = 0; i <=0b111; i++){
				vals[n.leafIndex] += values.get(myTree.getNbr_c2v(n, i).index);//*/Math.signum(values.get(myTree.getVertex(n, i).index));
			}
			vals[n.leafIndex] /=8;
			//vals[n.leafIndex] = Math.abs(vals[n.leafIndex]) < 5.99 ? -1: 1;
		}
		
		this.addElement(vals, Semantic.USERSPECIFIED , 1, "func");
	}

	public int glRenderFlag() {
		return GL.GL_POINTS;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		
	}
}
