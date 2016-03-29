/**
 * 
 */
package eu.diachron.qualitymetrics.intrinsic.syntacticvalidity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.unibonn.iai.eis.diachron.semantics.DQM;
import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.datatypes.ProblemList;
import de.unibonn.iai.eis.luzzu.exceptions.AfterException;
import de.unibonn.iai.eis.luzzu.exceptions.BeforeException;
import de.unibonn.iai.eis.luzzu.properties.EnvironmentProperties;
import de.unibonn.iai.eis.luzzu.semantics.utilities.Commons;

/**
 * @author Jeremy Debattista
 * 
 */
public class CorrectLanguageTag implements ComplexQualityMetric {
	
	private static Logger logger = LoggerFactory.getLogger(CorrectLanguageTag.class);
	
	private String lexvoDataURI = "http://www.lexvo.org/data/term/{language}/{term}";
	private String lexvoResourceURI = "http://lexvo.org/id/term/{language}/{term}";
	private String languageTranslatorURI = "https://services.open.xerox.com/bus/op/LanguageIdentifier/GetLanguageForString";
	
	private Map<String,String> langMap = new HashMap<String, String>();
	

	private int totalvalidLangStrings = 0;
	private int totalCorrectStrings = 0;
	
	@Override
	public void compute(Quad quad) {
		Node obj = quad.getObject();
		
		if (obj.isLiteral()){
			RDFNode n = Commons.asRDFNode(obj);
			Literal lt = (Literal) n;
			String language = lt.getLanguage();
			
			if (!language.equals("")){
				totalvalidLangStrings++;
				try {
					if (this.correctLanguageTag(obj)) totalCorrectStrings++;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public double metricValue() {
		
		double metricValue = (double) totalCorrectStrings / (double) totalvalidLangStrings;
		
		statsLogger.info("Correct Language Tag. Dataset: {} - Total # Correct Strings : {}; # Total Valid Language Strings : {}, Metric Value: {}", 
				EnvironmentProperties.getInstance().getDatasetURI(), totalCorrectStrings, totalvalidLangStrings,metricValue);

		return metricValue;
	}

	@Override
	public Resource getMetricURI() {
		return DQM.CorrectLanguageTag;
	}

	@Override
	public ProblemList<?> getQualityProblems() {
		return new ProblemList<Model>();
	}

	@Override
	public boolean isEstimate() {
		return true;
	}

	@Override
	public Resource getAgentURI() {
		return DQM.LuzzuProvenanceAgent;
	}
	
	
	private boolean correctLanguageTag(Node lit_obj) throws UnsupportedEncodingException{
		RDFNode n = Commons.asRDFNode(lit_obj);
		Literal lt = (Literal) n;
		logger.info("Checking for {} :"+lt.toString());
		String stringValue = lt.getLexicalForm().trim();
		String language = lt.getLanguage();
		
		
		if (!language.equals("")){
			String[] splited = stringValue.split("\\b+"); 
			
			if (splited.length > 2){
				//its a sentence
				String lang = "";
				try {
					lang = this.langRestAPI(stringValue).replace("\"","");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if (lang.equals("")){
					// we cannot identify the language
					return false;
				} else {
					String shortLang = language.length() > 2 ? language.substring(language.length() - 2) : language;
					return shortLang.equals(lang);
				}
			} else {
				//its one word
				String shortLang = language.length() > 2 ? language.substring(language.length() - 2) : language;
				String lexvoLang = "";
				if (langMap.containsKey(shortLang)) lexvoLang = langMap.get(shortLang).substring(langMap.get(shortLang).length() - 3);
				
				if (!(lexvoLang.equals(""))){
					String data = this.lexvoDataURI.replace("{language}", lexvoLang).replace("{term}", URLEncoder.encode(stringValue, "UTF-8"));
					String uri = this.lexvoResourceURI.replace("{language}", lexvoLang).replace("{term}", URLEncoder.encode(stringValue, "UTF-8"));
					
					Model m = RDFDataMgr.loadModel(data);
					return m.contains(m.createResource(uri), RDFS.seeAlso);
				}
			}
		}
		return false;
	}
	
	private String langRestAPI(String content) throws UnsupportedEncodingException, IOException{
		URL url = new URL(languageTranslatorURI);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);

		OutputStream os = conn.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		writer.write("document="+content);
		writer.flush();
		writer.close();
		os.close();

		conn.connect();

		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						
		String input = "";
						
		while ((input = br.readLine()) != null)
			return input;
		
		return input;
	}
	
	@Override
	public void before(Object... args) throws BeforeException {
		String filename = CorrectLanguageTag.class.getClassLoader().getResource("lexvo/language_mapping.tsv").getFile();
		try {
			logger.info("Loading language file");
			CSVReader reader = new CSVReader(new FileReader(filename),'\t');
			List<String[]> allLanguages = reader.readAll();
			for(String[] language : allLanguages)
				langMap.put(language[0], language[1]);
			reader.close();

		} catch (IOException e) {
			logger.error("Error Loading language file: {}", e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public void after(Object... args) throws AfterException {
		// Nothing to do here
	}
	
}
