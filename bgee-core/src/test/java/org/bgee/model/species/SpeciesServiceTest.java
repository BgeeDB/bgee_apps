package org.bgee.model.species;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO.InfoType;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTOResultSet;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.source.Source;
import org.bgee.model.source.SourceService;
import org.junit.Before;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code SpeciesService} class.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, May 2019
 * @since   Bgee 13
 */
public class SpeciesServiceTest extends TestAncestor {

    @Test
    public void testLoadSpeciesInDataGroups() {
        // initialize mocks
        DAOManager managerMock = mock(DAOManager.class);
        SpeciesDAO speciesDAOMock = mock(SpeciesDAO.class);
        when(managerMock.getSpeciesDAO()).thenReturn(speciesDAOMock);
        SourceToSpeciesDAO sourceToSpeciesDAOMock = mock(SourceToSpeciesDAO.class);
        when(managerMock.getSourceToSpeciesDAO()).thenReturn(sourceToSpeciesDAOMock);
        SourceService sourceService = mock(SourceService.class);
        when(sourceService.loadAllSources(false)).thenReturn(
                Arrays.asList(new Source(1), new Source(2), new Source(3)));
        Map<Integer, Source> sourceMap = new HashMap<>();
        sourceMap.put(1, new Source(1));
        sourceMap.put(2, new Source(2));
        sourceMap.put(3, new Source(3));
        when(sourceService.loadSourcesByIds(null)).thenReturn(sourceMap);
        when(sourceService.loadAllSources(false)).thenReturn(
                Arrays.asList(new Source(1), new Source(2), new Source(3)));
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getSourceService()).thenReturn(sourceService);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
		
		// mock behavior
        List<SpeciesTO> speciesTos = Arrays.asList(
                new SpeciesTO(9606, "human", "Homo", "sapiens", 1, 4312, "3241",
                        "version1", "assemblyXRef1", 1, 321), 
                new SpeciesTO(1234, "name", "genus", "someSpecies", 2, 1123, "3432241",
                        "versionA", "assemblyXRefA", 1, 1321));
        // ResultSet cannot be reused. As we have 2 tests, we need 2 ResultSet
        SpeciesTOResultSet speciesRS = getMockResultSet(SpeciesTOResultSet.class, speciesTos);
        SpeciesTOResultSet speciesRS2 = getMockResultSet(SpeciesTOResultSet.class, speciesTos);
        when(speciesDAOMock.getSpeciesFromDataGroups(null)).thenReturn(speciesRS).thenReturn(speciesRS2);
        
        SourceToSpeciesTOResultSet sToSpRS = getMockResultSet(SourceToSpeciesTOResultSet.class, 
                Arrays.asList(
                        new SourceToSpeciesTO(1, 9606, DAODataType.EST, InfoType.DATA),
                        new SourceToSpeciesTO(1, 9606, DAODataType.IN_SITU, InfoType.DATA),
                        new SourceToSpeciesTO(2, 9606, DAODataType.AFFYMETRIX, InfoType.ANNOTATION),
                        new SourceToSpeciesTO(3, 9606, DAODataType.RNA_SEQ, InfoType.DATA),
                        new SourceToSpeciesTO(2, 1234, DAODataType.IN_SITU, InfoType.ANNOTATION)));
        when(sourceToSpeciesDAOMock.getSourceToSpecies(null, 
                new HashSet<>(Arrays.asList(9606, 1234)), null, null, null)).thenReturn(sToSpRS);

		Set<Species> expectedSpecies = new HashSet<>(Arrays.asList(
		        new Species(9606, "human", null, "Homo", "sapiens", "version1", "assembly1", new Source(1), 4312, null, null, null, 1),
		        new Species(1234, "name", null, "genus", "someSpecies", "versionA", "assemblyA", new Source(1), 1123, null, null, null, 2)));


        // actual use of the service
        SpeciesService speciesService = new SpeciesService(serviceFactory);
        assertEquals(expectedSpecies, speciesService.loadSpeciesInDataGroups(false));
        
        Map<Source, Set<DataType>> forData9606 = new HashMap<>();
        forData9606.put(new Source(1), new HashSet<DataType>(Arrays.asList(DataType.EST, DataType.IN_SITU)));
        forData9606.put(new Source(3), new HashSet<DataType>(Arrays.asList(DataType.RNA_SEQ)));
        Map<Source, Set<DataType>> forAnnot9606 = new HashMap<>();
        forAnnot9606.put(new Source(2), new HashSet<DataType>(Arrays.asList(DataType.AFFYMETRIX)));
        Map<Source, Set<DataType>> forAnnot1234 = new HashMap<>();
        forAnnot1234.put(new Source(2), new HashSet<DataType>(Arrays.asList(DataType.IN_SITU)));
        expectedSpecies.clear();
        expectedSpecies.add(new Species(9606, "human", null, "Homo", "sapiens", "version1", "assembly1", new Source(1),
                null, null, forData9606, forAnnot9606, null));
        expectedSpecies.add(new Species(1234, "name", null, "genus", "someSpecies", "versionA", "assemblyA", new Source(1),
                null, null, new HashMap<>(), forAnnot1234, null));

        assertEquals(expectedSpecies, speciesService.loadSpeciesInDataGroups(true));
    }

    private final static Set<Integer> SPECIES_IDS = new HashSet<>(Arrays.asList(9606, 1234));
    private final static Set<Integer> TAXON_IDS = new HashSet<>(Arrays.asList(9605, 1233));

    @Before
    public void prepareMockObjects() {
        List<SpeciesTO> speciesTos = Arrays.asList(
                new SpeciesTO(9606, "human", "Homo", "sapiens", 1, 4312, "3241",
                        "version1", "assemblyXRef1", 1, 321), 
                new SpeciesTO(1234, "name", "genus", "someSpecies", 2, 1123, "3432241",
                        "versionA", "assemblyXRefA", 1, 1321));
        // ResultSet cannot be reused. As we have 2 tests, we need 2 ResultSet
        SpeciesTOResultSet speciesRS = getMockResultSet(SpeciesTOResultSet.class, speciesTos);
        SpeciesTOResultSet speciesRS2 = getMockResultSet(SpeciesTOResultSet.class, speciesTos);
        when(speciesDAO.getSpeciesByIds(SPECIES_IDS, null)).thenReturn(speciesRS).thenReturn(speciesRS2);
        // ResultSet cannot be reused. As we have 2 tests, we need 2 ResultSet
        SpeciesTOResultSet speciesTaxRS = getMockResultSet(SpeciesTOResultSet.class, speciesTos);
        SpeciesTOResultSet speciesTaxRS2 = getMockResultSet(SpeciesTOResultSet.class, speciesTos);
        when(speciesDAO.getSpeciesByTaxonIds(TAXON_IDS, null)).thenReturn(speciesTaxRS)
        .thenReturn(speciesTaxRS2);

        SourceToSpeciesTOResultSet sToSpRS = getMockResultSet(SourceToSpeciesTOResultSet.class, 
                Arrays.asList(
                        new SourceToSpeciesTO(1, 9606, DAODataType.EST, InfoType.DATA),
                        new SourceToSpeciesTO(1, 9606, DAODataType.IN_SITU, InfoType.DATA),
                        new SourceToSpeciesTO(2, 9606, DAODataType.AFFYMETRIX, InfoType.ANNOTATION),
                        new SourceToSpeciesTO(3, 9606, DAODataType.RNA_SEQ, InfoType.DATA),
                        new SourceToSpeciesTO(2, 1234, DAODataType.IN_SITU, InfoType.ANNOTATION)));
        when(sourceToSpeciesDAO.getSourceToSpecies(null, 
                new HashSet<>(Arrays.asList(9606, 1234)), null, null, null)).thenReturn(sToSpRS);

        when(sourceService.loadAllSources(false)).thenReturn(
                Arrays.asList(new Source(1), new Source(2), new Source(3)));
    }

    @Test
    public void testLoadSpeciesByIds() {
        // actual use of the service
        SpeciesService service = new SpeciesService(serviceFactory);
        Set<Species> expected = new HashSet<>(Arrays.asList(
                new Species(9606, "human", null, "Homo", "sapiens", "version1", "assembly1", new Source(1), 4312, null, null, null, 1),
                new Species(1234, "name", null, "genus", "someSpecies", "versionA", "assemblyA", new Source(1), 1123, null, null, null, 2)));
        assertEquals(expected, service.loadSpeciesByIds(SPECIES_IDS, false));
        
        Map<Source, Set<DataType>> forData9606 = new HashMap<>();
        forData9606.put(new Source(1), new HashSet<DataType>(Arrays.asList(DataType.EST, DataType.IN_SITU)));
        forData9606.put(new Source(3), new HashSet<DataType>(Arrays.asList(DataType.RNA_SEQ)));
        Map<Source, Set<DataType>> forAnnot9606 = new HashMap<>();
        forAnnot9606.put(new Source(2), new HashSet<DataType>(Arrays.asList(DataType.AFFYMETRIX)));
        Map<Source, Set<DataType>> forAnnot1234 = new HashMap<>();
        forAnnot1234.put(new Source(2), new HashSet<DataType>(Arrays.asList(DataType.IN_SITU)));
        expected.clear();
        expected.add(new Species(9606, "human", null, "Homo", "sapiens", "version1", "assembly1", new Source(1), 0, 4312,
                forData9606, forAnnot9606, 1));
        expected.add(new Species(1234, "name", null, "genus", "someSpecies", "versionA", "assemblyA", new Source(1), 0, 1123,
                new HashMap<>(), forAnnot1234, 2));

        assertEquals(expected, service.loadSpeciesByIds(SPECIES_IDS, true));
    }

    /**
     * Test the method {@link SpeciesService#loadSpeciesByTaxonIds(Collection, boolean)}
     */
    @Test
    public void testLoadSpeciesByTaxonIds() {
        // actual use of the service
        SpeciesService service = new SpeciesService(serviceFactory);
        Set<Species> expected = new HashSet<>(Arrays.asList(
                new Species(9606, "human", null, "Homo", "sapiens", "version1", "assembly1", new Source(1), 0, 4312, null, null, 1),
                new Species(1234, "name", null, "genus", "someSpecies", "versionA", "assemblyA", new Source(1), 0, 1123, null, null, 2)));
        assertEquals(expected, service.loadSpeciesByTaxonIds(TAXON_IDS, false));
        
        Map<Source, Set<DataType>> forData9606 = new HashMap<>();
        forData9606.put(new Source(1), new HashSet<DataType>(Arrays.asList(DataType.EST, DataType.IN_SITU)));
        forData9606.put(new Source(3), new HashSet<DataType>(Arrays.asList(DataType.RNA_SEQ)));
        Map<Source, Set<DataType>> forAnnot9606 = new HashMap<>();
        forAnnot9606.put(new Source(2), new HashSet<DataType>(Arrays.asList(DataType.AFFYMETRIX)));
        Map<Source, Set<DataType>> forAnnot1234 = new HashMap<>();
        forAnnot1234.put(new Source(2), new HashSet<DataType>(Arrays.asList(DataType.IN_SITU)));
        expected.clear();
        expected.add(new Species(9606, "human", null, "Homo", "sapiens", "version1", "assembly1", new Source(1),
                null, null, forData9606, forAnnot9606, null));
        expected.add(new Species(1234, "name", null, "genus", "someSpecies", "versionA", "assemblyA", new Source(1),
                null, null, new HashMap<>(), forAnnot1234, null));

        assertEquals(expected, service.loadSpeciesByTaxonIds(TAXON_IDS, true));
    }
}