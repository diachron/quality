/**
 * 
 */
package eu.diachron.quaitymetrics.utilities;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;

import de.unibonn.iai.eis.luzzu.semantics.vocabularies.DAQ;
import eu.diachron.qualitymetrics.utilities.VocabularyLoader;


/**
 * @author Jeremy Debattista
 * 
 */
public class VocabularyLoaderTest extends Assert {
	
	@Before
	public void setUp() throws Exception {
		VocabularyLoader.clearDataset();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void knownVocabularyPropertiesTest() throws IOException, URISyntaxException {
		assertTrue(VocabularyLoader.checkTerm(FOAF.page.asNode()));
		assertFalse(VocabularyLoader.checkTerm(ModelFactory.createDefaultModel().createResource(FOAF.NS+"false").asNode()));
		assertTrue(VocabularyLoader.checkTerm(RDF.li(1).asNode()));
		assertFalse(VocabularyLoader.checkTerm(ModelFactory.createDefaultModel().createResource(FOAF.NS+"_1").asNode()));
	}

	@Test
	public void unknownVocabularyPropertiesTest() throws IOException, URISyntaxException {
		assertTrue(VocabularyLoader.checkTerm(DAQ.computedOn.asNode()));
		assertFalse(VocabularyLoader.checkTerm(ModelFactory.createDefaultModel().createResource(DAQ.NS+"false").asNode()));
	}
	
	@Test
	public void knownVocabularyClassTest() throws IOException, URISyntaxException {
		assertTrue(VocabularyLoader.checkTerm(FOAF.Agent.asNode()));
		assertFalse(VocabularyLoader.checkTerm(ModelFactory.createDefaultModel().createResource(FOAF.NS+"False").asNode()));
	}

	@Test
	public void unknownVocabularyClassTest() throws IOException, URISyntaxException {
		assertTrue(VocabularyLoader.checkTerm(DAQ.Category.asNode()));
		assertFalse(VocabularyLoader.checkTerm(ModelFactory.createDefaultModel().createResource(DAQ.NS+"False").asNode()));
	}

}
