package org.bgee.pipeline.expression.downloadfile;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
     * Test {@link InsertSpeciesDataGroups#insert()} for propagation of expression.
     * @throws IOException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldInsertGlobalExpression() throws IOException  {
        
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
        File file1 = File.createTempFile("sp1_diffexpr-anatomy-complete.tsv.zip", null, tmpFolder1);
        try (FileWriter fw = new FileWriter(file1)) {
            fw.write("sp1_diffexpr-anatomy-complete text");            
        }
        File file2 = File.createTempFile("sp1_diffexpr-anatomy-simple.tsv.zip", null, tmpFolder1);
        try (FileWriter fw = new FileWriter(file2)) {
            fw.write("sp1_diffexpr-anatomy-simple text");
        }
        File file3 = File.createTempFile("sp1_Affymetrix_experiments_chips.zip", null, tmpFolder2);
        try (FileWriter fw = new FileWriter(file3)) {
            fw.write("sp1_Affymetrix_experiments_chips text");
        }        
        File file4 = File.createTempFile("sp2_expr-complete.tsv.zip", null, tmpFolder1);
        try (FileWriter fw = new FileWriter(file4)) {
            fw.write("sp2_expr-complete text");
        }        
        File file5 = File.createTempFile("sp2_RNA-Seq_read_counts_RPKM_GSE41637.tsv.zip", null, tmpFolder1);
        try (FileWriter fw = new FileWriter(file5)) {
            fw.write("sp2_RNA-Seq_read_counts_RPKM_GSE41637 text");
        }        
        File file6 = File.createTempFile("gp1_orthologs.tsv.zip", null, tmpFolder3);
        try (FileWriter fw = new FileWriter(file6)) {
            fw.write("gp1_orthologs.tsv text");
        }        
        File file7 = File.createTempFile("gp1_multi-diffexpr-anatomy-complete.tsv.zip", null, tmpFolder3);
        try (FileWriter fw = new FileWriter(file7)) {
            fw.write("gp1_multi-diffexpr-anatomy-complete text");
        }        
        File file8 = File.createTempFile("gp2_orthologs.tsv.zip", null, tmpFolder3);
        try (FileWriter fw = new FileWriter(file8)) {
            fw.write("gp2_orthologs text");
        }        
        File file9 = File.createTempFile("gp2_multi-diffexpr-development-simple.tsv.zip", null, tmpFolder3);
        try (FileWriter fw = new FileWriter(file9)) {
            fw.write("gp2_multi-diffexpr-development-simple text");
        }        

        // Create constructor parameters 
        Map<String, Set<String>> groupToSpecies = new HashMap<String, Set<String>>();
        groupToSpecies.put("groupOneSpecies1", new HashSet<String>(Arrays.asList("sp1")));
        groupToSpecies.put("groupOneSpecies2", new HashSet<String>(Arrays.asList("sp2")));
        groupToSpecies.put("groupSeveralSpecies1", new HashSet<String>(Arrays.asList("sp1", "sp2")));
        groupToSpecies.put("groupSeveralSpecies2", new HashSet<String>(Arrays.asList("sp3", "sp2")));
        groupToSpecies.put("groupNoSpecies", new HashSet<String>());
        
        Map<String, Set<String>> groupToFilePaths = new HashMap<String, Set<String>>();
        groupToFilePaths.put("groupOneSpecies1", new HashSet<String>(Arrays.asList(
                file1.getAbsolutePath(), file2.getAbsolutePath(), file3.getAbsolutePath())));
        groupToFilePaths.put("groupOneSpecies2", new HashSet<String>(Arrays.asList(
                file4.getAbsolutePath(), file5.getAbsolutePath())));
        groupToFilePaths.put("groupSeveralSpecies1", new HashSet<String>(Arrays.asList(
                file6.getAbsolutePath(), file7.getAbsolutePath())));
        groupToFilePaths.put("groupSeveralSpecies2", new HashSet<String>(Arrays.asList(
                file8.getAbsolutePath(), file9.getAbsolutePath())));
        groupToFilePaths.put("groupNoSpecies", new HashSet<String>());


        Map<String, String> filePathToCategory = new HashMap<String, String>();
        filePathToCategory.put(file1.getAbsolutePath(), CategoryEnum.DIFF_EXPR_ANAT_COMPLETE.getStringRepresentation());
        filePathToCategory.put(file2.getAbsolutePath(), CategoryEnum.DIFF_EXPR_ANAT_SIMPLE.getStringRepresentation());
        filePathToCategory.put(file3.getAbsolutePath(), CategoryEnum.AFFY_ANNOT.getStringRepresentation());
        filePathToCategory.put(file4.getAbsolutePath(), CategoryEnum.EXPR_CALLS_COMPLETE.getStringRepresentation());
        filePathToCategory.put(file5.getAbsolutePath(), CategoryEnum.RNASEQ_DATA.getStringRepresentation());
        filePathToCategory.put(file6.getAbsolutePath(), CategoryEnum.ORTHOLOG.getStringRepresentation());
        filePathToCategory.put(file7.getAbsolutePath(), CategoryEnum.DIFF_EXPR_ANAT_COMPLETE.getStringRepresentation());
        filePathToCategory.put(file8.getAbsolutePath(), CategoryEnum.ORTHOLOG.getStringRepresentation());
        filePathToCategory.put(file9.getAbsolutePath(), CategoryEnum.DIFF_EXPR_DEV_SIMPLE.getStringRepresentation());

        // Insert data
        InsertSpeciesDataGroups insert = new InsertSpeciesDataGroups(mockManager, 
                groupToSpecies, groupToFilePaths, filePathToCategory, testFolder.getRoot().toString());
        insert.insert();

        // Verify arguments passed to insertSpeciesDataGroups()
        Set<SpeciesDataGroupTO> expectedSpeciesDataGroupTOs = 
                // It's fake IDs, they avoid to have one element only in the set
                new HashSet<SpeciesDataGroupTO>(Arrays.asList(
                        new SpeciesDataGroupTO("1", "groupOneSpecies1", null),
                        new SpeciesDataGroupTO("2", "groupOneSpecies2", null),
                        new SpeciesDataGroupTO("3", "groupSeveralSpecies1", null),
                        new SpeciesDataGroupTO("4", "groupSeveralSpecies2", null)));
        ArgumentCaptor<Set> speciesDataGroupTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockSpeciesDataGroupDAO).
            insertSpeciesDataGroups(speciesDataGroupTOsArg.capture());
        Set<SpeciesDataGroupTO> actualSpeciesDataGroupTOs = speciesDataGroupTOsArg.getValue();
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
        
        // Verify arguments passed to insertDownloadFiles()
        Set<DownloadFileTO> expectedDownloadFileTOs = 
                new HashSet<DownloadFileTO>(Arrays.asList(
                        new DownloadFileTO("1", file1.getName(), null, "folder1/" + file1.getName(), 
                                file1.length(), CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, "1"),
                        new DownloadFileTO("2", file2.getName(), null, "folder1/" + file2.getName(), 
                                file2.length(), CategoryEnum.DIFF_EXPR_ANAT_SIMPLE, "1"),
                        new DownloadFileTO("3", file3.getName(), null, "folder2/" + file3.getName(), 
                                file3.length(), CategoryEnum.AFFY_ANNOT, "1"),
                        new DownloadFileTO("4", file4.getName(), null, "folder1/" + file4.getName(), 
                                file4.length(), CategoryEnum.EXPR_CALLS_COMPLETE, "2"),
                        new DownloadFileTO("5", file5.getName(), null, "folder1/" + file5.getName(), 
                                file5.length(), CategoryEnum.RNASEQ_DATA, "2"),
                        new DownloadFileTO("6", file6.getName(), null, "folder3/" + file6.getName(), 
                                file6.length(), CategoryEnum.ORTHOLOG,  "3"),
                        new DownloadFileTO("7", file7.getName(), null, "folder3/" + file7.getName(), 
                                file7.length(), CategoryEnum.DIFF_EXPR_ANAT_COMPLETE, "3"),
                        new DownloadFileTO("8", file8.getName(), null, "folder3/" + file8.getName(), 
                                file8.length(), CategoryEnum.ORTHOLOG, "4"),
                        new DownloadFileTO("9", file9.getName(), null, "folder3/" + file9.getName(), 
                                file9.length(), CategoryEnum.DIFF_EXPR_DEV_SIMPLE, "4")));
        ArgumentCaptor<Set> downloadFileTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockDownloadFileDAO).insertDownloadFiles(downloadFileTOsArg.capture());
        if (!TOComparator.areTOCollectionsEqual(expectedDownloadFileTOs, downloadFileTOsArg.getValue(), false)) {
            throw new AssertionError("Incorrect DownloadFileTOs generated, expected " + 
                    expectedDownloadFileTOs + ", but was " + downloadFileTOsArg.getValue());
        }
    }
}
