package de.unibonn.iai.eis.diachron.io;

import de.unibonn.iai.eis.diachron.exceptions.ProcessorNotInitialised;

public interface IOProcessor {

	/**
	 * This method will set up the processor which will consume triples or quads
	 * from a dataset.
	 * 
	 */
	void setUpProcess();
	
	/**
	 * This method starts the processing of the rdf dump(s). 
	 * This should be called after the setUpProcess() method
	 * @throws ProcessorNotInitialised 
	 */
	void startProcessing() throws ProcessorNotInitialised;
	
	
	/**
	 * This method ends the streaming process, removing any 
	 * data from memory required for the parsing
	 * @throws ProcessorNotInitialised
	 */
	void cleanUp() throws ProcessorNotInitialised;

}
