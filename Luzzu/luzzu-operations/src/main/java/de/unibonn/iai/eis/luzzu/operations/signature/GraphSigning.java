package de.unibonn.iai.eis.luzzu.operations.signature;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.codec.digest.DigestUtils;

import com.hp.hpl.jena.sparql.core.Quad;

public class GraphSigning {
	
	private Set<String> securehashSet = Collections.synchronizedSortedSet(new TreeSet<String>());
	private Set<String> hashSet = Collections.synchronizedSortedSet(new TreeSet<String>());

	public static final BigInteger N_MUL = BigInteger.probablePrime(1024, new Random(Long.MAX_VALUE)); // Multiplication modulo to make it more secure (See Karsen paper - HashCombinator.java)
	
	public void addSecureHash(Quad quad){
		// TODO 1. we should ignore blank nodes
		// TODO 2. we should ignore certain triples 
		
		byte[] subjectHash = null;
		if (!(quad.getSubject().isBlank())){
			subjectHash = DigestUtils.md5(quad.getSubject().toString());
		}
		
		byte[] propertyHash = DigestUtils.md5(quad.getPredicate().toString());
		
		byte[]  objectHash = null;
		if (!(quad.getObject().isBlank())){
			objectHash = DigestUtils.md5(quad.getObject().toString());
		}
		
		byte[]  graphHash = null;
		if (quad.getGraph() != null){
			graphHash = DigestUtils.md5(quad.getGraph().toString());
		}
		
		int subjectHashLength = (subjectHash != null) ? subjectHash.length : 0;
		int propertyHashLength = propertyHash.length;
		int objectHashLength = (objectHash != null) ? objectHash.length : 0;
		int graphHashlength = (graphHash != null) ? graphHash.length : 0;
		
		int byteLength = subjectHashLength + propertyHashLength + objectHashLength + graphHashlength;
		byte[] totalHash = new byte[byteLength];
		
		if (subjectHash != null) System.arraycopy(subjectHash, 0, totalHash, 0, subjectHashLength);
		System.arraycopy(propertyHash, 0, totalHash, subjectHashLength, propertyHash.length);
		if (objectHash != null) System.arraycopy(objectHash, 0, totalHash, subjectHashLength + propertyHashLength, objectHash.length);
		if (graphHash != null) System.arraycopy(graphHash, 0, totalHash, subjectHashLength + propertyHashLength + objectHashLength, graphHash.length);
		
		BigInteger totHash_int = new BigInteger(totalHash);
		this.securehashSet.add(DigestUtils.md5Hex(totHash_int.multiply(N_MUL).toByteArray()));
	}
	
	public void addHash(Quad quad){
		// TODO 1. we should ignore blank nodes
		// TODO 2. we should ignore certain triples 
		
		String subjectHash = "";
		if (!(quad.getSubject().isBlank())){
			subjectHash = DigestUtils.md5Hex(quad.getSubject().toString());
		}
		
		String propertyHash = DigestUtils.md5Hex(quad.getPredicate().toString());
		
		String objectHash = "";
		if (!(quad.getObject().isBlank())){
			objectHash = DigestUtils.md5Hex(quad.getObject().toString());
		}
		
		String graphHash = "";
		if (quad.getGraph() != null){
			graphHash = DigestUtils.md5Hex(quad.getGraph().toString());
		}
		
		this.hashSet.add(DigestUtils.md5Hex(subjectHash+propertyHash+objectHash+graphHash));
	}
	
	public String retrieveHash(boolean secureHash){
		StringBuilder sb = new StringBuilder();
		
		
		if (secureHash){
			for (String s : this.securehashSet){
				sb.append(s);
			}
			this.securehashSet = Collections.synchronizedSortedSet(new TreeSet<String>());
		} else {
			for (String s : this.hashSet){
				sb.append(s);
			}
			this.hashSet = Collections.synchronizedSortedSet(new TreeSet<String>());
		}
		
		return DigestUtils.md5Hex(sb.toString());
	}
}
