package org.bgee.model.dao.mysql.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.file.DownloadFileDAO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO.CategoryEnum;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * The integration tests for the {@link MySQLDownloadFileDAO} class.
 * 
 * @author Philippe Moret
 * @author Valentine Rech de Laval
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
                new DownloadFileDAO.DownloadFileTO(1, "file1.zip", "this is file1", "/dir/to/file1", 0L, 
                        DownloadFileDAO.DownloadFileTO.CategoryEnum.EXPR_CALLS_SIMPLE, 1, null),
                new DownloadFileDAO.DownloadFileTO(2, "file2.zip", "this is file2", "/dir/to/file2", 0L, 
                        DownloadFileDAO.DownloadFileTO.CategoryEnum.EXPR_CALLS_SIMPLE, 2, null),
                new DownloadFileDAO.DownloadFileTO(3, "file3.zip", null, "/dir/to/file3", 10L, 
                        DownloadFileDAO.DownloadFileTO.CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, 2, null)
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
                new DownloadFileDAO.DownloadFileTO(null, null, "this is file1", null, 0L, null,1, null),
                new DownloadFileDAO.DownloadFileTO(null, null, "this is file2", null, 0L, null, 2, null),
                new DownloadFileDAO.DownloadFileTO(null, null, null, null, 10L, null, 2, null)
        );

        assertTrue("DownloadFileTOs are incorrectly retrieved\nGOT\n"+allDownloadFiles+"\nEXPECTED\n"
                        +expectedDownloadFiles, TOComparator.areTOCollectionsEqual(allDownloadFiles,
                expectedDownloadFiles));
    }

    /**
     * Test the {@link MySQLDownloadFileDAO#insertDownloadFiles()} method.
     * 
     * @throws SQLException if an error happens with the MySQL database
     */
    @Test
    public void testInsertDownloadFiles() throws SQLException {
        this.useEmptyDB();
        
        //create a Collection of DownloadFileTOs to be inserted
        Collection<DownloadFileTO> fileTOs = Arrays.asList(
                new DownloadFileTO(1, "file name 1", "file desc 1", "path/file1", 6L,
                        CategoryEnum.EXPR_CALLS_SIMPLE, 11, null),
                new DownloadFileTO(2, "file name 2", "file desc 2", "path/file2/xx", 2L,
                        CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, 11, null),
                new DownloadFileTO(3, "file name 3", "file desc 3", "path/file3", 10L,
                        CategoryEnum.AFFY_ANNOT, 22, null));
        try {
            MySQLDownloadFileDAO dao = new MySQLDownloadFileDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertDownloadFiles(fileTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            //This test method could be better written (DRY, ...)
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("SELECT 1 FROM downloadFile WHERE downloadFileId = ? AND "
                            + "downloadFileName = ? AND downloadFileDescription = ? AND "
                            + "downloadFileRelativePath = ? AND downloadFileSize = ? AND downloadFileCategory = ? AND "
                            + "speciesDataGroupId = ?")) {
                
                stmt.setInt(1, 1);
                stmt.setString(2, "file name 1");
                stmt.setString(3, "file desc 1");
                stmt.setString(4, "path/file1");
                stmt.setLong(5, 6L);
                stmt.setEnumDAOField(6, CategoryEnum.EXPR_CALLS_SIMPLE);
                stmt.setInt(7, 11);
                assertTrue("DownloadFileTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 2);
                stmt.setString(2, "file name 2");
                stmt.setString(3, "file desc 2");
                stmt.setString(4, "path/file2/xx");
                stmt.setLong(5, 2L);
                stmt.setEnumDAOField(6, CategoryEnum.DIFF_EXPR_ANAT_COMPLETE);
                stmt.setInt(7, 11);
                assertTrue("DownloadFileTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 3);
                stmt.setString(2, "file name 3");
                stmt.setString(3, "file desc 3");
                stmt.setString(4, "path/file3");
                stmt.setLong(5, 10L);
                stmt.setEnumDAOField(6, CategoryEnum.AFFY_ANNOT);
                stmt.setInt(7, 22);
                assertTrue("DownloadFileTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            try {                
                dao.insertDownloadFiles(new HashSet<DownloadFileTO>());
                fail("No IllegalArgumentException was thrown while no DownloadFileTO was provided"); 
            } catch (IllegalArgumentException e) {
                // Test passed
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
}
