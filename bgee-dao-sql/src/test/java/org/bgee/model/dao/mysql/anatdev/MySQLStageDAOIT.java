package org.bgee.model.dao.mysql.anatdev;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLStageDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class MySQLStageDAOIT extends MySQLITAncestor {
    private final static Logger log = LogManager.getLogger(MySQLStageDAOIT.class.getName());
        
    public MySQLStageDAOIT() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the select methods {@link MySQLStageDAO#getStagesBySpeciesIds(Set)}
     * and {@link MySQLStageDAO#getStagesBySpeciesIds(Set, Boolean, Integer)} .
     */
    @Test
    public void shouldGetStagesBySpeciesIds() throws SQLException {

        this.useSelectDB();

        MySQLStageDAO dao = new MySQLStageDAO(this.getMySQLDAOManager());
        
        // Test recovery of all attributes without filter on species IDs
        List<StageTO> expectedStages = this.getAllStageTOs();
        assertTrue("StageTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(dao.getStagesBySpeciesIds(null).getAllTOs(), expectedStages));
        
        // Test recovery of one attribute without filter on species IDs
        dao.setAttributes(Arrays.asList(StageDAO.Attribute.ID, StageDAO.Attribute.NAME));
        expectedStages = Arrays.asList(
                new StageTO("Stage_id1", "stageN1", null, null, null, null, null, null), 
                new StageTO("Stage_id2", "stageN2", null, null, null, null, null, null), 
                new StageTO("Stage_id3", "stageN3", null, null, null, null, null, null), 
                new StageTO("Stage_id4", "stageN4", null, null, null, null, null, null), 
                new StageTO("Stage_id5", "stageN5", null, null, null, null, null, null), 
                new StageTO("Stage_id6", "stageN6", null, null, null, null, null, null), 
                new StageTO("Stage_id7", "stageN7", null, null, null, null, null, null), 
                new StageTO("Stage_id8", "stageN8", null, null, null, null, null, null), 
                new StageTO("Stage_id9", "stageN9", null, null, null, null, null, null), 
                new StageTO("Stage_id10", "stageN10", null, null, null, null, null, null), 
                new StageTO("Stage_id11", "stageN11", null, null, null, null, null, null), 
                new StageTO("Stage_id12", "stageN12", null, null, null, null, null, null), 
                new StageTO("Stage_id13", "stageN13", null, null, null, null, null, null), 
                new StageTO("Stage_id14", "stageN14", null, null, null, null, null, null), 
                new StageTO("Stage_id15", "stageN15", null, null, null, null, null, null), 
                new StageTO("Stage_id16", "stageN16", null, null, null, null, null, null), 
                new StageTO("Stage_id17", "stageN17", null, null, null, null, null, null), 
                new StageTO("Stage_id18", "stageN18", null, null, null, null, null, null));
        assertTrue("StageTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(dao.getStagesBySpeciesIds(null).getAllTOs(), expectedStages));

        // Test recovery of all attributes with filter on species IDs
        dao.clearAttributes();
        Set<String> speciesIds = this.getSpeciesFilter();
        expectedStages = this.getFilteredStageTOsBySpecies();
        assertTrue("StageTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getStagesBySpeciesIds(speciesIds).getAllTOs(), expectedStages));
        
        dao.clearAttributes();
        speciesIds = new HashSet<String>(Arrays.asList("11", "21", "31", "44"));
        expectedStages = this.getAllStageTOs();
        List<StageTO> retrievedStageTOs = dao.getStagesBySpeciesIds(speciesIds).getAllTOs();
        assertTrue("StageTOs incorrectly retrieved, expected " + expectedStages + 
                " - but was: " + retrievedStageTOs,
                TOComparator.areTOCollectionsEqual(retrievedStageTOs, expectedStages));
        
        // Test recovery of all attributes with filter on species IDs and grouping stage
        speciesIds = new HashSet<String>(Arrays.asList("11","44"));
        expectedStages = Arrays.asList(
                new StageTO("Stage_id1", "stageN1", "stage Desc 1", 1, 36, 1, false, true), 
                new StageTO("Stage_id8", "stageN8", "stage Desc 8", 12, 13, 4, false, true), 
                new StageTO("Stage_id15", "stageN15", "stage Desc 15", 27, 32, 3, false, true));
        retrievedStageTOs = dao.getStagesBySpeciesIds(speciesIds, true, null).getAllTOs();
        assertTrue("StageTOs incorrectly retrieved, expected " + expectedStages + 
                " - but was: " + retrievedStageTOs,
                TOComparator.areTOCollectionsEqual(expectedStages, retrievedStageTOs));
        
        // Test recovery of all attributes with filter on grouping stage only
        expectedStages = this.getAllStageTOs().stream()
                .filter(s -> s.isGroupingStage() == false)
                .collect(Collectors.toList());
        retrievedStageTOs = dao.getStagesBySpeciesIds(null, false, null).getAllTOs();
        assertTrue("StageTOs incorrectly retrieved, expected " + expectedStages + 
                " - but was: " + retrievedStageTOs,
                TOComparator.areTOCollectionsEqual(expectedStages, retrievedStageTOs));
        
        // Test recovery of all attributes with filter on species IDs, groupingStage and level
        expectedStages = Arrays.asList(
                new StageTO("Stage_id15", "stageN15", "stage Desc 15", 27, 32, 3, false, true));

        retrievedStageTOs = dao.getStagesBySpeciesIds(speciesIds, true, 3).getAllTOs();
        assertTrue("StageTOs incorrectly retrieved, expected " + expectedStages + 
                " - but was: " + retrievedStageTOs,
                TOComparator.areTOCollectionsEqual(expectedStages, retrievedStageTOs));

        // Test recovery of all attributes with filter on level only
        expectedStages = this.getAllStageTOs().stream()
                .filter(s -> s.getLevel() == 2)
                .collect(Collectors.toList());
        retrievedStageTOs = dao.getStagesBySpeciesIds(null, null, 2).getAllTOs();

        // Test recovery of all attributes with filter on grouping stage and level
        expectedStages = this.getAllStageTOs().stream()
                .filter(s -> s.isGroupingStage() == true)
                .filter(s -> s.getLevel() == 1)
                .collect(Collectors.toList());
        retrievedStageTOs = dao.getStagesBySpeciesIds(null, true, 1).getAllTOs();
    }
    /**
     * Test the select method {@link MySQLStageDAO#getStagesByIds(Set)}.
     */
    @Test
    public void shouldGetStagesByIds() throws SQLException {

        this.useSelectDB();

        MySQLStageDAO dao = new MySQLStageDAO(this.getMySQLDAOManager());
        
        // Test recovery of all attributes without filter on stage IDs
        List<StageTO> expectedStages = this.getAllStageTOs();
        assertTrue("StageTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getStagesByIds(new HashSet<String>()).getAllTOs(),
                        expectedStages));
        
        final Set<String> stageIds = this.getStageFilter();

        expectedStages = this.getAllStageTOs().stream()
                .filter(s -> stageIds.contains(s.getId()))
                .collect(Collectors.toList());
        assertTrue("StageTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(dao.getStagesByIds(stageIds).getAllTOs(),
                        expectedStages));
    }
    
    /**
     * Test the select method {@link MySQLStageDAO#getStages(Collection, Collection, Boolean, Integer)}.
     */
    @Test
    public void shouldGetStages() throws SQLException {

        this.useSelectDB();

        MySQLStageDAO dao = new MySQLStageDAO(this.getMySQLDAOManager());
        
        // Test recovery of all attributes without any filter
        List<StageTO> expectedStages = this.getAllStageTOs();
        assertTrue("StageTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getStages(null, null, null, null).getAllTOs(),
                        expectedStages));
        
        // Test recovery TO filtered by stage IDs and grouping stage
        final Set<String> stageIds = this.getStageFilter();
        expectedStages = this.getAllStageTOs().stream()
                .filter(s -> stageIds.contains(s.getId()))
                .filter(s -> !s.isGroupingStage())
                .collect(Collectors.toList());
        assertTrue("StageTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getStages(null, stageIds, false, null).getAllTOs(),
                        expectedStages));

        // Test recovery nothing with all filters
        final Set<String> speciesIds = this.getSpeciesFilter();
        assertTrue("StageTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getStages(speciesIds, stageIds, true, 4).getAllTOs(),
                        new ArrayList<>()));
        
        // Test recovery one TO (due to DISCTINCT) with all filters
        dao.setAttributes(StageDAO.Attribute.LEVEL, StageDAO.Attribute.GRANULAR, StageDAO.Attribute.GROUPING);
        speciesIds.add("21");
        dao.setAttributes(StageDAO.Attribute.LEVEL, StageDAO.Attribute.GRANULAR, StageDAO.Attribute.GROUPING);
        expectedStages = Arrays.asList(
                new StageTO(null, null, null, null, null, 4, false, false));
        assertTrue("StageTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getStages(speciesIds, stageIds, false, 4).getAllTOs(),
                        expectedStages));

    }
    
    /**
     * Test the select method {@link MySQLStageDAO#getStages(Collection, Boolean, Collection, 
     * Boolean, Integer, Collection)}.
     */
    @Test
    public void shouldGetStagesAllSpecies() throws SQLException {

        this.useSelectDB();

        MySQLStageDAO dao = new MySQLStageDAO(this.getMySQLDAOManager());
        
        Collection<StageTO> expectedStages = Arrays.asList(
                new StageTO("Stage_id2", "stageN2", null, null, null, null, null, null),
                new StageTO("Stage_id5", "stageN5", null, null, null, null, null, null),
                new StageTO("Stage_id6", "stageN6", null, null, null, null, null, null),
                new StageTO("Stage_id7", "stageN7", null, null, null, null, null, null));
        assertTrue("StageTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getStages(Arrays.asList("11", "21"), false, 
                                Arrays.asList("Stage_id2", "Stage_id5", "Stage_id6", "Stage_id7"), 
                                false, null, 
                                EnumSet.of(StageDAO.Attribute.ID, StageDAO.Attribute.NAME)).getAllTOs(),
                        expectedStages));
    }
    
    /**
     * @return The {@code List} of all {@code StageTO}s of the data test 
     *          with all {@code StageDAO.Attribute}.
     */
    private List<StageTO> getAllStageTOs() {
        return Arrays.asList(
                new StageTO("Stage_id1", "stageN1", "stage Desc 1", 1, 36, 1, false, true),
                new StageTO("Stage_id2", "stageN2", "stage Desc 2", 2, 7, 2, false, false),
                new StageTO("Stage_id3", "stageN3", "stage Desc 3", 3, 4, 3, true, false),
                new StageTO("Stage_id4", "stageN4", "stage Desc 4", 5, 6, 3, false, false),
                new StageTO("Stage_id5", "stageN5", "stage Desc 5", 8, 17, 2, false, false),
                new StageTO("Stage_id6", "stageN6", "stage Desc 6", 9, 10, 3, false, false),
                new StageTO("Stage_id7", "stageN7", "stage Desc 7", 11, 16, 3, false, false),
                new StageTO("Stage_id8", "stageN8", "stage Desc 8", 12, 13, 4, false, true),
                new StageTO("Stage_id9", "stageN9", "stage Desc 9", 14, 15, 4, false, false),
                new StageTO("Stage_id10", "stageN10", "stage Desc 10", 18, 25, 2, false, true),
                new StageTO("Stage_id11", "stageN11", "stage Desc 11", 19, 20, 3, false, true),
                new StageTO("Stage_id12", "stageN12", "stage Desc 12", 21, 22, 3, false, true),
                new StageTO("Stage_id13", "stageN13", "stage Desc 13", 23, 24, 3, false, true),
                new StageTO("Stage_id14", "stageN14", "stage Desc 14", 26, 35, 2, false, false),
                new StageTO("Stage_id15", "stageN15", "stage Desc 15", 27, 32, 3, false, true),
                new StageTO("Stage_id16", "stageN16", "stage Desc 16", 28, 29, 4, false, false),
                new StageTO("Stage_id17", "stageN17", "stage Desc 17", 30, 31, 4, false, false),
                new StageTO("Stage_id18", "stageN18", "stage Desc 18", 33, 34, 3, false, false));
    }
    
    /**
     * @return  The {@code List} of {@code StageTO}s of the data test filtered by species IDs
     *          returned by {@link #getSpeciesFilter()} with all {@code StageDAO.Attribute}s.  
     */
    private List<StageTO> getFilteredStageTOsBySpecies() {
        return Arrays.asList(
                new StageTO("Stage_id1", "stageN1", "stage Desc 1", 1, 36, 1, false, true),
                new StageTO("Stage_id2", "stageN2", "stage Desc 2", 2, 7, 2, false, false),
                new StageTO("Stage_id5", "stageN5", "stage Desc 5", 8, 17, 2, false, false),
                new StageTO("Stage_id6", "stageN6", "stage Desc 6", 9, 10, 3, false, false),
                new StageTO("Stage_id7", "stageN7", "stage Desc 7", 11, 16, 3, false, false),
                new StageTO("Stage_id8", "stageN8", "stage Desc 8", 12, 13, 4, false, true),
                new StageTO("Stage_id14", "stageN14", "stage Desc 14", 26, 35, 2, false, false),
                new StageTO("Stage_id15", "stageN15", "stage Desc 15", 27, 32, 3, false, true),
                new StageTO("Stage_id16", "stageN16", "stage Desc 16", 28, 29, 4, false, false),
                new StageTO("Stage_id18", "stageN18", "stage Desc 18", 33, 34, 3, false, false));
    }
    
    /**
     * @return
     */
    private Set<String> getStageFilter() {
        final Set<String> stageIds = new HashSet<String>();
        stageIds.add("Stage_id1");
        stageIds.add("Stage_id9");
        stageIds.add("Stage_id16");
        stageIds.add("Fake");
        return stageIds;
    }
    
    /**
     * @return  The {@code Set} of {@code String}s that are species IDs used as filter.
     * @see #getFilteredStageTOsBySpecies()
     */
    private Set<String> getSpeciesFilter() {
        Set<String> speciesIds = new HashSet<String>(Arrays.asList("11","44"));
        return speciesIds;
    }
    
    /**
     * Test the insertion method {@link MySQLStageDAO#insertStages(Collection)}.
     */
    @Test
    public void shouldInsertStages() throws SQLException {
        
        this.useEmptyDB();
        
        //create a Collection of TaxonTOs to be inserted
        Collection<StageTO> stageTOs = new ArrayList<StageTO>();
        stageTOs.add(new StageTO("stId1", "name 1", "desc 1", 1, 6, 1, false, true));
        stageTOs.add(new StageTO("stId2", "name 2", "desc 2", 2, 3, 2, true, false));
        stageTOs.add(new StageTO("stId3", "name 3", "desc 3", 4, 5, 2, false, false));
        try {
            MySQLStageDAO dao = new MySQLStageDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertStages(stageTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            //This test method could be better written (DRY, ...)
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from stage where stageId = ? and " +
                            "stageName = ? and stageDescription = ? and " +
                            "stageLeftBound = ? and stageRightBound = ? and stageLevel = ? " +
                            "and tooGranular = ? and groupingStage = ?")) {
                
                stmt.setString(1, "stId1");
                stmt.setString(2, "name 1");
                stmt.setString(3, "desc 1");
                stmt.setInt(4, 1);
                stmt.setInt(5, 6);
                stmt.setInt(6, 1);
                stmt.setBoolean(7, false);
                stmt.setBoolean(8, true);
                assertTrue("StageTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "stId2");
                stmt.setString(2, "name 2");
                stmt.setString(3, "desc 2");
                stmt.setInt(4, 2);
                stmt.setInt(5, 3);
                stmt.setInt(6, 2);
                stmt.setBoolean(7, true);
                stmt.setBoolean(8, false);
                assertTrue("StageTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "stId3");
                stmt.setString(2, "name 3");
                stmt.setString(3, "desc 3");
                stmt.setInt(4, 4);
                stmt.setInt(5, 5);
                stmt.setInt(6, 2);
                stmt.setBoolean(7, false);
                stmt.setBoolean(8, false);
                assertTrue("StageTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            try {
                dao.insertStages(new HashSet<StageTO>());
                fail("An IllegalArgumentException should be thrown");
            } catch (IllegalArgumentException e) {
                // Test passed
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
}
