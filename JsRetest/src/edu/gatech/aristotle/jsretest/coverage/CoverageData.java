package edu.gatech.aristotle.jsretest.coverage;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class CoverageData {
	// Coverage matrix for all test cases
    // ArrayList<String> = <t1, t2...>; ArrayList<ArrayList<String>> = <t1, t2..>, <t2, t3>....; HashMap... = <script1, [<t1, t2..>, <t2, t3>....]..>
    private HashMap<String, HashMap<Integer, ArrayList<String>>> linesCoverage;
    // ArrayList<String> = <t1, t2...>; HashMap<String, ArrayList<String>> = [1T, <t1, t2>..]; HashMap ... = <script, [1T, <t1, t2>]>..
    private HashMap<String, HashMap<String, ArrayList<String>>> branchesCoverage;
    
  //script -> 1,1-> test1
    public static HashMap<String, HashMap<String, ArrayList<String>>> switchCoverage;

    
    private String reportDir;
    private final String lineCoverageFile = "wholeLineCoverage.dat";
    private final String branchCoverageFile = "wholeBranchCoverage.dat";
    private final String switchCoverageFile = "wholeSwitchCoverage.dat";
    
    public CoverageData(String reportDir){
    	this.reportDir = reportDir;
    	init();
    }
    
    private void init(){
    	// Deserializing coverage data
    	try{
    		FileInputStream fileIn = new FileInputStream(reportDir + File.separator + lineCoverageFile);
    		ObjectInputStream in = new ObjectInputStream(fileIn);
    		linesCoverage = (HashMap<String, HashMap<Integer, ArrayList<String>>>)in.readObject();
    		in.close();
    		fileIn.close();
    		
    		fileIn = new FileInputStream(reportDir + File.separator + branchCoverageFile);
    	    in = new ObjectInputStream(fileIn);
    	    branchesCoverage = (HashMap<String, HashMap<String, ArrayList<String>>>)in.readObject();
    		in.close();
    		fileIn.close();
    		
    		fileIn = new FileInputStream(reportDir + File.separator + switchCoverageFile);
    	    in = new ObjectInputStream(fileIn);
    	    switchCoverage = (HashMap<String, HashMap<String, ArrayList<String>>>)in.readObject();
    		in.close();
    		fileIn.close();
    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    // Get covered tests for given dangerous edges
    public ArrayList<String> getCoverageByEdges(ArrayList<Edge> dangerousEdges){
    	ArrayList<String> coveredTests = new ArrayList<String>();
    	for(int i=0; i<dangerousEdges.size(); i++){
    		Edge edge = dangerousEdges.get(i);
    		ArrayList<String> ts = null;
    		
    		String edgeLabel = edge.getLabel();
    		if(edgeLabel == "" || edgeLabel.equals("exception")){
    			// This is a normal edge(straight line in one basic block), need to cover both src and target
    			// Or this is a special exception edge, both source and target should be covered too
    			ts = getCommonTests(linesCoverage.get(edge.getScriptName()).get(edge.getSourceLine()), linesCoverage.get(edge.getScriptName()).get(edge.getTargetLine()));
    		}else if(edgeLabel.endsWith("T") || edgeLabel.endsWith("F")){
    			// This is a branch edge, need to cover sourceLineT, sourceLineF
    			ts = branchesCoverage.get(edge.getScriptName()).get(edge.getSourceLine() + edgeLabel);
    		}else{
    			// This is a switch-case branch, need to cover the source and label(1,1)
    			ts = switchCoverage.get(edge.getScriptName()).get(edge.getSourceLine() + "," + edgeLabel);
    		}
    		
    		if(ts != null){
    			for(int j=0; i<ts.size(); j++){
    				if(!coveredTests.contains(ts.get(j)))
    					coveredTests.add(ts.get(j));
    			}
    		}
    	}
    	
    	return coveredTests;
    }
    
    // Get converged test cases, cover both source and target
    public ArrayList<String> getCommonTests(ArrayList<String> ts_src, ArrayList<String> ts_target){
    	ArrayList<String> ts = new ArrayList<String>();
    	for(int i=0; i<ts_target.size(); i++){
    		String test = ts_target.get(i);
    		if(ts_src.contains(test)){
    			ts.add(test);
    		}
    	}
    	
    	return ts;
    }

}
