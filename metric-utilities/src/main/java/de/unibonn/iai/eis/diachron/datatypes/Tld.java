/**
 * 
 */
package de.unibonn.iai.eis.diachron.datatypes;

import de.unibonn.iai.eis.diachron.technques.probabilistic.ReservoirSampler;


/**
 * Structure representing a TLD, which includes its corresponding set of Fully Qualified URIs.
 * Encapsulates the items that will be stored in the reservoir of TLDs
 * @author slondono
 */
public class Tld {

	private String uri;
	private ReservoirSampler<String> fqUris;	// Set of fully qualified URIs part of the TLD
	
	private int maxSize;
	
	public Tld(String uri, int maxSize) {
		this.uri = uri;
		this.maxSize = maxSize;
	}
	
	/**
	 * Adds a new fully-qualified URI to those comprised by the TLD, if it is not already there
	 * @param fqUri New fully-qualified URI to be added
	 * @return True if the URI was added, false otherwise (either because it's already registered or was discarded)
	 */
	public boolean addFqUri(String fqUri) {
		// Lazy initialization of reservoir containing fully-qualified URIs
		if(this.fqUris == null) {
			this.fqUris = new ReservoirSampler<String>(maxSize, true);
		}
		// Check that the URI is not already in the reservoir
		if(this.fqUris.findItem(fqUri) == null) {
			return this.fqUris.add(fqUri);
		}
		return false;
	}
	
	public String getUri(){
		return this.uri;
	}
	
	public ReservoirSampler<String> getfqUris(){
		return this.fqUris;
	}
	
	/**
	 * Returns the number of fully-qualified URIs comprised by this TLD
	 */
	public int countFqUris() {
		if(this.fqUris == null) {
			return 0;
		}
		return this.fqUris.size();
	}
	
	/**
	 * For performance purposes and in order to make instances of this class as lightweight as possible
	 * when stored in Hashed datastructures, use the URI to generate hash codes (which entails that URI 
	 * should be unique among all instances of Tld)
	 */
	@Override
	public int hashCode() {
		return this.uri.hashCode();
	}
	
	@Override
    public boolean equals(Object obj) {
       if (!(obj instanceof Tld))
            return false;
        if (obj == this)
            return true;

        return (this.uri.equals(((Tld)obj).uri));
    }
}