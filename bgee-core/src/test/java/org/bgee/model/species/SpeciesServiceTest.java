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

import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
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
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code SpeciesService} class.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 13, July 2016
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
		
		// mock behavior
        List<SpeciesTO> speciesTos = Arrays.asList(
                new SpeciesTO("9606", "human", "Homo", "sapiens", "4312", "3241",
                        "version1", "321", null), 
                new SpeciesTO("1234", "name", "genus", "someSpecies", "1123", "3432241",
                        "versionA", "1321", null));
        // ResultSet cannot be reused. As we have 2 tests, we need 2 ResultSet
        SpeciesTOResultSet speciesRS = getMockResultSet(SpeciesTOResultSet.class, speciesTos);
        SpeciesTOResultSet speciesRS2 = getMockResultSet(SpeciesTOResultSet.class, speciesTos);
		when(speciesDAOMock.getSpeciesFromDataGroups()).thenReturn(speciesRS).thenReturn(speciesRS2);
		
		SourceToSpeciesTOResultSet sToSpRS = getMockResultSet(SourceToSpeciesTOResultSet.class, 
		        Arrays.asList(
		                new SourceToSpeciesTO("s1", "9606", SourceToSpeciesTO.DataType.EST, InfoType.DATA),
		                new SourceToSpeciesTO("s1", "9606", SourceToSpeciesTO.DataType.IN_SITU, InfoType.DATA),
                        new SourceToSpeciesTO("s2", "9606", SourceToSpeciesTO.DataType.AFFYMETRIX, InfoType.ANNOTATION),
                        new SourceToSpeciesTO("s3", "9606", SourceToSpeciesTO.DataType.RNA_SEQ, InfoType.DATA),
		                new SourceToSpeciesTO("s2", "1234", SourceToSpeciesTO.DataType.IN_SITU, InfoType.ANNOTATION)));
		when(sourceToSpeciesDAOMock.getSourceToSpecies(null, 
		        new HashSet<String>(Arrays.asList("9606", "1234")), null, null, null)).thenReturn(sToSpRS);

		Set<Species> expectedSpecies = new HashSet<>(Arrays.asList(
		        new Species("9606", "human", null, "Homo", "sapiens", "version1"), 
		        new Species("1234", "name", null, "genus", "someSpecies", "versionA")));

		SourceService sourceService = mock(SourceService.class);
		when(sourceService.loadAllSources(false)).thenReturn(
		        Arrays.asList(new Source("s1"), new Source("s2"), new Source("s3")));

		// actual use of the service
        SpeciesService speciesService = new SpeciesService(managerMock, sourceService);
		assertEquals(expectedSpecies, speciesService.loadSpeciesInDataGroups(false));
		
		Map<Source, Set<DataType>> forData9606 = new HashMap<>();
        forData9606.put(new Source("s1"), new HashSet<DataType>(Arrays.asList(DataType.EST, DataType.IN_SITU)));
        forData9606.put(new Source("s3"), new HashSet<DataType>(Arrays.asList(DataType.RNA_SEQ)));
        Map<Source, Set<DataType>> forAnnot9606 = new HashMap<>();
        forAnnot9606.put(new Source("s2"), new HashSet<DataType>(Arrays.asList(DataType.AFFYMETRIX)));
        Map<Source, Set<DataType>> forAnnot1234 = new HashMap<>();
        forAnnot1234.put(new Source("s2"), new HashSet<DataType>(Arrays.asList(DataType.IN_SITU)));
        expectedSpecies.clear();
        expectedSpecies.add(new Species("9606", "human", null, "Homo", "sapiens", "version1", 
                forData9606, forAnnot9606));
        expectedSpecies.add(new Species("1234", "name", null, "genus", "someSpecies", "versionA", 
                null, forAnnot1234));

		assertEquals(expectedSpecies, speciesService.loadSpeciesInDataGroups(true));
	}
	
	@Test
	public void testLoadSpeciesByIds() {
	    // initialize mocks
	    DAOManager managerMock = mock(DAOManager.class);
	    SpeciesDAO speciesDAOMock = mock(SpeciesDAO.class);
        when(managerMock.getSpeciesDAO()).thenReturn(speciesDAOMock);
        SourceToSpeciesDAO sourceToSpeciesDAOMock = mock(SourceToSpeciesDAO.class);
        when(managerMock.getSourceToSpeciesDAO()).thenReturn(sourceToSpeciesDAOMock);

	    // mock behavior
        List<SpeciesTO> speciesTos = Arrays.asList(
                new SpeciesTO("9606", "human", "Homo", "sapiens", "4312", "3241",
                        "version1", "321", null), 
                new SpeciesTO("1234", "name", "genus", "someSpecies", "1123", "3432241",
                        "versionA", "1321", null));
        // ResultSet cannot be reused. As we have 2 tests, we need 2 ResultSet
	    Set<String> speciesIds = new HashSet<>(Arrays.asList("9606", "1234"));
        SpeciesTOResultSet speciesRS = getMockResultSet(SpeciesTOResultSet.class, speciesTos);
        SpeciesTOResultSet speciesRS2 = getMockResultSet(SpeciesTOResultSet.class, speciesTos);
        when(speciesDAOMock.getSpeciesByIds(speciesIds)).thenReturn(speciesRS).thenReturn(speciesRS2);

	    SourceToSpeciesTOResultSet sToSpRS = getMockResultSet(SourceToSpeciesTOResultSet.class, 
	            Arrays.asList(
	                    new SourceToSpeciesTO("s1", "9606", SourceToSpeciesTO.DataType.EST, InfoType.DATA),
	                    new SourceToSpeciesTO("s1", "9606", SourceToSpeciesTO.DataType.IN_SITU, InfoType.DATA),
	                    new SourceToSpeciesTO("s2", "9606", SourceToSpeciesTO.DataType.AFFYMETRIX, InfoType.ANNOTATION),
	                    new SourceToSpeciesTO("s3", "9606", SourceToSpeciesTO.DataType.RNA_SEQ, InfoType.DATA),
	                    new SourceToSpeciesTO("s2", "1234", SourceToSpeciesTO.DataType.IN_SITU, InfoType.ANNOTATION)));
	    when(sourceToSpeciesDAOMock.getSourceToSpecies(null, 
	            new HashSet<String>(Arrays.asList("9606", "1234")), null, null, null)).thenReturn(sToSpRS);

	    SourceService sourceService = mock(SourceService.class);
	    when(sourceService.loadAllSources(false)).thenReturn(
	            Arrays.asList(new Source("s1"), new Source("s2"), new Source("s3")));

	    // actual use of the service
	    SpeciesService service = new SpeciesService(managerMock, sourceService);
	    Set<Species> expected = new HashSet<>(Arrays.asList(
	            new Species("9606", "human", null, "Homo", "sapiens", "version1"),
	            new Species("1234", "name", null, "genus", "someSpecies", "versionA")));
        assertEquals(expected, service.loadSpeciesByIds(speciesIds, false));
        
        Map<Source, Set<DataType>> forData9606 = new HashMap<>();
        forData9606.put(new Source("s1"), new HashSet<DataType>(Arrays.asList(DataType.EST, DataType.IN_SITU)));
        forData9606.put(new Source("s3"), new HashSet<DataType>(Arrays.asList(DataType.RNA_SEQ)));
        Map<Source, Set<DataType>> forAnnot9606 = new HashMap<>();
        forAnnot9606.put(new Source("s2"), new HashSet<DataType>(Arrays.asList(DataType.AFFYMETRIX)));
        Map<Source, Set<DataType>> forAnnot1234 = new HashMap<>();
        forAnnot1234.put(new Source("s2"), new HashSet<DataType>(Arrays.asList(DataType.IN_SITU)));
        expected.clear();
        expected.add(new Species("9606", "human", null, "Homo", "sapiens", "version1", 
                forData9606, forAnnot9606));
        expected.add(new Species("1234", "name", null, "genus", "someSpecies", "versionA", 
                null, forAnnot1234));

        assertEquals(expected, service.loadSpeciesByIds(speciesIds, true));
	}
}
