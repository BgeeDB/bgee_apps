package org.bgee.model.dao.mysql.species;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.species.TaxonDAO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
    
    public MySQLTaxonDAOIT() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test the insertion method {@link MySQLTaxonDAO#insertTaxa(Collection)}.
     */
    @Test
    public void shouldInsertTaxa() throws SQLException {
        
        this.useEmptyDB();
        
        //create a Collection of TaxonTOs to be inserted
        Collection<TaxonTO> taxonTOs = new ArrayList<TaxonTO>();
        taxonTOs.add(new TaxonTO(10, "commonName1", "sciName1", 1, 10, 1, true));
        taxonTOs.add(new TaxonTO(50, "commonName2", "sciName2", 2, 5, 2, false));
        taxonTOs.add(new TaxonTO(60, "commonName3", "sciName3", 6, 9, 3, true));
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
            
            this.thrown.expect(IllegalArgumentException.class);
            dao.insertTaxa(new HashSet<TaxonTO>());
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
    
    /**
     * Test the select method {@link MySQLTaxonDAO#getAllTaxa()}.
     */
    @Test
    public void shouldGetAllTaxa() throws SQLException {

        this.useSelectDB();

        // Generate result with the method
        MySQLTaxonDAO dao = new MySQLTaxonDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(TaxonDAO.Attribute.values()));
        List<TaxonTO> methResults = dao.getAllTaxa().getAllTOs();

        // Generate manually expected result
        List<TaxonTO> expectedTaxa = Arrays.asList(
                new TaxonTO(111, "taxCName111", "taxSName111", 1, 14, 1, true),
                new TaxonTO(211, "taxCName211", "taxSName211", 2, 3, 2, false), 
                new TaxonTO(311, "taxCName311", "taxSName311", 4, 11, 2, false),
                new TaxonTO(411, "taxCName411", "taxSName411", 5, 6, 3, true), 
                new TaxonTO(511, "taxCName511", "taxSName511", 7, 10, 3, true),
                new TaxonTO(611, "taxCName611", "taxSName611", 8, 9, 4, true),
                new TaxonTO(711, "taxCName711", "taxSName711", 12, 13, 2, false));
        // Compare
        assertTrue("TaxonTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methResults, expectedTaxa));

        dao.setAttributes(Arrays.asList(TaxonDAO.Attribute.LCA));
        methResults = dao.getAllTaxa().getAllTOs();
        
        // Generate manually expected result
        expectedTaxa = Arrays.asList(
                new TaxonTO(null, null, null, null, null, null, true), 
                new TaxonTO(null, null, null, null, null, null, false)); 
        assertTrue("TaxonTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(methResults, expectedTaxa));
    }
    
    /**
     * Test the select method {@link MySQLTaxonDAO#getAllLeastCommonAncestorAndParentTaxa(Collection)}.
     */
    @Test
    public void shouldGetAllLeastCommonAncestorAndParentTaxa() throws SQLException {
        
        this.useSelectDB();
        
        // Generate result with the method
        MySQLTaxonDAO dao = new MySQLTaxonDAO(this.getMySQLDAOManager());

        // Generate manually expected result
        List<TaxonTO> expectedTaxa = Arrays.asList(
                new TaxonTO(111, "taxCName111", "taxSName111", 1, 14, 1, true),
                new TaxonTO(211, "taxCName211", "taxSName211", 2, 3, 2, false), 
                new TaxonTO(311, "taxCName311", "taxSName311", 4, 11, 2, false),
                new TaxonTO(411, "taxCName411", "taxSName411", 5, 6, 3, true), 
                new TaxonTO(511, "taxCName511", "taxSName511", 7, 10, 3, true),
                new TaxonTO(611, "taxCName611", "taxSName611", 8, 9, 4, true));
        // Compare
        assertTrue("TaxonTOs incorrectly retrieved", TOComparator.areTOCollectionsEqual(
                dao.getAllLeastCommonAncestorAndParentTaxa(null).getAllTOs(), expectedTaxa));
    }
    
    /**
     * Test the select method {@link MySQLTaxonDAO#getLeastCommonAncestor()}.
     */
    @Test
    public void shouldGetLeastCommonAncestor() throws SQLException {
        
        this.useSelectDB();
        
        Set<Integer> speciesIds = new HashSet<>();
        boolean includeAncestors = false;
        
        MySQLTaxonDAO dao = new MySQLTaxonDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(TaxonDAO.Attribute.values()));

        // No speciesIds - includeAncestors = false
        // => find the LCA of all species in Bgee
        List<TaxonTO> actualResults =
                dao.getLeastCommonAncestor(speciesIds, includeAncestors).getAllTOs();
        List<TaxonTO> expectedTaxa = Arrays.asList(
                new TaxonTO(111, "taxCName111", "taxSName111", 1, 14, 1, true));
        assertTrue("TaxonTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(actualResults, expectedTaxa));

        // No speciesIds - includeAncestors = true
        // => find the LCA of all species in Bgee and its ancestors
        includeAncestors = true;
        actualResults = dao.getLeastCommonAncestor(speciesIds, includeAncestors).getAllTOs();
        // Here, it should be the same result because 111 is the older ancestor of test taxonomy
        assertTrue("TaxonTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(actualResults, expectedTaxa));
                
        // One species ID - includeAncestors = false
        // => find the LCA of the species
        speciesIds.add(41);
        includeAncestors = false;
        actualResults = dao.getLeastCommonAncestor(speciesIds, includeAncestors).getAllTOs();
        expectedTaxa = Arrays.asList(
                new TaxonTO(411, "taxCName411", "taxSName411", 5, 6, 3, true));
        assertTrue("TaxonTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(actualResults, expectedTaxa));

        // One species ID - includeAncestors = true
        // => find the LCA of the species and its ancestors
        dao.setAttributes(Arrays.asList(TaxonDAO.Attribute.COMMON_NAME));
        includeAncestors = true;
        actualResults = dao.getLeastCommonAncestor(speciesIds, includeAncestors).getAllTOs();
        expectedTaxa = Arrays.asList(
                new TaxonTO(null, "taxCName111", null, null, null, null, null),
                new TaxonTO(null, "taxCName311", null, null, null, null, null),
                new TaxonTO(null, "taxCName411", null, null, null, null, null));
        assertTrue("TaxonTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(actualResults, expectedTaxa));

        // Severals species IDs (species are member of a same taxon leaf) - includeAncestors = true
        // => find the LCA of the species and its ancestors
        dao.setAttributes(Arrays.asList(TaxonDAO.Attribute.LCA, TaxonDAO.Attribute.LEVEL));
        speciesIds.add(42);
        actualResults = dao.getLeastCommonAncestor(speciesIds, includeAncestors).getAllTOs();
        expectedTaxa = Arrays.asList(
                new TaxonTO(null, null, null, null, null, 3, true),
                new TaxonTO(null, null, null, null, null, 2, false),
                new TaxonTO(null, null, null, null, null, 1, true));
        assertTrue("TaxonTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(actualResults, expectedTaxa));

        // Severals species IDs (species 31 is member of parent taxon) - includeAncestors = true
        // => find the LCA of the species and its ancestors
        dao.setAttributes(Arrays.asList(TaxonDAO.Attribute.ID));
        speciesIds.add(31);
        actualResults = dao.getLeastCommonAncestor(speciesIds, includeAncestors).getAllTOs();
        expectedTaxa = Arrays.asList(
                new TaxonTO(111, null, null, null, null, null, null),
                new TaxonTO(311, null, null, null, null, null, null));
        assertTrue("TaxonTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(actualResults, expectedTaxa));
    }
}
