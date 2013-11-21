package assignment5;

import static org.junit.Assert.*;
import javax.vecmath.Matrix4f;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import org.junit.Before;
import org.junit.Test;

public class ErrorMatrixTests {

	private HalfEdgeStructure hs;

	@Before
	public void setUp() throws Exception {
		try {
			WireframeMesh m = ObjReader.read("objs/dragon3.obj", false);
			hs = new HalfEdgeStructure();
			hs.init(m);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
		
	@Test
	public void errorMatNotNaNOrInf() {
		QSlim qSlim = new QSlim(hs);
		
		// for each vertex of mesh
		for(Vertex vertex: hs.getVertices()) {
			Matrix4f mat = qSlim.getVertexMatrixAt(vertex);
			// for each element of matrix
			for(int k = 0; k < 4; k++){
				for(int l = 0; l < 4; l++){
					float element = mat.getElement(k, k);
					assertFalse(Float.isNaN(element));
					assertFalse(Float.isInfinite(element));
				}
			}
		}
	}

}
