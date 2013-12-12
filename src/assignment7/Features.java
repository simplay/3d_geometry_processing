package assignment7;

import java.util.List;

public class Features {
	private List<Integer> ids;
	private List<String> labels;
	
	// TODO: read features from file at path
	// assing ids and labels reading the file
	public Features(String path){
		
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
}
