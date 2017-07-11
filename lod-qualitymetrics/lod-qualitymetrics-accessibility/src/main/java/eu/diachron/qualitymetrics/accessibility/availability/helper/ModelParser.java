/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability.helper;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedQuadsStream;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.luzzu.datatypes.Object2Quad;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
import eu.diachron.qualitymetrics.utilities.LinkedDataContent;

/**
 * @author Jeremy Debattista
 * 
 * This class contains some utilities for parsing
 * resources, such as a snapshot parser, which is useful 
 * to peek into a resource and check if it contains RDF data
 * or not
 */
public class ModelParser {
			
	final static Logger logger = LoggerFactory.getLogger(ModelParser.class);
	
//	final static RequestConfig requestConfig = RequestConfig.custom()
//			.setSocketTimeout(2000)
//			.setConnectTimeout(2000)
//			.setRedirectsEnabled(true)
//			.build();
//
//	final static CloseableHttpClient httpClient = HttpClientBuilder
//								.create()
//								.setDefaultRequestConfig(requestConfig)
//								.build();
//	static{
//		HttpOp.setDefaultHttpClient(httpClient);
//		HttpOp.createCachingHttpClient();
//	}
	
	public static boolean timeoutModel(final String uri){
    	final Model m = ModelFactory.createDefaultModel();	
		try {
		      TimeLimitedCodeBlock.runWithTimeout(new Runnable() {
		        @Override
		        public void run() {
		        	RDFReader arp = m.getReader("RDF/XML");
					arp.setProperty("WARN_REDEFINITION_OF_ID","EM_IGNORE");
					arp.read(m, uri);
//					m.read(uri);
		        }
		      }, 3, TimeUnit.SECONDS);
		    }
		catch (Exception e) {
			logger.debug("Timeout Reading Model: "+uri);
		}
		
		if (m.size() > 0){
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean timeoutModel(final String uri, final Lang tryLang){
    	final Model m = ModelFactory.createDefaultModel();	
		try {
				final RDFReader arp = m.getReader(tryLang.getName());
				TimeLimitedCodeBlock.runWithTimeout(new Runnable() {
					@Override
				    public void run() {
						arp.setProperty("WARN_REDEFINITION_OF_ID","EM_IGNORE");
						arp.read(m, uri);
				//		m.read(uri, tryLang.getName());
				    }
				}, 3, TimeUnit.SECONDS);
		    } catch (Exception e) {
		    	logger.debug("Timeout Reading Model: "+uri);
		    }
		
		if (m.size() > 0){
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean snapshotParser(final String uri){
		return snapshotParser(uri, Lang.RDFXML); //by default
	}

	@SuppressWarnings("unchecked")
	public static boolean snapshotParser(final String uri, final Lang lang){
//		Lang lang  =  Lang.RDFXML;
		
		logger.info("Initiating Streams and Iterators");
		final PipedRDFIterator<?> iterator;
		final PipedRDFStream<?> rdfStream;
		
		if ((lang == Lang.NQ) || (lang == Lang.NQUADS)) {
			iterator = new PipedRDFIterator<Quad>();
			rdfStream = new PipedQuadsStream((PipedRDFIterator<Quad>)iterator);
		} else {
			iterator = new PipedRDFIterator<Triple>();
			rdfStream = new PipedTriplesStream((PipedRDFIterator<Triple>)iterator);
		}
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		
		Runnable parser = new Runnable(){
			@Override
			public void run() {
				try{
					logger.info("Trying to parse resource {}.", uri);
					RDFDataMgr.parse(rdfStream, uri, lang, null);
				} catch (Exception e){
					logger.info("Resource {} could not be parsed.", uri);
					rdfStream.finish();
				}
			}			
		};

		Future<?> future = executor.submit(parser);
		executor.shutdown();
		boolean tripleParsed = false;
	
		try {
 			if(iterator.hasNext()) {
				String tripleRead = (iterator.next()).toString();
				logger.debug("{} contains RDF. Triple read: {}", uri, tripleRead);
				// OK we know there's some RDF, stop processing
				tripleParsed = true;
			} 
			else {
				logger.debug("{} does not contain RDF", uri);
			}
			future.cancel(true);
			iterator.close();
		} catch (Exception e) {
			tripleParsed = false;
		}
		
		return tripleParsed;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private static boolean snapshotParser(final CachedHTTPResource httpResource, final Lang givenLang){
		// First, check if the resource is already known to contain RDF
		if (httpResource.isContentParsable() != null) {
			return httpResource.isContentParsable();
		}
		
		Lang lang  = (tryGetLang(httpResource) != null) ? tryGetLang(httpResource) : Lang.RDFXML;
						
		if ((httpResource.getDereferencabilityStatusCode() == StatusCode.SC4XX) ||
				(httpResource.getDereferencabilityStatusCode() == StatusCode.SC5XX) ||
				(httpResource.getDereferencabilityStatusCode() == StatusCode.BAD)) {
			return false;
		}
		
		logger.info("Initiating Streams and Iterators");
		final PipedRDFIterator<?> iterator;
		final PipedRDFStream<?> rdfStream;
		
		if ((lang == Lang.NQ) || (lang == Lang.NQUADS)) {
			iterator = new PipedRDFIterator<Quad>();
			rdfStream = new PipedQuadsStream((PipedRDFIterator<Quad>)iterator);
		} else {
			iterator = new PipedRDFIterator<Triple>();
			rdfStream = new PipedTriplesStream((PipedRDFIterator<Triple>)iterator);
		}
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		
		Runnable parser = new Runnable() {
			public void run() {
				try{
					logger.info("Trying to parse resource {}.", httpResource.getUri());
					if (givenLang == null) RDFDataMgr.parse(rdfStream, httpResource.getUri());
					else RDFDataMgr.parse(rdfStream, httpResource.getUri(), givenLang, null);
				} catch (Exception e){
					logger.info("Resource {} could not be parsed. Exception {}", httpResource.getUri(), e.getMessage());
					rdfStream.finish();
				}
			}
		};

		Future<?> future = executor.submit(parser);
		executor.shutdown();
		boolean tripleParsed = false;
	
		try {
			if(iterator.hasNext()) {
				String tripleRead = (iterator.next()).toString();
				logger.debug("{} contains RDF. Triple read: {}", httpResource.getUri(), tripleRead);
				// OK we know there's some RDF, stop processing
				tripleParsed = true;
			} else {
				logger.debug("{} does not contain RDF", httpResource.getUri());
			}
			future.cancel(true);
			iterator.close();
		} catch (Exception e) {
			tripleParsed = false;
		}
		
		return tripleParsed;
	}
	
	
	@SuppressWarnings("unchecked")
	public static boolean snapshotParserForForwardDereference(final CachedHTTPResource httpResource, final Lang givenLang, final String subjectURI){
		// First, check if the resource is already known to contain RDF
		if (httpResource.isContentParsable() != null) {
			return httpResource.isContentParsable();
		}
		
		Lang lang  = (tryGetLang(httpResource) != null) ? tryGetLang(httpResource) : Lang.TURTLE;
						
		if ((httpResource.getDereferencabilityStatusCode() == StatusCode.SC4XX) ||
				(httpResource.getDereferencabilityStatusCode() == StatusCode.SC5XX) ||
				(httpResource.getDereferencabilityStatusCode() == StatusCode.BAD)) {
			return false;
		}
		
		logger.info("Initiating Streams and Iterators");
		final PipedRDFIterator<?> iterator;
		final PipedRDFStream<?> rdfStream;
		
		if ((lang == Lang.NQ) || (lang == Lang.NQUADS)) {
			iterator = new PipedRDFIterator<Quad>();
			rdfStream = new PipedQuadsStream((PipedRDFIterator<Quad>)iterator);
		} else {
			iterator = new PipedRDFIterator<Triple>();
			rdfStream = new PipedTriplesStream((PipedRDFIterator<Triple>)iterator);
		}
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		
		Runnable parser = new Runnable() {
			public void run() {
				try{
					logger.info("Trying to parse resource {}.", httpResource.getUri());
					if (givenLang == null) RDFDataMgr.parse(rdfStream, httpResource.getUri());
					else RDFDataMgr.parse(rdfStream, httpResource.getUri(), givenLang, null);
				} catch (Exception e){
					logger.info("Resource {} could not be parsed. Exception {}", httpResource.getUri(), e.getMessage());
					rdfStream.finish();
				}
			}
		};

		Future<?> future = executor.submit(parser);
		executor.shutdown();
		boolean tripleParsed = false;
		
	
		try {
			while((iterator.hasNext()) && (tripleParsed == false))   {
				Object2Quad stmt = new Object2Quad(iterator.next());
				String tripleRead = (iterator.next()).toString();
				logger.debug("{} contains RDF. Triple read: {}", httpResource.getUri(), tripleRead);
				// OK we know there's some RDF, stop processing
				if (stmt.getStatement().getSubject().getURI().equals(subjectURI))
					tripleParsed = true;
			}
			future.cancel(true);
			iterator.close();
		} catch (Exception e) {
			tripleParsed = false;
		}
		
		return tripleParsed;
	}
		
	private static Lang tryGetLang(CachedHTTPResource resource){
		Lang lang = null;
		for (SerialisableHttpResponse shr : resource.getResponses()){
			String conType = shr.getHeaders("Content-Type");
			if(conType != null) {
				String[] s1 = conType.split(",");
				for(String s : s1){
					String[] p = s.split(";");
					lang = LinkedDataContent.contentTypeToLang(p[0]);
				}
			} else {
				return null;
			}
		}
		return lang;
	}


	public static boolean hasRDFContent(CachedHTTPResource httpResource, Lang lang){
		Dereferencer.parsable(httpResource, lang);
		return httpResource.isContentParsable();
	}
		
	public static void main(String[]args) throws IOException{
		String uris = "http://transparency.270a.info/dataset/corruption-perceptions-index/2011/ZW,http://www.icane.es/opendata/void#ICANE";
		for (String uri : uris.split(",")){
			System.out.println(ModelParser.timeoutModel(uri));
		}
	}
	
}