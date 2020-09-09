package org.bgee.model.dao.mysql.gene;

import static org.junit.Assert.assertTrue;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.gene.GeneHomologsDAO.GeneHomologsTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLGeneXRefDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Apr. 2019
 * @see org.bgee.model.dao.api.gene.GeneXRefDAO
 * @since   Bgee 13, May 2014
 */
public class MySQLGeneHomologsDAOIT extends MySQLITAncestor{

    private final static Logger log = LogManager.getLogger(MySQLGeneHomologsDAOIT.class.getName());
    
    public MySQLGeneHomologsDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the select methods {@link MySQLGeneHomologsDAO#getOrthologousGenes(Integer)} and 
     * {@link MySQLGeneHomologsDAO#getOrthologousGenesAtTaxonLevel(Integer, Integer)}.
     */
    @Test
    public void shouldGetOrthologsByGeneId() throws SQLException {
        
        this.useSelectDB();
        MySQLGeneHomologsDAO dao = new MySQLGeneHomologsDAO(this.getMySQLDAOManager());

        
        // Check filtering with gene ID filter
        Integer bgeeGeneId = 1;
        // get all orthologs of gene 1
        List<GeneHomologsTO> methOrthologs = dao.getOrthologousGenes(Collections.singleton(bgeeGeneId)).getAllTOs();
        List<GeneHomologsTO> expectedOrthologs = this.getAllGeneHomologsTOs(bgeeGeneId,null);
        this.assertRetrievedGeneHomologsTOs(expectedOrthologs, methOrthologs);

        // Check filtering with gene ID and taxon ID
        Integer taxonId = 311;
        // get all orthologs of gene 1 at taxonomical level descendant of 311
        methOrthologs = dao.getOrthologousGenesAtTaxonLevel(Collections.singleton(bgeeGeneId),
                taxonId, null).getAllTOs();
        expectedOrthologs = this.getAllGeneHomologsTOs(bgeeGeneId, taxonId);
        this.assertRetrievedGeneHomologsTOs(expectedOrthologs, methOrthologs);
    }
    
    /**
     * Test the select methods {@link MySQLGeneHomologsDAO#getParalogousGenes(Integer)} and 
     * {@link MySQLGeneHomologsDAO#getParalogousGenesAtTaxonLevel(Integer, Integer)}.
     */
    @Test
    public void shouldGetParalogsByGeneId() throws SQLException {
        
        this.useSelectDB();
        MySQLGeneHomologsDAO dao = new MySQLGeneHomologsDAO(this.getMySQLDAOManager());

        
        // Check filtering with gene ID filter
        Integer bgeeGeneId = 2;
        // get all paralogs of gene 2
        List<GeneHomologsTO> methParalogs = dao.getParalogousGenes(
                Collections.singleton(bgeeGeneId)).getAllTOs();
        List<GeneHomologsTO> expectedParalogs = this.getAllGeneHomologsTOs(bgeeGeneId,null);
        this.assertRetrievedGeneHomologsTOs(expectedParalogs, methParalogs);

        // Check filtering with gene ID and taxon ID
        Integer taxonId = 311;
        // get all paralogs of gene 2 at taxonomical level descendant of 311
        methParalogs = dao.getParalogousGenesAtTaxonLevel(Collections.singleton(bgeeGeneId)
                , taxonId, null).getAllTOs();
        expectedParalogs = this.getAllGeneHomologsTOs(bgeeGeneId, taxonId);
        this.assertRetrievedGeneHomologsTOs(expectedParalogs, methParalogs);
    }
    
    /**
     * Create all homologs of test data.
     * 
     * @return  A {@code List} of {@code GeneHomologsTO}s that are homologs in the test data.
     */
    private List<GeneHomologsTO> getAllGeneHomologsTOs(Integer bgeeGeneId, Integer taxonId) {
        if( taxonId == null) {
            if (bgeeGeneId == 1) {
                return Arrays.asList(new GeneHomologsTO(1, "ID2", 111), 
                        new GeneHomologsTO(1, "ID3", 311), 
                        new GeneHomologsTO(4, "ID1", 511));
            } else if (bgeeGeneId == 2) {
                return Arrays.asList(new GeneHomologsTO(1, "ID2", 111), 
                        new GeneHomologsTO(2, "ID3", 611), 
                        new GeneHomologsTO(2, "ID4", 211));
            } else if (bgeeGeneId == 3) {
                return Arrays.asList(new GeneHomologsTO(1, "ID1", 311), 
                        new GeneHomologsTO(2, "ID3", 611), 
                        new GeneHomologsTO(3, "ID4", 711));
            } else {
                throw log.throwing(new IllegalArgumentException("provided bgeeGeneId not implemented to be "
                        + "used in tests"));
            }
        } else if (taxonId == 311) {
            if (bgeeGeneId == 1) {
                return Arrays.asList(new GeneHomologsTO(1, "ID3", 311), 
                        new GeneHomologsTO(4, "ID1", 511));
            } else if (bgeeGeneId == 2) {
                return Arrays.asList(new GeneHomologsTO(2, "ID3", 611));
            } else if (bgeeGeneId == 3) {
                return Arrays.asList(new GeneHomologsTO(1, "ID1", 311), 
                        new GeneHomologsTO(2, "ID3", 611));
            } else {
                throw log.throwing(new IllegalArgumentException("provided bgeeGeneId not implemented to be "
                        + "used in tests"));
            }
        } else {
            throw log.throwing(new IllegalArgumentException("provided taxonId not implemented to be "
                + "used in tests"));
        }
        
    }
    
    /**
     * Asserts that a condition is true printing nicest message.
     * 
     * @param expectedHomologs A {@code List} of {@code GeneHomologsTO}s that are expected {@code GeneHomologsTO}s
     * @param methXrefs     A {@code List} of {@code GeneHomologsTO}s that are actual {@code GeneHomologsTO}s.
     */
    private void assertRetrievedGeneHomologsTOs(List<GeneHomologsTO> expectedHomologs, 
            List<GeneHomologsTO> methHomologs) {
        assertTrue(String.format("GeneHomologsTOs incorrectly retrieved: expected=%s - actual=%s",
                expectedHomologs, methHomologs), 
                TOComparator.areTOCollectionsEqual(expectedHomologs, methHomologs));
    }
}
