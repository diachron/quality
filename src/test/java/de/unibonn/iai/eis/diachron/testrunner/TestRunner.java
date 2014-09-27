/**
 * 
 */
package de.unibonn.iai.eis.diachron.testrunner;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.unibonn.iai.eis.diachron.qualitymetrics.contextual.relevancy.CoverageTest;
import de.unibonn.iai.eis.diachron.qualitymetrics.contextual.relevancy.RelevantTermsWithinMetaInformationTest;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability.BlackListingTest;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability.IdentityInformationProviderTest;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability.ProvenanceInformationTest;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.believability.TrustworthinessRDFStatementTest;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.reputation.ReputationTest;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.verifiability.AuthenticityDatasetTest;
import de.unibonn.iai.eis.diachron.qualitymetrics.trust.verifiability.DigitalSignatureTest;

@RunWith(Suite.class)
@SuiteClasses({ DigitalSignatureTest.class, 
        AuthenticityDatasetTest.class, 
        ReputationTest.class, 
        TrustworthinessRDFStatementTest.class,
        ProvenanceInformationTest.class,
        IdentityInformationProviderTest.class,
        BlackListingTest.class,
        CoverageTest.class,
        RelevantTermsWithinMetaInformationTest.class})
public class TestRunner {

}
