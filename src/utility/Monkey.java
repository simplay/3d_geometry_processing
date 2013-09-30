package utility;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Point3f;

import meshes.Face;
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
}
