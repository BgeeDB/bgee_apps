package org.bgee.model.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.file.DownloadFileDAO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTOResultSet;
import org.bgee.model.file.DownloadFile.CategoryEnum;
import org.junit.Test;

/**
 * This class holds unit tests for {@code DownloadFileService}
 * @author Philippe Moret
 *
 */
public class DownloadFileServiceTest extends TestAncestor {

	
	@Test
	public void testGetAllDownlaodFiles() {

		// initialize mocks
		DAOManager managerMock = mock(DAOManager.class);
		DownloadFileDAO downloadFileDaoMock = mock(DownloadFileDAO.class);
		DownloadFileTOResultSet resultSetMock = mock(DownloadFileTOResultSet.class);
		
		// DAO return values
		DownloadFileTO to1 = new DownloadFileTO("ID", "NAME", "DESC", "/tmp/foo", Long.valueOf(42),
		        org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO.CategoryEnum.AFFY_ANNOT, "22");
		DownloadFileTO to2 = new DownloadFileTO("ID2", "NAME2", "DESC2", "/tmp/foo", Long.valueOf(1337),
		        org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO.CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, "22");
		List<DownloadFileTO> tos = new ArrayList<>();
		tos.add(to1);
		tos.add(to2);

		// mock behavior
		when(resultSetMock.stream()).thenReturn(tos.stream());
		when(downloadFileDaoMock.getAllDownloadFiles()).thenReturn(resultSetMock);
		when(managerMock.getDownloadFileDAO()).thenReturn(downloadFileDaoMock);
		
		//expected values
		DownloadFile df1 = new DownloadFile("/tmp/foo", "NAME", CategoryEnum.AFFY_ANNOT, 42L, "22");
		DownloadFile df2 = new DownloadFile("/tmp/foo", "NAME2", CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, 1337L, "22");
		List<DownloadFile> expected = new ArrayList<>();
		expected.add(df1);
		expected.add(df2);
		
		// actual use of the service
		DownloadFileService service = new DownloadFileService(managerMock);
		List<DownloadFile> files = service.getAllDownloadFiles();
		
		assertEquals(expected, files);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testExceptionOnBadDataDownlaodFiles() {

		// initialize mocks
		DAOManager managerMock = mock(DAOManager.class);
		DownloadFileDAO downloadFileDaoMock = mock(DownloadFileDAO.class);
		DownloadFileTOResultSet resultSetMock = mock(DownloadFileTOResultSet.class);
		
		// mock return values
		DownloadFileTO to1 = new DownloadFileTO(null, null, "DESC2", "/tmp/foo", Long.valueOf(1337),
		        org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO.CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, null);
		List<DownloadFileTO> tos = new ArrayList<>();
		tos.add(to1);
		
		// mock behavior
		when(resultSetMock.stream()).thenReturn(tos.stream());
		when(downloadFileDaoMock.getAllDownloadFiles()).thenReturn(resultSetMock);
		when(managerMock.getDownloadFileDAO()).thenReturn(downloadFileDaoMock);
		
		// actual use of the service
		DownloadFileService service = new DownloadFileService(managerMock);
		service.getAllDownloadFiles();
		
		//should never reach this point
		fail("Should fail on bad data from the DAO");
	
	}

}
