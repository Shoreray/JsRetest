package edu.gatech.aristotle.jsretest.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.gatech.aristotle.jsretest.CFGBuilder;
import edu.gatech.aristotle.jsretest.RegressionalComparator;
import edu.gatech.aristotle.jsretest.cfg.ControlFlowGraph;
import edu.gatech.aristotle.jsretest.cfg.JsSourceNode;

public class TestComparator {

	@Test
	public void test() throws Exception{
		CFGBuilder builder=new CFGBuilder("compareTests/qunit.js","qunit.js");
		CFGBuilder builder2=new CFGBuilder("compareTests/qunit_mod1.js","qunit_mod1.js");
		RegressionalComparator comparator=new RegressionalComparator(builder,builder2);
		
		for(String cfgId:builder.getCFGs().keySet()){
			ControlFlowGraph<JsSourceNode> cfg=builder.getCFGs().get(cfgId);
			cfg.dotify("compareTests\\dotFiles\\"+builder.getSourceURI()+"."+cfgId+".dot");
			Runtime.getRuntime().exec("dot -Tpdf compareTests\\dotFiles\\"+builder.getSourceURI()+"."+cfgId+".dot -o compareTests\\pdfFiles\\"+builder.getSourceURI()+"."+cfgId+".pdf");
		}
		System.out.println(comparator.getDangerousEdgeIdentifiers());
		
	}

}
