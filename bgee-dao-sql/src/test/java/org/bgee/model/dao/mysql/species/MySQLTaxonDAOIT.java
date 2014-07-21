package org.bgee.model.dao.mysql.species;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.species.TaxonDAO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTOResultSet;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLTaxonDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class MySQLTaxonDAOIT extends MySQLITAncestor {
    private final static Logger log = LogManager.getLogger(MySQLTaxonDAOIT.class.getName());
    
    /**
     * A {@code String} that is the name of the table into which data are inserted 
     * during testing of {@link MySQLTaxonDAO} methods inserting data.
     */
    private final static String INSERTTABLENAME = "taxon";
    
    public MySQLTaxonDAOIT() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the insertion method {@link MySQLTaxonDAO#insertTaxa(Collection)}.
     */
    @Test
    public void shouldInsertTaxa() throws SQLException {
        this.useEmptyDB();
        //create a Collection of TaxonTOs to be inserted
        Collection<TaxonTO> taxonTOs = new ArrayList<TaxonTO>();
        taxonTOs.add(new TaxonTO("10", "commonName1", "sciName1", 1, 10, 1, true));
        taxonTOs.add(new TaxonTO("50", "commonName2", "sciName2", 2, 5, 2, false));
        taxonTOs.add(new TaxonTO("60", "commonName3", "sciName3", 6, 9, 3, true));
        try {
            MySQLTaxonDAO dao = new MySQLTaxonDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertTaxa(taxonTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            //This test method could be better written (DRY, ...)
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from taxon where taxonId = ? and " +
                            "taxonCommonName = ? and taxonScientificName = ? and " +
                            "taxonLeftBound = ? and taxonRightBound = ? and taxonLevel = ? " +
                            "and bgeeSpeciesLCA = ?")) {
                
                stmt.setInt(1, 10);
                stmt.setString(2, "commonName1");
                stmt.setString(3, "sciName1");
                stmt.setInt(4, 1);
                stmt.setInt(5, 10);
                stmt.setInt(6, 1);
                stmt.setBoolean(7, true);
                assertTrue("TaxonTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 50);
                stmt.setString(2, "commonName2");
                stmt.setString(3, "sciName2");
                stmt.setInt(4, 2);
                stmt.setInt(5, 5);
                stmt.setInt(6, 2);
                stmt.setBoolean(7, false);
                assertTrue("TaxonTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 60);
                stmt.setString(2, "commonName3");
                stmt.setString(3, "sciName3");
                stmt.setInt(4, 6);
                stmt.setInt(5, 9);
                stmt.setInt(6, 3);
                stmt.setBoolean(7, true);
                assertTrue("TaxonTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
        } finally {
            this.deleteFromTableAndUseDefaultDB(INSERTTABLENAME);
        }
    }
    
    /**
     * Test the select method {@link MySQLTaxonDAO#shouldGetAllTaxa()}.
     */
    @Test
    public void shouldGetAllTaxa() throws SQLException {
        log.entry();
        this.getMySQLDAOManager().setDatabaseToUse(System.getProperty(POPULATEDDBKEYKEY));
        // TODO Populate database if empty in a @BeforeClass in MySQLITAncestor instead here
        try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                prepareStatement("select 1 from dataSource")) {
            if (!stmt.getRealPreparedStatement().executeQuery().next()) {
                this.populateAndUseDatabase(System.getProperty(POPULATEDDBKEYKEY));
            }
        }

        // Generate result with the method
        MySQLTaxonDAO dao = new MySQLTaxonDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(TaxonDAO.Attribute.values()));
        TaxonTOResultSet methResults = dao.getAllTaxa();

        // Generate manually expected result
        List<TaxonTO> expectedTaxa = Arrays.asList(
                new TaxonTO("111", "taxCName111", "taxSName111", 1, 10, 1, true), 
                new TaxonTO("211", "taxCName211", "taxSName211", 2, 3, 2, false), 
                new TaxonTO("311", "taxCName311", "taxSName311", 4, 9, 2, false), 
                new TaxonTO("411", "taxCName411", "taxSName411", 5, 6, 1, true), 
                new TaxonTO("511", "taxCName511", "taxSName511", 7, 8, 1, true)); 

        while (methResults.next()) {
            boolean found = false;
            TaxonTO methTaxon = methResults.getTO();
            for (TaxonTO expTaxon: expectedTaxa) {
                log.trace("Comparing {} to {}", methTaxon, expTaxon);
                if (areTaxonTOsEqual(methTaxon, expTaxon)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.debug("No equivalent taxon found for {}", methTaxon);
                throw log.throwing(new AssertionError("Incorrect generated TO"));
            }
        }
        methResults.close();

        dao.setAttributes(Arrays.asList(TaxonDAO.Attribute.ID));
        methResults = dao.getAllTaxa();

        // Generate manually expected result
        expectedTaxa = Arrays.asList(
                new TaxonTO("111", null, null, 0, 0, 0, false), 
                new TaxonTO("211", null, null, 0, 0, 0, false), 
                new TaxonTO("311", null, null, 0, 0, 0, false), 
                new TaxonTO("411", null, null, 0, 0, 0, false), 
                new TaxonTO("511", null, null, 0, 0, 0, false)); 

        while (methResults.next()) {
            boolean found = false;
            TaxonTO methTaxon = methResults.getTO();
            for (TaxonTO expTaxon: expectedTaxa) {
                log.trace("Comparing {} to {}", methTaxon, expTaxon);
                if (areTaxonTOsEqual(methTaxon, expTaxon)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.debug("No equivalent taxon found for {}", methTaxon);
                throw log.throwing(new AssertionError("Incorrect generated TO"));
            }
        }
        methResults.close();

        log.exit();
    }

    /**
     * Method to compare two {@code TaxonTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code TaxonTO}s is solely
     * based on their ID, not on other attributes.
     * 
     * @param taxonTO1 A {@code TaxonTO} to be compared to {@code taxonTO2}.
     * @param taxonTO2 A {@code TaxonTO} to be compared to {@code taxonTO1}.
     * @return {@code true} if {@code taxonTO1} and {@code taxonTO2} have all attributes
     *         equal.
     */
    private boolean areTaxonTOsEqual(TaxonTO taxonTO1, TaxonTO taxonTO2) {
        log.entry(taxonTO1, taxonTO2);
        if (taxonTO1.getId().equals(taxonTO2.getId()) && 
            (taxonTO1.getName() == null && taxonTO2.getName() == null || 
              taxonTO1.getName() != null && taxonTO1.getName().equals(taxonTO2.getName())) && 
            (taxonTO1.getScientificName() == null && taxonTO2.getScientificName() == null || 
              taxonTO1.getScientificName() != null && taxonTO1.getScientificName().equals(taxonTO2.getScientificName())) && 
            taxonTO1.getLeftBound() == taxonTO2.getLeftBound() && 
            taxonTO1.getRightBound() == taxonTO2.getRightBound() && 
            taxonTO1.getLevel() == taxonTO2.getLevel() && 
            taxonTO1.isLca() == taxonTO2.isLca()) {
            return log.exit(true);
        }
        log.debug("Taxa are not equivalent {}", taxonTO1.getId());
        return log.exit(false);
    }

}
