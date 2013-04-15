package edu.gatech.aristotle.jsretest.cfg;
import java.util.ArrayList;

import org.mozilla.javascript.ast.*;

public class JsSourceNode {
	
	private AstNode astNode;
	
	private ArrayList<String> functions;
	
	public JsSourceNode(AstNode astNode){
		if(astNode==null){
			throw new IllegalArgumentException("AstNode cannot be null");
		}
		this.astNode=astNode;
		functions=new ArrayList<String>();
	}
	
	public AstNode getAstNode(){
		return astNode;
	}
	
	public int getLineno(){
		return astNode.getLineno();
	}
	
	public String getSourceText(){
		return astNode.getStatementText();
	}
	
	public int getAstNodeType(){
		return astNode.getType();
	}
	
	public void addFunctionIdentifier(String identifier){
		if(functions.contains(identifier)){
			return;
		}
		functions.add(identifier);
	}
	
	public ArrayList<String> getFunctionIdentifiers(){
		return new ArrayList<String>(functions);
	}
	
	public String toString(){
		return getSourceText();
	}

}
