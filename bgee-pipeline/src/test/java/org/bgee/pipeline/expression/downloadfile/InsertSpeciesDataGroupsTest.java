package org.bgee.pipeline.expression.downloadfile;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO.CategoryEnum;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesDataGroupTO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesToDataGroupTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO.MySQLSpeciesTOResultSet;
import org.bgee.pipeline.TestAncestor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
public class InsertSpeciesDataGroupsTest extends TestAncestor {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(InsertSpeciesDataGroupsTest.class.getName());
    
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    /**
     * Default Constructor. 
     */
    public InsertSpeciesDataGroupsTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link InsertSpeciesDataGroups#insert()}.
     * @throws IOException 
     */
    //Suppress warnings caused by the use of Mockito ArgumentCaptor. 
    //suppress warnings caused by unclosed RandomAccessFile in temp folder. 
    @SuppressWarnings({ "unchecked", "rawtypes", "resource" })
    @Test
    public void shouldInsertSpeciesDataGroups() throws IOException  {
        
        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        MockDAOManager mockManager = new MockDAOManager();

        // Mock the call to MySQLSpeciesTO
        MySQLSpeciesTOResultSet mockSpeciesTORs = createMockDAOResultSet(Arrays.asList(
                new SpeciesTO("sp1", null, null, null, null, null, null, null),
                new SpeciesTO("sp2", null, null, null, null, null, null, null),
                new SpeciesTO("sp3", null, null, null, null, null, null, null)),
                MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getAllSpecies()).thenReturn(mockSpeciesTORs);

        // Create temporary folders and files. 
        File tmpFolder1 = testFolder.newFolder("folder1");
        File tmpFolder2 = testFolder.newFolder("folder2");
        File tmpFolder3 = testFolder.newFolder("folder3");
        String fileExtension = ".tsv.zip";
        File file1 = File.createTempFile("sp1_diffexpr-anatomy-complete", fileExtension, tmpFolder1);
        new RandomAccessFile(file1, "rw").setLength(100);
        
        File file2 = File.createTempFile("sp1_diffexpr-anatomy-simple", fileExtension, tmpFolder1);
        new RandomAccessFile(file2, "rw").setLength(200);
        
        File file3 = File.createTempFile("sp1_Affymetrix_experiments_chips", fileExtension, tmpFolder2);
        new RandomAccessFile(file3, "rw").setLength(300);
        
        File file4 = File.createTempFile("sp2_expr-complete", fileExtension, tmpFolder1);
        new RandomAccessFile(file4, "rw").setLength(400);  
        
        File file5 = File.createTempFile("sp2_RNA-Seq_read_counts_RPKM_GSE41637", fileExtension, tmpFolder1);
        new RandomAccessFile(file5, "rw").setLength(500);  
        
        File file6 = File.createTempFile("gp1_orthologs", fileExtension, tmpFolder3);
        new RandomAccessFile(file6, "rw").setLength(600);
        
        File file7 = File.createTempFile("gp1_multi-diffexpr-anatomy-complete", fileExtension, tmpFolder3);
        new RandomAccessFile(file7, "rw").setLength(700);
        
        File file8 = File.createTempFile("gp2_orthologs", fileExtension, tmpFolder3);
        new RandomAccessFile(file8, "rw").setLength(800);
        
        File file9 = File.createTempFile("gp2_multi-diffexpr-development-simple", fileExtension, tmpFolder3);
        new RandomAccessFile(file9, "rw").setLength(900);      

        // Create constructor parameters 
        LinkedHashMap<String, Set<String>> groupToSpecies = new LinkedHashMap<>();
        groupToSpecies.put("groupOneSpecies1", new HashSet<String>(Arrays.asList("sp1")));
        groupToSpecies.put("groupOneSpecies2", new HashSet<String>(Arrays.asList("sp2")));
        groupToSpecies.put("groupSeveralSpecies1", new HashSet<String>(Arrays.asList("sp1", "sp2")));
        groupToSpecies.put("groupSeveralSpecies2", new HashSet<String>(Arrays.asList("sp3", "sp2")));
        
        Path tmpPath = testFolder.getRoot().toPath();
        LinkedHashMap<String, Set<String>> groupToFilePaths = new LinkedHashMap<>();
        groupToFilePaths.put("groupOneSpecies1", new HashSet<String>(Arrays.asList(
                tmpPath.relativize(file1.toPath()).toString(), 
                tmpPath.relativize(file2.toPath()).toString(), 
                tmpPath.relativize(file3.toPath()).toString())));
        groupToFilePaths.put("groupOneSpecies2", new HashSet<String>(Arrays.asList(
                tmpPath.relativize(file4.toPath()).toString(), 
                tmpPath.relativize(file5.toPath()).toString())));
        groupToFilePaths.put("groupSeveralSpecies1", new HashSet<String>(Arrays.asList(
                tmpPath.relativize(file6.toPath()).toString(), 
                tmpPath.relativize(file7.toPath()).toString())));
        groupToFilePaths.put("groupSeveralSpecies2", new HashSet<String>(Arrays.asList(
                tmpPath.relativize(file8.toPath()).toString(), 
                tmpPath.relativize(file9.toPath()).toString())));


        Map<String, String> filePathToCategory = new HashMap<String, String>();
        filePathToCategory.put(tmpPath.relativize(file1.toPath()).toString(), 
                CategoryEnum.DIFF_EXPR_ANAT_COMPLETE.getStringRepresentation());
        filePathToCategory.put(tmpPath.relativize(file2.toPath()).toString(), 
                CategoryEnum.DIFF_EXPR_ANAT_SIMPLE.getStringRepresentation());
        filePathToCategory.put(tmpPath.relativize(file3.toPath()).toString(), 
                CategoryEnum.AFFY_ANNOT.getStringRepresentation());
        filePathToCategory.put(tmpPath.relativize(file4.toPath()).toString(), 
                CategoryEnum.EXPR_CALLS_COMPLETE.getStringRepresentation());
        filePathToCategory.put(tmpPath.relativize(file5.toPath()).toString(), 
                CategoryEnum.RNASEQ_DATA.getStringRepresentation());
        filePathToCategory.put(tmpPath.relativize(file6.toPath()).toString(), 
                CategoryEnum.ORTHOLOG.getStringRepresentation());
        filePathToCategory.put(tmpPath.relativize(file7.toPath()).toString(), 
                CategoryEnum.DIFF_EXPR_ANAT_COMPLETE.getStringRepresentation());
        filePathToCategory.put(tmpPath.relativize(file8.toPath()).toString(), 
                CategoryEnum.ORTHOLOG.getStringRepresentation());
        filePathToCategory.put(tmpPath.relativize(file9.toPath()).toString(), 
                CategoryEnum.DIFF_EXPR_DEV_SIMPLE.getStringRepresentation());

        // Insert data
        InsertSpeciesDataGroups insert = new InsertSpeciesDataGroups(mockManager, 
                groupToSpecies, groupToFilePaths, filePathToCategory, testFolder.getRoot().toString());
        insert.insert();

        // Verify arguments passed to insertSpeciesDataGroups()
        Set<SpeciesDataGroupTO> expectedSpeciesDataGroupTOs = 
                // It's fake IDs, they avoid to have one element only in the set
                new HashSet<SpeciesDataGroupTO>(Arrays.asList(
                        new SpeciesDataGroupTO("1", "groupOneSpecies1", null, 1),
                        new SpeciesDataGroupTO("2", "groupOneSpecies2", null, 2),
                        new SpeciesDataGroupTO("3", "groupSeveralSpecies1", null, 3),
                        new SpeciesDataGroupTO("4", "groupSeveralSpecies2", null, 4)));
        ArgumentCaptor<Set> speciesDataGroupTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockSpeciesDataGroupDAO).
            insertSpeciesDataGroups(speciesDataGroupTOsArg.capture());
        Collection<SpeciesDataGroupTO> actualSpeciesDataGroupTOs = speciesDataGroupTOsArg.getValue();
        if (!TOComparator.areTOCollectionsEqual(
                expectedSpeciesDataGroupTOs, actualSpeciesDataGroupTOs, false)) {
            throw new AssertionError("Incorrect SpeciesDataGroupTOs generated, expected " + 
                    expectedSpeciesDataGroupTOs + ", but was " + speciesDataGroupTOsArg.getValue());
        }

        // Verify arguments passed to insertSpeciesToDataGroup()
        Set<SpeciesToDataGroupTO> expectedSpeciesToDataGroupTOs = 
                new HashSet<SpeciesToDataGroupTO>(Arrays.asList(
                        new SpeciesToDataGroupTO("sp1", "1"),
                        new SpeciesToDataGroupTO("sp2", "2"),
                        new SpeciesToDataGroupTO("sp1", "3"),
                        new SpeciesToDataGroupTO("sp2", "3"),
                        new SpeciesToDataGroupTO("sp2", "4"),
                        new SpeciesToDataGroupTO("sp3", "4")));
        ArgumentCaptor<Set> speciesToDataGroupTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockSpeciesDataGroupDAO).
            insertSpeciesToDataGroup(speciesToDataGroupTOsArg.capture());
        if (!TOComparator.areTOCollectionsEqual(
                expectedSpeciesToDataGroupTOs, speciesToDataGroupTOsArg.getValue())) {
            throw new AssertionError("Incorrect SpeciesToDataGroupTOs generated, expected " + 
                expectedSpeciesToDataGroupTOs + ", but was " + speciesToDataGroupTOsArg.getValue());
        }
        
        // Verify arguments passed to insertDownloadFiles(). The ID generation is random, 
        // so we can't verify them, but we need to set them anyway, 
        // to be properly stored in the Set (equals/hashCode solely based on ID).
        Set<DownloadFileTO> expectedDownloadFileTOs = 
                new HashSet<DownloadFileTO>(Arrays.asList(
                        new DownloadFileTO("1", file1.getName(), null, 
                                tmpPath.relativize(file1.toPath()).toString(), 
                                file1.length(), CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, "1"),
                        new DownloadFileTO("2", file2.getName(), null, 
                                tmpPath.relativize(file2.toPath()).toString(), 
                                file2.length(), CategoryEnum.DIFF_EXPR_ANAT_SIMPLE, "1"),
                        new DownloadFileTO("3", file3.getName(), null, 
                                tmpPath.relativize(file3.toPath()).toString(), 
                                file3.length(), CategoryEnum.AFFY_ANNOT, "1"),
                        new DownloadFileTO("4", file4.getName(), null, 
                                tmpPath.relativize(file4.toPath()).toString(),  
                                file4.length(), CategoryEnum.EXPR_CALLS_COMPLETE, "2"),
                        new DownloadFileTO("5", file5.getName(), null, 
                                tmpPath.relativize(file5.toPath()).toString(),  
                                file5.length(), CategoryEnum.RNASEQ_DATA, "2"),
                        new DownloadFileTO("6", file6.getName(), null, 
                                tmpPath.relativize(file6.toPath()).toString(),  
                                file6.length(), CategoryEnum.ORTHOLOG,  "3"),
                        new DownloadFileTO("7", file7.getName(), null, 
                                tmpPath.relativize(file7.toPath()).toString(),  
                                file7.length(), CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, "3"),
                        new DownloadFileTO("8", file8.getName(), null, 
                                tmpPath.relativize(file8.toPath()).toString(),  
                                file8.length(), CategoryEnum.ORTHOLOG, "4"),
                        new DownloadFileTO("9", file9.getName(), null, 
                                tmpPath.relativize(file9.toPath()).toString(), 
                                file9.length(), CategoryEnum.DIFF_EXPR_DEV_SIMPLE, "4")));
        ArgumentCaptor<Set> downloadFileTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockDownloadFileDAO).insertDownloadFiles(downloadFileTOsArg.capture());
        if (!TOComparator.areTOCollectionsEqual(expectedDownloadFileTOs, downloadFileTOsArg.getValue(), false)) {
            throw new AssertionError("Incorrect DownloadFileTOs generated, expected " + 
                    expectedDownloadFileTOs + ", but was " + downloadFileTOsArg.getValue());
        }
    }
}
