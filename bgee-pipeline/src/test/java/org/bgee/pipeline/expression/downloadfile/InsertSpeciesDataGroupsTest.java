package org.bgee.pipeline.expression.downloadfile;

import static org.junit.Assert.fail;
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

import org.apache.commons.io.FileUtils;
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

/**
 * Unit tests for {@link InsertSpeciesDataGroups}.
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 *
 */
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
        tmpFolder1.deleteOnExit();
        File tmpFolder2 = testFolder.newFolder("folder2");
        tmpFolder2.deleteOnExit();
        File tmpFolder3 = testFolder.newFolder("folder3");
        tmpFolder3.deleteOnExit();
        File tmpFolder4 = testFolder.newFolder("sp2");
        tmpFolder4.deleteOnExit();
        
        String fileExtension = ".tsv.zip";

        // We create test files. We do not use createTempFile() because we cannot have 2 files
        // having the same category because of random number added in file name. Files will be
        // deleted because they are in a temporary folder, and we also called deleteOnExist 
        //on the directories used.
        File file1 = new File(tmpFolder1, "sp1_diffexpr-anatomy-complete" + fileExtension);
        file1.createNewFile();
        this.writeSeveralLines(file1);
        new RandomAccessFile(file1, "rw").setLength(10000);

        File file2 = new File(tmpFolder1, "sp1_diffexpr-anatomy-simple" + fileExtension);
        file2.createNewFile();
        this.writeOneLine(file2);
        new RandomAccessFile(file2, "rw").setLength(200);

        File file3 = new File(tmpFolder2, "sp1_Affymetrix_experiments_chips" + fileExtension);
        file3.createNewFile();
        this.writeSeveralLines(file3);
        new RandomAccessFile(file3, "rw").setLength(30000);

        File file4 = new File(tmpFolder4, "sp2_expr-complete" + fileExtension);
        file4.createNewFile();
        this.writeSeveralLines(file4);
        new RandomAccessFile(file4, "rw").setLength(40000);  

        File file5 = new File(tmpFolder4, "sp2_RNA-Seq_read_counts_RPKM_GSE41637" + fileExtension);
        file5.createNewFile();
        this.writeSeveralLines(file5);
        new RandomAccessFile(file5, "rw").setLength(50000);  

        File file6 = new File(tmpFolder3, "gp1_orthologs" + fileExtension);
        file6.createNewFile();
        this.writeSeveralLines(file6);
        new RandomAccessFile(file6, "rw").setLength(60000);

        File file7 = new File(tmpFolder3, "gp1_multi-diffexpr-anatomy-complete" + fileExtension);
        file7.createNewFile();
        this.writeSeveralLines(file7);
        new RandomAccessFile(file7, "rw").setLength(70000);

        File file8 = new File(tmpFolder3, "gp2_orthologs" + fileExtension);
        file8.createNewFile();
        this.writeSeveralLines(file8);
        new RandomAccessFile(file8, "rw").setLength(80000);

        File file9 = new File(tmpFolder3, "gp2_multi-diffexpr-development-simple" + fileExtension);
        file9.createNewFile();
        this.writeSeveralLines(file9);
        new RandomAccessFile(file9, "rw").setLength(90000); 

        // Create constructor parameters 
        LinkedHashMap<String, Set<String>> groupToSpecies = new LinkedHashMap<>();
        groupToSpecies.put("groupOneSpecies1", new HashSet<String>(Arrays.asList("sp1")));
        groupToSpecies.put("groupOneSpecies2", new HashSet<String>(Arrays.asList("sp2")));
        groupToSpecies.put("groupSeveralSpecies1", new HashSet<String>(Arrays.asList("sp1", "sp2")));
        groupToSpecies.put("groupSeveralSpecies2", new HashSet<String>(Arrays.asList("sp3", "sp2")));
        groupToSpecies.put("groupWithNoExistingFile", new HashSet<String>(Arrays.asList("sp3", "sp2")));
        
        Path tmpPath = testFolder.getRoot().toPath();
        LinkedHashMap<String, Set<String>> groupToCategories = new LinkedHashMap<>();
        groupToCategories.put("groupOneSpecies1", new HashSet<String>(Arrays.asList(
                CategoryEnum.DIFF_EXPR_ANAT_COMPLETE.getStringRepresentation(),
                CategoryEnum.DIFF_EXPR_ANAT_SIMPLE.getStringRepresentation(),
                CategoryEnum.AFFY_ANNOT.getStringRepresentation())));
        groupToCategories.put("groupOneSpecies2", new HashSet<String>(Arrays.asList(
                CategoryEnum.EXPR_CALLS_COMPLETE.getStringRepresentation(),
                CategoryEnum.RNASEQ_DATA.getStringRepresentation())));
        groupToCategories.put("groupSeveralSpecies1", new HashSet<String>(Arrays.asList(
                CategoryEnum.ORTHOLOG.getStringRepresentation(),
                CategoryEnum.DIFF_EXPR_ANAT_COMPLETE.getStringRepresentation())));
        groupToCategories.put("groupSeveralSpecies2", new HashSet<String>(Arrays.asList(
                CategoryEnum.ORTHOLOG.getStringRepresentation(),
                CategoryEnum.DIFF_EXPR_DEV_SIMPLE.getStringRepresentation())));
        groupToCategories.put("groupWithNoExistingFile", new HashSet<String>(Arrays.asList(
                CategoryEnum.DIFF_EXPR_DEV_SIMPLE.getStringRepresentation())));

        LinkedHashMap<String, String> groupToReplacement = new LinkedHashMap<>();
        groupToReplacement.put("groupOneSpecies1", "sp1");
        groupToReplacement.put("groupOneSpecies2", "sp2");
        groupToReplacement.put("groupSeveralSpecies1", "gp1");
        groupToReplacement.put("groupSeveralSpecies2", "gp2");
        groupToReplacement.put("groupWithNoExistingFile", "xx");

        Map<String, String> singleSpCategoryToFilePathPattern = new HashMap<String, String>();
        singleSpCategoryToFilePathPattern.put(CategoryEnum.DIFF_EXPR_ANAT_COMPLETE.getStringRepresentation(),
                tmpPath.relativize(file1.toPath()).toString()
                .replaceAll("sp1", InsertSpeciesDataGroups.STRING_TO_REPLACE));
        singleSpCategoryToFilePathPattern.put(CategoryEnum.DIFF_EXPR_ANAT_SIMPLE.getStringRepresentation(),
                tmpPath.relativize(file2.toPath()).toString()
                .replaceAll("sp1", InsertSpeciesDataGroups.STRING_TO_REPLACE));
        singleSpCategoryToFilePathPattern.put(CategoryEnum.AFFY_ANNOT.getStringRepresentation(),
                tmpPath.relativize(file3.toPath()).toString()
                .replaceAll("sp1", InsertSpeciesDataGroups.STRING_TO_REPLACE));
        singleSpCategoryToFilePathPattern.put(CategoryEnum.EXPR_CALLS_COMPLETE.getStringRepresentation(),
                tmpPath.relativize(file4.toPath()).toString()
                .replaceAll("sp2", InsertSpeciesDataGroups.STRING_TO_REPLACE));
        singleSpCategoryToFilePathPattern.put(CategoryEnum.RNASEQ_DATA.getStringRepresentation(),
                tmpPath.relativize(file5.toPath()).toString()
                .replaceAll("sp2", InsertSpeciesDataGroups.STRING_TO_REPLACE));

        Map<String, String> multiSpCategoryToFilePathPattern = new HashMap<String, String>();
        multiSpCategoryToFilePathPattern.put(CategoryEnum.ORTHOLOG.getStringRepresentation(),
                tmpPath.relativize(file6.toPath()).toString()
                .replaceAll("gp1", InsertSpeciesDataGroups.STRING_TO_REPLACE));
        multiSpCategoryToFilePathPattern.put(CategoryEnum.DIFF_EXPR_ANAT_COMPLETE.getStringRepresentation(),
                tmpPath.relativize(file7.toPath()).toString()
                .replaceAll("gp1", InsertSpeciesDataGroups.STRING_TO_REPLACE));
        multiSpCategoryToFilePathPattern.put(CategoryEnum.DIFF_EXPR_DEV_SIMPLE.getStringRepresentation(),
                tmpPath.relativize(file9.toPath()).toString()
                .replaceAll("gp2", InsertSpeciesDataGroups.STRING_TO_REPLACE));

        // Insert data
        InsertSpeciesDataGroups insert = new InsertSpeciesDataGroups(mockManager, 
                groupToSpecies, groupToCategories, groupToReplacement, singleSpCategoryToFilePathPattern, 
                multiSpCategoryToFilePathPattern, testFolder.getRoot().toString());
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
                        // file2 has less than 3 lines, so it is considered as empty
//                        new DownloadFileTO("2", file2.getName(), null, 
//                                tmpPath.relativize(file2.toPath()).toString(), 
//                                file2.length(), CategoryEnum.DIFF_EXPR_ANAT_SIMPLE, "1"),
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
        if (!TOComparator.areTOCollectionsEqual(
                expectedDownloadFileTOs, downloadFileTOsArg.getValue(), false)) {
            throw new AssertionError("Incorrect DownloadFileTOs generated, expected " + 
                    expectedDownloadFileTOs + ", but was " + downloadFileTOsArg.getValue());
        }
        
        // Tests with incorrect arguments provided
        Set<String> groupOneSpecies2 = null;
        try {
            // Store element to restore in proper state afterwards.
            groupOneSpecies2 = groupToCategories.remove("groupOneSpecies2");
            insert = new InsertSpeciesDataGroups(mockManager, groupToSpecies, groupToCategories,
                    groupToReplacement, singleSpCategoryToFilePathPattern,
                    multiSpCategoryToFilePathPattern, testFolder.getRoot().toString());
            fail("An exception should be thrown");
        } catch (IllegalArgumentException e) {
            /// Test passed, restore the removed element
            groupToCategories.put("groupOneSpecies2", groupOneSpecies2);
        }

        try {
            // Store element to restore in proper state afterwards.
            groupToCategories.get("groupOneSpecies2").remove(
                    CategoryEnum.RNASEQ_DATA.getStringRepresentation());
            insert = new InsertSpeciesDataGroups(mockManager, groupToSpecies, groupToCategories,
                    groupToReplacement, singleSpCategoryToFilePathPattern,
                    multiSpCategoryToFilePathPattern, testFolder.getRoot().toString());
            fail("An exception should be thrown");
        } catch (IllegalArgumentException e) {
            /// Test passed, restore the removed element
            groupToCategories.get("groupOneSpecies2").add(
                    CategoryEnum.RNASEQ_DATA.getStringRepresentation());
        }
    }
    
    /**
     * Write one line in {@code file}.
     * <p>
     * In fact, we obtain a file with two lines because of the default line separator is
     * added to the end of the line.
     * 
     * @param file          A {@code File} that is the file to be used to write line.
     * @throws IOException  If an error occurred while writing line.
     */
    private void writeOneLine(File file) throws IOException {
        FileUtils.writeLines(file, Arrays.asList("One line"));
    }

    /**
     * Write several lines in {@code file}.
     * 
     * @param file          A {@code File} that is the file to be used to write lines.
     * @throws IOException  If an error occurred while writing lines.
     */
    private void writeSeveralLines(File file) throws IOException {
        FileUtils.writeLines(file, Arrays.asList("First line", "Second line"));
    }
}
