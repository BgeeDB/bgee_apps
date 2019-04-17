package org.bgee.model.dao.mysql.gene;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.gene.GeneXRefDAO;
import org.bgee.model.dao.api.gene.GeneXRefDAO.GeneXRefTO;
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
public class MySQLGeneXRefDAOIT extends MySQLITAncestor {

    private final static Logger log = LogManager.getLogger(MySQLGeneXRefDAOIT.class.getName());

    public MySQLGeneXRefDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the select method {@link MySQLGeneXRefDAO#getAllGeneXRefs(java.util.Collection)}.
     */
    @Test
    public void shouldGetAllGeneXRefs() throws SQLException {
        
        this.useSelectDB();
        MySQLGeneXRefDAO dao = new MySQLGeneXRefDAO(this.getMySQLDAOManager());

        // Check without attributes
        List<GeneXRefTO> methXrefs = dao.getAllGeneXRefs(null).getAllTOs();
        List<GeneXRefTO> expectedXrefs = this.getAllGeneXrefTOs();
        this.assertRetrievedGeneXRefTOs(expectedXrefs, methXrefs);
        
        // Check with specified attributes
        List<GeneXRefDAO.Attribute> attributes = 
                Arrays.asList(GeneXRefDAO.Attribute.BGEE_GENE_ID, GeneXRefDAO.Attribute.DATA_SOURCE_ID); 
        methXrefs = dao.getAllGeneXRefs(attributes).getAllTOs();
        expectedXrefs= Arrays.asList(
                new GeneXRefTO(1, null, null, 1),
                new GeneXRefTO(2, null, null, 2),
                new GeneXRefTO(3, null, null, 1),
                new GeneXRefTO(4, null, null, 2),
                new GeneXRefTO(5, null, null, 3),
                new GeneXRefTO(6, null, null, 4));
        this.assertRetrievedGeneXRefTOs(expectedXrefs, methXrefs);
    }

    /**
     * Test the select method {@link MySQLGeneXRefDAO#getGeneXRefsByGeneIds(Collection, Collection)}.
     */
    @Test
    public void shouldGetGeneXRefsByGeneIds() throws SQLException {
        
        this.useSelectDB();
        MySQLGeneXRefDAO dao = new MySQLGeneXRefDAO(this.getMySQLDAOManager());
        
        // Check filtering without any filter
        List<GeneXRefTO> methXrefs = dao.getGeneXRefsByGeneIds(null, null).getAllTOs();
        List<GeneXRefTO> expectedXrefs = this.getAllGeneXrefTOs();
        this.assertRetrievedGeneXRefTOs(expectedXrefs, methXrefs);

        // Check filtering with gene ID filter
        Collection<Integer> bgeeGeneIds = Arrays.asList(1, 3); 
        methXrefs = dao.getGeneXRefsByGeneIds(bgeeGeneIds, null).getAllTOs();
        expectedXrefs = this.getAllGeneXrefTOs().stream()
                .filter(x -> bgeeGeneIds.contains(x.getBgeeGeneId()))
                .collect(Collectors.toList());
        this.assertRetrievedGeneXRefTOs(expectedXrefs, methXrefs);
    }

    /**
     * Test the select method {@link MySQLGeneXRefDAO#getGeneXRefsByXRefIds(Collection, Collection)}.
     */
    @Test
    public void shouldGetGeneXRefsByXRefIds() throws SQLException {
        
        this.useSelectDB();
        MySQLGeneXRefDAO dao = new MySQLGeneXRefDAO(this.getMySQLDAOManager());
        
        // Check filtering without any argument
        List<GeneXRefTO> methXrefs = dao.getGeneXRefsByXRefIds(null, null).getAllTOs();
        List<GeneXRefTO> expectedXrefs = this.getAllGeneXrefTOs();
        this.assertRetrievedGeneXRefTOs(expectedXrefs, methXrefs);
        
        // Check filtering with x-ref ID filter
        Collection<String> xRefIds = Arrays.asList("A0A183", "AL162596", "Bt.16194"); 
        methXrefs = dao.getGeneXRefsByXRefIds(xRefIds, null).getAllTOs();
        expectedXrefs = this.getAllGeneXrefTOs().stream()
                .filter(x -> xRefIds.contains(x.getXRefId()))
                .collect(Collectors.toList());
        this.assertRetrievedGeneXRefTOs(expectedXrefs, methXrefs);
    }

    /**
     * Test the select method 
     * {@link MySQLGeneXRefDAO#getGeneXRefs(Collection, Collection, Collection, Collection)}.
     */
    @Test
    public void shouldGetGeneXRefs() throws SQLException {
        
        this.useSelectDB();
        MySQLGeneXRefDAO dao = new MySQLGeneXRefDAO(this.getMySQLDAOManager());
        
        // Check without any argument
        List<GeneXRefTO> methXrefs = dao.getGeneXRefs(null, null, null, null).getAllTOs();
        List<GeneXRefTO> expectedXrefs = this.getAllGeneXrefTOs();
        this.assertRetrievedGeneXRefTOs(expectedXrefs, methXrefs);
        
        // Check filtering with all filters
        Collection<Integer> bgeeGeneIds = Arrays.asList(1, 3); 
        Collection<String> xRefIds = Arrays.asList("AL162596", "DQ991251", "Bt.16194"); 
        Collection<Integer> dataSourceIds = Arrays.asList(2);
        methXrefs = dao.getGeneXRefs(bgeeGeneIds, xRefIds, dataSourceIds, null).getAllTOs();
        expectedXrefs = this.getAllGeneXrefTOs().stream()
                .filter(x -> bgeeGeneIds.contains(x.getBgeeGeneId()))
                .filter(x -> xRefIds.contains(x.getXRefId()))
                .filter(x -> dataSourceIds.contains(x.getDataSourceId()))
                .collect(Collectors.toList());
        this.assertRetrievedGeneXRefTOs(expectedXrefs, methXrefs);
                
        // Check filtering with some filter combination
        methXrefs = dao.getGeneXRefs(null, xRefIds, dataSourceIds, null).getAllTOs();
        expectedXrefs = this.getAllGeneXrefTOs().stream()
                .filter(x -> xRefIds.contains(x.getXRefId()))
                .filter(x -> dataSourceIds.contains(x.getDataSourceId()))
                .collect(Collectors.toList());
        this.assertRetrievedGeneXRefTOs(expectedXrefs, methXrefs);

        methXrefs = dao.getGeneXRefs(bgeeGeneIds, null, dataSourceIds, null).getAllTOs();
        expectedXrefs = this.getAllGeneXrefTOs().stream()
                .filter(x -> bgeeGeneIds.contains(x.getBgeeGeneId()))
                .filter(x -> dataSourceIds.contains(x.getDataSourceId()))
                .collect(Collectors.toList());
        this.assertRetrievedGeneXRefTOs(expectedXrefs, methXrefs);
        
        methXrefs = dao.getGeneXRefs(bgeeGeneIds, xRefIds, null, null).getAllTOs();
        expectedXrefs = this.getAllGeneXrefTOs().stream()
                .filter(x -> bgeeGeneIds.contains(x.getBgeeGeneId()))
                .filter(x -> xRefIds.contains(x.getXRefId()))
                .collect(Collectors.toList());
        this.assertRetrievedGeneXRefTOs(expectedXrefs, methXrefs);

        // Check filtering with all gene ID and data source ID filters
        List<GeneXRefDAO.Attribute> attributes = 
                Arrays.asList(GeneXRefDAO.Attribute.BGEE_GENE_ID, GeneXRefDAO.Attribute.XREF_NAME); 
        methXrefs = dao.getGeneXRefs(bgeeGeneIds, null, dataSourceIds, attributes).getAllTOs();
        expectedXrefs = Arrays.asList(new GeneXRefTO(1, null, "", null));
        this.assertRetrievedGeneXRefTOs(expectedXrefs, methXrefs);
    }
    
    /**
     * Create all cross-references of test data.
     * 
     * @return  A {@code List} of {@code GeneXRefTO}s that are the cross-references in the test data.
     */
    private List<GeneXRefTO> getAllGeneXrefTOs() {
        return Arrays.asList(
                new GeneXRefTO(1, "A0A183", "LCE6A_HUMAN", 1),
                new GeneXRefTO(1, "AL162596", "", 2),
                new GeneXRefTO(1, "DQ991251", "", 2),
                new GeneXRefTO(2, "A0AUZ9", "KAL1L_HUMAN", 1),
                new GeneXRefTO(2, "I6L9A8", "I6L9A8_HUMAN", 3),
                new GeneXRefTO(2, "AC007038", "", 2),
                new GeneXRefTO(3, "Bt.16194", "", 4));
    }
    
    /**
     * Asserts that a condition is true printing nicest message.
     * 
     * @param expectedXrefs A {@code List} of {@code GeneXRefTO}s that are expected {@code GeneXRefTO}s
     * @param methXrefs     A {@code List} of {@code GeneXRefTO}s that are actual {@code GeneXRefTO}s.
     */
    private void assertRetrievedGeneXRefTOs(List<GeneXRefTO> expectedXrefs, List<GeneXRefTO> methXrefs) {
        assertTrue(String.format("GeneXRefTOs incorrectly retrieved: expected=%s - actual=%s",
                                expectedXrefs, methXrefs), 
                TOComparator.areTOCollectionsEqual(expectedXrefs, methXrefs));
    }
}
