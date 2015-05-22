package org.bgee.pipeline.expression.downloadfile;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTOResultSet;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.anatdev.MySQLAnatEntityDAO.MySQLAnatEntityTOResultSet;
import org.bgee.model.dao.mysql.anatdev.MySQLStageDAO.MySQLStageTOResultSet;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO.MySQLGeneTOResultSet;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO.MySQLSpeciesTOResultSet;
import org.bgee.pipeline.TestAncestor;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * This abstract class provides convenient common methods that test generation of TSV download files 
 * from the Bgee database.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class GenerateDownloadFileTest extends TestAncestor {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(GenerateDownloadFileTest.class.getName());

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    public GenerateDownloadFileTest(){
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Assert that common columns row are equal. It checks gene name, stage ID, stage name, 
     * anatomical entity ID, and anatomical entity name columns.
     * 
     * @param geneId            A {@code String} that is the gene ID of the row.
     * @param expGeneName       A {@code String} that is the expected gene name.
     * @param geneName          A {@code String} that is the actual gene name.
     * @param expStageId        A {@code String} that is the expected stage ID.
     * @param stageId           A {@code String} that is the actual stage ID.
     * @param expStageName      A {@code String} that is the expected stage name.
     * @param stageName         A {@code String} that is the actual stage name.
     * @param expAnatEntityId   A {@code String} that is the expected anatomical entity ID.
     * @param anatEntityId      A {@code String} that is the actual anatomical entity ID.
     * @param expAnatEntityName A {@code String} that is the expected anatomical entity name.
     * @param anatEntityName    A {@code String} that is the actual anatomical entity name.
     */
    protected void assertCommonColumnRowEqual(String geneId, String expGeneName, String geneName, 
            String expStageName, String stageName, String expAnatEntityName, String anatEntityName,
            String expResume, String resume, String expQual, String quality) {
        assertEquals("Incorrect gene name for " + geneId, expGeneName, geneName);
        assertEquals("Incorrect stage name for " + geneId, expStageName, stageName);
        assertEquals("Incorrect anaEntity name for " + geneId, expAnatEntityName, anatEntityName);
        assertEquals("Incorrect resume for " + geneId, expResume, resume);
        assertEquals("Incorrect quality for " + geneId, expQual, quality);
    }

    /**
     * Define a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies.
     * 
     * @param mockManager A {@code MySQLDAOManager} to for the class to acquire mock DAOs.
     */
    protected SpeciesTOResultSet mockGetSpecies(MockDAOManager mockManager, Set<String> speciesIds) {
        MySQLSpeciesTOResultSet mockSpeciesTORs = createMockDAOResultSet(
                Arrays.asList(
                        new SpeciesTO("11", null, "Genus11", "species11", null, null, null, null),
                        new SpeciesTO("22", null, "Genus22", "species22", null, null, null, null)),
                        MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getSpeciesByIds(speciesIds)).thenReturn(mockSpeciesTORs);
        return mockSpeciesTORs;
    }   
    
    /**
     * Define a mock MySQLGeneTOResultSet to mock the return of getGenesBySpeciesIds.
     * 
     * @param mockManager A {@code MySQLDAOManager} to for the class to acquire mock DAOs.
     */
    protected GeneTOResultSet mockGetGenes(MockDAOManager mockManager, Set<String> speciesIds) {
        MySQLGeneTOResultSet mockGeneTORs = createMockDAOResultSet(
                Arrays.asList(
                        new GeneTO("ID1", "genN1", null),
                        new GeneTO("ID2", "genN2", null),
                        new GeneTO("ID3", "genN3", null),
                        new GeneTO("ID4", "genN4", null),
                        new GeneTO("ID5", "genN5", null),
                        new GeneTO("ID6", "genN6", null)),
                        MySQLGeneTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockGeneDAO.getGenesBySpeciesIds(speciesIds)).thenReturn(mockGeneTORs);
        return mockGeneTORs;
    }

    /**
     * Define a mock MySQLStageTOResultSet to mock the return of getStagesBySpeciesIds.
     * 
     * @param mockManager A {@code MySQLDAOManager} to for the class to acquire mock DAOs.
     */
    protected StageTOResultSet mockGetStages(MockDAOManager mockManager, Set<String> speciesIds) {
        MySQLStageTOResultSet mockStageTORs = createMockDAOResultSet(
                Arrays.asList(
                        new StageTO("Stage_id1", "stageN1", null, null, null, null, null, null),
                        new StageTO("ParentStage_id1", "parentstageN1", null, null, null, null, null, null),
                        new StageTO("ParentStage_id2", "parentstageN2", null, null, null, null, null, null),
                        new StageTO("Stage_id2", "stageN2", null, null, null, null, null, null),
                        new StageTO("Stage_id3", "stageN3", null, null, null, null, null, null),
                        new StageTO("Stage_id5", "stageN5", null, null, null, null, null, null),
                        new StageTO("ParentStage_id5", "parentstageN5", null, null, null, null, null, null),
                        new StageTO("Stage_id6", "stageN6", null, null, null, null, null, null),
                        new StageTO("Stage_id7", "stageN7", null, null, null, null, null, null),
                        new StageTO("Stage_id18", "stageN18", null, null, null, null, null, null)),
                MySQLStageTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockStageDAO.getStagesBySpeciesIds(speciesIds)).thenReturn(mockStageTORs);
        return mockStageTORs;
    }
    
    /**
     * Define a mock MySQLAnatEntityTOResultSet to mock the return of getStagesBySpeciesIds.
     * 
     * @param mockManager A {@code MySQLDAOManager} to for the class to acquire mock DAOs.
     */
    protected AnatEntityTOResultSet mockGetAnatEntities(MockDAOManager mockManager, Set<String> speciesIds) {
        MySQLAnatEntityTOResultSet mockAnatEntityTORs = createMockDAOResultSet(
                Arrays.asList(
                        new AnatEntityTO("Anat_id1", "anatName1", null, null, null, null),
                        new AnatEntityTO("Anat_id2", "anatName2", null, null, null, null),
                        new AnatEntityTO("Anat_id3", "anatName3", null, null, null, null),
                        new AnatEntityTO("Anat_id4", "anatName4", null, null, null, null),
                        new AnatEntityTO("Anat_id5", "anatName5", null, null, null, null),
                        new AnatEntityTO("NonInfoAnatEnt1", "xxx", null, null, null, null),
                        new AnatEntityTO("NonInfoAnatEnt2", "zzz", null, null, null, null),
                        new AnatEntityTO("Anat_id8", "anatName8", null, null, null, null),
                        new AnatEntityTO("Anat_id9", "anatName9", null, null, null, null),
                        new AnatEntityTO("Anat_id13", "anatName13", null, null, null, null)),
                 MySQLAnatEntityTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockAnatEntityDAO.getAnatEntitiesBySpeciesIds(speciesIds)).
            thenReturn(mockAnatEntityTORs);
        return mockAnatEntityTORs;
    }

}
