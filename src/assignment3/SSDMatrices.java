/**
 * Tiangulate isosurface {x | f(x) = 0}.
 * Given set of points in a point-cloud, and normals
 * or we can approximate them by defining a line
 * given by a point p_k and its successor point p_k+1
 * the normal n_k corresponding to p_k is then a normalized
 * vector which is perpendicular to this provided line.
 * 
 * We define an energy E(f) for any function f as:
 * E(f) = lambda_0*E_D_0(f)+lambda_R*E_D_R(f)+lambda_R*E_D_R(f)
 * Where 
 * E_D_0 will ensure, that zero-isosurface is close to points
 * E_D_0(f) = 1/N sum_i {f(p_i)^2}
 * E_D_1 will ensure, that gradients are similar to normals
 * E_D_1(f) = 1/N sum_i {||(grad f(p_i))-n_i||^2}
 * E_D_R will ensure, smoothness
 * E_D_R(f) = 1/|V| * (integral_V{||Hf(x)||^2 dx})
 * 
 * Note: grad == Gradient, H == Hessian 
 * 
 * We want f* such that f* = argmin_f {E(f)}
 * Intuition: f* is best function with minimum energy
 * 
 * Goal: solve for this system: [D_0; D_1; R]*x = [0; n; 0]
 * We use a simpler regularization term R
 * Stack will be solved in least square fashion.
 */

package assignment3;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.PointCloud;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.LinearSystem;
import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import assignment2.HashOctreeVertex;
import assignment2.MortonCodes;


public class SSDMatrices {
	
	
	/**
	 * Example Matrix creation:
	 * Create an identity matrix, clamped to the provided format.
	 */
	public static CSRMatrix eye(int nRows, int nCols){
		CSRMatrix eye = new CSRMatrix(0, nCols);
		
		//initialize the identity matrix part
		for(int i = 0; i< Math.min(nRows, nCols); i++){
			eye.addRow();
			eye.lastRow().add(
						//column i, value 1
					new col_val(i,1));
		}
		//fill up the matrix with empt rows.
		for(int i = Math.min(nRows, nCols); i < nRows; i++){
			eye.addRow();
		}
		
		return eye;
	}
	
	
	/**
	 * Example matrix creation:
	 * Identity matrix restricted to boundary per vertex values.
	 */
	public static CSRMatrix Eye_octree_boundary(HashOctree tree){
		
		CSRMatrix result = new CSRMatrix(0, tree.numberOfVertices());
				
		for(HashOctreeVertex v : tree.getVertices()){
			if(MortonCodes.isVertexOnBoundary(v.code, tree.getDepth())){
				result.addRow();
				result.lastRow().add(new col_val(v.index,1));
			}
		}
		
		return result;
	}
	
	
	/**
	 * Matrix D_0 has number of point in PointCloud rows and 
	 * number of hashtree vertices columns.
	 * the k-th row of this matrix contains the trilinear
	 * interpolation weights of the cell, containing point k times f
	 * Sanity check: D_0 times cellvertexcoordinates == pointCoordinates.
	 * This Matrix is representing the energy E_D_0.
	 * 
	 * Note relative vertex vertex_d = (x_d, y_d, z_d)
	 * where 
	 * x_d = (x-x_0)/(x_1 - x_0)
	 * y_d = (y-y_0)/(y_1 - y_0)
	 * z_d = (z-y_0)/(z_1 - z_0)
	 * 
	 * @param tree hashtree
	 * @param cloud pointcloud
	 * @return
	 */
	public static CSRMatrix D0Term(HashOctree tree, PointCloud cloud){
		CSRMatrix D0 = new CSRMatrix(0, tree.numberOfVertices());
		
		// for each point in our point cloud
		for (Point3f p: cloud.points) {
			HashOctreeCell cell = tree.getCell(p);
			Vector3f vertex_d = getRelativeCoordinates(tree, cell, p);
			D0.addRow();
			ArrayList<col_val> currentRow = D0.lastRow();
			
			// iterate over each corner of cell and store 
			// depending on its global index in row
			// i.e. only 8 elements of the current row are non-zero
			for(int k = 0; k < 8; k++){
				float weight_x = getNormalizedWeight(0b100, k, vertex_d.x);
				float weight_y = getNormalizedWeight(0b010, k, vertex_d.y);
				float weight_z = getNormalizedWeight(0b001, k, vertex_d.z);
				float weight = weight_x * weight_y * weight_z;
				
				MarchableCube cornerElement = cell.getCornerElement(k, tree);
				
				currentRow.add(new col_val(cornerElement.getIndex(), weight));
			}
		}		
		return D0;
	}
	
	/**
	 * 
	 * @param dir
	 * @param cornerIndex
	 * @param alpha
	 * @return
	 */
	private static float getNormalizedWeight(long dir, int cornerIndex, float alpha){
		boolean isCornerIndexPositiveDir = (dir & cornerIndex) != dir;
		return (isCornerIndexPositiveDir ? (1.0f - alpha) : alpha);
	}
	
	/**
	 * 
	 * @param tree
	 * @param cell
	 * @param point
	 * @return
	 */
	private static Vector3f getRelativeCoordinates(HashOctree tree, HashOctreeCell cell, Point3f point){
		Point3f v_000 = cell.getCornerElement(0b000, tree).getPosition();
		
		// vertex with relativ coordinates
		Vector3f vertex_d = new Vector3f();
		vertex_d.sub(point, v_000);
		
		// all sides of cell have same length
		vertex_d.scale(1f/cell.side); 
		
		return vertex_d;
	}

	/**
	 * matrix with three rows per point and 1 column per octree vertex.
	 * rows with i%3 = 0 cover x gradients, =1 y-gradients, =2 z gradients;
	 * The row i, i+1, i+2 correxponds to the point/normal i/3.
	 * Three consecutant rows belong to the same gradient, the gradient in the cell
	 * of pointcloud.point[row/3]; 
	 */
	public static CSRMatrix D1Term(HashOctree tree, PointCloud cloud) {
		CSRMatrix mat = new CSRMatrix(0, tree.numberOfVertices());
		
		for (Point3f p: cloud.points) {
			HashOctreeCell c = tree.getCell(p);
			float gradientNormalizationTerm = 1/(4*c.side);
			//add 3 rows, for x, y and z derivative
			mat.addRow();
			ArrayList<col_val> xRow = mat.lastRow();
			
			mat.addRow();
			ArrayList<col_val> yRow = mat.lastRow();
			
			mat.addRow();
			ArrayList<col_val> zRow = mat.lastRow();
			
			for (int i = 0; i < 8; i++) {
				float xGrad = (i & 0b100) == 0b100 ? gradientNormalizationTerm : -gradientNormalizationTerm;
				float yGrad = (i & 0b010) == 0b010 ? gradientNormalizationTerm : -gradientNormalizationTerm;
				float zGrad = (i & 0b001) == 0b001 ? gradientNormalizationTerm : -gradientNormalizationTerm;
				int idx = c.getCornerElement(i, tree).getIndex();
				xRow.add(new col_val(idx, xGrad));
				yRow.add(new col_val(idx, yGrad));
				zRow.add(new col_val(idx, zGrad));
			}
		}		
		return mat;
	}
	
	
	
	public static CSRMatrix RTerm(HashOctree tree){
		CSRMatrix mat = new CSRMatrix(0, tree.numberOfVertices());
		float scaleFactor = 0;
		for (HashOctreeVertex j: tree.getVertices()) {
			for (int shift = 0b100; shift > 0b000; shift >>= 1) {
				HashOctreeVertex i = tree.getNbr_v2vMinus(j, shift); //nbr in minus direction
				if (i == null)
					continue;
				HashOctreeVertex k = tree.getNbr_v2v(j, shift); //nbr in plus direction
				if (k == null)
					continue;

				
				mat.addRow();
				ArrayList<col_val> currentRow = mat.lastRow();
				
				
				float dist_ij = i.getPosition().distance(j.getPosition());
				float dist_kj = k.getPosition().distance(j.getPosition());
				float dist_ik = dist_ij + dist_kj;
				currentRow.add(new col_val(j.getIndex(), 1));
				currentRow.add(new col_val(k.getIndex(), -dist_ij/(dist_ik)));
				currentRow.add(new col_val(i.getIndex(), -dist_kj/(dist_ik)));
				scaleFactor += dist_ij*dist_kj;
			}
		}
		mat.scale(1/scaleFactor);
		return mat;
	}

	/**
	 * Set up the linear system for ssd: append the three matrices, 
	 * appropriately scaled. And set up the appropriate right hand side, i.e. the
	 * b in Ax = b
	 * @param tree
	 * @param pc
	 * @param lambda0
	 * @param lambda1
	 * @param lambda2
	 * @return
	 */
	public static LinearSystem ssdSystem(HashOctree tree, PointCloud pc, 
			float lambda0,
			float lambda1,
			float lambda2){
		
				
		LinearSystem system = new LinearSystem();
		system.mat = new CSRMatrix(0, tree.numberOfVertices());
		system.b = new ArrayList<Float>();

		int N = tree.numberOfVertices();
		CSRMatrix D0 = D0Term(tree, pc);
		system.mat.append(D0, (float)Math.sqrt(lambda0/N));
		system.b.addAll(new ArrayList<Float>(Collections.nCopies(D0.nRows, 0f)));
		
		CSRMatrix D1 = D1Term(tree, pc);
		float scaleD1 =  (float) Math.sqrt(lambda1/N);
		system.mat.append(D1, scaleD1);
		
		for (Vector3f n: pc.normals) {
			system.b.add(n.x*scaleD1);
			system.b.add(n.y*scaleD1);
			system.b.add(n.z*scaleD1);
		}
		
		CSRMatrix R = RTerm(tree);
		//careful, the 1/sum(..) was already scaled in method
		float scaleR = (float) Math.sqrt(lambda2);
		system.mat.append(R, scaleR);
		system.b.addAll(new ArrayList<Float>(Collections.nCopies(R.nRows, 0f)));
		
		return system;
	}

}
