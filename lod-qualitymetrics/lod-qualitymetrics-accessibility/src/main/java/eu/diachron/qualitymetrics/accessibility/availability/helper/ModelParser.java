/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility.availability.helper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.lang.PipedQuadsStream;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import eu.diachron.qualitymetrics.cache.CachedHTTPResource;
import eu.diachron.qualitymetrics.cache.CachedHTTPResource.SerialisableHttpResponse;

/**
 * @author Jeremy Debattista
 * 
 */
public class ModelParser {
	
	protected static PipedRDFIterator<?> iterator;
	protected static PipedRDFStream<?> rdfStream;
	protected static ExecutorService executor;
	
	public static boolean isContentRDF(final String uri){
		Lang lang  = Lang.TURTLE;//(tryGetLang(httpResource) != null) ? tryGetLang(httpResource) : Lang.TURTLE;
		
		initiate(lang);
		
		System.out.println(uri);
		Runnable parser = new Runnable(){
			@Override
			public void run() {
				try{
					RDFDataMgr.parse(rdfStream, uri);
				} catch (Exception e){
					rdfStream.finish();
				}
			}			
		};
		
		executor.submit(parser);
	
		try{
			while (iterator.hasNext()){
				return true;
			}
		} catch (Exception e){
			return false;
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private static void initiate(Lang lang){
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
		if (!executor.isShutdown()){
			try{
				rdfStream.finish();
			}catch (Exception e){}
			executor.shutdownNow();
		}
		
		iterator = null;
		rdfStream = null;
	}

	public static void main (String [] args) {
		System.out.println(isContentRDF("http://www.dadadsa.com"));
		System.out.println(isContentRDF("http://imf.270a.info/data/imf.observations.ttl"));
		System.out.println(isContentRDF("http://imf.270a.info/dataset/MCORE.ttl"));
		System.out.println(isContentRDF("http://www.google.com"));

		destroy();
	}
		
	private Lang tryGetLang(CachedHTTPResource resource){
		Lang lang = null;
		for (SerialisableHttpResponse shr : resource.getResponses()){
			String conType = shr.getHeaders("Content-Type");
			String[] s1 = conType.split(",");
			for(String s : s1){
				String[] p = s.split(";");
				lang = WebContent.contentTypeToLang(p[0]);
				if (lang == Lang.NTRIPLES) lang = Lang.TURTLE;
			}
		}
		return lang;
	}

}
