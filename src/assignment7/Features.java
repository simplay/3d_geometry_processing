package assignment7;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Features {
	private List<Integer> ids;
	private List<String> labels;
	
	// assing ids and labels reading the file
	public Features(String file) throws Exception{
		this.ids = new ArrayList<Integer>();
		this.labels = new ArrayList<String>();
		
		// regex whole label stuff
	    BufferedReader br = new BufferedReader(new FileReader(file));
	    try {
	        String line = br.readLine();
	        while (line != null) {
	        	String[] substrings = line.split("\\s+");
	        	int idAsInt = Integer.parseInt(substrings[0]);
	        	ids.add(idAsInt);
	        	labels.add(substrings[1]);
	        	System.out.println(line);
	        	System.out.println(ids.get(ids.size()-1));
	        	System.out.println(labels.get(labels.size()-1));
	        	System.out.println();
	        	
	            line = br.readLine();
	        }
	
	    } finally {
	        br.close();
	    }
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
