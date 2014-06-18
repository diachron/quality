package forth.diachron.qualitymetrics.dynamicity.volatility;

public class TestEvolutionQualityMetrics {

	public static void main (String args[]){
		
		VersionsVolatility vv = new VersionsVolatility();
		System.out.println("Counting VersionsVolatility...");
		vv.compute();
		System.out.println("VersionsVolatility Result:" +vv.metricValue());
		
		AverageVolatility av = new AverageVolatility();
		System.out.println("Counting AverageVolatility...");
		av.compute();
		System.out.println("AverageVolatility Result:" +av.metricValue());
		
	}
}
