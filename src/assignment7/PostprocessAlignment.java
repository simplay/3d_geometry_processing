package assignment7;

import java.util.ArrayList;
import java.util.List;

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
	 * center each mesh to avg position zero
	 */
	private void shiftFeaturesToAvgZero(){
		int counter = 0;
		int featureCount = featuresList.size();
		for(Features features : featuresList){
			WireframeMesh currentMesh = meshList.get(counter);
			float averageShiftAmount = 0.0f;
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
		
	}
	
	private void constructRotationMatrix(){
		
	}
	
	private void applyAlignmentRotation(){
		
	}
	
	public List<List<Vertex>> getAlignedMeshes(){
		return null;
	}
}
