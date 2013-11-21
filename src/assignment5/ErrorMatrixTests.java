package assignment5;

import static org.junit.Assert.*;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

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
	public void errorMatrixShouldNotContainNaN() {
		QSlim qSlim = new QSlim(hs);
		
		for(Vertex v: hs.getVertices()) {
			Matrix4f m = qSlim.getVertexMatrixAt(v);
			
			assertFalse(Float.isNaN(m.m00));
			assertFalse(Float.isNaN(m.m01));
			assertFalse(Float.isNaN(m.m02));
			assertFalse(Float.isNaN(m.m03));
			assertFalse(Float.isNaN(m.m10));
			assertFalse(Float.isNaN(m.m11));
			assertFalse(Float.isNaN(m.m12));
			assertFalse(Float.isNaN(m.m13));
			assertFalse(Float.isNaN(m.m20));
			assertFalse(Float.isNaN(m.m21));
			assertFalse(Float.isNaN(m.m22));
			assertFalse(Float.isNaN(m.m23));
			assertFalse(Float.isNaN(m.m30));
			assertFalse(Float.isNaN(m.m31));
			assertFalse(Float.isNaN(m.m32));
			assertFalse(Float.isNaN(m.m33));
		}
	}

	@Test
	public void errorMatrixShouldNotContainInfinity() {
		QSlim qSlim = new QSlim(hs);
		
		for(Vertex v: hs.getVertices()) {
			Matrix4f m = qSlim.getVertexMatrixAt(v);
			assertFalse(Float.isInfinite(m.m00));
			assertFalse(Float.isInfinite(m.m01));
			assertFalse(Float.isInfinite(m.m02));
			assertFalse(Float.isInfinite(m.m03));
			assertFalse(Float.isInfinite(m.m10));
			assertFalse(Float.isInfinite(m.m11));
			assertFalse(Float.isInfinite(m.m12));
			assertFalse(Float.isInfinite(m.m13));
			assertFalse(Float.isInfinite(m.m20));
			assertFalse(Float.isInfinite(m.m21));
			assertFalse(Float.isInfinite(m.m22));
			assertFalse(Float.isInfinite(m.m23));
			assertFalse(Float.isInfinite(m.m30));
			assertFalse(Float.isInfinite(m.m31));
			assertFalse(Float.isInfinite(m.m32));
			assertFalse(Float.isInfinite(m.m33));
		}
	}
}
