package edu.gatech.aristotle.jsretest.cfg;


public class ControlFlowEdge<T>{
	
	private T source;
	private T destination;
	private String label;
	
	public ControlFlowEdge(T source,T destination,String label){
		this.source=source;
		this.destination=destination;
		this.label=label;
	}
	
	public ControlFlowEdge(ControlFlowEdge<T> e){
		this.source=e.getSource();
		this.destination=e.getDestination();
		this.label=e.getLabel();
	}
	
	public T getSource() {
		return source;
	}
	public void setSource(T source) {
		this.source = source;
	}
	public T getDestination() {
		return destination;
	}
	public void setDestination(T destination) {
		this.destination = destination;
	}
	public String getLabel() {
		return label==null?"":label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public boolean equals(Object o){
		if(! (o instanceof ControlFlowEdge<?>)){
			return false;
		}
		ControlFlowEdge<?> edge=(ControlFlowEdge<?>)o;
		return edge.getSource()==this.getSource() && edge.getDestination()==this.getDestination() && edge.getLabel().equals(this.getLabel());
		
	}
	
	public int hashCode(){
		return source.hashCode()+destination.hashCode()+label.hashCode();
	}
	
	//Debug info
	private String color;

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
}