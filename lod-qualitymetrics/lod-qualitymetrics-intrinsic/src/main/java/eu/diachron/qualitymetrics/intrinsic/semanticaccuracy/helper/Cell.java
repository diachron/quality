/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.semanticaccuracy.helper;

import java.util.ArrayList;
import java.util.List;

import eu.diachron.qualitymetrics.utilities.SerialisableTriple;

/**
 * @author Jeremy Debattista
 * 
 */
public class Cell {

	private int count = 0;
	private List<SerialisableTriple> triplesInCell = new ArrayList<SerialisableTriple>();
	private CellColor color = CellColor.WHITE;
	private double aggregatedDistance = 0.0;
	private double cellLength = 0.0;
	
	public Cell(double cellLength){
		this.setCellLength(cellLength);
	}
	
	public int getCount() {
		return count;
	}
	
	public void incrementCount() {
		this.count++;
	}
	
	public void addTriplesInCell(SerialisableTriple t){
		this.triplesInCell.add(t);
	}
	
	public List<SerialisableTriple> getAllTriplesInCell(){
		return this.triplesInCell;
	}

	public CellColor getColor() {
		return color;
	}

	public void setColor(CellColor color) {
		this.color = color;
	}
	
	public double getAggregatedDistance(){
		return this.aggregatedDistance;
	}

	public double getCellLength() {
		return cellLength;
	}

	public void setCellLength(double cellLength) {
		this.cellLength = cellLength;
	}
}
