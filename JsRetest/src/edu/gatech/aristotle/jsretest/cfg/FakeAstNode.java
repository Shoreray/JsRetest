package edu.gatech.aristotle.jsretest.cfg;

import org.mozilla.javascript.ast.NodeVisitor;

/**
 * Don't call any methods other than those explicitly defined in this class.
 * This class should only be used as the content of a JsSourceNode instance.
 * 
 * @author Shoreray
 *
 */
public class FakeAstNode extends org.mozilla.javascript.ast.AstNode{
	
	private String source;
	private int lineno;
	
	public FakeAstNode(String source,int lineno){
		
		this.source=source;
		this.lineno=lineno;
		if(source==null){
			this.source="";
		}
	}

	@Override
	public String toSource(int depth) {
		
		return source;
	}
	
	public int getLineno(){
		return lineno;
	}
	
	public boolean isSingleStatement(){
		return true;
	}
	
	public boolean isStructuralNode(){
		return false;
	}
	
	public boolean isJumpStatement(){
		return false;
	}

	@Override
	public void visit(NodeVisitor visitor) {
		visitor.visit(this);
		
	}
	
	public int hashCode(){
		return (toSource()+getLineno()).hashCode();
	}
	
	public boolean equals(Object o){
		if(!(o instanceof FakeAstNode)){
			return false;
		}
		FakeAstNode astNode=(FakeAstNode)o;
		return (toSource()+getLineno()).equals(astNode.toSource()+astNode.getLineno());
		
	}
	
	

}
