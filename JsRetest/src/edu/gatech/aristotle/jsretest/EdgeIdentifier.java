package edu.gatech.aristotle.jsretest;

public class EdgeIdentifier {
	
	private int sourceLine;
	private int destLine;
	private String label;
	
	public EdgeIdentifier(int sourceLine,int destLine,String label){
		this.sourceLine=sourceLine;
		this.destLine=destLine;
		this.label=label;
	}

	public int getSourceLine() {
		return sourceLine;
	}

	public void setSourceLine(int sourceLine) {
		this.sourceLine = sourceLine;
	}

	public int getDestLine() {
		return destLine;
	}

	public void setDestLine(int destLine) {
		this.destLine = destLine;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
