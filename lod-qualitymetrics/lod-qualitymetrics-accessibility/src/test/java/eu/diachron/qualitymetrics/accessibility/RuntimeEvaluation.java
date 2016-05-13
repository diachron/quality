/**
 * 
 */
package eu.diachron.qualitymetrics.accessibility;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.luzzu.assessment.ComplexQualityMetric;
import de.unibonn.iai.eis.luzzu.assessment.QualityMetric;
import de.unibonn.iai.eis.luzzu.exceptions.AfterException;
import de.unibonn.iai.eis.luzzu.exceptions.BeforeException;
import de.unibonn.iai.eis.luzzu.properties.PropertyManager;
import eu.diachron.qualitymetrics.accessibility.availability.EstimatedDereferenceabilityByStratified;
import eu.diachron.qualitymetrics.accessibility.availability.EstimatedDereferenceabilityForwardLinks;
import eu.diachron.qualitymetrics.accessibility.availability.EstimatedMisreportedContentType;
import eu.diachron.qualitymetrics.accessibility.availability.RDFAccessibility;
import eu.diachron.qualitymetrics.accessibility.availability.SPARQLAccessibility;
import eu.diachron.qualitymetrics.accessibility.interlinking.EstimatedDereferenceBackLinks;
import eu.diachron.qualitymetrics.accessibility.interlinking.EstimatedInterlinkDetectionMetric;
import eu.diachron.qualitymetrics.accessibility.interlinking.EstimatedLinkExternalDataProviders;
import eu.diachron.qualitymetrics.accessibility.licensing.HumanReadableLicense;
import eu.diachron.qualitymetrics.accessibility.licensing.MachineReadableLicense;
import eu.diachron.qualitymetrics.accessibility.performance.CorrectURIUsage;
import eu.diachron.qualitymetrics.accessibility.performance.DataSourceScalability;
import eu.diachron.qualitymetrics.accessibility.performance.HighThroughput;
import eu.diachron.qualitymetrics.accessibility.performance.LowLatency;
import eu.diachron.qualitymetrics.accessibility.security.DigitalSignatureUsage;
import eu.diachron.qualitymetrics.utilities.TestLoader;

/**
 * @author Jeremy Debattista
 * 
 */
public class RuntimeEvaluation {

	protected static TestLoader loader = new TestLoader();

	protected static QualityMetric m; //EstimatedDereferenceability.class, ,
	protected static Class<?>[] testing = new Class<?>[] { EstimatedDereferenceabilityByStratified.class, EstimatedDereferenceabilityForwardLinks.class, 
				EstimatedMisreportedContentType.class, RDFAccessibility.class, SPARQLAccessibility.class,
				EstimatedDereferenceBackLinks.class, EstimatedInterlinkDetectionMetric.class, EstimatedLinkExternalDataProviders.class,
				HumanReadableLicense.class, MachineReadableLicense.class, CorrectURIUsage.class, DataSourceScalability.class, HighThroughput.class, 
				LowLatency.class, DigitalSignatureUsage.class
			};
			
	public static void main (String [] args) throws BeforeException, AfterException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		loader.loadDataSet("/Users/jeremy/Dropbox/pdev-lemon.nt.gz");
		PropertyManager.getInstance().addToEnvironmentVars("baseURI", "http://social.mercedes-benz.com/de/");
		
		for (Class<?> clazz : testing){
			System.out.println("Evaluating "+ clazz.getSimpleName());
			long tMin = Long.MAX_VALUE;
			long tMax = Long.MIN_VALUE;
			long tAvg = 0;

			for (int i = -2; i < 10; i++){
				List<Quad> streamingQuads = loader.getStreamingQuads();
				long tStart = System.currentTimeMillis();
				m = (QualityMetric) clazz.getConstructor().newInstance(new Object[] {});
				if (m instanceof ComplexQualityMetric){
					((ComplexQualityMetric)m).before();
				}
				for(Quad quad : streamingQuads){
					m.compute(quad);
				}
				if (m instanceof ComplexQualityMetric){
					((ComplexQualityMetric)m).after();
				}
				if (i == 9){
					System.out.println(m.metricValue());
				} else {
					System.out.println(m.metricValue());
				}
				long tEnd = System.currentTimeMillis();
				if (i >= 0){
					long difference = tEnd - tStart;
					tAvg += difference;
					tMax = (tMax < difference) ? difference : tMax;
					tMin = (tMin > difference) ? difference : tMin;
				}
			}
			tAvg = tAvg/10;
			System.out.println("Min: "+ (tMin/1000.0) + " Max: "+ (tMax/1000.0) + " Avg: "+ (tAvg/1000.0));
		}
	}
}


