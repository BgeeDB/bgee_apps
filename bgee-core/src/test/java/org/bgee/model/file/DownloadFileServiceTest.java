package org.bgee.model.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.file.DownloadFileDAO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTOResultSet;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.file.DownloadFile.CategoryEnum;
import org.junit.Test;

/**
 * This class holds unit tests for {@code DownloadFileService}
 * @author Philippe Moret
 * @author Frederic Bastian
 * @version Bgee 15, Oct. 2021
 */
public class DownloadFileServiceTest extends TestAncestor {

	
	@Test
	public void testGetAllDownlaodFiles() {

		// initialize mocks
		DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
		DownloadFileDAO downloadFileDaoMock = mock(DownloadFileDAO.class);
		DownloadFileTOResultSet resultSetMock = getMockResultSet(DownloadFileTOResultSet.class, 
		        Arrays.asList(new DownloadFileTO(1, "NAME", "DESC", "/tmp/foo", Long.valueOf(42),
		                          DownloadFileTO.CategoryEnum.AFFY_ANNOT, 22,
		                          Arrays.asList(ConditionDAO.Attribute.ANAT_ENTITY_ID)), 
		                      new DownloadFileTO(2, "NAME2", "DESC2", "/tmp/foo", Long.valueOf(1337),
		                          DownloadFileTO.CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, 22,
		                          Arrays.asList(ConditionDAO.Attribute.ANAT_ENTITY_ID,
		                                  ConditionDAO.Attribute.STAGE_ID))));

		// mock behavior
		when(downloadFileDaoMock.getDownloadFiles(null)).thenReturn(resultSetMock);
		when(managerMock.getDownloadFileDAO()).thenReturn(downloadFileDaoMock);
		
		//expected values
		List<DownloadFile> expected = Arrays.asList(
		        new DownloadFile("/tmp/foo", "NAME", CategoryEnum.AFFY_ANNOT, 42L, 22,
		                Arrays.asList(CallService.Attribute.ANAT_ENTITY_ID)), 
		        new DownloadFile("/tmp/foo", "NAME2", CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, 1337L, 22,
		                Arrays.asList(CallService.Attribute.ANAT_ENTITY_ID,
		                        CallService.Attribute.DEV_STAGE_ID)));
		
		// actual use of the service
		DownloadFileService service = new DownloadFileService(serviceFactory);
		List<DownloadFile> files = service.getDownloadFiles(null);
		
		assertEquals(expected, files);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testExceptionOnBadDataDownlaodFiles() {

		// initialize mocks
		DAOManager managerMock = mock(DAOManager.class);
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.getDAOManager()).thenReturn(managerMock);
		DownloadFileDAO downloadFileDaoMock = mock(DownloadFileDAO.class);
		DownloadFileTOResultSet resultSetMock = getMockResultSet(DownloadFileTOResultSet.class, 
		        //provide incorrect DownloadFileTO will some values null.
		        Arrays.asList(new DownloadFileTO(null, null, "DESC2", "/tmp/foo", Long.valueOf(1337),
                DownloadFileTO.CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, null, null)));
		
		// mock behavior
		when(downloadFileDaoMock.getDownloadFiles(null)).thenReturn(resultSetMock);
		when(managerMock.getDownloadFileDAO()).thenReturn(downloadFileDaoMock);
		
		// actual use of the service
		DownloadFileService service = new DownloadFileService(serviceFactory);
		service.getDownloadFiles(null);
		
		//should never reach this point
		fail("Should fail on bad data from the DAO");
	
	}

}
