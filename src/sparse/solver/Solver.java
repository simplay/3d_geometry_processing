package sparse.solver;

import java.util.ArrayList;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import sparse.CSRMatrix;
import sparse.LinearSystem;

public abstract class Solver {

	/**
	 * x will be used as an initial guess, the result will be stored in x
	 * 
	 * @param mat
	 * @param b
	 * @param x
	 */
	public abstract void solve(CSRMatrix mat, ArrayList<Float> b,
			ArrayList<Float> x);

	public void solve(LinearSystem l, ArrayList<Float> x) {
		if (l.mat.nCols == l.mat.nRows) {
			solve(l.mat, l.b, x);
		} else {
			throw new UnsupportedOperationException(
					"can solve only square mats");
		}
	}
	
	private ArrayList<Float> helperX(ArrayList<Tuple3f> vec){
		ArrayList<Float> arr = new ArrayList<Float>();
		for(Tuple3f t : vec){
			arr.add(t.x);
		}
		return arr;
	}
	
	private ArrayList<Float> helperY(ArrayList<Tuple3f> vec){
		ArrayList<Float> arr = new ArrayList<Float>();
		for(Tuple3f t : vec){
			arr.add(t.y);
		}
		return arr;
	}
	
	private ArrayList<Float> helperZ(ArrayList<Tuple3f> vec){
		ArrayList<Float> arr = new ArrayList<Float>();
		for(Tuple3f t : vec){
			arr.add(t.z);
		}
		return arr;
	}
	
	public void solveTuple(CSRMatrix A, ArrayList<Tuple3f> b,
		ArrayList<Tuple3f> x) {
		ArrayList<Float> bX = new ArrayList<Float>();
		ArrayList<Float> bY = new ArrayList<Float>();
		ArrayList<Float> bZ = new ArrayList<Float>();
		
		ArrayList<Float> xX = new ArrayList<Float>();
		ArrayList<Float> xY = new ArrayList<Float>();
		ArrayList<Float> xZ = new ArrayList<Float>();
		
		
		if(xX.size() != x.size()) xX = helperX(x);
		if(xY.size() != x.size()) xY = helperY(x);
		if(xZ.size() != x.size()) xZ = helperZ(x);
		
		for (Tuple3f t : b) {
			bX.add(t.x);
			bY.add(t.y);
			bZ.add(t.z);
		}
		
		solve(A, bX, xX);
		solve(A, bY, xY);
		solve(A, bZ, xZ);
		x.clear();
		
		for (int i = 0; i < b.size(); i++) {
			x.add(new Vector3f(xX.get(i), xY.get(i), xZ.get(i)));
		}

	}



}
