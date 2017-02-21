package org.bgee.model.dao.mysql.source;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
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
import org.bgee.model.dao.api.source.SourceToSpeciesDAO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.Attribute;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO.DataType;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO.InfoType;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLSourceToSpeciesDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for important information.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, July 2016
 * @since   Bgee 13, June 2016
 */
public class MySQLSourceToSpeciesDAOIT extends MySQLITAncestor {

    private final static Logger log = LogManager.getLogger(MySQLSourceToSpeciesDAOIT.class.getName());
    
    public MySQLSourceToSpeciesDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the select method
     * {@link MySQLSourceToSpeciesDAO#getAllSourceToSpecies(Collection)}.
     */
    @Test
    public void shouldGetAllSourceToSpecies() throws SQLException {

        this.useSelectDB();

        MySQLSourceToSpeciesDAO dao = new MySQLSourceToSpeciesDAO(this.getMySQLDAOManager());
        
        // Test recovery of all attributes with any filter
        List<SourceToSpeciesTO> expectedTOs = this.getAllSourceToSpeciesTOs();
        assertTrue("SourceToSpeciesTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(dao.getAllSourceToSpecies(null).getAllTOs(), expectedTOs));
        
        // Test recovery of two attributes with any filter
        EnumSet<Attribute> attributes = EnumSet.of(SourceToSpeciesDAO.Attribute.DATASOURCE_ID,
                SourceToSpeciesDAO.Attribute.DATA_TYPE);
        expectedTOs = this.getAllSourceToSpeciesTOs().stream()
                .map(s -> new SourceToSpeciesTO(s.getDataSourceId(), null, s.getDataType(), null))
                .distinct()
                .collect(Collectors.toList());
        assertTrue("SourceToSpeciesTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getAllSourceToSpecies(attributes).getAllTOs(), expectedTOs));
    }

    /**
     * Test the select method {@link MySQLSourceToSpeciesDAO#getSourceToSpecies(
     * Collection, Collection, Collection, Collection, Collection))}.
     */
    @Test
    public void shouldGetSourceToSpecies() throws SQLException {

        this.useSelectDB();

        MySQLSourceToSpeciesDAO dao = new MySQLSourceToSpeciesDAO(this.getMySQLDAOManager());

        // Test recovery of all attributes with filter on species IDs
        List<SourceToSpeciesTO> expectedTOs = this.getAllSourceToSpeciesTOs();
        assertTrue("SourceToSpeciesTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getSourceToSpecies(null, null, null, null, null).getAllTOs(),
                        expectedTOs));
        
        // Test recovery of all attributes with filter on species IDs
        Set<String> speciesIds = new HashSet<String>(Arrays.asList("11","44"));
        expectedTOs = this.getAllSourceToSpeciesTOs().stream()
                .filter(s -> speciesIds.contains(s.getSpeciesId()))
                .collect(Collectors.toList());
        assertTrue("SourceToSpeciesTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getSourceToSpecies(null, speciesIds, null, null, null).getAllTOs(),
                        expectedTOs));
        
        // Test recovery of all attributes with filter on species IDs and info types
        EnumSet<InfoType> infoTypes = EnumSet.of(InfoType.DATA);
        expectedTOs = this.getAllSourceToSpeciesTOs().stream()
                .filter(s -> speciesIds.contains(s.getSpeciesId()))
                .filter(s -> infoTypes.contains(s.getInfoType()))
                .collect(Collectors.toList());
        assertTrue("SourceToSpeciesTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getSourceToSpecies(null, speciesIds, null, infoTypes, null).getAllTOs(),
                        expectedTOs));
        
        // Test recovery of one attribute with filter on data source IDs and data types
        EnumSet<Attribute> attributes = EnumSet.of(SourceToSpeciesDAO.Attribute.DATA_TYPE);
        Set<String> dataSourceIds = new HashSet<String>(Arrays.asList("4","99"));
        EnumSet<DataType> dataTypes = EnumSet.of(DataType.EST, DataType.IN_SITU);
        expectedTOs = this.getAllSourceToSpeciesTOs().stream()
                .filter(s -> dataSourceIds.contains(s.getDataSourceId()))
                .filter(s -> dataTypes.contains(s.getDataType()))
                .map(s -> new SourceToSpeciesTO(null, null, s.getDataType(), null))
                .distinct()
                .collect(Collectors.toList());
        assertTrue("SourceToSpeciesTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getSourceToSpecies(dataSourceIds, null, dataTypes, null, attributes).getAllTOs(),
                        expectedTOs));

        // Test recovery of two attributes with all filters
        attributes = EnumSet.of(SourceToSpeciesDAO.Attribute.SPECIES_ID, 
                SourceToSpeciesDAO.Attribute.INFO_TYPE);
        expectedTOs = this.getAllSourceToSpeciesTOs().stream()
                .filter(s -> dataSourceIds.contains(s.getDataSourceId()))
                .filter(s -> speciesIds.contains(s.getSpeciesId()))
                .filter(s -> dataTypes.contains(s.getDataType()))
                .filter(s -> infoTypes.contains(s.getInfoType()))
                .map(s -> new SourceToSpeciesTO(null, s.getSpeciesId(), null, s.getInfoType()))
                .distinct()
                .collect(Collectors.toList());
        assertTrue("SourceToSpeciesTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getSourceToSpecies(dataSourceIds, speciesIds, dataTypes, infoTypes, attributes).getAllTOs(),
                        expectedTOs));
    }

    private List<SourceToSpeciesTO> getAllSourceToSpeciesTOs() {
        return Arrays.asList(
                new SourceToSpeciesTO(1, 11, DataType.AFFYMETRIX, InfoType.DATA),
                new SourceToSpeciesTO(1, 11, DataType.AFFYMETRIX, InfoType.ANNOTATION),
                new SourceToSpeciesTO(1, 21, DataType.AFFYMETRIX, InfoType.DATA),
                new SourceToSpeciesTO(1, 21, DataType.AFFYMETRIX, InfoType.ANNOTATION),
                new SourceToSpeciesTO(2, 11, DataType.EST, InfoType.DATA),
                new SourceToSpeciesTO(3, 11, DataType.EST, InfoType.ANNOTATION),
                new SourceToSpeciesTO(4, 11, DataType.EST, InfoType.DATA),
                new SourceToSpeciesTO(4, 11, DataType.EST, InfoType.ANNOTATION),
                new SourceToSpeciesTO(4, 21, DataType.RNA_SEQ, InfoType.DATA),
                new SourceToSpeciesTO(4, 21, DataType.IN_SITU, InfoType.ANNOTATION));
    }
}
