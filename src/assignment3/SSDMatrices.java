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
		// find its cell
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
				
				// get corner c_k of considered cell_p
				MarchableCube cornerElement = cell.getCornerElement(k, tree);
				// depending on index of row depending on corner index.
				currentRow.add(new col_val(cornerElement.getIndex(), weight));
			}
		}		
		return D0;
	}
	
	/**
	 * Get correct interpolation weight.
	 * See for further convenience slides 04, surface reconstruction
	 * or some reference to trilinear interpolation.
	 * @param dir direction we are interested in 
	 * x = 0b100
	 * y = 0b010
	 * z = 0b001
	 * @param cornerIndex currenter corner vertex of cell we are considering
	 * @param alpha normalized value for given direction.
	 * @return
	 */
	private static float getNormalizedWeight(long dir, int cornerIndex, float alpha){
		boolean isCornerIndexPositiveDir = (dir & cornerIndex) == dir;
		return (isCornerIndexPositiveDir ? alpha : (1.0f - alpha));
	}
	
	/**
	 * Find relative representation for vertex v = (x, y, z)
	 * whereas v is the vertex representation of our provided point 
	 * from the point cloud.
	 * @param tree hashoctree
	 * @param cell cell of this point
	 * @param point point in point cloud
	 * @return returns relative coordinates of provided input point.
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
	 * Matrix D_1 has 3 times number of point in PointCloud rows and 
	 * number of hashtree vertices columns.
	 * Each line computes either the x, the y or the z gradient of f on some cell
	 * Sanity check: if f is a sampled linear function, 
	 * say ax + by + cz, then, D1 times f is equal (a,b,c) on each cell.
	 * 
	 * This Matrix is representing the energy E_D_1.
	 * 
	 */
	public static CSRMatrix D1Term(HashOctree tree, PointCloud cloud) {
		CSRMatrix D1 = new CSRMatrix(0, tree.numberOfVertices());
		
		for (Point3f p: cloud.points) {
			HashOctreeCell c = tree.getCell(p);
			// 1 over 4 times delta_alpha: cell has same side lengths
			float gradientNormalizationTerm = 1f/(4f*c.side);
			
			//add 3 rows, for x, y and z derivative
			D1.addRow();
			ArrayList<col_val> dxRow = D1.lastRow();
			
			D1.addRow();
			ArrayList<col_val> dyRow = D1.lastRow();
			
			D1.addRow();
			ArrayList<col_val> dzRow = D1.lastRow();
			
			// for each corner vertec of considered cell
			for (int k = 0; k < 8; k++) {
				
				float grad_x = getNormalizedGradient(0b100, k, gradientNormalizationTerm);
				float grad_y = getNormalizedGradient(0b010, k, gradientNormalizationTerm);
				float grad_z = getNormalizedGradient(0b001, k, gradientNormalizationTerm);
				
				int rowIndex = c.getCornerElement(k, tree).getIndex();
				
				dxRow.add(new col_val(rowIndex, grad_x));
				dyRow.add(new col_val(rowIndex, grad_y));
				dzRow.add(new col_val(rowIndex, grad_z));
			}
		}		
		return D1;
	}
	
	/**
	 * Get correct interpolation weight.
	 * See for further convenience slides 04, surface reconstruction
	 * slide number 39, the provided vector
	 * @param dir direction we are interested in 
	 * x = 0b100
	 * y = 0b010
	 * z = 0b001
	 * @param dir considered direction: see above.
	 * @param cornerIndex index of considered vertex
	 * @param grad normalization weight
	 * @return
	 */
	private static float getNormalizedGradient(long dir, int cornerIndex, float grad){
		boolean isCornerIndexPositiveDir = (dir & cornerIndex) == dir;
		return (isCornerIndexPositiveDir ? grad : -grad);
	}
	
	/**
	 * Matrix R ? number of rows and 
	 * number of hashtree vertices columns.
	 * 
	 * Construct hessain by the follwoing constraints:
	 * If a vertex has a positive and a negative vertex neighbor in some direction.
	 * Second derivative is equal zero in that direction.
	 * 
	 * i.e. constraint is:
	 * (f_j - f_i)/dist_ji - (f_k - f_j)/dist_jk = 0
	 * 
	 * f_i -------- f_k ------------- f_k
	 * [  dist_ji   ] [     dist_jk     ]
	 * 
	 * #1
	 * which is the same as:
	 * f_j - (dist_ij * f_k)/(dist_ij + dist_kj) - (dist_jk * f_i)/(dist_ij + dist_kj) = 0
	 * 
	 * #2
	 * Sace R term with 1 / sum(neighbor_tribles(i,j,k) * dist_ij * dist_jk)
	 * 
	 * #3
	 * Final R matrix has one row per neighbor vertex triple looking like this:
	 * 
	 * [... 1 ... -dist_ij/(dust_ij+dist_kj) ... -  -dist_jk/(dust_ij+dist_kj) ...]
	 *    col_j               col_k                            col_i
	 *    
	 * 
	 * Sanity check: R times f should be 0 for any linear function f.
	 * @param tree
	 * @return
	 */
	public static CSRMatrix RTerm(HashOctree tree){
		CSRMatrix R = new CSRMatrix(0, tree.numberOfVertices());
		float scaleFactor = 0;
		
		for (HashOctreeVertex j: tree.getVertices()) {
			// visit whole neighborhood
			for (int mask = 0b100; mask > 0b000; mask >>= 1) {
				
				// right neighbor of j
				HashOctreeVertex k = tree.getNbr_v2v(j, mask); //nbr in plus direction
				if (k == null) continue;
				
				// left neighbor of j
				HashOctreeVertex i = tree.getNbr_v2vMinus(j, mask); //nbr in minus direction
				if (i == null) continue;
				
				R.addRow();
				ArrayList<col_val> currentRow = R.lastRow();
				
				// compute all the required distances
				float dist_ij = i.getPosition().distance(j.getPosition());
				float dist_kj = k.getPosition().distance(j.getPosition());
				
				// since from i to j and from j to k is distance from i to k
				float dist_ik = dist_ij + dist_kj;
				
				// See #3
				currentRow.add(new col_val(j.getIndex(), 1));
				currentRow.add(new col_val(k.getIndex(), -dist_ij/(dist_ik)));
				currentRow.add(new col_val(i.getIndex(), -dist_kj/(dist_ik)));
				
				// See #2
				scaleFactor += dist_ij*dist_kj;
			}
		}
		// See #2
		R.scale(1/scaleFactor);
		
		return R;
	}

	/**
	 * Construct final matrix stack:
	 * a := sqrt(lambda_0 / N)
	 * b := sqrt(lambda_1 / N)
	 * c := sqrt(lambda_R / sum {...})
	 * [a*D0; b*D1; c*R]*x = [0; sqrt(lambda_1/N)*n; 0]
	 * <=>
	 * Ax = b
	 * Set up the linear system for ssd: append the three matrices, 
	 * appropriately scaled. And set up the appropriate right hand side, i.e. the
	 * b in Ax = b
	 * @param tree
	 * @param pc
	 * @param lambda_0
	 * @param lambda_1
	 * @param lambda_r
	 * @return
	 */
	public static LinearSystem ssdSystem(HashOctree tree, PointCloud pc, 
			float lambda_0,
			float lambda_1,
			float lambda_r){
		
		// initialize required data
		LinearSystem linSystem = new LinearSystem();
		linSystem.mat = new CSRMatrix(0, tree.numberOfVertices());
		linSystem.b = new ArrayList<Float>();

		// Matrix Terms and other data	
		CSRMatrix D0 = D0Term(tree, pc);
		CSRMatrix D1 = D1Term(tree, pc);
		CSRMatrix R = RTerm(tree);
		int N = tree.numberOfVertices();
		
		// Patch matrix A 
		
		linSystem.mat.append(D0, (float)Math.sqrt(lambda_0/N));
		
		float scaleD1 =  (float) Math.sqrt(lambda_1/N);
		linSystem.mat.append(D1, scaleD1);
		
		// we do not have to devide by the sum, 
		// since R has already been scaled by 
		// this sum within its computation, See #2
		float scaleR = (float) Math.sqrt(lambda_r);
		
		linSystem.mat.append(R, scaleR);
		
		
		// Patch right-handside, vector b 
		
		for(int k = 0; k < D0.nRows; k++){
			linSystem.b.add(0f);
		}
		
		for (Vector3f n: pc.normals) {
			linSystem.b.add(n.x*scaleD1);
			linSystem.b.add(n.y*scaleD1);
			linSystem.b.add(n.z*scaleD1);
		}
		
		for(int k = 0; k < R.nRows; k++){
			linSystem.b.add(0f);
		}
			
		return linSystem;
	}
}
