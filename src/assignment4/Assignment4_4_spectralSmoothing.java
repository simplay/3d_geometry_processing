package assignment4;

import glWrapper.GLHalfedgeStructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Color3f;

import openGL.MyDisplay;
import sparse.CSRMatrix;
import sparse.SCIPYEVD;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;



/**
 * You can implement the spectral smoothing application here....
 * @author Alf
 *
 */
public class Assignment4_4_spectralSmoothing {

	private static boolean isSpectralDemo = false;
	
    public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException{
    	if(isSpectralDemo) spectralSmoothingDemo();
    	else harmonicsDemo();
    }
    
    private static void spectralSmoothingDemo(){
        HalfEdgeStructure hs = new HalfEdgeStructure();
        WireframeMesh mesh = null;
		try {
			mesh = ObjReader.read("objs/bunny.obj", false);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        try {
			hs.init(mesh);
		} catch (MeshNotOrientedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DanglingTriangleException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}        
        
        MyDisplay d = new MyDisplay();
        
        try {
			SpectralSmoothing.smooth(hs, 6);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        GLHalfedgeStructure glHs = new GLHalfedgeStructure(hs);
        glHs.configurePreferredShader("shaders/trimesh_flatColor3f.vert",
                        "shaders/trimesh_flatColor3f.frag",
                        "shaders/trimesh_flatColor3f.geom");
        d.addToDisplay(glHs);
    }
    
    
    
    private static void harmonicsDemo(){
        HalfEdgeStructure hs = new HalfEdgeStructure();
        WireframeMesh mesh = null;
		try {
			mesh = ObjReader.read("objs/bunny.obj", false);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        try {
			hs.init(mesh);
		} catch (MeshNotOrientedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DanglingTriangleException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        CSRMatrix m = LMatrices.symmetricCotanLaplacian(hs);
        ArrayList<Float> eigenValues = new ArrayList<Float>();
        ArrayList<ArrayList<Float>> eigenVectors = new ArrayList<ArrayList<Float>>();
        try {
			SCIPYEVD.doSVD(m, "eigenStuff", 20, eigenValues, eigenVectors);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        MyDisplay d = new MyDisplay();
        int eigenVectorCount = 0;
        for (ArrayList<Float> eigenVector: eigenVectors) {
                GLHalfedgeStructure glHs = new GLHalfedgeStructure(hs);
                ArrayList<Color3f> eigenVectorColor = new ArrayList<Color3f>();
                float minEV = Collections.min(eigenVector);
                float maxEV = Collections.max(eigenVector);
                // coloring as described by @alf
                for (Float v: eigenVector) {
                        float vTilde = (v - minEV)/(Math.max(maxEV - minEV, 0.001f));
                        Color3f c = new Color3f();
                        c.x = Math.min(2*Math.max(vTilde, 0.1f), 0.8f);
                        c.z = Math.min(2*Math.max(1 - vTilde, 0.1f), 0.8f);
                        c.y = Math.min(c.x, c.z);
                        eigenVectorColor.add(c);
                }
                glHs.addCol(eigenVectorColor, "color");
                glHs.configurePreferredShader("shaders/trimesh_flatColor3f.vert",
                                "shaders/trimesh_flatColor3f.frag",
                                "shaders/trimesh_flatColor3f.geom", "Eigenvector nr. " + ++eigenVectorCount);
                d.addToDisplay(glHs);
        }
    
    }
    
    
    
}
