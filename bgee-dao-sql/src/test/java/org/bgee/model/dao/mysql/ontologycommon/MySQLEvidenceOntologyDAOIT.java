package org.bgee.model.dao.mysql.ontologycommon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.ontologycommon.EvidenceOntologyDAO;
import org.bgee.model.dao.api.ontologycommon.EvidenceOntologyDAO.ECOTermTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;


/**
 * Integration tests for {@link MySQLEvidenceOntologyDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.ontologycommon.EvidenceOntologyDAO
 * @since Bgee 13
 */
public class MySQLEvidenceOntologyDAOIT  extends MySQLITAncestor {

    private final static Logger log = 
            LogManager.getLogger(MySQLEvidenceOntologyDAOIT.class.getName());
    
    public MySQLEvidenceOntologyDAOIT() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the insert method {@link MySQLEvidenceOntologyDAO#getAllECOTerms(Collection<ECOTermTO>)}.
     */
    @Test
    public void shouldGetAllECOTerms() throws SQLException {

        this.useSelectDB();
        
        // Generate result with the method
        MySQLEvidenceOntologyDAO dao = new MySQLEvidenceOntologyDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(EvidenceOntologyDAO.Attribute.values()));
        List<ECOTermTO> methECOTerms = dao.getAllECOTerms().getAllTOs();

        // Generate manually expected result
        List<ECOTermTO> expectedECOTerms = Arrays.asList(
                new ECOTermTO("ECO:1", "name1", "desc1"), 
                new ECOTermTO("ECO:2", "name2", null), 
                new ECOTermTO("ECO:3", "name3", "desc3"), 
                new ECOTermTO("ECO:4", "name4", null), 
                new ECOTermTO("ECO:5", "name5", "desc5")); 
        //Compare
        assertTrue("ECOTermTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(methECOTerms, expectedECOTerms));

        // without declared attribute should return same TOs that with all attributes 
        dao.clearAttributes();
        methECOTerms = dao.getAllECOTerms().getAllTOs();
        //Compare
        assertTrue("ECOTermTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methECOTerms, expectedECOTerms));

        // Generate manually expected result
        dao.setAttributes(Arrays.asList(EvidenceOntologyDAO.Attribute.NAME));
        methECOTerms = dao.getAllECOTerms().getAllTOs();
        expectedECOTerms = Arrays.asList(
                new ECOTermTO(null, "name1", null), 
                new ECOTermTO(null, "name2", null), 
                new ECOTermTO(null, "name3", null), 
                new ECOTermTO(null, "name4", null), 
                new ECOTermTO(null, "name5", null)); 
        //Compare
        assertTrue("ECOTermTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methECOTerms, expectedECOTerms));
    }

    /**
     * Test the insert method {@link MySQLEvidenceOntologyDAO#insertECOTerms(Collection<ECOTermTO>)}.
     */
    @Test
    public void shouldInsertECOTerms() throws SQLException {
        
        this.useEmptyDB();

        // Create a Collection of ECOTermTO to be inserted,
        // only the attribute description could be null according database schema.
        Collection<ECOTermTO> ecoTOs = Arrays.asList(
                new ECOTermTO("ECO:1", "name1", "desc1"),
                new ECOTermTO("ECO:2", "name2", null));

        try {
            MySQLEvidenceOntologyDAO dao = new MySQLEvidenceOntologyDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 2, dao.insertECOTerms(ecoTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM evidenceOntology " +
                            "WHERE ECOId = ? AND ECOName = ? AND ECODescription = ?")) {
                
                stmt.setString(1, "ECO:1");
                stmt.setString(2, "name1");
                stmt.setString(3, "desc1");
                assertTrue("ECOTermTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM evidenceOntology " +
                            "WHERE ECOId = ? AND ECOName = ? AND ECODescription IS NULL")) {
                stmt.setString(1, "ECO:2");
                stmt.setString(2, "name2");
                assertTrue("ECOTermTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            // We should throw an IllegalArgumentException when no ECOTermTO provided
            try {
                dao.insertECOTerms(new HashSet<ECOTermTO>());
                // Test failed
                throw new AssertionError("insertECOTerms did not " +
                        "throw an IllegalArgumentException as expected");
            } catch (IllegalArgumentException e) {
                // Test passed, do nothing
                log.catching(e);
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
}
