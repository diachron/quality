/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.semanticaccuracy.helper;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sml.sm.core.utils.SMConstants;
import slib.utils.ex.SLIB_Ex_Critic;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

import de.unibonn.iai.eis.diachron.datatypes.Pair;
import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;
import eu.diachron.qualitymetrics.utilities.SerialisableTriple;
import eu.diachron.qualitymetrics.utilities.exceptions.VocabularyUnreachableException;

/**
 * @author Jeremy Debattista
 * 
 */
public class PredicateClusteringIndexing implements Serializable {

	private static Logger logger = LoggerFactory.getLogger(PredicateClusteringIndexing.class);

	private static final long serialVersionUID = -5723782767214130285L;
	
	private Set<SerialisableTriple> triples = new HashSet<SerialisableTriple>();
	private Set<SerialisableTriple> outliers = new HashSet<SerialisableTriple>();
	
	private String predicate = "";
	
	public  int RESERVOIR_SIZE = 400; //private final
	public  double MIN_FRACTION_OBJ_P = 0.99;  //private final
	
	private double cellLength = 0.0;
	
	
	private ExtendedReservoirSampler<SerialisableTriple> resSampler = new ExtendedReservoirSampler<SerialisableTriple>(RESERVOIR_SIZE, true);

	private int arraySize = 10;
	public  double approxD = 0.0; //private double
	public  boolean manual = false; //to remove
	public  boolean useTriples = false; //to remove
	
	private Set<Pair<Integer,Integer>> occupiedCells = new HashSet<Pair<Integer,Integer>>();

	private Pair<Integer,Integer> hostLocation = new Pair<Integer,Integer>(-1,-1);
	
	private Map<Pair<String,String>, Pair<Integer,Integer>> cellIndex = new HashMap<Pair<String,String>, Pair<Integer,Integer>>();
	
	private SimilarityComparatorStatic similarityComparator;
	
	
	
	public String ici = SMConstants.FLAG_ICI_ZHOU_2008;
	public String sim = SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_MAZANDU_2012;
	
	public PredicateClusteringIndexing(Resource predicate){
		this.predicate = predicate.getURI();
		try {
			similarityComparator = new SimilarityComparatorStatic(ici, sim);
		} catch (SLIB_Ex_Critic | VocabularyUnreachableException e) {
			e.printStackTrace();
		}
	}
	
	public PredicateClusteringIndexing(Resource predicate, String sim, String ici){
		this.predicate = predicate.getURI();
		try {
			this.sim = sim;
			this.ici = ici;
			similarityComparator = new SimilarityComparatorStatic(ici, sim);
		} catch (SLIB_Ex_Critic | VocabularyUnreachableException e) {
			e.printStackTrace();
		}
	}
	
	public void addQuad(Quad q){
		SerialisableTriple t = new SerialisableTriple(q.asTriple());
		if (!triples.contains(t)) this.triples.add(t);
		if (useTriples) resSampler.add(t);
	}
	
	public void addTriple(Triple t){
		this.addQuad(new Quad(null,t));
	}
		
	@Override
	public int hashCode() {
		 return predicate.hashCode();
	 }

	@Override
	public boolean equals(Object obj) {
		 if (obj instanceof PredicateClusteringIndexing){
			 PredicateClusteringIndexing other = (PredicateClusteringIndexing)obj;
			 return this.predicate.equals(other.predicate);
		 }
		 
		 return false;
	 }
	
	/*
	 * FindAllOutsM - E.M. Knorr et al.
	 */
	public void findOutliers(final Map<String,String> typesMap){
		try {
			
			if (!useTriples){
				for(SerialisableTriple t : this.triples){
					this.resSampler.add(t, typesMap);
				}
			}
			
			// Set up variables
			if (!manual) approxD = this.getApproximateD(typesMap);
			
			cellLength = approxD / (2.0 * Math.sqrt(2.0));
			
			int numberOfTriplesN = triples.size();

			double maximumM = numberOfTriplesN * (1 - MIN_FRACTION_OBJ_P);
		
			
			logger.info("Clustering Stats - Property: {}; Number of Objects: {}; Approximate Distance D: {}; Minimum Fraction Object P: {}; Maximum number of objects M: {}",
					this.predicate, numberOfTriplesN, approxD, MIN_FRACTION_OBJ_P, maximumM);
			
			// Create and Initialise 2d Arrays
			if ((numberOfTriplesN < 10000) && (numberOfTriplesN > 100)) arraySize =  ((numberOfTriplesN / 10) < 100) ? numberOfTriplesN / 10 : 100;  
			if ((numberOfTriplesN < 100000) && (numberOfTriplesN > 10000)) arraySize = ((numberOfTriplesN / 100) < 1000) ? numberOfTriplesN / 100 : 1000;
			if (numberOfTriplesN > 100001) arraySize =  numberOfTriplesN / 1000;

			
			logger.info("Clustering Stats - Array Size: {}", arraySize);
			
			Cell[][] cells = new Cell[arraySize+1][arraySize+1];
			
			// Find random host from the reservoir and assign a cell
			Random r = new Random();
			int hostPos = r.nextInt(resSampler.getItems().size());
			
			int hostCellX = r.nextInt(arraySize);
			int hostCellY = r.nextInt(arraySize);
			this.hostLocation = new Pair<Integer,Integer>(hostCellX, hostCellY);
			
			logger.info("Clustering Stats - Host Location: {},{}", hostCellX, hostCellY);
			
			SerialisableTriple hostTriple = resSampler.getItems().get(hostPos);
			
//			Triple triple = new Triple(
//					ModelFactory.createDefaultModel().createResource("http://dbpedia.org/resource/Ripatransone").asNode(),
//					ModelFactory.createDefaultModel().createProperty("http://dbpedia.org/property/saint").asNode(),
//					ModelFactory.createDefaultModel().createResource("http://dbpedia.org/resource/Mary_Magdalene").asNode()
//			);
//			SerialisableTriple hostTriple = new SerialisableTriple(triple);
			
			logger.info("Clustering Stats - Host {},{}", hostTriple.getTriple().getSubject().toString(), hostTriple.getTriple().getObject().toString());

			
			cells[hostCellX][hostCellY] = new Cell(cellLength);
			(cells[hostCellX][hostCellY]).addTriplesInCell(hostTriple);
			(cells[hostCellX][hostCellY]).incrementCount();
			this.occupiedCells.add(hostLocation);
			
			this.addToCellIndex(hostTriple, hostLocation, typesMap);
			
			this.triples.remove(resSampler.getItems().get(hostPos));

			// Mapping each triple P to an appropriate cell Cq - STEP 2
			for(SerialisableTriple t : this.triples){
				
				Pair<Integer,Integer> cellLocation = this.isPairIndexed(t, typesMap);
				if (cellLocation == null) {
					logger.debug("Clustering Stats - Triple to cluster: {}", t.getTriple().toString());
					cellLocation = this.allocateCell(hostTriple, t, typesMap, cells);
					this.addToCellIndex(t, cellLocation, typesMap);
				}
				else logger.debug("Clustering Stats - Triple: {}; Already Clustered; Cell {},{}", 
						t.getTriple().toString(), 
						cellLocation.getFirstElement(), 
						cellLocation.getSecondElement());

				int _x = cellLocation.getFirstElement();
				int _y = cellLocation.getSecondElement();
				
				if (cells[_x][_y] == null) cells[_x][_y] = new Cell(cellLength);
				cells[_x][_y].addTriplesInCell(t);
				cells[_x][_y].incrementCount();
				this.occupiedCells.add(cellLocation);
			}
			
			Set<Pair<Integer,Integer>> redCells = new HashSet<Pair<Integer,Integer>>();
			Set<Pair<Integer,Integer>> pinkCells = new HashSet<Pair<Integer,Integer>>();

			
			// Label Cells Red - STEP 3
			for(Pair<Integer, Integer> cell : this.occupiedCells){
				int x = cell.getFirstElement();
				int y = cell.getSecondElement();
				
				if (cells[x][y].getCount() >= maximumM) {
					cells[x][y].setColor(CellColor.RED);
					redCells.add(cell);
					logger.debug("Cell {},{} is RED",x,y);
				}
			}
			
			// Label Cells Pink - STEP 4
			for(Pair<Integer, Integer> cell : redCells){
				List<Pair<Integer, Integer>> l1s = this.getL1neighbours(cell);
				
				for(Pair<Integer, Integer> p : l1s){
					int x = p.getFirstElement();
					int y = p.getSecondElement();
					
					if (cells[x][y] == null){
						cells[x][y] = new Cell(cellLength);
					}
					
					if (cells[x][y].getColor() == CellColor.WHITE) {
						cells[x][y].setColor(CellColor.PINK);
						pinkCells.add(cell);
						logger.debug("Cell {},{} is PINK",x,y);
					}
				}
			}
			
			// Find Outliers from white cells - STEP 5
			Set<Pair<Integer,Integer>> _tmp = new HashSet<Pair<Integer,Integer>>();
			_tmp.addAll(this.occupiedCells);
			_tmp.removeAll(redCells);
			_tmp.removeAll(pinkCells);
			
			for(Pair<Integer, Integer> whiteCell : _tmp){
				int x = whiteCell.getFirstElement();
				int y = whiteCell.getSecondElement();
				
				int _count = cells[x][y].getCount();
				List<Pair<Integer, Integer>> l1s = this.getL1neighbours(whiteCell);
				for(Pair<Integer, Integer> p : l1s){
					
					int _x = p.getFirstElement();
					int _y = p.getSecondElement();
					
					if (cells[_x][_y] == null) continue;
					_count += cells[_x][_y].getCount();
				}
				
				if (_count > maximumM) {
					cells[x][y].setColor(CellColor.PINK);
					pinkCells.add(whiteCell);
					logger.debug("Cell {},{} is PINK",x,y);
				} else {
					List<Pair<Integer, Integer>> l2s = this.getL2neighbours(whiteCell,false);
					for(Pair<Integer, Integer> p : l2s){
						int _x = p.getFirstElement();
						int _y = p.getSecondElement();
						if (cells[_x][_y] == null) continue;
						_count += cells[_x][_y].getCount();
					}
					if (_count <= maximumM) outliers.addAll(cells[x][y].getAllTriplesInCell());
					else {
						boolean isOutlier = false;
						List<SerialisableTriple> triples = cells[x][y].getAllTriplesInCell();
						
						for (SerialisableTriple t : triples){
							isOutlier = true;
							int countP = _count;
							
							List<Pair<Integer, Integer>> occupiedCellsWithinCwL2 = new ArrayList<Pair<Integer, Integer>>();
							occupiedCellsWithinCwL2.retainAll(l2s);
							
							for(Pair<Integer, Integer> p : occupiedCellsWithinCwL2){
								int _x = p.getFirstElement();
								int _y = p.getSecondElement();
								
								List<SerialisableTriple> _l2Triples = cells[_x][_y].getAllTriplesInCell();
								for(SerialisableTriple l2t : _l2Triples){
									double distance = 1 - this.similarityOfTriples(t, l2t, typesMap);
									if (distance <= approxD) countP++;
									if (countP > maximumM) {
										isOutlier = false;
										break;
									}
								}
								if (countP > maximumM) break;
							}
							if (isOutlier)this.outliers.add(t);
						}
					}
				}
			}
//			this.createImage(cells);
		} catch (SLIB_Ex_Critic e) {
			e.printStackTrace();
		}
	}
	
	
	
	// Methods for indexing - fast method for mapping
	private Pair<Integer,Integer> isPairIndexed(SerialisableTriple triple, Map<String,String> types){
		String sub = types.get(triple.getTriple().getSubject().getURI());
		String obj = types.get(triple.getTriple().getObject().getURI());
		
		if (sub == null) sub = OWL.Thing.getURI();
		if (obj == null) obj = OWL.Thing.getURI();
		
		Pair<String,String> p = new Pair<String,String>(sub,obj);
		
		if (this.cellIndex.containsKey(p)) return this.cellIndex.get(p);
		else return null;
	}
	
	private void addToCellIndex(SerialisableTriple triple, Pair<Integer, Integer> location, Map<String,String> types){
		String sub = types.get(triple.getTriple().getSubject().getURI());
		String obj = types.get(triple.getTriple().getObject().getURI());
		
		if (sub == null) sub = OWL.Thing.getURI();
		if (obj == null) obj = OWL.Thing.getURI();
		
		this.cellIndex.put(new Pair<String,String>(sub,obj), location);
	}
	
	// Method to find a cell location for a previously unseen Subject-Object pair
	private Pair<Integer, Integer> allocateCell(SerialisableTriple hostTriple, SerialisableTriple t, 
			Map<String,String> typesMap, Cell[][] cells) throws SLIB_Ex_Critic{
		double distance = 1 - this.similarityOfTriples(hostTriple, t, typesMap);
		
		Pair<Integer, Integer> cellLocation = null;
		if (distance <= (approxD/2)){
			//Property 1 - Triples within same cell
			cellLocation = this.hostLocation;
			logger.debug("Clustering Stats - Triple: {}; Distance to Host: {}; Property 1 ", t.getTriple().toString(), distance);
		}
		else if (distance <= approxD) {
			// Property 2 - The assessed triple is in L1 if it is at least D apart
			cellLocation = this.allocateCell(hostLocation, cells, t, LayerType.ONE, typesMap, distance);
			logger.debug("Clustering Stats - Triple: {}; Distance to Host: {}; Property 2; Cell {},{} ", t.getTriple().toString(), distance,cellLocation.getFirstElement(), cellLocation.getSecondElement() );
		} else if (distance > approxD){
			// Property 3 - over L2
			cellLocation = this.allocateCell(hostLocation, cells, t, LayerType.THREE, typesMap, distance);
			logger.debug("Clustering Stats - Triple: {}; Distance to Host: {}; Property 3; Cell {},{} ", t.getTriple().toString(), distance,cellLocation.getFirstElement(), cellLocation.getSecondElement() );
		}
		
		return cellLocation;
	}
	
	
	
	private List<Pair<Integer,Integer>> intersection(Collection<Pair<Integer,Integer>> c1, Collection<Pair<Integer,Integer>> c2){
		List<Pair<Integer,Integer>> _tmp1 = new ArrayList<Pair<Integer,Integer>>();
		_tmp1.addAll(c1);
		List<Pair<Integer,Integer>> _tmp2 = new ArrayList<Pair<Integer,Integer>>();
		_tmp2.addAll(c2);
		
		_tmp2.retainAll(_tmp1);
		return _tmp2;
	}
	
	private List<Pair<Integer,Integer>> difference(Collection<Pair<Integer,Integer>> c1, Collection<Pair<Integer,Integer>> c2){
		List<Pair<Integer,Integer>> _tmp1 = new ArrayList<Pair<Integer,Integer>>();
		_tmp1.addAll(c1);
		List<Pair<Integer,Integer>> _tmp2 = new ArrayList<Pair<Integer,Integer>>();
		_tmp2.addAll(c2);
		
		_tmp1.removeAll(_tmp2);
		return _tmp1;
	}
	
	
	
	private boolean isDiagonal(double distance, double cellLength){
		if (distance > (cellLength *2)) return true;
		return false;
	}
	
	private List<Pair<Integer,Integer>> getDiagonals(Pair<Integer,Integer> currentLocation, List<Pair<Integer,Integer>> possibleCells){
		List<Pair<Integer,Integer>> returnedDiagonals = new ArrayList<Pair<Integer,Integer>>();
		for(Pair<Integer,Integer> p : possibleCells){
			if ((p.getFirstElement() == currentLocation.getFirstElement()) || (p.getSecondElement() == currentLocation.getSecondElement()))
				continue; else returnedDiagonals.add(p);
		}
		return returnedDiagonals;
	}
	
	private List<Pair<Integer,Integer>> getCross(Pair<Integer,Integer> currentLocation, List<Pair<Integer,Integer>> possibleCells){
		List<Pair<Integer,Integer>> returnedDiagonals = new ArrayList<Pair<Integer,Integer>>();
		for(Pair<Integer,Integer> p : possibleCells){
			if ((p.getFirstElement() == currentLocation.getFirstElement()) || (p.getSecondElement() == currentLocation.getSecondElement()))
				returnedDiagonals.add(p); else continue;
		}
		return returnedDiagonals;
	}
	
	
	private Pair<Integer,Integer> allocateCell(Pair<Integer,Integer> currentLocation, Cell[][] cells, 
			SerialisableTriple tripleToRelocate, LayerType lType, final Map<String,String> typesMap, double _distance) throws SLIB_Ex_Critic{
		
		List<Pair<Integer,Integer>> possibleAllocation;
		
		if ((lType == LayerType.ONE) || (lType == LayerType.TWO))  possibleAllocation = this.getL2neighbours(currentLocation,false);
		else possibleAllocation = this.getL3neighbours(currentLocation);
		
		
		List<Pair<Integer,Integer>> oCells = (List<Pair<Integer, Integer>>) this.intersection(occupiedCells, possibleAllocation); // retain all locations that are in possibleAllocation and in occupiedCells
		List<Pair<Integer,Integer>> fCells = (List<Pair<Integer, Integer>>) this.difference(possibleAllocation,occupiedCells); // remove all locations that are in occupiedCells but not in possibleAllocation 
		
		if (oCells.size() == 0){
			if (lType == LayerType.ONE){
				possibleAllocation = this.getL1neighbours(currentLocation);
			}
			
			if (isDiagonal(_distance,this.cellLength)){
				possibleAllocation = getDiagonals(currentLocation,possibleAllocation);
			} else {
				possibleAllocation = getCross(currentLocation,possibleAllocation);
			}
			Random r = new Random();
			int randomP = r.nextInt(fCells.size());
			return fCells.get(randomP);
		} else {
			List<Pair<Integer,Integer>> commonArea = new ArrayList<Pair<Integer,Integer>>();
			commonArea.addAll(possibleAllocation);
			
			Pair<Integer,Integer> shortestDistPair = new Pair<Integer,Integer>(-1,-1);
			double shortestDist = 1.0;
			
			for(Pair<Integer,Integer> pair : oCells){
				SerialisableTriple t = cells[pair.getFirstElement()][pair.getSecondElement()].getAllTriplesInCell().get(0);
				double distance = 1 - this.similarityOfTriples(tripleToRelocate, t, typesMap);
				
				if (distance <= (approxD/2)) return pair;
			}
			for(Pair<Integer,Integer> pair : oCells){
				SerialisableTriple t = cells[pair.getFirstElement()][pair.getSecondElement()].getAllTriplesInCell().get(0);
				double distance = 1 - this.similarityOfTriples(tripleToRelocate, t, typesMap);
				
				if (distance <= approxD) {
					commonArea.remove(pair);
					List<Pair<Integer,Integer>> pairArea = this.getL2neighbours(pair,false);
					commonArea = this.intersection(commonArea, pairArea);
				} else if (distance > approxD){
					List<Pair<Integer,Integer>> pairArea = this.getL3neighbours(pair);
					commonArea = this.intersection(commonArea, pairArea);
				}
				
				if (shortestDist > distance) {
					shortestDist = distance;
					shortestDistPair = pair;
				}
			}
			commonArea.remove(currentLocation);
			commonArea.removeAll(oCells);
			
			if (commonArea.size() == 0){
				if (lType == LayerType.THREE) commonArea.addAll(this.getL3neighbours(currentLocation));
				if ((lType == LayerType.ONE) || (lType == LayerType.TWO)) commonArea.addAll(this.getL2neighbours(currentLocation, false));
				commonArea = this.intersection(commonArea, this.getOuterL2neighbours(shortestDistPair, true));
				commonArea = this.difference(possibleAllocation,occupiedCells);
			}
			
			if (lType == LayerType.ONE){
				List<Pair<Integer,Integer>> _tmp = this.getL1neighbours(currentLocation);
				fCells = (List<Pair<Integer, Integer>>) this.difference(_tmp,commonArea); // remove all locations that are in occupiedCells but not in possibleAllocation 
				
				if (fCells.size() > 0) commonArea = fCells;
			}
				

			// Get the shortest path and place the new item there
//			List<Pair<Integer,Integer>> L1sp = this.getL1neighbours(shortestDistPair);
//			L1sp = this.intersection(commonArea, L1sp);
//			L1sp = this.intersection(L1sp, oCells);
//			
//			if (L1sp.size() > 0){
//				
//				if (isDiagonal(shortestDist,this.cellLength)){
//					L1sp = getDiagonals(shortestDistPair,L1sp);
//				} else {
//					L1sp = getCross(shortestDistPair,L1sp);
//				}
//				
//				if (L1sp.size() > 0){
//					Random r = new Random();
//					int randomP = r.nextInt(L1sp.size());
//					return L1sp.get(randomP);
//				}
//			}
//			
//			List<Pair<Integer,Integer>> L2sp = this.getL2neighbours(shortestDistPair,true);
//			L1sp = this.intersection(commonArea, L2sp);
//			L1sp = this.intersection(L2sp, oCells);
//			
//			if (L2sp.size() > 0){
//				
//				if (isDiagonal(shortestDist,this.cellLength)){
//					L2sp = getDiagonals(shortestDistPair,L2sp);
//				} else {
//					L2sp = getCross(shortestDistPair,L2sp);
//				}
//				
//				if (L2sp.size() > 0){
//					Random r = new Random();
//					int randomP = r.nextInt(L2sp.size());
//					return L2sp.get(randomP);
//				}
//			}
//			
			Random r = new Random();
			int randomP = r.nextInt(commonArea.size());
			return commonArea.get(randomP);
		}
	}
	
	
	private List<Pair<Integer,Integer>> getL1neighbours(Pair<Integer,Integer> location){
		
		int hostX = location.getFirstElement();
		int hostY = location.getSecondElement();
		
		List<Pair<Integer,Integer>> nCells = new ArrayList<Pair<Integer,Integer>>();

		
		for (int dx = (hostX > 0 ? -1 : 0); dx <= (hostX < this.arraySize - 1 ? 1 : 0); ++dx)
		{
		    for (int dy = (hostY > 0 ? -1 : 0); dy <= (hostY < this.arraySize - 1  ? 1 : 0); ++dy)
		    {
		    	if ((dx != 0) || (dy != 0)) 
		    		nCells.add(new Pair<Integer,Integer>(hostX + dx, hostY + dy));
		    }
		}
		
		return nCells;
	}
	
	
	private  List<Pair<Integer,Integer>> getOuterL2neighbours(Pair<Integer,Integer> location, boolean removeL1){
		
		int hostX = location.getFirstElement();
		int hostY = location.getSecondElement();
		
		List<Pair<Integer,Integer>> nCells = new ArrayList<Pair<Integer,Integer>>();

		int bounderiesLeft = (hostX > 2) ? -2 : (0 - hostX);
		int bounderiesRight = (hostX < (this.arraySize - 2)) ? 2 : (this.arraySize - hostX);
		
		int bounderiesUp = (hostY > 2) ? -2 : (0 - hostY);
		int bounderiesDown = (hostY < (this.arraySize - 2)) ? 2 : (this.arraySize - hostY);
		
		for (int dx = bounderiesLeft; dx <= bounderiesRight; dx++){
			for (int dy = bounderiesUp; dy <= bounderiesDown; dy++){
				if ((dx != 0) || (dy != 0))
					nCells.add(new Pair<Integer,Integer>(hostX + dx, hostY + dy));
			}
		}
		
		if (removeL1) nCells.removeAll(getL1neighbours(location));
		
		List<Pair<Integer,Integer>> _tmp = getL2neighbours(location, true);
		
		_tmp.removeAll(nCells);
		
		return _tmp;
	}
	
	public  List<Pair<Integer,Integer>> getL2neighbours(Pair<Integer,Integer> location, boolean removeL1){
		
		int hostX = location.getFirstElement();
		int hostY = location.getSecondElement();
		
		List<Pair<Integer,Integer>> nCells = new ArrayList<Pair<Integer,Integer>>();

		int bounderiesLeft = (hostX > 2) ? -3 : (0 - hostX);
		int bounderiesRight = (hostX < (this.arraySize - 3)) ? 3 : (this.arraySize - hostX);
		
		int bounderiesUp = (hostY > 2) ? -3 : (0 - hostY);
		int bounderiesDown = (hostY < (this.arraySize - 3)) ? 3 : (this.arraySize - hostY);
		
		for (int dx = bounderiesLeft; dx <= bounderiesRight; dx++){
			for (int dy = bounderiesUp; dy <= bounderiesDown; dy++){
				if ((dx != 0) || (dy != 0))
					nCells.add(new Pair<Integer,Integer>(hostX + dx, hostY + dy));
			}
		}
		
		if (removeL1) nCells.removeAll(getL1neighbours(location));
		
		return nCells;
	}
	
	public static void main (String[]args){
		PredicateClusteringIndexing pci = new PredicateClusteringIndexing(ModelFactory.createDefaultModel().createResource("http://dbpedia.org"));
		System.out.println(pci.getOuterL2neighbours(new Pair<Integer,Integer>(3,67),true));
	}
	

	public List<Pair<Integer,Integer>> getL3neighbours(Pair<Integer,Integer> location){
		List<Pair<Integer,Integer>> nCells = new ArrayList<Pair<Integer,Integer>>();

		
		for (int dx = 0; dx <= this.arraySize ; ++dx)
		{
		    for (int dy = 0; dy <= this.arraySize ; ++dy)
		    {
		    		nCells.add(new Pair<Integer,Integer>(dx, dy));
		    }
		}
		
		nCells.removeAll(getL2neighbours(location,false));
		nCells.remove(location);
		return nCells;
	}
	
	
	/**
	 * Using a sampling technique, an estimate D is found  
	 * @param typesMap
	 * @return an estimate similarity measure
	 * @throws SLIB_Ex_Critic
	 */
	private double getApproximateD(final Map<String,String> typesMap) throws SLIB_Ex_Critic{
		List<SerialisableTriple> lst = resSampler.getItems();
		List<Double> vals = new ArrayList<Double>();
//		Random r = new Random();
//		int hostPos = r.nextInt(lst.size());
//		SerialisableTriple host = lst.get(hostPos);
//		lst.remove(host);
		
		// using median
//		for(SerialisableTriple st : lst){
////			logger.debug("Subject {}, Object {}", typesMap.get(st.getTriple().getSubject().getURI()),typesMap.get(st.getTriple().getObject().getURI()));
//			double distance = 1 - this.similarityOfTriples(st, host, typesMap);
//			vals.add(distance);
//		}
		
		for (SerialisableTriple host : lst){
			double medianDistance = 0.0;
			List<Double> dist = new ArrayList<Double>();
			for(SerialisableTriple st : lst){
				logger.debug("Subject {}, Object {}", typesMap.get(st.getTriple().getSubject().getURI()),typesMap.get(st.getTriple().getObject().getURI()));
				double distance = (1 - this.similarityOfTriples(st, host, typesMap));
				dist.add(distance);
			}
			Collections.sort(dist);
			
			if (dist.size() % 2 == 0){
				int mid1 = dist.size() / 2;
				int mid2 = mid1 + 1;
				medianDistance = (dist.get(mid1) + dist.get(mid2)) / 2.0;
			} else {
				int mid1 = (int) (((double)dist.size() / 2.0) + 0.5);
				medianDistance = dist.get(mid1);
			}
			
			vals.add(medianDistance);
		}
		
		
		
//		List<Double> _lst = new ArrayList<Double>();
//		_lst.add(0.0);
//		vals.removeAll(_lst);
		Collections.sort(vals);
		double value = 0.0;
		if (vals.size() % 2 == 0){
			int mid1 = vals.size() / 2;
			int mid2 = mid1 + 1;
			value = (vals.get(mid1) + vals.get(mid2)) / 2.0;
		} else {
			int mid1 = (int) (((double)vals.size() / 2.0) + 0.5);
			value = vals.get(mid1);
		}
		
		System.out.println(value);
		return value;
		
		// mean
		/*
		for(SerialisableTriple st : lst){
			double distance = 1 - this.similarityOfTriples(st, host, typesMap);
			summation += distance;
		}
		
		System.out.println(summation/(double)lst.size());
		return summation/(double)lst.size();
		*/
	}
	
	
	private double similarityOfTriples(SerialisableTriple t1, SerialisableTriple t2, final Map<String,String> typesMap) throws SLIB_Ex_Critic{
		//get subjects of t_i and t_j and compare
		Resource sub_ti = ModelFactory.createDefaultModel().createResource(typesMap.get(t1.getTriple().getSubject().getURI()));
		Resource sub_tj = ModelFactory.createDefaultModel().createResource(typesMap.get(t2.getTriple().getSubject().getURI()));
		
		sub_ti = (sub_ti.isAnon()) ? OWL.Thing : sub_ti;
		sub_tj = (sub_tj.isAnon()) ? OWL.Thing : sub_tj;
		
//		SimilarityComparator subjs = new SimilarityComparator(sub_ti, sub_tj, false);
		double sim1 = 0.0;
		try {
			sim1 = similarityComparator.compute(sub_ti, sub_tj); // compute similarity between the subjects of the triples; 
		} catch (VocabularyUnreachableException e) {
			logger.error(e.getMessage());
			sim1 = 0.0;
		} 
										  
		
		//get objects of t_i and t_j and compare
		Resource obj_ti = (t1.getTriple().getObject().isLiteral()) ? getLiteralType(t1.getTriple().getObject()) :  
			ModelFactory.createDefaultModel().createResource(typesMap.get(t1.getTriple().getObject().getURI()));

		Resource obj_tj = (t2.getTriple().getObject().isLiteral()) ? getLiteralType(t2.getTriple().getObject()) :  
			ModelFactory.createDefaultModel().createResource(typesMap.get(t2.getTriple().getObject().getURI()));

		obj_ti = (obj_ti.isAnon()) ? OWL.Thing : obj_ti;
		obj_tj = (obj_tj.isAnon()) ? OWL.Thing : obj_tj;

		
//		SimilarityComparator objs = new SimilarityComparator(obj_ti, obj_tj, false); 
		double sim2 = 0.0;
		try {
			sim2 = similarityComparator.compute(obj_ti, obj_tj); // compute similarity between the objects of the triples
		} catch (VocabularyUnreachableException e) {
			logger.error(e.getMessage());
			sim2 = 0.0;
		} 
		
		return ((sim1 + sim2) / 2.0); // Average similarity of both triples
	}
	
	private Resource getLiteralType(Node lit_obj){
		RDFNode n = Commons.asRDFNode(lit_obj);
		
		if (((Literal)n).getDatatype() != null){
			return ModelFactory.createDefaultModel().createResource(lit_obj.getLiteralDatatype().getURI());
		} else {
			Literal lt = (Literal) n;
			if (lt.getValue() instanceof Double) return XSD.xdouble;
			else if (lt.getValue() instanceof Integer) return XSD.xint;
			else if (lt.getValue() instanceof Boolean) return XSD.xboolean;
			else if (lt.getValue() instanceof String) return XSD.xstring;
			else if (lt.getValue() instanceof Float) return XSD.xfloat;
			else if (lt.getValue() instanceof Short) return XSD.xshort;
			else if (lt.getValue() instanceof Long) return XSD.xlong;
			else if (lt.getValue() instanceof Byte) return XSD.xbyte;
			else return RDFS.Literal;
		}
	}
	
	public Set<SerialisableTriple> getOutliers(){
		return this.outliers;
	}
	
	public Model evaluationModel(Map<String,String> typesMap){
		Model m = ModelFactory.createDefaultModel();
		
		Resource thisEval = Commons.generateURI();
		Property _setP = m.createProperty("uri:minimumFractionObjectP"); //MIN_FRACTION_OBJ_P
		Property _approxD = m.createProperty("uri:approximateD"); //approxD
		Property _setResSize = m.createProperty("uri:reserviorSize"); // RESERVIOR SIZE
		Property _noTriples = m.createProperty("uri:numberOfTriples"); 
		Property _maximumM = m.createProperty("uri:maximumM");
		Property _assessedProperty = m.createProperty("uri:assessedProperty");
		Property _totalOutliers = m.createProperty("uri:totalOutliers");
		Property _simMeasureUsed = m.createProperty("uri:similarityMeasureUsed");
		Property _icMeasureUsed = m.createProperty("uri:icMeasureUsed");

		
		double mm = this.triples.size() * (1 - MIN_FRACTION_OBJ_P);
		
		m.add(m.createStatement(thisEval, _setP, m.createTypedLiteral(MIN_FRACTION_OBJ_P)));
		m.add(m.createStatement(thisEval, _approxD, m.createTypedLiteral(this.approxD)));
		m.add(m.createStatement(thisEval, _setResSize, m.createTypedLiteral(RESERVOIR_SIZE)));
		m.add(m.createStatement(thisEval, _noTriples, m.createTypedLiteral(this.triples.size())));
		m.add(m.createStatement(thisEval, _maximumM, m.createTypedLiteral(mm)));
		m.add(m.createStatement(thisEval, _assessedProperty, m.createResource(this.predicate)));
		m.add(m.createStatement(thisEval, _totalOutliers, m.createTypedLiteral(this.outliers.size())));
		m.add(m.createStatement(thisEval, _simMeasureUsed, m.createTypedLiteral(sim)));
		m.add(m.createStatement(thisEval, _icMeasureUsed, m.createTypedLiteral(ici)));

		Resource bag = Commons.generateURI();
		Bag b = m.createBag(bag.getURI());
		
		Property _hasSubject = m.createProperty("uri:subject");
		Property _hasSubjectType = m.createProperty("uri:subjectType");
		Property _hasObject = m.createProperty("uri:object");
		Property _hasObjectType = m.createProperty("uri:objectType");

		
		
		for (SerialisableTriple t : this.outliers){
			Resource outlier = Commons.generateURI();
			
			String subjType = typesMap.get(t.getTriple().getSubject().getURI());
			Resource resSubjType = null;
			if (subjType == null) resSubjType = OWL.Thing;
			else 
				resSubjType = (subjType.equals("")) ? OWL.Thing : m.createResource(subjType);
			
			String objType = typesMap.get(t.getTriple().getObject().getURI());
			Resource resObjType = null;
			if (objType == null) resObjType = OWL.Thing;
			else
				resObjType = (objType.equals("") ) ? OWL.Thing : m.createResource(objType);
			
			
			m.add(m.createStatement(outlier, _hasSubject, Commons.asRDFNode(t.getTriple().getSubject())));
			m.add(m.createStatement(outlier, _hasSubjectType,  resSubjType));
			m.add(m.createStatement(outlier, _hasObject, Commons.asRDFNode(t.getTriple().getObject())));
			m.add(m.createStatement(outlier, _hasObjectType, resObjType));
			
			b.add(outlier);
		}
		
		m.add(m.createStatement(thisEval, m.createProperty("urn:outliers"), b));
		
		return m;
	}
	
	
	@SuppressWarnings("unused")
	private void createImage(Cell[][] cells){
		int scaleSize = 50;
		BufferedImage bufferedImage = new BufferedImage(cells.length * scaleSize, cells.length * scaleSize,
		        BufferedImage.TYPE_INT_RGB);
    	Graphics2D graphics = bufferedImage.createGraphics();

		for (int x = 0; x < cells.length; x++) {
		    for (int y = 0; y < cells.length; y++) {
		    	Color c;
		    	
		    	if (cells[x][y] == null) c = Color.WHITE;
		    	else if (cells[x][y].getColor() == CellColor.PINK) c = Color.PINK;
		    	else if (cells[x][y].getColor() == CellColor.RED) c = Color.RED;
		    	else c = Color.WHITE;
		    	
		    	graphics.drawRect(x * scaleSize, y * scaleSize, scaleSize, scaleSize);
		    	graphics.setColor(c);
		    	graphics.fillRect(x * scaleSize, y * scaleSize, scaleSize, scaleSize);
		    	
		    	int trips = (cells[x][y] != null) ? cells[x][y] .getCount() : 0;
		    	
		    	for (int i = 1; i <= trips; i++){
		    		Random r = new Random();
		    		
		    		int _x = r.nextInt(scaleSize);
		    		int _y = r.nextInt(scaleSize);
		    		
	 	    		graphics.drawRect((x*scaleSize) + _x, (y*scaleSize) + _y, scaleSize/10, scaleSize/10);
			    	graphics.setColor(Color.BLACK);
			    	graphics.fillRect((x*scaleSize) + _x, (y*scaleSize) + _y, scaleSize/10, scaleSize/10);
			 	}
		    
		    	graphics.setColor(Color.BLACK);
		    	graphics.drawLine(x * scaleSize, y, x * scaleSize, y * scaleSize);
		    	
		    	graphics.setColor(Color.BLACK);
		    	graphics.drawLine(x, y * scaleSize, x * scaleSize, y * scaleSize);
		    	
		    }
		}
		
		try {
		    File outputfile = new File("/Users/jeremy/Desktop/"+UUID.randomUUID().toString()+".png");
		    ImageIO.write(bufferedImage, "png", outputfile);
		} catch (IOException e) {
		}
	}
	
	public double getApproximateValue(){
		return (double) this.outliers.size() / (double) this.triples.size();
	}

	private enum LayerType{
		ONE,TWO,THREE
	}
	
	private class ExtendedReservoirSampler<T> extends ReservoirSampler<T>{

		private static final long serialVersionUID = -6355915036188110104L;
		Set<Pair<String,String>> pairSets = new HashSet<Pair<String,String>>();
		
		public ExtendedReservoirSampler(int k, boolean indexItems) {
			super(k, indexItems);
		}
		
		public synchronized boolean add(T item, Map<String,String> typesMap) {
			Triple t =  ((SerialisableTriple)item).getTriple();
			
			Resource sub_t = ModelFactory.createDefaultModel().createResource(typesMap.get(t.getSubject().getURI()));
			Resource obj_t = ModelFactory.createDefaultModel().createResource(typesMap.get(t.getObject().getURI()));
			
			sub_t = (sub_t.isAnon()) ? OWL.Thing : sub_t;
			obj_t = (obj_t.isAnon()) ? OWL.Thing : obj_t;

			Pair<String,String> p = new Pair<String,String>(sub_t.getURI(), obj_t.getURI());
			
			if (pairSets.contains(p)) return false;
			else{
				pairSets.add(p);
				return super.add(item);
			}
		}
	}
}
