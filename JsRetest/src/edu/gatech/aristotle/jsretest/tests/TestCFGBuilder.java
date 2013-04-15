package edu.gatech.aristotle.jsretest.tests;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.Test;

import edu.gatech.aristotle.jsretest.*;
import edu.gatech.aristotle.jsretest.cfg.*;

public class TestCFGBuilder {

	@Test
	public void test() throws IOException {
		
		File testsDir=new File("jsTests");
		File pdfFileDir=new File("jsTests\\pdfFiles");
		for(File pdfFile:pdfFileDir.listFiles()){
			pdfFile.delete();
		}
		for(File testFile:testsDir.listFiles()){
			if(!testFile.isFile()){
				continue;
			}
			try{
				
				String filename=testFile.getName();
				System.out.println("=====Processing "+filename+"=======");
				System.out.flush();
				CFGBuilder builder=new CFGBuilder("jsTests\\"+filename,filename);
				for(String cfgId:builder.getCFGs().keySet()){
					ControlFlowGraph<JsSourceNode> cfg=builder.getCFGs().get(cfgId);
					cfg.dotify("jsTests\\dotFiles\\"+filename+"."+cfgId+".dot");
					Runtime.getRuntime().exec("dot -Tpdf jsTests\\dotFiles\\"+filename+"."+cfgId+".dot -o jsTests\\pdfFiles\\"+filename+"."+cfgId+".pdf");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		
	}

}
