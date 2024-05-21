package org.bgee.model.file;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesDataGroupTOResultSet;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesToDataGroupTOResultSet;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesToGroupOrderingAttribute;
import org.bgee.model.file.SpeciesDownloadFile.Category;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class holds the unit tests for {@code SpeciesDataGroup}
 * 
 * @author Philippe Moret
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 15, Oct. 2021
 * @since   Bgee 13
 */
public class SpeciesDataGroupServiceTest extends TestAncestor {

	@Test
	public void testGetAllDatagroups() {

		// initialize mocks
		DAOManager managerMock = mock(DAOManager.class);
		SpeciesDataGroupDAO dao = mock(SpeciesDataGroupDAO.class);
        SpeciesService speciesService = mock(SpeciesService.class);
		DownloadFileService downloadFileService = mock(DownloadFileService.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        when(serviceFactory.getDownloadFileService()).thenReturn(downloadFileService);


		// services return values
		Species v1 = new Species(9606);
		Species v2 = new Species(1234);
		
        Set<Species> species = new HashSet<>();
		species.add(v1);
		species.add(v2);
		SpeciesDownloadFile df1 = new SpeciesDownloadFile("/tmp/foo", "NAME", null, 42L, Category.AFFY_ANNOT, 22);
		SpeciesDownloadFile df2 = new SpeciesDownloadFile("/tmp/foo2", "NAME2", null, 1337L, Category.DIFF_EXPR_ANAT_COMPLETE, 22);
		SpeciesDownloadFile df3 = new SpeciesDownloadFile("/tmp/foo3", "NAME", null, 42L, Category.AFFY_ANNOT, 42);

        List<SpeciesDownloadFile> downloadFiles = Arrays.asList(df1,df2,df3);


		// DAO return values
        SpeciesDataGroupDAO.SpeciesDataGroupTO to1 =
                new SpeciesDataGroupDAO.SpeciesDataGroupTO(22, "group1", "the group 1", 2);
        SpeciesDataGroupDAO.SpeciesDataGroupTO to2 =
                new SpeciesDataGroupDAO.SpeciesDataGroupTO(42, "group2", "group2", 1);

        SpeciesDataGroupDAO.SpeciesToDataGroupTO mto1 = 
            new SpeciesDataGroupDAO.SpeciesToDataGroupTO(9606, 22);
        SpeciesDataGroupDAO.SpeciesToDataGroupTO mto2 =
            new SpeciesDataGroupDAO.SpeciesToDataGroupTO(1234, 22);
        SpeciesDataGroupDAO.SpeciesToDataGroupTO mto3 =
            new SpeciesDataGroupDAO.SpeciesToDataGroupTO(1234, 42);

        // mock behavior
        SpeciesDataGroupTOResultSet sdgResultSet = getMockResultSet(
                SpeciesDataGroupTOResultSet.class, Arrays.asList(to1,to2));
        SpeciesToDataGroupTOResultSet stdgResultSet = getMockResultSet(
                SpeciesToDataGroupTOResultSet.class,Arrays.asList(mto1,mto2,mto3));
        LinkedHashMap<SpeciesToGroupOrderingAttribute, DAO.Direction> orderAttrs = new LinkedHashMap<>();
        orderAttrs.put(SpeciesToGroupOrderingAttribute.DATA_GROUP_ID, DAO.Direction.ASC);
        orderAttrs.put(SpeciesToGroupOrderingAttribute.DISTANCE_TO_SPECIES, DAO.Direction.ASC);
        when(dao.getAllSpeciesToDataGroup(orderAttrs)).thenReturn(stdgResultSet);
        LinkedHashMap<SpeciesDataGroupDAO.OrderingAttribute, DAO.Direction> orderAttrs2 = new LinkedHashMap<>();
        orderAttrs2.put(SpeciesDataGroupDAO.OrderingAttribute.PREFERRED_ORDER, DAO.Direction.ASC);
        when(dao.getAllSpeciesDataGroup(null, orderAttrs2)).thenReturn(sdgResultSet);
        when(managerMock.getSpeciesDataGroupDAO()).thenReturn(dao);

        when(speciesService.loadSpeciesInDataGroups(false)).thenReturn(species);
        when(downloadFileService.getDownloadFiles(EnumSet.allOf(SpeciesDownloadFile.Category.class)))
        .thenReturn(downloadFiles);

		//expected values
        Set<SpeciesDownloadFile> groupFiles1 = new HashSet<>(Arrays.asList(df1,df2));
        SpeciesDataGroup g1 = new SpeciesDataGroup(22, "group1", "the group 1", Arrays.asList(v1,v2), groupFiles1);
        Set<SpeciesDownloadFile> groupFiles2 = new HashSet<>(Arrays.asList(df3));
        SpeciesDataGroup g2 = new SpeciesDataGroup(42, "group2","group2", Arrays.asList(v2), groupFiles2);
        List<SpeciesDataGroup> expDataGroups = Arrays.asList(g1,g2);

		// actual use of the service
		SpeciesDataGroupService service = new SpeciesDataGroupService(serviceFactory);
        List<SpeciesDataGroup> dataGroups = service.loadAllSpeciesDataGroup();
        Assert.assertEquals(2, dataGroups.size());
        Assert.assertEquals(expDataGroups, dataGroups);
	}

}
