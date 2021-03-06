/**
 * 
 */
package eu.diachron.qualitymetrics.utilities;

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.sparql.graph.GraphFactory;

import de.unibonn.iai.eis.luzzu.cache.JenaCacheObject;

/**
 * @author Jeremy Debattista
 * 
 */
public class SerialisableModel extends ModelCom implements Serializable, JenaCacheObject<Model>{
	
	private static final long serialVersionUID = -6886059925250721814L;

	private transient Model model;
	private String _modelString = "";
	
	public SerialisableModel(){
		super(GraphFactory.createDefaultGraph());
	}

	public SerialisableModel(Model model){
		super(model.getGraph());
		this.model = model;
		
		StringWriter sw = new StringWriter();
		RDFDataMgr.write(sw, this.model, Lang.TURTLE);
		this._modelString = sw.toString();
	}
	
	public Model toJenaModel(){
		StringReader sr = new StringReader(this._modelString);
		RDFDataMgr.read(this.model, sr, "", Lang.TURTLE);
		return this.model;
	}

	@Override
	public Model deserialise() {
		return this.toJenaModel();
	}
}
