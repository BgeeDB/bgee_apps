package org.bgee.model.species;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.junit.Assert;
import org.junit.Test;

/**
 * This class holds the unit tests for the {@code SpeciesService} class.
 * @author Philippe Moret
 */
public class SpeciesServiceTest extends TestAncestor {

	@Test
	public void testLoadAllSpecies() {
		// initialize mocks
		DAOManager managerMock = mock(DAOManager.class);
		SpeciesDAO speciesDAOMock = mock(SpeciesDAO.class);
		SpeciesTOResultSet resultSetMock = mock(SpeciesTOResultSet.class);

		// DAO return values
		SpeciesTO to0 = new SpeciesTO("9606", "human", "Homo", "sapiens", "4312", "3241", "321", null);
		SpeciesTO to1 = new SpeciesTO("1234", "name", "genus", "someSpecies", "1123", "3432241", "1321", null);
		ArrayList<SpeciesTO> tos = new ArrayList<>();
		tos.add(to0);
		tos.add(to1);
		
		// mock behavior
		when(resultSetMock.stream()).thenReturn(tos.stream());
		when(speciesDAOMock.getSpeciesFromDataGroups()).thenReturn(resultSetMock);
		when(managerMock.getSpeciesDAO()).thenReturn(speciesDAOMock);

		// expected values
		Species v1 = new Species("9606", "human", null, "Homo", "sapiens");
		Species v2 = new Species("1234", "name", null, "genus", "someSpecies");
		
		Set<Species> expected = new HashSet<>();
		expected.add(v1);
		expected.add(v2);

		// actual use of the service
		SpeciesService service = new SpeciesService(managerMock);
		Set<Species> files = service.loadSpeciesInDataGroups();

		assertEquals(expected, files);

	}
	
	@Test
	public void testLoadNullpecies() {
		// initialize mocks
		DAOManager managerMock = mock(DAOManager.class);
		SpeciesDAO speciesDAOMock = mock(SpeciesDAO.class);
		SpeciesTOResultSet resultSetMock = mock(SpeciesTOResultSet.class);

		// DAO return values
		SpeciesTO to0 = new SpeciesTO("9606", null, "Homo", "sapiens", "4312", "3241", "321", null);
		SpeciesTO to1 = new SpeciesTO("1234", "name", null, "someSpecies", "1123", "3432241", "1321", null);
		ArrayList<SpeciesTO> tos = new ArrayList<>();
		tos.add(to0);
		tos.add(to1);
		
		// mock behavior
		when(resultSetMock.stream()).thenReturn(tos.stream());
		when(speciesDAOMock.getSpeciesFromDataGroups()).thenReturn(resultSetMock);
		when(managerMock.getSpeciesDAO()).thenReturn(speciesDAOMock);

		// expected values
		Species v1 = new Species("9606", "human", null, "Homo", "sapiens");
		Species v2 = new Species("1234", "name", null, "genus", "someSpecies");
		
		Set<Species> expected = new HashSet<>();
		expected.add(v1);
		expected.add(v2);

		// actual use of the service
		SpeciesService service = new SpeciesService(managerMock);
		Set<Species> files = service.loadSpeciesInDataGroups();

		Assert.assertNotEquals(expected, files);

	}
}
