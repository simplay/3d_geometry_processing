package meshes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3f;

/**
 * A Wireframe Mesh represents a mesh as a list of vertices and a list of faces.
 * Very lightweight representation.
 * @author bertholet
 *
 */
public class WireframeMesh {

	public ArrayList<Point3f> vertices;
	public ArrayList<int[]> faces;
	
	private int currentFaceIndex = 0;
	private int[] currentFace = new int[3];
	
	
	public WireframeMesh(){
		vertices = new ArrayList<Point3f>();
		faces = new ArrayList<>();
//		flush();
	}

	/**
	 * Add a new face to the wireframe mesh using a lazy add approach:
	 * first collect three indices and then, after having received 3 indices 
	 * which might form a mesh, check if they actually do form a face which is not
	 * degenerated.
	 * @param index
	 */
	public void addLazyFace(int index){
		currentFace[currentFaceIndex++] = index;
//		currentFaceIndex++;
		if(currentFaceIndex == 3){
//			if(!isDegeneratedCurrentFace()) faces.add(currentFace);
			if(!isDegenerated(currentFace)) {
				faces.add(currentFace);
			}
			flush();
		}
		
	}
	
	
	/**
	 * check if there are equal indices in the current face array.
	 * If there are, then this means we have given a degenerated face.
	 * @return is current face degenerated?
	 */
	private boolean isDegeneratedCurrentFace(){
		List<Integer> tmp = new LinkedList<Integer>();
		tmp.add(currentFace[0]);
		for(int k = 1; k < 3; k++){
			int val = currentFace[k];
			if(tmp.contains(val)) return true;
			tmp.add(val);
		}
		return false;
	}
	
	private boolean isDegenerated(int[] f){
		return (f[0] == f[1] || f[0] == f[2] || f[1] == f[2]);
	}
	
	
	/**
	 * reset helper storage to their default values.
	 */
	private void flush(){
		this.currentFaceIndex = 0;
		this.currentFace = new int[3];
	}
}
