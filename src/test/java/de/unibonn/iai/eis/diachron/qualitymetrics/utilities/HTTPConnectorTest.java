package de.unibonn.iai.eis.diachron.qualitymetrics.utilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class HTTPConnectorTest extends Assert{

	private Model m = ModelFactory.createDefaultModel();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void TestCorrectURI(){
		Node node = null;

		// Correct URIs
		node = m.createResource("http://aksw.org/MichaelMartin").asNode();
		assertTrue(HTTPConnector.isPossibleURL(node));

		node = m.createResource("http://139.18.2.164:8080/boa").asNode();
		assertTrue(HTTPConnector.isPossibleURL(node));

		node = m.createResource("https://twitter.com/FroehlichMarcel#").asNode();
		assertTrue(HTTPConnector.isPossibleURL(node));

		node = m.createResource("http://www.w3.org/ns/formats/RDF_XML").asNode();
		assertTrue(HTTPConnector.isPossibleURL(node));

		// Not Correct URIs
		node = m.createLiteral("Natanael Arndt").asNode();
		assertFalse(HTTPConnector.isPossibleURL(node));

		// TODO: although these are valid URIs, I am not sure if 
		// we require to check these.. as they are not well formed URL
		node = m.createResource("tel:+49-341-97-32322").asNode();
		assertFalse(HTTPConnector.isPossibleURL(node));

		node = m.createResource("mailto:martin@informatik.uni-leipzig.de").asNode();
		assertFalse(HTTPConnector.isPossibleURL(node));
	}

	@Test
	public void TestHTTPConnectorWithoutContentNegotiation() throws MalformedURLException, ProtocolException, IOException {
		Node node = null;
		boolean followRedirects = false;

		// This should return a 200 code
		node = m.createResource("http://bis.informatik.uni-leipzig.de/images/jpegPhoto.php?name=sn&value=arndt").asNode();
		assertEquals(200, HTTPConnector.connectToURI(node, followRedirects).getResponseCode());

		// This should return a 301 code
		node = m.createResource("http://www.twitter.com/Daniel_hladky#id").asNode();
		assertEquals(301, HTTPConnector.connectToURI(node, followRedirects).getResponseCode());

		// This should return a 302 code
		node = m.createResource("http://aksw.org/MichaelMartin").asNode();
		assertEquals(302, HTTPConnector.connectToURI(node, followRedirects).getResponseCode());

		// This should return a 303 code
		node = m.createResource("http://dbpedia.org/resource/Leipzig").asNode();
		assertEquals(303, HTTPConnector.connectToURI(node, followRedirects).getResponseCode());


		// This should return a 403 (forbidden) code
		node = m.createResource("https://si0.twimg.com/profile_images/1843305351/VPSN3258_bigger.jpg").asNode();
		assertEquals(403, HTTPConnector.connectToURI(node, followRedirects).getResponseCode());

		// This should return a 404 error code
		node = m.createResource("http://www.shop.sachsen.de/infai/leipziger-semantic-web-tag.htm").asNode();
		assertEquals(404, HTTPConnector.connectToURI(node, followRedirects).getResponseCode());
	}

	@Test
	public void TestHTTPConnectorWithContentNegotiation() throws MalformedURLException, ProtocolException, IOException {
		Node node = null;
		boolean followRedirects = true;
		String contentNegotiation = "application/rdf+xml";

		node = m.createResource("http://bis.informatik.uni-leipzig.de/images/jpegPhoto.php?name=sn&value=arndt").asNode();
		assertThat(HTTPConnector.connectToURI(node, contentNegotiation, followRedirects).getContentType(), CoreMatchers.is(CoreMatchers.not(CoreMatchers.containsString(contentNegotiation))));

		node = m.createResource("http://www.twitter.com/Daniel_hladky#id").asNode();
		assertThat(HTTPConnector.connectToURI(node, contentNegotiation, followRedirects).getContentType(), CoreMatchers.is(CoreMatchers.not(CoreMatchers.containsString(contentNegotiation))));

		node = m.createResource("http://aksw.org/MichaelMartin").asNode();
		assertThat(HTTPConnector.connectToURI(node, contentNegotiation, followRedirects).getContentType(), CoreMatchers.containsString(contentNegotiation) );

		node = m.createResource("http://dbpedia.org/resource/Leipzig").asNode();
		assertThat(HTTPConnector.connectToURI(node, contentNegotiation, followRedirects).getContentType(), CoreMatchers.containsString(contentNegotiation) );

		node = m.createResource("https://si0.twimg.com/profile_images/1843305351/VPSN3258_bigger.jpg").asNode();
		assertThat(HTTPConnector.connectToURI(node, contentNegotiation, followRedirects).getContentType(), CoreMatchers.is(CoreMatchers.not(CoreMatchers.containsString(contentNegotiation))));

		node = m.createResource("http://www.shop.sachsen.de/infai/leipziger-semantic-web-tag.htm").asNode();
		assertThat(HTTPConnector.connectToURI(node, contentNegotiation, followRedirects).getContentType(), CoreMatchers.is(CoreMatchers.not(CoreMatchers.containsString(contentNegotiation))));
	}

	@Test
	public void TestHTTPConnectorForParsableData() throws MalformedURLException, ProtocolException, IOException {
		Node node = null;
		boolean followRedirects = true;
		String contentNegotiation = "application/rdf+xml";

		node = m.createResource("http://bis.informatik.uni-leipzig.de/images/jpegPhoto.php?name=sn&value=arndt").asNode();
		assertFalse(HTTPConnector.connectToURI(node, contentNegotiation, followRedirects, true).isContentParsable());

		node = m.createResource("http://www.twitter.com/Daniel_hladky#id").asNode();
		assertFalse(HTTPConnector.connectToURI(node, contentNegotiation, followRedirects, true).isContentParsable());

		node = m.createResource("http://aksw.org/MichaelMartin").asNode();
		assertTrue(HTTPConnector.connectToURI(node, contentNegotiation, followRedirects, true).isContentParsable());

		node = m.createResource("http://dbpedia.org/resource/Leipzig").asNode();
		assertTrue(HTTPConnector.connectToURI(node, contentNegotiation, followRedirects, true).isContentParsable());

		node = m.createResource("https://si0.twimg.com/profile_images/1843305351/VPSN3258_bigger.jpg").asNode();
		assertFalse(HTTPConnector.connectToURI(node, contentNegotiation, followRedirects, true).isContentParsable());

		node = m.createResource("http://www.shop.sachsen.de/infai/leipziger-semantic-web-tag.htm").asNode();
		assertFalse(HTTPConnector.connectToURI(node, contentNegotiation, followRedirects, true).isContentParsable());
	}

}
