package org.bgee.model.dao.mysql.species;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLSpeciesDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class MySQLSpeciesDAOIT extends MySQLITAncestor {
    private final static Logger log = LogManager.getLogger(MySQLSpeciesDAOIT.class.getName());
    
    /**
     * A {@code String} that is the name of the table into which data are inserted 
     * during testing of {@link MySQLSpeciesDAO} methods inserting data.
     */
    private final static String INSERTTABLENAME = "species";
    
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
        speciesTOs.add(new SpeciesTO("10", "commonName1", "genus1", "speciesName1", 
                "100", "path/1", null, ""));
        speciesTOs.add(new SpeciesTO("20", "commonName2", "genus2", "speciesName2", 
                "120", "path/2", "200", "YEAH"));
        speciesTOs.add(new SpeciesTO("30", "commonName3", "genus3", "speciesName3", 
                "500", "path/3", "200", ""));
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
                            "taxonId = ? and genomeFilePath = ? and genomeSpeciesId = ? " +
                            "and fakeGeneIdPrefix = ?")) {
                
                stmt.setInt(1, 10);
                stmt.setString(2, "commonName1");
                stmt.setString(3, "genus1");
                stmt.setString(4, "speciesName1");
                stmt.setInt(5, 100);
                stmt.setString(6, "path/1");
                stmt.setInt(7, 0);
                stmt.setString(8, "");
                assertTrue("SpeciesTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 20);
                stmt.setString(2, "commonName2");
                stmt.setString(3, "genus2");
                stmt.setString(4, "speciesName2");
                stmt.setInt(5, 120);
                stmt.setString(6, "path/2");
                stmt.setInt(7, 200);
                stmt.setString(8, "YEAH");
                assertTrue("SpeciesTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 30);
                stmt.setString(2, "commonName3");
                stmt.setString(3, "genus3");
                stmt.setString(4, "speciesName3");
                stmt.setInt(5, 500);
                stmt.setString(6, "path/3");
                stmt.setInt(7, 200);
                stmt.setString(8, "");
                assertTrue("SpeciesTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
        } finally {
            this.deleteFromTableAndUseDefaultDB(INSERTTABLENAME);
        }
    }
    
    /**
     * Test the select method {@link MySQLSpeciesDAO#getAllSpecies()}.
     */
    @Test
    public void shouldGetAllSpecies() throws SQLException {
        log.entry();
        this.getMySQLDAOManager().setDatabaseToUse(System.getProperty(POPULATEDDBKEYKEY));
        // TODO Populate database if empty in a @BeforeClass
        // in MySQLITAncestor instead here
        try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                prepareStatement("select 1 from dataSource")) {
            if (!stmt.getRealPreparedStatement().executeQuery().next()) {
                this.populateAndUseDatabase(System.getProperty(POPULATEDDBKEYKEY));
            }
        }

        // Generate result with the method
        MySQLSpeciesDAO dao = new MySQLSpeciesDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(SpeciesDAO.Attribute.values()));
        SpeciesTOResultSet methResults = dao.getAllSpecies();

        // Generate manually expected result
        List<SpeciesTO> expectedSpecies = Arrays.asList(
                new SpeciesTO("11", "gen11", "sp11", "spCName11", "111", "path/genome11",
                        "0", ""), 
                new SpeciesTO("21", "gen21", "sp21", "spCName21", "211", "path/genome21", 
                        "52", "FAKEPREFIX"), 
                new SpeciesTO("31", "gen31", "sp31", "spCName31", "311", "path/genome31", 
                        "0", "")); 
        while (methResults.next()) {
            boolean found = false;
            SpeciesTO methSpecies = methResults.getTO();
            for (SpeciesTO expSpecies: expectedSpecies) {
                log.trace("Comparing {} to {}", methSpecies, expSpecies);
                if (areSpeciesTOsEqual(methSpecies, expSpecies)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.debug("No equivalent Species found for {}", methSpecies);
                throw log.throwing(new AssertionError("Incorrect generated TO"));
            }
        }
        methResults.close();
        log.exit();
    }
    
    /**
     * Method to compare two {@code SpeciesTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code SpeciesTO}s is 
     * solely based on their ID, not on other attributes.
     * 
     * @param spTO1     A {@code SpeciesTO} to be compared to {@code spTO2}.
     * @param spTO2     A {@code SpeciesTO} to be compared to {@code spTO1}.
     * @return          {@code true} if {@code spTO1} and {@code spTO2} have all 
     *                  attributes equal.
     *                  
                String fakeGeneIdPrefix
     */
    private boolean areSpeciesTOsEqual(SpeciesTO spTO1, SpeciesTO spTO2) {
            log.entry(spTO1, spTO2);
            if (spTO1.getId().equals(spTO2.getId()) && 
                (spTO1.getName() == null && spTO2.getName() == null || 
                    spTO1.getName() != null && spTO1.getName().equals(spTO2.getName())) && 
                (spTO1.getGenus() == null && spTO2.getGenus() == null || 
                    spTO1.getGenus() != null && spTO1.getGenus().equals(spTO2.getGenus())) && 
                (spTO1.getSpeciesName() == null && spTO2.getSpeciesName() == null || 
                    spTO1.getSpeciesName() != null && spTO1.getSpeciesName().equals(spTO2.getSpeciesName())) && 
                (spTO1.getParentTaxonId() == null && spTO2.getParentTaxonId() == null || 
                    spTO1.getParentTaxonId() != null && spTO1.getParentTaxonId().equals(spTO2.getParentTaxonId())) && 
                (spTO1.getGenomeFilePath() == null && spTO2.getGenomeFilePath() == null || 
                    spTO1.getGenomeFilePath() != null && spTO1.getGenomeFilePath().equals(spTO2.getGenomeFilePath())) && 
                (spTO1.getGenomeSpeciesId() == null && spTO2.getGenomeSpeciesId() == null || 
                    spTO1.getGenomeSpeciesId() != null && spTO1.getGenomeSpeciesId().equals(spTO2.getGenomeSpeciesId())) &&
                (spTO1.getFakeGeneIdPrefix()== null && spTO2.getFakeGeneIdPrefix() == null || 
                        spTO1.getFakeGeneIdPrefix() != null && spTO1.getFakeGeneIdPrefix().equals(spTO2.getFakeGeneIdPrefix()))) {
                return log.exit(true);
            }
            log.debug("Species are not equivalent {}", spTO1.getId());
            return log.exit(false);
    }

}
