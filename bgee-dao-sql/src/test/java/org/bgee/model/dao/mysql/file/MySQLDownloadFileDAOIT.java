package org.bgee.model.dao.mysql.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.file.DownloadFileDAO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Philippe Moret
 */
public class MySQLDownloadFileDAOIT extends MySQLITAncestor {

    private final static Logger log = LogManager.getLogger(MySQLDownloadFileDAOIT.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Tests the {@link MySQLDownloadFileDAO#getAllDownloadFiles()} methods
     * @throws SQLException if an error happens with the MySQL database
     */
    @Test
    public void testGetAllDownloadFiles() throws SQLException {
        this.useSelectDB();

        DownloadFileDAO dao = new MySQLDownloadFileDAO(this.getMySQLDAOManager());

        List<DownloadFileDAO.DownloadFileTO> allDownloadFiles = dao.getAllDownloadFiles().getAllTOs();
        List<DownloadFileDAO.DownloadFileTO> expectedDownloadFiles = Arrays.asList(
                new DownloadFileDAO.DownloadFileTO("1", "file1.zip", "this is file1", "/dir/to/file1", "0", 
                        DownloadFileDAO.DownloadFileTO.CategoryEnum.EXPR_CALLS,"1"),
                new DownloadFileDAO.DownloadFileTO("2", "file2.zip", "this is file2", "/dir/to/file2", "0", 
                        DownloadFileDAO.DownloadFileTO.CategoryEnum.EXPR_CALLS,"2")
        );

        assertTrue("DownloadFileTOs are incorrectly retrieved\nGOT\n"+allDownloadFiles+"\nEXPECTED\n"+expectedDownloadFiles,
                TOComparator.areTOCollectionsEqual(allDownloadFiles, expectedDownloadFiles));
    }

    /**
     * Tests the {@link MySQLDownloadFileDAO#getAllDownloadFiles()} methods
     * @throws SQLException if an error happens with the MySQL database
     */
    @Test
    public void testGetAllDownloadFiles2() throws SQLException {
        this.useSelectDB();

        DownloadFileDAO dao = new MySQLDownloadFileDAO(this.getMySQLDAOManager());
        dao.setAttributes(new DownloadFileDAO.Attribute[]{DownloadFileDAO.Attribute.DESCRIPTION, DownloadFileDAO
                .Attribute.FILE_SIZE, DownloadFileDAO.Attribute.SPECIES_DATA_GROUP_ID});
        List<DownloadFileDAO.DownloadFileTO> allDownloadFiles = dao.getAllDownloadFiles().getAllTOs();
        List<DownloadFileDAO.DownloadFileTO> expectedDownloadFiles = Arrays.asList(
                new DownloadFileDAO.DownloadFileTO(null, null, "this is file1", null, "0",
                        null,"1"),
                new DownloadFileDAO.DownloadFileTO(null, null, "this is file2", null,  "0", null,"2")
        );

        assertTrue("DownloadFileTOs are incorrectly retrieved\nGOT\n"+allDownloadFiles+"\nEXPECTED\n"
                        +expectedDownloadFiles, TOComparator.areTOCollectionsEqual(allDownloadFiles,
                expectedDownloadFiles));
    }



}
