/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.interlinking.helper;

/**
 * @author Jeremy Debattista
 * 
 */
public class RDFEdge {
	
	private String edgeLabel;
	
	public RDFEdge(String edgeLabel){
		this.edgeLabel = edgeLabel;
	}
	
	public String getRDFEdge(){
		return this.edgeLabel;
	}
}
