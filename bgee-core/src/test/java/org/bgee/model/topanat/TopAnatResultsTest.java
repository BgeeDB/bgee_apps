package org.bgee.model.topanat;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.TestAncestor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit tests for {@link TopAnatResults}.
 * It uses a fake results tsv file placed in the resources folder
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, March 2016
 * @since Bgee 13
 */
public class TopAnatResultsTest extends TestAncestor {

    private final static Logger log = LogManager.getLogger(TopAnatParamsTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    } 

    /**
     * The {@link TopAnatResults} instance to be tested
     */
    private TopAnatResults topAnatResults;

    /**
     * The {@link BgeeProperties} instance that contains the properties
     */
    private BgeeProperties props;
    
    /**
     * This method inits the mock and real objects needed to run the tests
     */
    @Before
    public void initTest() {
        
        // init the BgeeProperties
        System.setProperty(BgeeProperties.TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY,TopAnatResultsTest.class
                .getClassLoader().getResource("").getPath().toString());
        this.props = BgeeProperties.getBgeeProperties();
        
        TopAnatParams mockTopAnatParams = mock(TopAnatParams.class);
        TopAnatController mockTopAnatController = mock(TopAnatController.class);
        when(mockTopAnatController.getBgeeProperties()).thenReturn(this.props);
        
        this.topAnatResults = new TopAnatResults(mockTopAnatParams, 
                "results",
                "test.tsv",
                "","","","","","","","",
                mockTopAnatController);
    }

    /**
     * Test the getRows method
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    @Test
    public void testGetRows() throws FileNotFoundException, IOException{
        assertEquals(this.topAnatResults.getRows().toString(),
        "[TopAnatResultRow [anatEntitiesId=UBERON:0003052, anatEntitiesName=midbrain-hindbrain "
        + "boundary, annotated=48.0, significant=20.0, expected=0.0609837255732349, "
        + "enrich=327.956349206349, pval=3.63340603984234E-52, fdr=1.86393729843912E-49], "
        + "TopAnatResultRow [anatEntitiesId=UBERON:0007651, anatEntitiesName=anatomical junction, "
        + "annotated=70.0, significant=20.0, expected=0.0889345997943009, enrich=224.884353741497, "
        + "pval=3.51013974794287E-48, fdr=9.00350845347345E-46], TopAnatResultRow "
        + "[anatEntitiesId=UBERON:0002616, anatEntitiesName=regional part of brain, annotated=145.0, "
        + "significant=18.0, expected=0.18422167100248, enrich=97.7083743842364, "
        + "pval=4.04315865669295E-35, fdr=6.91380130294494E-33], TopAnatResultRow "
        + "[anatEntitiesId=UBERON:0000073, anatEntitiesName=regional part of nervous system, "
        + "annotated=159.0, significant=18.0, expected=0.202008590961341, enrich=89.1051212938005, "
        + "pval=2.34390883546403E-34, fdr=3.00606308148262E-32], TopAnatResultRow "
        + "[anatEntitiesId=UBERON:0000924, anatEntitiesName=ectoderm, annotated=193.0, "
        + "significant=18.0, expected=0.245205396575715, enrich=73.4078460399704, "
        + "pval=9.14563003866293E-33, fdr=9.38341641966816E-31], TopAnatResultRow "
        + "[anatEntitiesId=UBERON:0001048, anatEntitiesName=primordium, annotated=274.0, "
        + "significant=19.0, expected=0.348115433480549, enrich=54.5795968022245, "
        + "pval=1.57264900361549E-32, fdr=1.34461489809125E-30], TopAnatResultRow "
        + "[anatEntitiesId=UBERON:0000923, anatEntitiesName=germ layer, annotated=292.0, "
        + "significant=19.0, expected=0.370984330570512, enrich=51.2151011089367, "
        + "pval=5.47277244903089E-32, fdr=3.50941533294106E-30], TopAnatResultRow "
        + "[anatEntitiesId=UBERON:0010316, anatEntitiesName=germ layer / neural crest, "
        + "annotated=292.0, significant=19.0, expected=0.370984330570512, enrich=51.2151011089367, "
        + "pval=5.47277244903089E-32, fdr=3.50941533294106E-30], TopAnatResultRow "
        + "[anatEntitiesId=UBERON:0005291, anatEntitiesName=embryonic tissue, annotated=343.0, "
        + "significant=19.0, expected=0.435779538992075, enrich=43.6000277662085, "
        + "pval=1.26851049582346E-30, fdr=7.23050982619374E-29]]");
    }    
}
