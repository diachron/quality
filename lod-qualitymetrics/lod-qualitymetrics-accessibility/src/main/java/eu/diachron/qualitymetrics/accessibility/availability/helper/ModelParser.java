/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedQuadsStream;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
import de.unibonn.iai.eis.luzzu.datatypes.Object2Quad;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.DiachronCacheManager;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;
import eu.diachron.qualitymetrics.utilities.HTTPRetriever;
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
	
	@SuppressWarnings("unchecked")
	public static boolean snapshotParser(final String uri){
		Lang lang  =  Lang.TURTLE;
		
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
					RDFDataMgr.parse(rdfStream, uri);
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
	
	@SuppressWarnings("unchecked")
	private static boolean snapshotParser(final CachedHTTPResource httpResource, final Lang givenLang){
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

	public static boolean hasRDFContent(CachedHTTPResource httpResource){
		return hasRDFContent(httpResource, null);
	}

	public static boolean hasRDFContent(CachedHTTPResource httpResource, Lang lang){
		boolean returnRes = snapshotParser(httpResource, lang);
		httpResource.setParsableContent(returnRes);
		return returnRes;
	}
	
	
	
	public static void main(String[]args) throws IOException{
		HTTPRetriever ret = new HTTPRetriever();
//		String uris = Files.readFirstLine(new File("/Users/jeremy/Desktop/uris.txt"), Charset.defaultCharset());
		String uris = "http://pdev.org.uk/pdevlemon/PDEN_LexicalEntry_1008";
		ret.addListOfResourceToQueue(Arrays.asList(uris.split(",")));
		ret.start();
		int counter = 0;
		int wrong = 0;
		List<String> nonderef = new ArrayList<String>();
		for (String uri : uris.split(",")){
			CachedHTTPResource httpResource = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);	
			while (httpResource == null){
				httpResource = (CachedHTTPResource) DiachronCacheManager.getInstance().getFromCache(DiachronCacheManager.HTTP_RESOURCE_CACHE, uri);
			}
			if (Dereferencer.hasValidDereferencability(httpResource))
				if (hasRDFContent(httpResource)) counter++; else wrong++;
			else {
				nonderef.add(httpResource.getUri());
				wrong++;
			}
		}
		System.out.println(counter + " " + wrong);
		for (String s : nonderef){
			System.out.println(s);
		}
	}
}