package de.unibonn.iai.eis.diachron.qualitymetrics.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hp.hpl.jena.graph.Node;

public final class CommonDataStructures {
	
	private CommonDataStructures(){}
	
	private static Map<Node,Boolean> uriMap = new ConcurrentHashMap<Node, Boolean>();

	public static boolean uriExists(Node node) {
		return uriMap.containsKey(node) ? true : false;
	}

	public static void addToUriMap(Node node, boolean bool)
	{
		uriMap.put(node, bool);
	}
	
	public static boolean isUriBroken(Node node){
		return uriMap.get(node);
	}
	
}
