/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability.helper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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

import com.google.common.io.Files;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.diachron.datatypes.StatusCode;
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
	
	protected static PipedRDFIterator<?> iterator;
	protected static PipedRDFStream<?> rdfStream;
	protected static ExecutorService executor;
	
	final static Logger logger = LoggerFactory.getLogger(ModelParser.class);

	
	public static boolean snapshotParser(final String uri){
		Lang lang  =  Lang.TURTLE;
		
		initiate(lang);
		
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

		Future future = executor.submit(parser);
	
		try{
			while (iterator.hasNext()){
				logger.info("{} contains RDF", uri);
				future.cancel(true);
				return true;
			}
		} catch (Exception e){
			return false;
		}
		
		return false;
	}
	
	
	private static boolean snapshotParser(final CachedHTTPResource httpResource, final Lang givenLang){
		if (httpResource.isContainsRDF() != null) return httpResource.isContainsRDF();
		
		Lang lang  = (tryGetLang(httpResource) != null) ? tryGetLang(httpResource) : Lang.TURTLE;
		
		initiate(lang);
		
		if ((httpResource.getDereferencabilityStatusCode() == StatusCode.SC4XX) ||
				(httpResource.getDereferencabilityStatusCode() == StatusCode.SC5XX) ||
				(httpResource.getDereferencabilityStatusCode() == StatusCode.BAD)){
			return false;
		}
		
		Runnable parser = new Runnable(){
			@Override
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

		Future future = executor.submit(parser);
	
		try{
			while (iterator.hasNext()){
				logger.info("{} contains RDF", httpResource.getUri());
				future.cancel(true);
				return true;
			}
		} catch (Exception e){
			return false;
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private static void initiate(Lang lang){
		logger.info("Initiating Streams and Iterators");
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if ((executor != null) && (!executor.isShutdown())){
			try{
				rdfStream.finish();
			}catch (Exception e){}
			executor.shutdownNow();
		}
		
		if ((lang == Lang.NQ) || (lang == Lang.NQUADS)){
			iterator = new PipedRDFIterator<Quad>();
			rdfStream = new PipedQuadsStream((PipedRDFIterator<Quad>) iterator);
		} else {
			iterator = new PipedRDFIterator<Triple>();
			rdfStream = new PipedTriplesStream((PipedRDFIterator<Triple>) iterator);
		}
		
		executor = Executors.newSingleThreadExecutor();
	}
	
	private static void destroy() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (executor != null){
			if (!executor.isShutdown()){
				try{
					rdfStream.finish();
				}catch (Exception e){}
				executor.shutdownNow();
			}
		}
		
		iterator = null;
		rdfStream = null;
	}
	
	private static Lang tryGetLang(CachedHTTPResource resource){
		Lang lang = null;
		for (SerialisableHttpResponse shr : resource.getResponses()){
			String conType = shr.getHeaders("Content-Type");
			String[] s1 = conType.split(",");
			for(String s : s1){
				String[] p = s.split(";");
				lang = LinkedDataContent.contentTypeToLang(p[0]);
				if (lang == Lang.NTRIPLES) lang = Lang.TURTLE;
			}
		}
		return lang;
	}

	public static boolean hasRDFContent(CachedHTTPResource httpResource){
		return hasRDFContent(httpResource, null);
	}

	public static boolean hasRDFContent(CachedHTTPResource httpResource, Lang lang){
		boolean returnRes = snapshotParser(httpResource, lang);
		httpResource.setContainsRDF(returnRes);
//		destroy();
		return returnRes;
	}
	
	
	public static void main(String[]args) throws IOException{
		HTTPRetriever ret = new HTTPRetriever();
		String uris =  Files.readFirstLine(new File("/Users/jeremy/Desktop/uris.txt"), Charset.defaultCharset());
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