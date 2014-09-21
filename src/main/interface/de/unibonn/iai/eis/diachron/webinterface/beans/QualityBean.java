package de.unibonn.iai.eis.diachron.webinterface.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.faces.event.ActionEvent;

import de.unibonn.iai.eis.diachron.qualitymetrics.utilities.ConfigurationLoader;
import de.unibonn.iai.eis.diachron.util.Dimension;
import de.unibonn.iai.eis.diachron.util.Metrics;
import de.unibonn.iai.eis.diachron.util.ResultDataSet;
import de.unibonn.iai.eis.diachron.util.Results;
import de.unibonn.iai.eis.diachron.util.ResultsHelper;
/**
 * Backing Bean that managed the behavior of the interface
 * 
 * @author Carlos_Montoya
 * @version 1.0, 25/01/2010
 */
public class QualityBean implements Serializable {

	// ////////////////////////////////////////////////////////////////////////
	// Serial version UID
	// ////////////////////////////////////////////////////////////////////////
	/** */
	private static final long serialVersionUID = 8760794775191152542L;

	// ////////////////////////////////////////////////////////////////////////
	// Attribute of the backing bean
	// ////////////////////////////////////////////////////////////////////////
	private List<SelectItem> availableCategories;
	private List<SelectItem> availableMetrics;
	private List<SelectItem> availableDataSets;
	
	private String currentCategory;
	private String currentMetric;
	private String currentDataSet;
	

	//private static String fileName = "C:\\Lab\\results.xml";
	private ResultDataSet results;
	
	/**
	 * Class constructor
	 */
	public QualityBean() {
		super();	
		try {
			ConfigurationLoader conf = new ConfigurationLoader();
			System.out.println(conf.loadDataBase(ConfigurationLoader.CONFIGURATION_FILE));
			results = ResultsHelper.read(conf.loadDataBase(ConfigurationLoader.CONFIGURATION_FILE));
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	// ////////////////////////////////////////////////////////////////////////
	// Methods
	// ////////////////////////////////////////////////////////////////////////
	
	/**
	 * Init the Backing Bean
	 */
	@PostConstruct
	public void init() {
		this.loadDimensions();
	}
	
	/**
	 * This method load all the Dimensions
	 */
	private void loadDimensions(){
		try {
			this.availableCategories = new ArrayList<SelectItem>();
			for (Results result : results.getResults()) {
				for(Dimension dimension: result.getDimensions()){
					SelectItem aux = new SelectItem(dimension.getName(),dimension.getName());
					boolean contained = false;
					for (SelectItem item : this.availableCategories) {
						if(item.getValue().equals(aux.getValue())){
							contained = true;
						}
					}
					if(!contained){
						System.out.println(aux);
						this.availableCategories.add(aux);
					}
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method is trigger when the Dimension change
	 * @param event
	 */
	public void valueChanged(ActionEvent event) {
		this.availableMetrics = new ArrayList<SelectItem>();
		if(this.currentCategory != null){
			this.loadMetrics();
		}
		
		if(this.currentCategory.equals("Please choose one")){
			this.currentCategory = null;
			this.currentMetric = null;
			this.currentDataSet = null;
		}
	}
	
	/**
	 * This method load all the metrics that are save in the file
	 */
	private void loadMetrics(){
		for (Results result: results.getResults()) {
			for (Dimension dimension : result.getDimensions()) {
				if(dimension.getName().equals(this.currentCategory)){
					for (Metrics metric : dimension.getMetrics()) {
						SelectItem aux = new SelectItem(metric.getName(),metric.getName());
						boolean contained = false;
						for (SelectItem item : this.availableMetrics) {
							if(item.getValue().equals(aux.getValue())){
								contained = true;
							}
						}
						if(!contained)
							this.availableMetrics.add(aux);
					}
				}
			}
		}		
	}
	
	/**
	 * This method is the action listener when the metric change
	 * @param event
	 */
	public void valueChangedMetric(ActionEvent event) {
		this.availableDataSets = new ArrayList<SelectItem>();
		if(this.currentMetric != null){
			this.loadDataSet();
		}
		
		if(this.currentMetric.equals("Please choose one")){
			this.currentMetric = null;
			this.currentDataSet = null;
		}
	}
	
	/**
	 * This method load all the data sets
	 */
	private void loadDataSet(){
		for (Results result: results.getResults()) {
			SelectItem aux = new SelectItem(result.getUrl(), result.getUrl());

			for (Dimension dimension : result.getDimensions()) {
				for (Metrics metric : dimension.getMetrics()) {
					if(this.currentMetric.equals(metric.getName())){
						if(!this.availableDataSets.contains(aux))
							this.availableDataSets.add(aux);
					}
				}
			}
		
			
		}
	}
	
	/**
	 * @return the availableCategories
	 */
	public List<SelectItem> getAvailableCategories() {
		return availableCategories;
	}



	/**
	 * @param availableCategories the availableCategories to set
	 */
	public void setAvailableCategories(List<SelectItem> availableCategories) {
		this.availableCategories = availableCategories;
		
	}

	
	/**
	 * @return the currentCategory
	 */
	public String getCurrentCategory() {
		return currentCategory;
	}



	/**
	 * @param currentCategory the currentCategory to set
	 */
	public void setCurrentCategory(String currentCategory) {
		this.currentCategory = currentCategory;
	}



	/**
	 * @return the availableMetrics
	 */
	public List<SelectItem> getAvailableMetrics() {
		return availableMetrics;
	}



	/**
	 * @param availableMetrics the availableMetrics to set
	 */
	public void setAvailableMetrics(List<SelectItem> availableMetrics) {
		this.availableMetrics = availableMetrics;
	}



	/**
	 * @return the availableDataSets
	 */
	public List<SelectItem> getAvailableDataSets() {
		return availableDataSets;
	}



	/**
	 * @param availableDataSets the availableDataSets to set
	 */
	public void setAvailableDataSets(List<SelectItem> availableDataSets) {
		this.availableDataSets = availableDataSets;
	}

	/**
	 * @return the currentMetric
	 */
	public String getCurrentMetric() {
		return currentMetric;
	}

	/**
	 * @param currentMetric the currentMetric to set
	 */
	public void setCurrentMetric(String currentMetric) {
		this.currentMetric = currentMetric;
	}

	/**
	 * @return the currentDataSet
	 */
	public String getCurrentDataSet() {
		return currentDataSet;
	}

	/**
	 * @param currentDataSet the currentDataSet to set
	 */
	public void setCurrentDataSet(String currentDataSet) {
		this.currentDataSet = currentDataSet;
	}

	/**
	 * This method return the first strign to return the value of the DataSet
	 * @return
	 */
	public String getCadenaDataSet(){
		if(this.currentDataSet != null)
			if(!this.currentDataSet.equals("Please choose one"))
				return "The Selected Data Set is: " + this.currentDataSet;
		
		return "";
	}
		
	/**
	 * This method return the result for the input values for the user
	 * @return
	 */
	public String getCadena(){
		String ret = "";
		for (Results result: results.getResults()) {
			if(result.getUrl().equals(this.currentDataSet)){
				for (Dimension dimension : result.getDimensions()) {
					if(dimension.getName().equals(this.currentCategory)){
						for (Metrics metric : dimension.getMetrics()) {
							if(metric.getName().equals(this.currentMetric)){
								ret = ret + "The value of the Metric " + metric.getName() + ", that belongs to the dimension: " + dimension.getName()+ ", is: " + metric.getValue();
							}
						}
					}
				}
			}
		}
		return ret;
		//return this.getCurrentCategory() + ", " + this.getCurrentMetric() + ", " + this.currentDataSet;
	}
	
}
