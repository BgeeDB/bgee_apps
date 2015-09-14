package org.bgee.model.dao.mysql.anatdev;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Integration tests for {@link MySQLAnatEntityDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor MySQLITAncestor} for 
 * important information.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.anatdev.AnatEntityDAO
 * @since Bgee 13
 */
public class MySQLAnatEntityDAOIT extends MySQLITAncestor {
    
    private final static Logger log = LogManager.getLogger(MySQLAnatEntityDAOIT.class.getName());

    public MySQLAnatEntityDAOIT() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test the select method {@link MySQLAnatEntityDAO#getAnatEntities()}.
     */
    @Test
    public void shouldGetAnatEntities() throws SQLException {

        this.useSelectDB();

        MySQLAnatEntityDAO dao = new MySQLAnatEntityDAO(this.getMySQLDAOManager());
        List<AnatEntityTO> allAnatEntityTOs = Arrays.asList(
        new AnatEntityTO("Anat_id1",  "anatStruct",  "anatStruct desc",  "Stage_id1",  "Stage_id2", true), 
        new AnatEntityTO("Anat_id10", "hindbrain", "hindbrain desc", "Stage_id8", "Stage_id17", true), 
        new AnatEntityTO("Anat_id11", "cerebellum", "cerebellum desc", "Stage_id9", "Stage_id13", false), 
        new AnatEntityTO("Anat_id12", "anat12", "unused anatE 12", "Stage_id1", "Stage_id13", false), 
        new AnatEntityTO("Anat_id13", "anat13", "unused anatE 13", "Stage_id9", "Stage_id10", true), 
        new AnatEntityTO("Anat_id14", "anat14", "anatE 14", "Stage_id6", "Stage_id13", true), 
        new AnatEntityTO("Anat_id2", "organ", "organ desc", "Stage_id10", "Stage_id18", false), 
        new AnatEntityTO("Anat_id3", "heart", "heart desc", "Stage_id16", "Stage_id18", false), 
        new AnatEntityTO("Anat_id4", "gill", "gill desc", "Stage_id12", "Stage_id18", false), 
        new AnatEntityTO("Anat_id5", "brain", "brain desc", "Stage_id11", "Stage_id17", false), 
        new AnatEntityTO("Anat_id6", "embryoStruct", "embryoStruct desc", "Stage_id2", "Stage_id5", false), 
        new AnatEntityTO("Anat_id7", "ectoderm", "ectoderm desc", "Stage_id6", "Stage_id13", false), 
        new AnatEntityTO("Anat_id8", "neuralTube", "neuralTube desc", "Stage_id8", "Stage_id17", false), 
        new AnatEntityTO("Anat_id9", "forebrain", "forebrain desc", "Stage_id8", "Stage_id17", false),
        new AnatEntityTO("UBERON:0001687", "stapes bone", "stapes bone description", "Stage_id1", "Stage_id2", false),
        new AnatEntityTO("UBERON:0001853", "utricle of membranous labyrinth", "utricle of membranous labyrinth description", "Stage_id1", "Stage_id2", false),
        new AnatEntityTO("UBERON:0011606", "hyomandibular bone", "hyomandibular bone description", "Stage_id1", "Stage_id2", false));

        // Test recovery of all attributes without filter on species IDs
        List<AnatEntityTO> expectedAnatEntities = allAnatEntityTOs;
        assertTrue("AnatEntityTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getAnatEntitiesBySpeciesIds(null).getAllTOs(), expectedAnatEntities));

        // Test recovery of one attribute without filter on species IDs
        dao.setAttributes(Arrays.asList(AnatEntityDAO.Attribute.ID));
        expectedAnatEntities = Arrays.asList(
                new AnatEntityTO("Anat_id1", null, null, null, null, null),
                new AnatEntityTO("Anat_id10", null, null, null, null, null),
                new AnatEntityTO("Anat_id11", null, null, null, null, null),
                new AnatEntityTO("Anat_id12", null, null, null, null, null),
                new AnatEntityTO("Anat_id13", null, null, null, null, null),
                new AnatEntityTO("Anat_id14", null, null, null, null, null),
                new AnatEntityTO("Anat_id2", null, null, null, null, null),
                new AnatEntityTO("Anat_id3", null, null, null, null, null),
                new AnatEntityTO("Anat_id4", null, null, null, null, null),
                new AnatEntityTO("Anat_id5", null, null, null, null, null),
                new AnatEntityTO("Anat_id6", null, null, null, null, null),
                new AnatEntityTO("Anat_id7", null, null, null, null, null),
                new AnatEntityTO("Anat_id8", null, null, null, null, null),
                new AnatEntityTO("Anat_id9", null, null, null, null, null),
                new AnatEntityTO("UBERON:0001687", null, null, null, null, null),
                new AnatEntityTO("UBERON:0001853", null, null, null, null, null),
                new AnatEntityTO("UBERON:0011606", null, null, null, null, null));
        assertTrue("AnatEntityTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getAnatEntitiesBySpeciesIds(null).getAllTOs(), expectedAnatEntities));

        // Test recovery of several attributes with filter on species IDs
        dao.clearAttributes();
        dao.setAttributes(Arrays.asList(AnatEntityDAO.Attribute.ID,
                AnatEntityDAO.Attribute.START_STAGE_ID, AnatEntityDAO.Attribute.END_STAGE_ID,
                AnatEntityDAO.Attribute.NON_INFORMATIVE));
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11","44"));
        expectedAnatEntities = Arrays.asList(
                new AnatEntityTO("Anat_id1", null, null, "Stage_id1", "Stage_id2", true),
                new AnatEntityTO("Anat_id11", null, null, "Stage_id9", "Stage_id13", false),
                new AnatEntityTO("Anat_id13", null, null, "Stage_id9", "Stage_id10", true),
                new AnatEntityTO("Anat_id14", null, null, "Stage_id6", "Stage_id13", true),
                new AnatEntityTO("Anat_id2", null, null, "Stage_id10", "Stage_id18", false),
                new AnatEntityTO("Anat_id6", null, null, "Stage_id2", "Stage_id5", false),
                new AnatEntityTO("Anat_id8", null, null, "Stage_id8", "Stage_id17", false),
                new AnatEntityTO("UBERON:0001687", null, null, "Stage_id1", "Stage_id2", false),
                new AnatEntityTO("UBERON:0001853", null, null, "Stage_id1", "Stage_id2", false),
                new AnatEntityTO("UBERON:0011606", null, null, "Stage_id1", "Stage_id2", false));
        assertTrue("AnatEntityTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getAnatEntitiesBySpeciesIds(speciesIds).getAllTOs(), expectedAnatEntities));

        dao.clearAttributes();
        speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11", "21", "31", "44"));
        expectedAnatEntities = allAnatEntityTOs;
        assertTrue("AnatEntityTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getAnatEntitiesBySpeciesIds(speciesIds).getAllTOs(), expectedAnatEntities));
    }

    /**
     * Test the select method {@link MySQLAnatEntityDAO#getNonInformativeAnatEntities()}.
     */
    @Test
    public void shouldGetNonInformativeAnatEntities() throws SQLException {
        
        this.useSelectDB();

        MySQLAnatEntityDAO dao = new MySQLAnatEntityDAO(this.getMySQLDAOManager());
        // Test recovery of all attributes without filter on species IDs
        List<AnatEntityTO> expectedAnatEntities = Arrays.asList(
                new AnatEntityTO("Anat_id10", "hindbrain", "hindbrain desc", 
                        "Stage_id8", "Stage_id17", true),
                new AnatEntityTO("Anat_id13", "anat13", "unused anatE 13", 
                        "Stage_id9", "Stage_id10", true));
        assertTrue("AnatEntityTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getNonInformativeAnatEntitiesBySpeciesIds(null).getAllTOs(),
                        expectedAnatEntities));

        // Test recovery of all attributes with filter on species IDs
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11","44"));
        expectedAnatEntities = Arrays.asList(
                new AnatEntityTO("Anat_id13", "anat13", "unused anatE 13", 
                        "Stage_id9", "Stage_id10", true));
        assertTrue("AnatEntityTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getNonInformativeAnatEntitiesBySpeciesIds(speciesIds).getAllTOs(),
                        expectedAnatEntities));
        
        // Test recovery of anatomical entity names with filter on species IDs
        dao.setAttributes(AnatEntityDAO.Attribute.NAME);
        speciesIds.clear();
        speciesIds.addAll(Arrays.asList("31"));
        expectedAnatEntities = Arrays.asList(
                new AnatEntityTO(null, "hindbrain", null, null, null, null), 
                new AnatEntityTO(null, "anat13", null, null, null, null));
        List<AnatEntityTO> retrievedAnatEntities = 
                dao.getNonInformativeAnatEntitiesBySpeciesIds(speciesIds).getAllTOs();
        assertTrue("AnatEntityTOs incorrectly retrieved, expected: " + expectedAnatEntities + 
                ", but was: " + retrievedAnatEntities,
                TOComparator.areTOCollectionsEqual(retrievedAnatEntities, expectedAnatEntities));
    }

    /**
     * Test the insert method {@link MySQLAnatEntityDAO#insertAnatEntities()}.
     */
    @Test
    public void shouldInsertAnatEntities() throws SQLException {
        
        this.useEmptyDB();
        
        //create a Collection of AnatEntityTO to be inserted
        Collection<AnatEntityTO> anatEntityTOs = Arrays.asList(
                new AnatEntityTO("Anat_id10","hindbrain","hindbrain desc","Stage_id8","Stage_id17", true),
                new AnatEntityTO("Anat_id5","brain","brain desc","Stage_id11","Stage_id17", false),
                new AnatEntityTO("Anat_id1","anatStruct",null,"Stage_id1","Stage_id2", false));

        try {
            MySQLAnatEntityDAO dao = new MySQLAnatEntityDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertAnatEntities(anatEntityTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from anatEntity where " +
                      "anatEntityId = ? AND anatEntityName = ? AND anatEntityDescription = ? " +
                            "AND startStageId = ? AND endStageId = ? AND nonInformative = ?")) {
                
                stmt.setString(1, "Anat_id10");
                stmt.setString(2, "hindbrain");
                stmt.setString(3, "hindbrain desc");
                stmt.setString(4, "Stage_id8");
                stmt.setString(5, "Stage_id17");
                stmt.setBoolean(6, true);
                assertTrue("AnatEntityTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "Anat_id5");
                stmt.setString(2, "brain");
                stmt.setString(3, "brain desc");
                stmt.setString(4, "Stage_id11");
                stmt.setString(5, "Stage_id17");
                stmt.setBoolean(6, false);
                assertTrue("AnatEntityTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from anatEntity where " +
                      "anatEntityId = ? AND anatEntityName = ? AND anatEntityDescription is null " +
                            "AND startStageId = ? AND endStageId = ? AND nonInformative = ?")) {

                stmt.setString(1, "Anat_id1");
                stmt.setString(2, "anatStruct");
                stmt.setString(3, "Stage_id1");
                stmt.setString(4, "Stage_id2");
                stmt.setBoolean(5, false);
                assertTrue("AnatEntityTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            this.thrown.expect(IllegalArgumentException.class);
            dao.insertAnatEntities(new HashSet<AnatEntityTO>());
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }   
}
