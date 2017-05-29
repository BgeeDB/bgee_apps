package org.bgee.model.dao.mysql.source;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.source.SourceDAO;
import org.bgee.model.dao.api.source.SourceDAO.SourceTO;
import org.bgee.model.dao.api.source.SourceDAO.SourceTO.SourceCategory;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLSourceDAOIT}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, July 2016
 * @since   Bgee 13, Mar. 2016
 * @see     org.bgee.model.dao.api.source.SourceDAO
 */
public class MySQLSourceDAOIT extends MySQLITAncestor {

    private final static Logger log = LogManager.getLogger(MySQLSourceDAOIT.class.getName());

    public MySQLSourceDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the select method {@link MySQLSourceDAO#getAllDataSources(java.util.Collection)}.
     */
    @Test
    public void shouldGetAllDataSources() throws SQLException {
        this.useSelectDB();

        // Generate result with the method
        MySQLSourceDAO dao = new MySQLSourceDAO(this.getMySQLDAOManager());
        List<SourceTO> methSources = dao.getAllDataSources(null).getAllTOs();

        // Generate manually expected result
        List<SourceTO> expectedSources = getAllSources(); 
        //Compare
        assertTrue("SourceTOs incorrectly retrieved: expectedSources= " + expectedSources + 
                " methSources= " + methSources, TOComparator.areTOCollectionsEqual(expectedSources, methSources));

        // with all attributes declared should return same TOs that with all attributes 
        List<SourceDAO.Attribute> attributes = Arrays.asList(SourceDAO.Attribute.values()); 

        methSources = dao.getAllDataSources(attributes).getAllTOs();
        //Compare
        assertTrue("SourceTOs incorrectly retrieved: expectedSources= " + expectedSources + 
                " methSources= " + methSources, TOComparator.areTOCollectionsEqual(expectedSources, methSources));

        // Generate manually expected result
        attributes = Arrays.asList(SourceDAO.Attribute.NAME, SourceDAO.Attribute.EVIDENCE_URL);
        methSources = dao.getAllDataSources(attributes).getAllTOs();
        expectedSources = Arrays.asList(
                new SourceTO(null, "First DataSource", null, null, null, "evidenceUrl", null, null, null, null, null, null),
                new SourceTO(null, "NCBI Taxonomy", null, null, null, "", null, null, null, null, null, null),
                new SourceTO(null, "Ensembl", null, null, null, "", null, null, null, null, null, null), 
                new SourceTO(null, "ZFIN", null, null, null, "http://zfin.org/cgi-bin/ZFIN_jump?record=[evidence_id]",
                        null, null, null, null, null, null)); 
        //Compare
        assertTrue("SourceTOs incorrectly retrieved: expectedSources= " + expectedSources + 
                " methSources= " + methSources, TOComparator.areTOCollectionsEqual(expectedSources, methSources));
    }

    /**
     * Test the select method {@link MySQLSourceDAO#getDisplayableDataSources(java.util.Collection)}.
     */
    @Test
    public void shouldGetDisplayableDataSources() throws SQLException {
        this.useSelectDB();
        
        // Generate result with the method
        MySQLSourceDAO dao = new MySQLSourceDAO(this.getMySQLDAOManager());
        List<SourceTO> methSources = dao.getDisplayableDataSources(null).getAllTOs();

        // Generate manually expected result
        List<SourceTO> expectedSources = getAllSources().stream()
                .filter(s -> s.isToDisplay())
                .collect(Collectors.toList()); 
        //Compare
        assertTrue("SourceTOs incorrectly retrieved: expectedSources= " + expectedSources + 
                " methSources= " + methSources, TOComparator.areTOCollectionsEqual(expectedSources, methSources));

        // with all attributes declared should return same TOs that with all attributes 
        List<SourceDAO.Attribute> attributes = Arrays.asList(SourceDAO.Attribute.values()); 
        methSources = dao.getDisplayableDataSources(attributes).getAllTOs();
        //Compare
        assertTrue("SourceTOs incorrectly retrieved: expectedSources= " + expectedSources + 
                " methSources= " + methSources, TOComparator.areTOCollectionsEqual(expectedSources, methSources));

        // Generate manually expected result
        attributes = Arrays.asList(SourceDAO.Attribute.RELEASE_VERSION);
        methSources = dao.getDisplayableDataSources(attributes).getAllTOs();
        expectedSources = Arrays.asList(
                new SourceTO(null, null, null, null, null, null, null, null, "v13", null, null, null),
                new SourceTO(null, null, null, null, null, null, null, null, "v1", null, null, null), 
                new SourceTO(null, null, null, null, null, null, null, null, "rv:2", null, null, null)); 
        //Compare
        assertTrue("SourceTOs incorrectly retrieved: expectedSources= " + expectedSources + 
                " methSources= " + methSources, TOComparator.areTOCollectionsEqual(expectedSources, methSources));
    }

    /**
     * Test the select method
     * {@link MySQLSourceDAO#getDataSourceByIds(java.util.Collection, java.util.Collection)}.
     */
    @Test
    public void shouldGetDataSourceByIds() throws SQLException {
        this.useSelectDB();

        // Test with no attributes declared. 
        MySQLSourceDAO dao = new MySQLSourceDAO(this.getMySQLDAOManager());
        List<SourceTO> expectedSources = getAllSources().stream()
                .filter(s -> s.getId().equals("2"))
                .collect(Collectors.toList()); 
        assertTrue("SourceTO incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedSources,
                        dao.getDataSourceByIds(Arrays.asList("2"), null).getAllTOs()));

        // Test with all attributes declared. This should return same TOs that with all attributes. 
        List<SourceDAO.Attribute> attributes = Arrays.asList(SourceDAO.Attribute.values()); 
        //Compare
        assertTrue("SourceTO incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedSources, 
                        dao.getDataSourceByIds(Arrays.asList("2"), attributes).getAllTOs()));

        // Test single attribute
        attributes = Arrays.asList(SourceDAO.Attribute.DESCRIPTION);
        expectedSources = Arrays.asList(
                new SourceTO(null, null, "Ensembl desc", null, null, null, null, null, null, null, null, null)); 
        assertTrue("SourceTO incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedSources,
                        dao.getDataSourceByIds(Arrays.asList("3"), attributes).getAllTOs()));
        
        // Test single attribute with multiple species IDs
        attributes = Arrays.asList(SourceDAO.Attribute.EXPERIMENT_URL, SourceDAO.Attribute.BASE_URL,
                SourceDAO.Attribute.CATEGORY);
        expectedSources = Arrays.asList(
                new SourceTO(null, null, null, null, "experimentUrl", null, "baseUrl", null,
                        null, null, SourceCategory.GENOMICS, null),
                new SourceTO(null, null, null, null, "", null, "http://May2012.archive.ensembl.org/", 
                        null, null, null, SourceCategory.NONE, null));
        log.debug("#1# "+expectedSources);
        log.debug("#2# "+dao.getDataSourceByIds(Arrays.asList("1", "3"), attributes).getAllTOs());
        assertTrue("SourceTO incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(expectedSources,
                        dao.getDataSourceByIds(Arrays.asList("1", "3"), attributes).getAllTOs()));
    }

    /**
     * Obtains an instance of Date from provided {@code year}, {@code month}, and {@code day}.
     * 
     * @param year  An {@code int} that is the year to be used.
     * @param month A {@code Month} that is the month-of-year to be used.
     * @param day   An {@code int} that is the day to be used.
     * @return      the {@code java.util.Date} representing the date of provided {@code year}, 
     *              {@code month}, and {@code day}.
     */
    private static java.util.Date asDate(int year, Month month, int day) {
        LocalDate localDate = LocalDate.of(year, month, day);
        return java.util.Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    /** 
     * Create all sources of test data.
     * 
     * @return  A {@code List} of {@code SourceTO}s that are the sources in the test data.
     */
    private List<SourceTO> getAllSources() {
        return Arrays.asList(
                new SourceTO(1, "First DataSource", "My custom data source", "XRefUrl", 
                        "experimentUrl", "evidenceUrl", "baseUrl", asDate(2012, Month.OCTOBER, 19),
                        "1.0", false, SourceCategory.GENOMICS, 1),
                new SourceTO(2, "NCBI Taxonomy", "Source taxonomy used in Bgee", "", "", "",
                        "https://www.ncbi.nlm.nih.gov/taxonomy",
                        asDate(2012, Month.OCTOBER, 20), "v13", true, SourceCategory.NONE, 3), 
                new SourceTO(3, "Ensembl", "Ensembl desc",
                        "http://Oct2012.archive.ensembl.org/[species_ensembl_link]/Gene/Summary?g=[gene_id];gene_summary=das:http://bgee.unil.ch/das/bgee=label", 
                        "", "", "http://May2012.archive.ensembl.org/", 
                        asDate(2014, Month.FEBRUARY, 18), "v1", true, SourceCategory.NONE, 255), 
                new SourceTO(4, "ZFIN", "ZFIN desc", 
                        "http://zfin.org/cgi-bin/ZFIN_jump?record=[xref_id]",
                        "http://zfin.org/cgi-bin/ZFIN_jump?record=[experiment_id]", 
                        "http://zfin.org/cgi-bin/ZFIN_jump?record=[evidence_id]", 
                        "http://zfin.org/", null, "rv:2", true, SourceCategory.IN_SITU, 2));
    }
}
