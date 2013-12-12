package assignment7;

import java.util.LinkedList;
import java.util.List;

public class Features {
	private List<Integer> ids;
	private List<String> labels;
	
	// TODO: read features from file at path
	// assing ids and labels reading the file
	public Features(String path) throws Exception{
		throw new Exception("pewpewqqsdfsdfsd");
	}
	
	public int getId(int at){
		return this.ids.get(at);
	}
	
	public String getLabel(int at){
		return this.labels.get(at);
	}
	
	public List<Integer> getIds(){
		return this.ids;
	}
	
	public List<String> getLabels(){
		return this.labels;
	}
	
	/**
	 * Assumption: ear labels are denoted as: 
	 * 'ear_left' and 'ear_right'
	 * convention: from left to right ear
	 * @return returns ids of ears, left, then right
	 */
	public List<Integer> getEarIds(){
		LinkedList<Integer> tmp = new LinkedList<Integer>();
		tmp.add(getFeatureIdFor("ear_left"));
		tmp.add(getFeatureIdFor("ear_right"));
		return tmp;
	}
	
	private int getFeatureIdFor(String targetLabel){
		int counter = 0;
		for(String label : labels){
			if(label.equals(targetLabel)){
				return counter; 
			}
			counter++;
		}
		return -1;
	}
}
