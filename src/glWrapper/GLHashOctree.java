package glWrapper;

import java.util.ArrayList;
import javax.media.opengl.GL;
import javax.vecmath.Tuple3f;

import assignment2.HashOctree;
import assignment2.HashOctreeVertex;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;

import meshes.PointCloud;

/**
 * Wrapper for Pointclouds
 * @author Alf
 *
 */
public class GLHashOctree extends GLDisplayable {

	public GLHashOctree(HashOctree hashOctTree) {
		super(hashOctTree.numberofVertices()*6);

		
		this.addElement(hashOctTree.getNeighborPositions(), Semantic.USERSPECIFIED , 3, "parent");
		this.addElement(hashOctTree.getVerticesPostions(), Semantic.POSITION , 3);
		this.addIndices(hashOctTree.getIndices());
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
