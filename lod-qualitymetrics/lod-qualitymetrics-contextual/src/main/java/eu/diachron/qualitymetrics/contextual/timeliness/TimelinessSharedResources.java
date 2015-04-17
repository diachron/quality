/**
 * 
 */
package eu.diachron.qualitymetrics.contextual.timeliness;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.mapdb.HTreeMap;

import de.unibonn.iai.eis.diachron.mapdb.MapDbFactory;

/**
 * @author Jeremy Debattista
 * 
 */
public class TimelinessSharedResources {

	private static HTreeMap<String, DatasetFreshness> map = MapDbFactory.createFilesystemDB().createHashMap("timeliness-dataset").make();

	public static DatasetFreshness getOrCreate(String uri){
		if (map.containsKey(uri)) return map.get(uri);
		else return map.put(uri, new DatasetFreshness());
	}
	
	public static Set<String> getURIs(){
		return map.keySet();
	}
	
	//In order to calculate the Currency we use the formula (dCurrency) provided in [56]
	public static double calculateCurrency(DatasetFreshness df){
		//in [29] they reuse the definition of currency as "the age of the data when it is delivered to the user"
		//Currency : the quality or state of information of being up-to-date or not outdated. (Martin Eppler)- http://iaidq.org/main/glossary.shtml
		//The currency of a value (or tuple) in a relational database is defined as the age of the value (or tuple)[3]; the age of a value 
		//(or a tuple) is computed as the difference between the current time and the time of last modification. - [56]
		
		double dCurrency = 0.0;
		
		long ageInMills = df.calculateAge();
		
		if (ageInMills == -1){
			return -1.0;
		} else {
			dCurrency = 1 - ((double)ageInMills / (df.getObservationDate().getTime() - ((df.getCreationDate() == null) ? df.getPublishedDate().getTime() : df.getCreationDate().getTime())));
		}
		
		return dCurrency;
	}
	
	
	protected static class DatasetFreshness implements Serializable{
		protected static final long serialVersionUID = -6068140495945782749L;
		
		public DatasetFreshness(){}

		private Date publishedDate = null;
		private Date creationDate = null;
		private Date updateDate = null;
		private Date expiryDate = null;
		private Date observationDate = new Date();
		
		public long calculateAge(){
			long ageInMills = -1l;
			if (((this.getUpdateDate() != null) && (this.getUpdateDate() != null) && (this.getUpdateDate().after(this.getCreationDate()))) 
					|| ((this.getUpdateDate() != null) && (this.getUpdateDate() == null))) {
				ageInMills = this.getObservationDate().getTime() - this.getUpdateDate().getTime();
			} else if ((this.getUpdateDate() == null) && (this.getCreationDate() != null)){
				ageInMills = this.getObservationDate().getTime() - this.getCreationDate().getTime();
			} 
			return ageInMills;
		}

		public Date getPublishedDate() {
			return publishedDate;
		}
		public void setPublishedDate(Date publishedDate) {
			synchronized(this.publishedDate){
				this.publishedDate = publishedDate;
			}
		}
		public Date getCreationDate() {
			return creationDate;
		}
		public void setCreationDate(Date creationDate) {
			synchronized(this.creationDate){
				this.creationDate = creationDate;
			}
		}
		public Date getUpdateDate() {
			return updateDate;
		}
		public void setUpdateDate(Date updateDate) {
			synchronized(this.updateDate){
				this.updateDate = updateDate;
			}
		}
		public Date getExpiryDate() {
			return expiryDate;
		}
		public void setExpiryDate(Date expiryDate) {
			synchronized(this.expiryDate){
				this.expiryDate = expiryDate;
			}
		}
		public Date getObservationDate() {
			return observationDate;
		}
		public void setObservationDate(Date observationDate) {
			synchronized(this.observationDate){
				this.observationDate = observationDate;
			}
		}
	}
}
