package assignment7;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.Vertex;
import meshes.WireframeMesh;

public class PostprocessAlignment {
	private WireframeMesh wireframeMesh;
	private List<Vertex> features;

	public PostprocessAlignment(WireframeMesh mesh, List<Vertex> featurelist){
		this.wireframeMesh = mesh;
		this.features = featurelist;
		shiftFeaturesToAvgZero();
		rescaleEarDistanceToOne();
		
	}
	
	private void shiftFeaturesToAvgZero(){
		float averageShiftAmount = 0.0f;
		for(Vertex feature : features){
			Point3f p = feature.getPos();
			float delta = (float) (Math.sqrt(p.x*p.x* + p.y*p.y* + p.z*p.z));;
			averageShiftAmount += delta;
		}
		
		Vector3f posShow = new Vector3f(averageShiftAmount,averageShiftAmount,averageShiftAmount);
		ArrayList<Point3f> positions = wireframeMesh.vertices;
		for(Point3f position : positions){
			position.sub(posShow);
		}
	}
	
	private void rescaleEarDistanceToOne(){
		
	}
}
