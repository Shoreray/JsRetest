package edu.gatech.aristotle.jsretest.coverage;

public class Edge {
	private String scriptName;
	private int sourceLine;
	private int targetLine;
	private String label;
	
	public Edge(){
		scriptName = "";
		sourceLine = -1;
		targetLine = -1;
		label = "";
	}
	
	public Edge(String scriptName,int sourceLine,int destLine,String label){
		this.scriptName=scriptName;
		this.sourceLine=sourceLine;
		this.targetLine=destLine;
		this.label=label;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public int getSourceLine() {
		return sourceLine;
	}

	public void setSourceLine(int sourceLine) {
		this.sourceLine = sourceLine;
	}

	public int getTargetLine() {
		return targetLine;
	}

	public void setTargetLine(int targetLine) {
		this.targetLine = targetLine;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String toString(){
		return ""+sourceLine+" --> "+targetLine+((label==null || "".equals(label))?"":"  ("+label+")");
	}

}
