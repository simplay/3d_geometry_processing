package assignment7;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;

import assignment6.Linalg3x3;
import assignment6.SVDProvider;

import meshes.HalfEdge;
import meshes.Vertex;
import meshes.WireframeMesh;

public class PostprocessAlignment {
	private WireframeMesh baseMesh;
	private List<WireframeMesh> meshList;
	private Features baseFeatures;
	private List<Features> featuresList;
	private Linalg3x3 decomposer;
	private ArrayList<Matrix3f> rotations;
	/**
	 * Performs allignment
	 * Assumption: first element of meshList and featuresList is the reference mesh for the whole allignment process.
	 * @param mesheList
	 * @param featuresList
	 * @throws Exception throws an exception if there are less than two meshes,featuresList or they have not same amount of elements contained
	 */
	public PostprocessAlignment(List<WireframeMesh> meshList, List<Features> featuresList) throws Exception{
		this.baseMesh = meshList.get(0);
		this.meshList = meshList;
		this.baseFeatures = featuresList.get(0);
		this.featuresList = featuresList;
		this.decomposer = new Linalg3x3(3);
		
		this.rotations = new ArrayList<Matrix3f>();
		for (int k = 0; k < baseMesh.vertices.size(); k++) {
			Matrix3f identity = new Matrix3f();
			identity.setIdentity();
			rotations.add(identity);
		}
		
		if(!validInput()){
			throw new Exception("incorrect input");
		}
		
		shiftFeaturesToAvgZero();
//		rescaleEarDistanceToOne();
		applyRotations();

	}
	
	private boolean validInput(){
		boolean sameCount = meshList.size() == featuresList.size();
		boolean atLeastTwo = meshList.size() > 1 && featuresList.size() > 1;
		return sameCount&&atLeastTwo;
	}
	
	/**
	 * for each mesh, find average for all feature positions.
	 * subtract each mesh's average from all its positions.
	 * this will center the whole mesh according to 
	 * its feature positions towards zero.
	 */
	private void shiftFeaturesToAvgZero(){
		int counter = 0;
		int featureCount = featuresList.get(0).getIds().size();
		
		for(Features features : featuresList){
			WireframeMesh currentMesh = meshList.get(counter);
			
			// for each feature id sum up positions
			Vector3f shiftVector = new Vector3f(0,0,0);
			for(Integer id : features.getIds()){
				Point3f p = currentMesh.vertices.get(id);
				shiftVector.add(p);
			}
			shiftVector.scale(1f/featureCount);
			System.out.println("avg shift direction "+shiftVector);
			
			ArrayList<Point3f> positions = currentMesh.vertices;
			int posIdx = 0;
			for(Point3f position : positions){
				position.sub(shiftVector);
				positions.set(posIdx, position);
				posIdx++;
			}
	
			// check if is centered to zero
			Point3f avg = new Point3f(0,0,0);
			for(Integer id : features.getIds()){
				Point3f distVal = currentMesh.vertices.get(id);
				avg.add(distVal);
				
			}
			avg.scale(1f/featureCount);
//			avg.sub(shiftVector);
			
			counter++;
			System.out.println("zero check " + avg.toString());
		}
		System.out.println("shifting towards zero perfromed");
	}
	
	/**
	 * 
	 */
	private void rescaleEarDistanceToOne(){
		int counter = 0;
		for(Features features : this.featuresList){
			WireframeMesh currentMesh = meshList.get(counter);
			List<Integer> earIds = features.getEarIds();
			ArrayList<Point3f> meshVertices = currentMesh.vertices;
			
			// prepare distance vectors
			Point3f earLeftPos = meshVertices.get(earIds.get(0));
			Point3f earRigthPos = meshVertices.get(earIds.get(1));
			Point3f delta = new Point3f();
			delta.sub(earRigthPos, earLeftPos);
			
			// build scale matrix
			float length = (float) Math.sqrt(delta.x*delta.x +
					delta.y*delta.y + delta.z*delta.z);
			
			Matrix3f scaleT = new Matrix3f();
			scaleT.setColumn(0, new Vector3f(1.0f/length, 0f, 0f));
			scaleT.setColumn(1, new Vector3f(0.0f, 1.0f/length, 0f));
			scaleT.setColumn(2, new Vector3f(0f, 0f, 1.0f/length));
			
			//scale each vertex of current mesh by its scale transformation
			int pew = 0;
			for(Point3f p : meshVertices){
				meshVertices.set(pew, matrix3fPoint3fMult(scaleT, p));
				pew++;
			}
		}
		System.out.println("meshes rescaled to one");
	}
	
	public List<WireframeMesh> getAlignedMeshes(){
		List<WireframeMesh> tmp = new LinkedList<WireframeMesh>();
		tmp.add(this.baseMesh);
		for(WireframeMesh mesh : this.meshList){
			tmp.add(mesh);
		}
		return tmp;
	}
	
	private Matrix3f computeRotationMatrixFor(Matrix3f S_i) {
		Matrix3f U = new Matrix3f();
		Matrix3f V = new Matrix3f();
		Matrix3f D = new Matrix3f();
		decomposer.svd(S_i, U, D, V);

		if (U.determinant() < 0) {
			Vector3f lastCol = new Vector3f();
			U.getColumn(2, lastCol);
			lastCol.negate();
			U.setColumn(2, lastCol);
		}
		U.transpose();
		V.mul(U);
		return V;
	}
	
	/**
	 * 
	 */
	public void applyRotations() {
		ArrayList<Point3f> baseVertices = baseMesh.vertices;
		int featuresCount = baseFeatures.getIds().size();
		featuresCount = baseVertices.size();
		
		// construct matrix X from baseFeatures
		CSRMatrix X = new CSRMatrix(0, 3);
		int upperX = 0;
		for(Integer featureId : baseFeatures.getIds()){
			Point3f p = baseVertices.get(featureId);
			X.addRow();
			ArrayList<col_val> currentRow = X.lastRow();
			col_val element1 = new col_val(0, p.x);
			currentRow.add(element1);
			col_val element2 = new col_val(1, p.y);
			currentRow.add(element2);
			col_val element3 = new col_val(2, p.z);
			currentRow.add(element3);
			if(upperX == 2) break;
			upperX++;
		}
		System.out.println("X matrix created");
		
		// compute W matrix which is the identity matrix
		CSRMatrix W = new CSRMatrix(0, 3);
		//initialize the identity matrix part
		for(int i = 0; i< Math.min(featuresCount, featuresCount); i++){
			W.addRow();
			W.lastRow().add(new col_val(i,1));
		}
		
		//fill up the matrix with empty rows.
		for(int i = Math.min(featuresCount, featuresCount); i < featuresCount; i++){
			W.addRow();
		}	
		System.out.println("W matrix created");
		
		// starts counting with idx equlas 1
		for(int idx = 1; idx < meshList.size(); idx++){
			ArrayList<Point3f> otherVertices = this.meshList.get(idx).vertices;
			Features otherFeatures = featuresList.get(idx);
			
			// construct Y matrix
			CSRMatrix Y = new CSRMatrix(0, 3);
			int upper = 0;
			for(Integer featureId : otherFeatures.getIds()){
				Point3f p = otherVertices.get(featureId);
				Y.addRow();
				ArrayList<col_val> currentRow = Y.lastRow();
				col_val element1 = new col_val(0, p.x);
				currentRow.add(element1);
				col_val element2 = new col_val(1, p.y);
				currentRow.add(element2);
				col_val element3 = new col_val(2, p.z);
				currentRow.add(element3);
				if(upper == 2) break;
				upper++;
			}
			Y = Y.transposed();
			
			// compute matrix S
			CSRMatrix S = new CSRMatrix(3, 3);
			CSRMatrix WYt = new CSRMatrix(featuresCount, 3);
			W.mult(Y, WYt);
			X.mult(WYt, S);
			Matrix3f SFull = new Matrix3f();
			
			// set rows for S full
			for(int k = 0; k < 3; k++){
				float e0 = S.getElement(k, 0);
				float e1 = S.getElement(k, 1);
				float e2 = S.getElement(k, 2);
				SFull.setRow(k, new float[]{e0,e1,e2});
			}
			
			// get rotation matrix
			Matrix3f R = computeRotationMatrixFor(SFull);

			// rotate positions
			int pew = 0;
			for(Point3f p : otherVertices){
//				R.transform(p);
				
				otherVertices.set(pew, matrix3fPoint3fMult(R, p));
				otherVertices.set(pew, p);
				pew++;
			}
			System.out.println("rotation for '"+ idx +"' performed");
			System.out.println();
		}
		System.out.println("realignment for all meshes performed");
	}
	
	/**
	 * matrix vector multiplication for special dimensionality case
	 * @param M 3x3 float matrix
	 * @param p 3x1 float vector
	 * @return returns a 3x1 float vector
	 */
	private Point3f matrix3fPoint3fMult(Matrix3f M, Point3f p){
		float[] row = new float[3];
		M.getRow(0, row);
		float val1 = row[0]*p.x + row[1]*p.y + row[2]*p.z;
		M.getRow(1, row);
		float val2 = row[0]*p.x + row[1]*p.y + row[2]*p.z;
		M.getRow(2, row);
		float val3 = row[0]*p.x + row[1]*p.y + row[2]*p.z;
		return new Point3f(val1, val2, val3);
	}
}
