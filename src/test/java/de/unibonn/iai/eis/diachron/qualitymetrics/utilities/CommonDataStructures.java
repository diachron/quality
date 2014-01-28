package de.unibonn.iai.eis.diachron.qualitymetrics.utilities;

import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CommonDataStructures {
	
	
	protected Set<URI> checkedURISet = Collections.newSetFromMap(new ConcurrentHashMap<URI, Boolean>());

	public boolean uriExists(URI uri) {
		
		if (checkedURISet.contains(uri))
			return true;
		else
			return false;
		
		}

	public void addCheckedURISet(URI uri)
	{
		checkedURISet.add(uri);
	}
	
	
	

}
