package org.bgee.model.species;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code SpeciesService} class.
 * 
 * @author Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Nov. 2015
 * @since   Bgee 13
 */
public class SpeciesServiceTest extends TestAncestor {

	@Test
	public void testLoadSpeciesInDataGroups() {
		// initialize mocks
		DAOManager managerMock = mock(DAOManager.class);
		SpeciesDAO speciesDAOMock = mock(SpeciesDAO.class);
		
		// mock behavior
        SpeciesTOResultSet resultSetMock = getMockResultSet(SpeciesTOResultSet.class, Arrays.asList(
                new SpeciesTO("9606", "human", "Homo", "sapiens", "4312", "3241",
                        "version1", "321", "1", null), 
                new SpeciesTO("1234", "name", "genus", "someSpecies", "1123", "3432241",
                        "versionA", "1321", "1", null)));
		when(speciesDAOMock.getSpeciesFromDataGroups()).thenReturn(resultSetMock);
		when(managerMock.getSpeciesDAO()).thenReturn(speciesDAOMock);
		
		Set<Species> expected = new HashSet<>(Arrays.asList(
		        new Species("9606", "human", null, "Homo", "sapiens", "version1"), 
		        new Species("1234", "name", null, "genus", "someSpecies", "versionA")));

		// actual use of the service
		SpeciesService service = new SpeciesService(managerMock);
		assertEquals(expected, service.loadSpeciesInDataGroups());
	}
	
	@Test
	public void testLoadSpeciesByIds() {
	    // initialize mocks
	    DAOManager managerMock = mock(DAOManager.class);
	    SpeciesDAO speciesDAOMock = mock(SpeciesDAO.class);

	    // mock behavior
	    Set<String> speciesIds = new HashSet<>(Arrays.asList("9606", "1234"));
	    SpeciesTOResultSet resultSetMock = getMockResultSet(SpeciesTOResultSet.class, Arrays.asList(
	            new SpeciesTO("9606", "human", "Homo", "sapiens", "4312", "3241",
	                    "version1", "1", "321", null), 
	            new SpeciesTO("1234", "name", "genus", "someSpecies", "1123", "3432241",
	                    "versionA", "1", "1321", null)));
        when(speciesDAOMock.getSpeciesByIds(speciesIds)).thenReturn(resultSetMock);
	    when(managerMock.getSpeciesDAO()).thenReturn(speciesDAOMock);

	    Set<Species> expected = new HashSet<>(Arrays.asList(
	            new Species("9606", "human", null, "Homo", "sapiens", "version1"),
	            new Species("1234", "name", null, "genus", "someSpecies", "versionA")));

	    // actual use of the service
	    SpeciesService service = new SpeciesService(managerMock);
	    assertEquals(expected, service.loadSpeciesByIds(speciesIds));
	}
}
