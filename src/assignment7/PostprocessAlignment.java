package assignment7;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.Vertex;
import meshes.WireframeMesh;

public class PostprocessAlignment {
	private WireframeMesh baseMesh;
	private List<WireframeMesh> meshList;
	private Features baseFeatures;
	private List<Features> featuresList;
	
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
		
		if(!validInput()){
			throw new Exception("incorrect input");
		}
		
		shiftFeaturesToAvgZero();
		rescaleEarDistanceToOne();
		constructRotationMatrix();
		applyAlignmentRotation();
	}
	
	private boolean validInput(){
		boolean sameCount = meshList.size() == featuresList.size();
		boolean atLeastTwo = meshList.size() > 1 && featuresList.size() > 1;
		return sameCount&&atLeastTwo;
	}
	
	/**
	 * for each mesh, find average for all feature positions.
	 * subtract each mesh's average for all its positions.
	 * this will center the whole mesh according to 
	 * its feature positions towards zero.
	 */
	private void shiftFeaturesToAvgZero(){
		int counter = 0;
		int featureCount = featuresList.size();
		for(Features features : featuresList){
			WireframeMesh currentMesh = meshList.get(counter);
			float averageShiftAmount = 0.0f;
			
			// for each feature id sum up positions
			for(Integer id : features.getIds()){
				Point3f p = currentMesh.vertices.get(id);
				float delta = (float) (Math.sqrt(p.x*p.x* + p.y*p.y* + p.z*p.z));;
				averageShiftAmount += delta;
			}
			averageShiftAmount /= featureCount;
			Vector3f posShow = new Vector3f(averageShiftAmount, averageShiftAmount, averageShiftAmount);
			
			ArrayList<Point3f> positions = currentMesh.vertices;
			for(Point3f position : positions){
				position.sub(posShow);
			}
			counter++;
		}
	}
	
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
			float length = (float) Math.sqrt(delta.x+delta.x +
					delta.y+delta.y + delta.z*delta.z);
			Matrix3f scaleT = new Matrix3f();
			scaleT.setColumn(0, new Vector3f(1.0f/length, 0f, 0f));
			scaleT.setColumn(1, new Vector3f(0.0f, 1.0f/length, 0f));
			scaleT.setColumn(2, new Vector3f(0f, 0f, 1.0f/length));
			
			//scale each vertex of current mesh by its scale transformation
			for(Point3f p : meshVertices){
				
				
			}
		}
	}
	
	private void constructRotationMatrix(){
		
	}
	
	private void applyAlignmentRotation(){
		
	}
	
	public List<WireframeMesh> getAlignedMeshes(){
		return null;
	}
}
