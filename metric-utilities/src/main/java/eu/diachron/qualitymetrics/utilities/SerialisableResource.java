/**
 * 
 */
package eu.diachron.qualitymetrics.utilities;

import java.io.Serializable;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

import de.unibonn.iai.eis.luzzu.cache.JenaCacheObject;

/**
 * @author Jeremy Debattista
 * 
 */
public class SerialisableResource extends ResourceImpl implements Serializable, JenaCacheObject<Resource> {

	private static final long serialVersionUID = -6999181374899138929L;

	private String _resource = "";
	private String literalDataType = "";
	
	private boolean isURI = false;
	private boolean isBlank = false;
	private boolean isLiteral = false;
	
	public SerialisableResource(){
		super();
	}
	
	private SerialisableResource(Resource resource){
		super();
		
		this.isURI = resource.isURIResource();
		this.isBlank = resource.isAnon();
		this.isLiteral = resource.isLiteral();
		
		this._resource = resource.toString();
		
		if (this.isLiteral)
			literalDataType = resource.asLiteral().getDatatypeURI();
	}
	
	public Resource getResource(){
		if (isURI) return ModelFactory.createDefaultModel().createResource(this._resource);
		else if (isBlank) return ModelFactory.createDefaultModel().createResource(this._resource);
		else if (isLiteral) return ModelFactory.createDefaultModel().createLiteral(this._resource, this.literalDataType).asResource();
		else return ModelFactory.createDefaultModel().createResource();
	}

	@Override
	public Resource deserialise() {
		return this.getResource();
	}
}
