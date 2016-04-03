/**
 * 
 */
package eu.diachron.qualitymetrics.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeremy Debattista
 * 
 * Access the LOV APIs
 * 
 * http://lov.okfn.org/dataset/lov/api
 */
public class LOVInterface {

	private final static String LOV_API_PATH = "http://lov.okfn.org/dataset/lov/api/";
	private final static String LOV_API_VERSION2_PATH = "v2/";
	
//	private final static String TERM_API_PATH = "term/";
	private final static String VOCABULARY_API_PATH = "vocabulary/";
//	private final static String AGENT_API_PATH = "agent/";
	
	private static Logger logger = LoggerFactory.getLogger(LOVInterface.class);

	
	public static List<String> getKnownVocabsPerDomain(String domain) throws ClientProtocolException, IOException{
		List<String> vocabs = new ArrayList<String>();
		logger.info("Searching LOV for vocabularies in the domain of {}.",domain);
		String uriPath = LOV_API_PATH + LOV_API_VERSION2_PATH + VOCABULARY_API_PATH + "search?q="+domain;
		
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(uriPath);
		HttpResponse response = client.execute(get);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonResponse = mapper.readTree(rd);
		
		Iterator<JsonNode> results = jsonResponse.findValue("results").getElements();
		while(results.hasNext()){
			JsonNode res = results.next();
			JsonNode source = res.findValue("_source");
			String uri = source.get("uri").getTextValue();
			//TODO: some sort of blacklist
			if (!(uri.contains("vocab.deri.ie"))){
				VocabularyLoader.getInstance().loadVocabulary(uri);
				if (VocabularyLoader.getInstance().getModelForVocabulary(uri) != null) vocabs.add(uri);
			}
		}
		return vocabs;
	}
	
}
