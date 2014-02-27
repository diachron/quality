package de.unibonn.iai.eis.diachron.qualitymetrics;

public class DimensionNamesOntology {

	public enum DimensionsGroup {
		ACCESIBILITY, INTRINSIC, TRUST, DATASET_DYNAMICITY, CONTEXTUAL, REPRESENTATIONAL
	};

	public class ACCESIBILITY {
		public static final String AVAILABILITY = "Availability";
		public static final String LICENSING = "Licensing";
		public static final String INTERLINKING = "Interlinking";
		public static final String SECURITY = "Security";
		public static final String PERFORMANCE = "Performance";
		public static final String GROUP_NAME = "Accesibility";
	}

	public class INTRINSIC {
		public static final String ACCURACY = "Accuracy";
		public static final String CONCISTENCY = "Concistency";
		public static final String CONCISENESS = "Concoseness";
		public static final String GROUP_NAME = "Intrincis";

	}

	public class TRUST {
		public static final String REPUTATION = "Reputation";
		public static final String BELIVABILITY = "Belivability";
		public static final String VERIFIABILITY = "Verifiability";
		public static final String OBJECTIVITY = "Objectivity";
		public static final String GROUP_NAME = "Trust";
	};

	public class DATASET_DYNAMICITY {
		public static final String CURRENCY = "Currency";
		public static final String VOLATILITY = "Volatility";
		public static final String TIMELINESS = "Timeliness";
		public static final String GROUP_NAME = "Dataset_dynamicity";
	};

	public class CONTEXTUAL {
		public static final String COMPLITENESS = "Compliteness";
		public static final String AMOUNT_OF_DATA = "Amount_of_data";
		public static final String RELEVANCY = "Relevancy";
		public static final String GROUP_NAME = "Contextual";
	};

	public class REPRESENTATIONAL {
		public static final String REPR_CONSINESS = "Repr_consiness";
		public static final String REPR_CONSISTENCY = "Repr_consistency";
		public static final String UNDERSTANDABILITY = "Understandability";
		public static final String INTERPRETABILITY = "Interpretability";
		public static final String VERSALITY = "Versality";
		public static final String GROUP_NAME = "Representational";
	};

}
