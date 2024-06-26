package org.bgee.model.dao.mysql.species;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLSpeciesDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14 Mar 2019
 * @since Bgee 13
 */
//TODO: adapt these tests to fix for issue #173
public class MySQLSpeciesDAOIT extends MySQLITAncestor {
    private final static Logger log = LogManager.getLogger(MySQLSpeciesDAOIT.class.getName());
    
    public MySQLSpeciesDAOIT() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the insertion method {@link MySQLSpeciesDAO#insertSpecies(Collection)}.
     */
    @Test
    public void shouldInsertSpecies() throws SQLException {
        
        this.useEmptyDB();
        
        //create a Collection of SpeciesTOs to be inserted
        Collection<SpeciesTO> speciesTOs = new ArrayList<SpeciesTO>();
        speciesTOs.add(new SpeciesTO(10, "commonName1", "genus1", "speciesName1", 2,
                100, "path/1", "1", "assemblyXRef", 1, null));
        speciesTOs.add(new SpeciesTO(20, "commonName2", "genus2", "speciesName2", 1,
                120, "path/2", "2", "assemblyXRef", 1, 200));
        speciesTOs.add(new SpeciesTO(30, "commonName3", "genus3", "speciesName3", 3,
                500, "path/3", "3", "assemblyXRef", 1, 200));
        try {
            MySQLSpeciesDAO dao = new MySQLSpeciesDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertSpecies(speciesTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            //This test method could be better written (DRY, ...)
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from species where speciesId = ? and " +
                            "speciesCommonName = ? and genus = ? and species = ? and " +
                            "speciesDisplayOrder = ? and taxonId = ? and genomeFilePath = ? and " + 
                            "genomeVersion = ? and dataSourceId = ? and genomeSpeciesId = ? " +
                            "and fakeGeneIdPrefix = ?")) {
                
                stmt.setInt(1, 10);
                stmt.setString(2, "commonName1");
                stmt.setString(3, "genus1");
                stmt.setString(4, "speciesName1");
                stmt.setInt(5, 2);
                stmt.setInt(6, 100);
                stmt.setString(7, "path/1");
                stmt.setString(8, "1");
                stmt.setInt(9, 1);
                stmt.setInt(10, 0);
                stmt.setString(11, "");
                assertTrue("SpeciesTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 20);
                stmt.setString(2, "commonName2");
                stmt.setString(3, "genus2");
                stmt.setString(4, "speciesName2");
                stmt.setInt(5, 1);
                stmt.setInt(6, 120);
                stmt.setString(7, "path/2");
                stmt.setString(8, "2");
                stmt.setInt(9, 1);
                stmt.setInt(10, 200);
                stmt.setString(11, "YEAH");
                assertTrue("SpeciesTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 30);
                stmt.setString(2, "commonName3");
                stmt.setString(3, "genus3");
                stmt.setString(4, "speciesName3");
                stmt.setInt(5, 3);
                stmt.setInt(6, 500);
                stmt.setString(7, "path/3");
                stmt.setString(8, "3");
                stmt.setInt(9, 1);
                stmt.setInt(10, 200);
                stmt.setString(11, "");
                assertTrue("SpeciesTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
    
    /**
     * Test the select method {@link MySQLSpeciesDAO#getAllSpecies()}.
     */
    @Test
    public void shouldGetAllSpecies() throws SQLException {

        this.useSelectDB();

        // Generate result with the method
        MySQLSpeciesDAO dao = new MySQLSpeciesDAO(this.getMySQLDAOManager());
        List<SpeciesTO> methSpecies = dao.getAllSpecies(null).getAllTOs();
        
        // Generate manually expected result
        List<SpeciesTO> expectedSpecies = Arrays.asList(
                new SpeciesTO(31, "spCName31", "gen31", "sp31", 1, 311, 
                        "gen31_sp31/gen31_sp31.genome31", "genome31", "assemblyXRef31",
                        1, 0),
                new SpeciesTO(41, "spCName41", "gen41", "sp41", 2, 411, 
                        "gen41_sp41/gen41_sp41.genome41", "genome41", "assemblyXRef41",
                        1, 0),
                new SpeciesTO(21, "spCName21", "gen21", "sp21", 3, 211, 
                        "gen51_sp51/gen51_sp51.genome51", "genome51", "assemblyXRef51",
                        1, 51),
                new SpeciesTO(11, "spCName11", "gen11", "sp11", 4, 111, 
                        "gen11_sp11/gen11_sp11.genome11", "genome11", "assemblyXRef11",
                        1, 0),
                new SpeciesTO(42, "spCName42", "gen41", "sp42", 5, 411, 
                        "gen41_sp41/gen41_sp41.genome41", "genome41", "assemblyXRef41",
                        1, 41),
                new SpeciesTO(51, "spCName51", "gen51", "sp51", 6, 511, 
                        "gen51_sp51/gen51_sp51.genome51", "genome51", "assemblyXRef51",
                        1, 0));
        // Compare
        assertTrue("SpeciesTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methSpecies, expectedSpecies));
        //verify species display order
        assertEquals("Incorrect species order", expectedSpecies, methSpecies);

        methSpecies = dao.getAllSpecies(EnumSet.of(SpeciesDAO.Attribute.COMMON_NAME)).getAllTOs();
        
        // Generate manually expected result
        expectedSpecies = Arrays.asList(
                new SpeciesTO(null, "spCName11", null, null, null, null, null, null, null, null, null),
                new SpeciesTO(null, "spCName21",null, null,  null, null, null, null, null, null, null),
                new SpeciesTO(null, "spCName31", null, null, null, null, null, null, null, null, null),
                new SpeciesTO(null, "spCName41", null, null, null, null, null, null, null, null, null),
                new SpeciesTO(null, "spCName42", null, null, null, null, null, null, null, null, null),
                new SpeciesTO(null, "spCName51", null, null, null, null, null, null, null, null, null));
        // Compare
        assertTrue("SpeciesTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(methSpecies, expectedSpecies));
    }
    
    /**
     * Test the select method {@link MySQLSpeciesDAO#getSpeciesByIds(Set)}.
     */
    @Test
    public void shouldGetSpeciesByIds() throws SQLException {

        this.useSelectDB();

        // Generate result with the method
        MySQLSpeciesDAO dao = new MySQLSpeciesDAO(this.getMySQLDAOManager());
        List<SpeciesTO> speciesTOs = dao.getSpeciesByIds(new HashSet<>(Arrays.asList(11, 31)), null)
                .getAllTOs();
        
        // expected result
        List<SpeciesTO> expectedSpeciesTOs = Arrays.asList(
                new SpeciesTO(11, "spCName11", "gen11", "sp11", 4, 111,
                        "gen11_sp11/gen11_sp11.genome11", "genome11", "assemblyXRef11", 1, 0),
                new SpeciesTO(31, "spCName31", "gen31", "sp31", 1, 311,
                        "gen31_sp31/gen31_sp31.genome31", "genome31", "assemblyXRef31", 1, 0));
        // Compare
        assertTrue("SpeciesTOs incorrectly retrieved, expected: " + expectedSpeciesTOs + 
                ", but was: " + speciesTOs, 
                TOComparator.areTOCollectionsEqual(speciesTOs, expectedSpeciesTOs));

        speciesTOs = dao.getSpeciesByIds(new HashSet<>(Arrays.asList(11, 31)),
                EnumSet.of(SpeciesDAO.Attribute.ID, SpeciesDAO.Attribute.GENUS,
                        SpeciesDAO.Attribute.SPECIES_NAME)).getAllTOs();
        
        // Generate manually expected result
        expectedSpeciesTOs = Arrays.asList(
                new SpeciesTO(11, null, "gen11", "sp11", null, null, null, null, null, null, null),
                new SpeciesTO(31, null, "gen31", "sp31", null, null, null, null, null, null, null));
        // Compare
        assertTrue("SpeciesTOs incorrectly retrieved, expected: " + expectedSpeciesTOs + 
                ", but was: " + speciesTOs, 
                TOComparator.areTOCollectionsEqual(speciesTOs, expectedSpeciesTOs));

        speciesTOs = dao.getSpeciesByIds(new HashSet<>(Arrays.asList(11, 31)),
                EnumSet.of(SpeciesDAO.Attribute.ID, SpeciesDAO.Attribute.GENUS,
                        SpeciesDAO.Attribute.SPECIES_NAME, SpeciesDAO.Attribute.DISPLAY_ORDER))
                .getAllTOs();
        
        // Generate manually expected result
        expectedSpeciesTOs = Arrays.asList(
                new SpeciesTO(11, null, "gen11", "sp11", 4, null, null, null, null, null, null),
                new SpeciesTO(31, null, "gen31", "sp31", 1, null, null, null, null, null, null));
        // Compare
        assertTrue("SpeciesTOs incorrectly retrieved, expected: " + expectedSpeciesTOs + 
                ", but was: " + speciesTOs, 
                TOComparator.areTOCollectionsEqual(speciesTOs, expectedSpeciesTOs));
    }

    @Test
    public void testGetSpeciesInDataGroup() throws SQLException {
        this.useSelectDB();

        // Generate result with the method
        MySQLSpeciesDAO dao = new MySQLSpeciesDAO(this.getMySQLDAOManager());
        List<SpeciesTO> methSpecies = dao.getSpeciesFromDataGroups(null).getAllTOs();

        // Generate manually expected result
        List<SpeciesTO> expectedSpecies = Arrays.asList(
                new SpeciesTO(41, "spCName41", "gen41", "sp41", 2, 411, 
                        "gen41_sp41/gen41_sp41.genome41", "genome41", "assemblyXRef41", 1, 0),
                new SpeciesTO(21, "spCName21", "gen21", "sp21", 3, 211, 
                        "gen51_sp51/gen51_sp51.genome51", "genome51", "assemblyXRef51", 1, 51),
                new SpeciesTO(51, "spCName51", "gen51", "sp51", 6, 511, 
                        "gen51_sp51/gen51_sp51.genome51", "genome51", "assemblyXRef51", 1, 0));
        // Compare
        assertTrue("SpeciesTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(methSpecies, expectedSpecies));

        methSpecies = dao.getSpeciesFromDataGroups(EnumSet.of(SpeciesDAO.Attribute.COMMON_NAME))
                .getAllTOs();

        // Generate manually expected result
        expectedSpecies = Arrays.asList(
                new SpeciesTO(null, "spCName41", null, null, null, null, null, null, null, null, null),
                new SpeciesTO(null, "spCName21",null, null,  null, null, null, null, null, null, null),
                new SpeciesTO(null, "spCName51", null, null, null, null, null, null, null, null, null));
        // Compare
        assertTrue("SpeciesTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(methSpecies, expectedSpecies));

    }
}