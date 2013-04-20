package edu.gatech.aristotle.jsretest.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import edu.gatech.aristotle.jsretest.CFGBuilder;
import edu.gatech.aristotle.jsretest.RegressionalComparator;
import edu.gatech.aristotle.jsretest.cfg.ControlFlowGraph;
import edu.gatech.aristotle.jsretest.cfg.JsSourceNode;
import edu.gatech.aristotle.jsretest.coverage.CoverageData;
import edu.gatech.aristotle.jsretest.coverage.Edge;

public class TestSelector {

	@Test
	public void test() throws Exception{
		CFGBuilder builder=new CFGBuilder("/Users/jielu/Dropbox/JSCover-0.2.6_updated/doc/rts_test1/switchTest.js","/doc/rts_test1/switchTest.js");
		CFGBuilder builder2=new CFGBuilder("/Users/jielu/Dropbox/JSCover-0.2.6_updated/doc/rts_test1/switchTest_modified.js","qunit_mod1.js");
		RegressionalComparator comparator=new RegressionalComparator(builder,builder2);
	    System.out.println("============================ Dangerous Edges =====================");
		System.out.println(comparator.getDangerousEdgeIdentifiers());
		
		CoverageData cov = new CoverageData("/Users/jielu/Dropbox/JSCover-0.2.6_updated/target");
	    ArrayList<String> tests = cov.getCoverageByEdges(comparator.getDangerousEdgeIdentifiers());

		
		System.out.println();
	    System.out.println("============================ Selected Tests =====================");
	    for(int i=0; i<tests.size(); i++){
	    	System.out.println(tests.get(i));
	    }
		
	}

}
