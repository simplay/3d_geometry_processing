package utility;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

import meshes.Face;
import meshes.HEData1d;
import meshes.HEData3d;
import meshes.Vertex;

public class Monkey {
	public void copyToArrayP3f(ArrayList<Vertex> arrayList, float[] verts) {
		int i = 0;
		for(Vertex v: arrayList){
			Point3f pos = v.getPos();
			verts[i++] = pos.x;
			verts[i++] = pos.y;
			verts[i++] = pos.z;
		}
	}
	
	/**
	 * Helper method that copies the face information to the ind array
	 * @param arrayList
	 * @param ind
	 */
	public void copyToArray(ArrayList<Face> arrayList, int[] ind) {
		int i = 0, j = 0;
		for(Face f : arrayList){
			Iterator<Vertex> iter = f.iteratorFV();
			j = 0;
			while(iter.hasNext()){
				Vertex v = iter.next();
				ind[i*3 + j] = v.index;
				j++;
			}
			i++;
		}
	}
	
	public float[] copyHead3dToArray3f(HEData3d head3d) {
		Iterator<Tuple3f> iter = head3d.iterator();
		float[] array3f = new float[3*head3d.size()];
		int t = 0;
		while(iter.hasNext()){
			Tuple3f el = iter.next();
			array3f[3*t] = el.x;
			array3f[3*t+1] = el.y;
			array3f[3*t+2] = el.z;
			t++;
		}
		return array3f;
	}
	
	
	public float[] copyHead1dToArray1i(HEData1d head1d) {
		Iterator<Number> iter = head1d.iterator();
		float[] array1i = new float[head1d.size()];
		int t = 0;
		while(iter.hasNext()){
			array1i[t] = ((Integer)iter.next());
			t++;
		}
		return array1i;
	}
	
	public float[] copyHead1dToArray1f(HEData1d head1d) {
		Iterator<Number> iter = head1d.iterator();
		float[] array1f = new float[head1d.size()];
		int t = 0;
		while(iter.hasNext()){
			array1f[t] = ((Float)iter.next());
			t++;
		}
		return array1f;
	}
	
	/**
	 * cotan(x) == 1/tan(x)
	 * @param angle
	 * @return
	 */
	public static float cot(float angle) {
		return 1.0f/(float)Math.tan(angle);
	}
	
	public static float clamppedCot(float angle) {
		float cotan = cot(angle);
		return (float)Math.min(1e2, Math.max(-1e2, cotan));
	}
	
}
