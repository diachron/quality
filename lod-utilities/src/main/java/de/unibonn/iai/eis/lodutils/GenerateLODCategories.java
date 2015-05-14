/**
 * 
 */
package de.unibonn.iai.eis.lodutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.io.Resources;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/**
 * @author Jeremy Debattista
 * 
 * This utility class generates an RDF file mapping 
 * Datasets to a category, as categorised in the
 * LOD Cloud.
 * 
 * @see [http://linkeddatacatalog.dws.informatik.uni-mannheim.de/state/LODCloudDiagram.html]
 */
public class GenerateLODCategories {
	
	private static Model m = ModelFactory.createDefaultModel();
	
	
	public static void main (String [] args) throws IOException{
		System.out.println("Reading Datasets and Categories...");
		URL file = Resources.getResource("datasetsAndCategories.tsv");
		CsvMapper mapper = new CsvMapper();
		mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
		File csvFile = new File(file.getFile());
		CsvSchema schema = CsvSchema.emptySchema().withColumnSeparator('\t');
		MappingIterator<String[]> it = mapper.reader(String[].class).with(schema).readValues(csvFile);
		while (it.hasNext()){
			String[] arr = it.next();
			m.add(m.createStatement(m.createResource(arr[0]), m.createProperty(":hasDomain"), m.createTypedLiteral(arr[1])));
		}
		System.out.println("Writing dump...");
		File f = new File("categories.ttl");
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		m.write(fos, "TURTLE");
	}
}
