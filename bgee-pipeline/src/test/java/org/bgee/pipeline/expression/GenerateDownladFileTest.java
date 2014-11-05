package org.bgee.pipeline.expression;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.mysql.anatdev.MySQLAnatEntityDAO.MySQLAnatEntityTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLExpressionCallDAO.MySQLExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLNoExpressionCallDAO.MySQLNoExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO.MySQLSpeciesTOResultSet;
import org.bgee.pipeline.BgeeDBUtilsTest;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.expression.GenerateDownladFile.ExpressionData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;


public class GenerateDownladFileTest  extends TestAncestor {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(GenerateDownladFileTest.class.getName());

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    public GenerateDownladFileTest(){
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link GenerateDownladFile#generateSingleSpeciesFiles(List, List, String)},
     * which is the central method of the class doing all the job.
     * @throws IOException 
     */
//    @Test
    public void shouldGenerateSingleSpeciesFiles() throws IOException {

        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        // This will allow to verify that the correct values were tried to be inserted 
        // into the database.
        MockDAOManager mockManager = new MockDAOManager();

        MySQLSpeciesTOResultSet mockSpeciesTORs = createMockDAOResultSet(
                Arrays.asList(
                        new SpeciesTO("11", null, null, null, null, null, null, null),
                        new SpeciesTO("21", null, null, null, null, null, null, null)),
                        MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getAllSpecies()).thenReturn(mockSpeciesTORs);

        // For each species, we need to mock getNonInformativeAnatEntities(), getExpressionCalls() 
        // and getNoExpressionCalls()
        
        // Species 11
        Set<String> setSpeciesIds = new HashSet<String>(); 
        setSpeciesIds.add("11");
        List<String> listSpeciesIds = Arrays.asList("11"); 

        MySQLAnatEntityTOResultSet mockAnatEntityRsSp11 = createMockDAOResultSet(
                Arrays.asList(
                        //TODO add test data. Attributes to fill: ID
                        new AnatEntityTO("", null, null, null, null, false),
                        new AnatEntityTO("", null, null, null, null, false)),
                        MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.getNonInformativeAnatEntities(eq(setSpeciesIds))).
                thenReturn(mockAnatEntityRsSp11);

        MySQLExpressionCallTOResultSet mockExprRsSp11 = createMockDAOResultSet(
                //TODO correct test data. Attributes to fill: GENEID, STAGEID, ANATENTITYID, AFFYMETRIXDATA, 
                // ESTDATA, INSITUDATA, RNASEQDATA);
                Arrays.asList(
                        new ExpressionCallTO(null, "ID1", "Anat_id4", "Stage_id6", 
                                DataState.NODATA, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                false, false, ExpressionCallTO.OriginOfLine.SELF),
                        new ExpressionCallTO(null, "ID3", "Anat_id1", "Stage_id7", 
                                DataState.NODATA, DataState.HIGHQUALITY, 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
                                false, false, ExpressionCallTO.OriginOfLine.SELF)),
                        MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(listSpeciesIds);
        params.setUseAnatDescendants(true);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(params))).
                thenReturn(mockExprRsSp11);

        MySQLNoExpressionCallTOResultSet mockNoExprRsSp11 = createMockDAOResultSet(
                //TODO correct test data. Attributes to fill: GENEID, DEVSTAGEID, ANATENTITYID, 
                //              AFFYMETRIXDATA, INSITUDATA, RNASEQDATA.
                Arrays.asList(
                        new NoExpressionCallTO(null, "ID1", "Anat_id4", "Stage_id6", 
                                DataState.NODATA, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                false, NoExpressionCallTO.OriginOfLine.SELF),
                        new NoExpressionCallTO(null, "ID3", "Anat_id1", "Stage_id7", 
                                DataState.NODATA, DataState.HIGHQUALITY, 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
                                false, NoExpressionCallTO.OriginOfLine.SELF)),
                        MySQLNoExpressionCallTOResultSet.class);
        NoExpressionCallParams noExprParams = new NoExpressionCallParams();
        params.addAllSpeciesIds(listSpeciesIds);
        params.setUseAnatDescendants(true);
        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                (NoExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(noExprParams))).
                thenReturn(mockNoExprRsSp11);

        // Species 22
        setSpeciesIds.clear(); 
        setSpeciesIds.add("22");
        listSpeciesIds = Arrays.asList("22"); 

        MySQLAnatEntityTOResultSet mockAnatEntityRsSp22 = createMockDAOResultSet(
                Arrays.asList(
                        //TODO add test data. Attributes to fill: ID
                        new AnatEntityTO("", null, null, null, null, false),
                        new AnatEntityTO("", null, null, null, null, false)),
                        MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.getNonInformativeAnatEntities(eq(setSpeciesIds))).
                thenReturn(mockAnatEntityRsSp22);

        MySQLExpressionCallTOResultSet mockExprRsSp22 = createMockDAOResultSet(
                //TODO correct test data. Attributes to fill: GENEID, STAGEID, ANATENTITYID, AFFYMETRIXDATA, 
                // ESTDATA, INSITUDATA, RNASEQDATA.
                Arrays.asList(
                        new ExpressionCallTO(null, "ID1", "Anat_id4", "Stage_id6", 
                                DataState.NODATA, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                false, false, ExpressionCallTO.OriginOfLine.SELF),
                        new ExpressionCallTO(null, "ID3", "Anat_id1", "Stage_id7", 
                                DataState.NODATA, DataState.HIGHQUALITY, 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
                                false, false, ExpressionCallTO.OriginOfLine.SELF)),
                        MySQLExpressionCallTOResultSet.class);
        params = new ExpressionCallParams();
        params.addAllSpeciesIds(listSpeciesIds);
        params.setUseAnatDescendants(true);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(params))).
                thenReturn(mockExprRsSp22);

        MySQLNoExpressionCallTOResultSet mockNoExprRsSp22 = createMockDAOResultSet(
                //TODO correct test data. Attributes to fill: GENEID, DEVSTAGEID, ANATENTITYID, 
                //              AFFYMETRIXDATA, INSITUDATA, RNASEQDATA.
                Arrays.asList(
                        new NoExpressionCallTO(null, "ID1", "Anat_id4", "Stage_id6", 
                                DataState.NODATA, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                false, NoExpressionCallTO.OriginOfLine.SELF),
                        new NoExpressionCallTO(null, "ID3", "Anat_id1", "Stage_id7", 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
                                DataState.NODATA, DataState.HIGHQUALITY, 
                                false, NoExpressionCallTO.OriginOfLine.SELF)),
                        MySQLNoExpressionCallTOResultSet.class);
        noExprParams = new NoExpressionCallParams();
        params.addAllSpeciesIds(listSpeciesIds);
        params.setUseAnatDescendants(true);
        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                (NoExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(noExprParams))).
                thenReturn(mockNoExprRsSp22);

        GenerateDownladFile generate = new GenerateDownladFile(mockManager);
        List<String> fileTypes = 
                Arrays.asList(GenerateDownladFile.EXPR_SIMPLE, GenerateDownladFile.EXPR_COMPLETE);
        generate.generateSingleSpeciesFiles(
                Arrays.asList("11", "22"), fileTypes, testFolder.newFolder("tmpFolder").getPath());

        // TODO test differential expression
    }

    /**
     * Test {@link GenerateDownladFile#writeDownloadFiles(List<Map<String, String>>, String, String)}.
     * @throws IOException 
     */
    @Test
    public void shouldWriteDownloadFiles() throws IOException {

        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        // This will allow to verify that the correct values were tried to be inserted 
        // into the database.
        MockDAOManager mockManager = new MockDAOManager();

        GenerateDownladFile generate = new GenerateDownladFile(mockManager);

        String outputSimpleFile = testFolder.newFile("simpleFile.tsv").getPath();
        String outputCompleteFile = testFolder.newFile("completeFile.tsv").getPath();

        // Generate input data
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        //  1/ no data - no data - no data - no data >> no data
        list.add(generateDataMap("Gid1", "Gname1", "STid1", "STname1", "AEid1", "AEname1",
                GenerateDownladFile.ExpressionData.NODATA, 
                GenerateDownladFile.ExpressionData.NODATA,
                GenerateDownladFile.ExpressionData.NODATA,
                GenerateDownladFile.ExpressionData.NODATA));
        //  2/ no data - no expr - no data - no expr >> no expr
        list.add(generateDataMap("Gid2", "Gname2", "STid2", "STname2", "AEid2", "AEname2",
                GenerateDownladFile.ExpressionData.NODATA, 
                GenerateDownladFile.ExpressionData.NOEXPRESSION,
                GenerateDownladFile.ExpressionData.NODATA,
                GenerateDownladFile.ExpressionData.NOEXPRESSION));
        //  3/ no data - low - low - no data >> low
        list.add(generateDataMap("Gid3", "Gname3", "STid3", "STname3", "AEid3", "AEname3",
                GenerateDownladFile.ExpressionData.NODATA, 
                GenerateDownladFile.ExpressionData.LOWEXPRESSION,
                GenerateDownladFile.ExpressionData.LOWEXPRESSION,
                GenerateDownladFile.ExpressionData.NODATA));
        //  4/ high - high - no data - no data >> high
        list.add(generateDataMap("Gid4", "Gname4", "STid4", "STname4", "AEid4", "AEname4",
                GenerateDownladFile.ExpressionData.HIGHEXPRESSION, 
                GenerateDownladFile.ExpressionData.HIGHEXPRESSION,
                GenerateDownladFile.ExpressionData.NODATA,
                GenerateDownladFile.ExpressionData.NODATA));
        //  5/ no data - no expr - low - no expr >> ambiguous
        list.add(generateDataMap("Gid5", "Gname5", "STid", "STname", "AEid", "AEname",
                GenerateDownladFile.ExpressionData.NODATA, 
                GenerateDownladFile.ExpressionData.NOEXPRESSION,
                GenerateDownladFile.ExpressionData.LOWEXPRESSION,
                GenerateDownladFile.ExpressionData.NOEXPRESSION));
        //  6/ no data - no expr - high - high >> ambiguous
        list.add(generateDataMap("Gid6", "Gname6", "STid", "STname", "AEid", "AEname",
                GenerateDownladFile.ExpressionData.NODATA, 
                GenerateDownladFile.ExpressionData.NOEXPRESSION,
                GenerateDownladFile.ExpressionData.HIGHEXPRESSION,
                GenerateDownladFile.ExpressionData.HIGHEXPRESSION));
        //  7/ no data - no expr - low - high >> ambiguous
        list.add(generateDataMap("Gid7", "Gname7", "STid", "STname", "AEid", "AEname",
                GenerateDownladFile.ExpressionData.NODATA, 
                GenerateDownladFile.ExpressionData.NOEXPRESSION,
                GenerateDownladFile.ExpressionData.LOWEXPRESSION,
                GenerateDownladFile.ExpressionData.HIGHEXPRESSION));
        //  8/ no data - low - high - low >> high
        list.add(generateDataMap("Gid8", "Gname8", "STid", "STname", "AEid", "AEname",
                GenerateDownladFile.ExpressionData.NODATA, 
                GenerateDownladFile.ExpressionData.LOWEXPRESSION,
                GenerateDownladFile.ExpressionData.HIGHEXPRESSION,
                GenerateDownladFile.ExpressionData.LOWEXPRESSION));
        //  9/ no expr - no expr - no expr - no expr >> no expr
        list.add(generateDataMap("Gid9", "Gname9", "STid", "STname", "AEid", "AEname",
                GenerateDownladFile.ExpressionData.NOEXPRESSION, 
                GenerateDownladFile.ExpressionData.NOEXPRESSION,
                GenerateDownladFile.ExpressionData.NOEXPRESSION,
                GenerateDownladFile.ExpressionData.NOEXPRESSION));
        // 10/ low - low - low - low >> low
        list.add(generateDataMap("Gid10", "Gname10", "STid", "STname", "AEid", "AEname",
                GenerateDownladFile.ExpressionData.LOWEXPRESSION, 
                GenerateDownladFile.ExpressionData.LOWEXPRESSION,
                GenerateDownladFile.ExpressionData.LOWEXPRESSION,
                GenerateDownladFile.ExpressionData.LOWEXPRESSION));
        // 11/ high - high - high - high >> high
        list.add(generateDataMap("Gid11", "Gname11", "STid", "STname", "AEid", "AEname",
                GenerateDownladFile.ExpressionData.HIGHEXPRESSION, 
                GenerateDownladFile.ExpressionData.HIGHEXPRESSION,
                GenerateDownladFile.ExpressionData.HIGHEXPRESSION,
                GenerateDownladFile.ExpressionData.HIGHEXPRESSION));

        // Generate TSV files
        generate.createDownloadFiles(list, outputSimpleFile, true, false);
        generate.createDownloadFiles(list, outputCompleteFile, false, false);

        //now read the created TSV files
        assertDownloadExprFile(outputSimpleFile, true);
        assertDownloadExprFile(outputCompleteFile, false);
    }

    private Map<String, String> generateDataMap(String geneId, String geneName,
            String stageId, String stageName, String anatEntityId, String anatEntityName,
            ExpressionData affymetrixData, ExpressionData estData, ExpressionData inSituData,
            ExpressionData rnaSeqData) {

        Map<String, String> data = new HashMap<String, String>();
        data.put(GenerateDownladFile.GENE_ID_COLUMN_NAME, geneId);
        data.put(GenerateDownladFile.GENE_NAME_COLUMN_NAME, geneName);
        data.put(GenerateDownladFile.STAGE_ID_COLUMN_NAME, stageId);
        data.put(GenerateDownladFile.STAGE_NAME_COLUMN_NAME, stageName);
        data.put(GenerateDownladFile.ANATENTITY_ID_COLUMN_NAME, anatEntityId);
        data.put(GenerateDownladFile.ANATENTITY_NAME_COLUMN_NAME, anatEntityName);
        data.put(GenerateDownladFile.AFFYMETRIXDATA_COLUMN_NAME, 
                affymetrixData.getStringRepresentation());
        data.put(GenerateDownladFile.ESTDATA_COLUMN_NAME, 
                estData.getStringRepresentation());
        data.put(GenerateDownladFile.INSITUDATA_COLUMN_NAME, 
                inSituData.getStringRepresentation());
        data.put(GenerateDownladFile.RNASEQDATA_COLUMN_NAME,
                rnaSeqData.getStringRepresentation());

        return data;
    }

    /**
     * Asserts that the download file is good.
     * <p>
     * Read given download file and check whether the file contents corresponds to what is expected. 
     * 
     * @param file              A {@code String} that is the path to the file were data was written 
     *                          as TSV.
     * @param isSimplifiedFile  A {@code boolean} defining whether the file is a simple file.
     * @throws IOException      If the file could not be used.
     */
    private void assertDownloadExprFile(String file, boolean isSiplifiedFile) throws IOException {

        try (ICsvMapReader mapReader = new CsvMapReader(new FileReader(file), Utils.TSVCOMMENTED)) {
            String[] headers = mapReader.getHeader(true);
            log.trace("Headers: {}", (Object[]) headers);
            CellProcessor[] processors;
            if (isSiplifiedFile) {
                processors = GenerateDownladFile.generateCellProcessor(true, false);
            } else {
                processors = GenerateDownladFile.generateCellProcessor(false, false);
            }
            Map<String, Object> rowMap;
            int i = 0;
            while( (rowMap = mapReader.read(headers, processors)) != null ) {
                log.trace("Row: {}", rowMap);
                i++;
                String geneId = (String) rowMap.get(headers[0]);
                String geneName = (String) rowMap.get(headers[1]);
                String stageId = (String) rowMap.get(headers[2]);
                String stageName = (String) rowMap.get(headers[3]);
                String anatEntityId = (String) rowMap.get(headers[4]);
                String anatEntityName = (String) rowMap.get(headers[5]);
                String expressionData = null, 
                        affymetrixData = null, estData = null, inSituData = null, rnaSeqData = null;
                if (isSiplifiedFile) {
                    expressionData = (String) rowMap.get(headers[6]);
                } else {
                    affymetrixData = (String) rowMap.get(headers[6]);
                    estData = (String) rowMap.get(headers[7]);
                    inSituData = (String) rowMap.get(headers[8]);
                    rnaSeqData = (String) rowMap.get(headers[9]);
                }

                //  1/ no data - no data - no data - no data >> no data
                if (geneId.equals("Gid1")) {
                    assertCommonColumnRowEqual(geneId, "Gname1", geneName, "STid1", stageId, "STname1", 
                            stageName, "AEid1", anatEntityId,  "AEname1", anatEntityName);
                    if (isSiplifiedFile) {
                        this.assertSimpleColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NODATA, expressionData);
                    } else {
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NODATA, affymetrixData,
                                GenerateDownladFile.ExpressionData.NODATA, estData,
                                GenerateDownladFile.ExpressionData.NODATA, inSituData,
                                GenerateDownladFile.ExpressionData.NODATA, rnaSeqData);
                    }
                }

                if (geneId.equals("Gid2")) {
                    //  2/ no data - no expr - no data - no expr >> no expr
                    assertCommonColumnRowEqual(geneId, "Gname2", geneName, "STid2", stageId, "STname2", 
                            stageName, "AEid2", anatEntityId,  "AEname2", anatEntityName);
                    if (isSiplifiedFile) {
                        this.assertSimpleColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, expressionData);
                    } else {
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NODATA, affymetrixData,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, estData,
                                GenerateDownladFile.ExpressionData.NODATA, inSituData,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, rnaSeqData);
                    }
                }

                if (geneId.equals("Gid3")) {
                    //  3/ no data - low - low - no data >> low
                    assertCommonColumnRowEqual(geneId, "Gname3", geneName, "STid3", stageId, "STname3", 
                            stageName, "AEid3", anatEntityId,  "AEname3", anatEntityName);
                    if (isSiplifiedFile) {
                        this.assertSimpleColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION, expressionData);
                    } else {
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NODATA, affymetrixData,
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION, estData,
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION, inSituData,
                                GenerateDownladFile.ExpressionData.NODATA, rnaSeqData);
                    }
                }

                if (geneId.equals("Gid4")) {
                    //  4/ high - high - no data - no data >> high
                    assertCommonColumnRowEqual(geneId, "Gname4", geneName, "STid4", stageId, "STname4", 
                            stageName, "AEid4", anatEntityId,  "AEname4", anatEntityName);
                    if (isSiplifiedFile) {
                        this.assertSimpleColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION, expressionData);
                    } else {
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION, affymetrixData,
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION, estData,
                                GenerateDownladFile.ExpressionData.NODATA, inSituData,
                                GenerateDownladFile.ExpressionData.NODATA, rnaSeqData);
                    }
                }

                if (geneId.equals("Gid5")) {
                    //  5/ no data - no expr - low - no expr >> ambiguous
                    assertCommonColumnRowEqual(geneId, "Gname5", geneName, "STid", stageId, "STname", 
                            stageName, "AEid", anatEntityId,  "AEname", anatEntityName);
                    if (isSiplifiedFile) {
                        this.assertSimpleColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.AMBIGUOUS, expressionData);
                    } else {
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NODATA, affymetrixData,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, estData,
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION, inSituData,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, rnaSeqData);
                    }
                }

                if (geneId.equals("Gid6")) {
                    //  6/ no data - no expr - high - high >> ambiguous
                    assertCommonColumnRowEqual(geneId, "Gname6", geneName, "STid", stageId, "STname", 
                            stageName, "AEid", anatEntityId,  "AEname", anatEntityName);
                    if (isSiplifiedFile) {
                        this.assertSimpleColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.AMBIGUOUS, expressionData);
                    } else {
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NODATA, affymetrixData,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, estData,
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION, inSituData,
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION, rnaSeqData);
                    }
                }

                if (geneId.equals("Gid7")) {
                    //  7/ no data - no expr - low - high >> ambiguous
                    assertCommonColumnRowEqual(geneId, "Gname7", geneName, "STid", stageId, "STname", 
                            stageName, "AEid", anatEntityId,  "AEname", anatEntityName);
                    if (isSiplifiedFile) {
                        this.assertSimpleColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.AMBIGUOUS, expressionData);
                    } else {
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NODATA, affymetrixData,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, estData,
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION, inSituData,
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION, rnaSeqData);
                    }
                }

                if (geneId.equals("Gid8")) {
                    //  8/ no data - low - high - low >> high
                    assertCommonColumnRowEqual(geneId, "Gname8", geneName, "STid", stageId, "STname", 
                            stageName, "AEid", anatEntityId,  "AEname", anatEntityName);
                    if (isSiplifiedFile) {
                        this.assertSimpleColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION, expressionData);
                    } else {
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NODATA, affymetrixData,
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION, estData,
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION, inSituData,
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION, rnaSeqData);
                    }
                }

                if (geneId.equals("Gid9")) {
                    //  9/ no expr - no expr - no expr - no expr >> no expr
                    assertCommonColumnRowEqual(geneId, "Gname9", geneName, "STid", stageId, "STname", 
                            stageName, "AEid", anatEntityId,  "AEname", anatEntityName);
                    if (isSiplifiedFile) {
                        this.assertSimpleColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, expressionData);
                    } else {
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, affymetrixData,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, estData,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, inSituData,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, rnaSeqData);
                    }
                }

                if (geneId.equals("Gid19")) {
                    // 10/ low - low - low - low >> low
                    assertCommonColumnRowEqual(geneId, "Gname10", geneName, "STid", stageId, "STname", 
                            stageName, "AEid", anatEntityId,  "AEname", anatEntityName);
                    if (isSiplifiedFile) {
                        this.assertSimpleColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION, expressionData);
                    } else {
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION, affymetrixData,
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION, estData,
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION, inSituData,
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION, rnaSeqData);
                    }
                }

                if (geneId.equals("Gid11")) {
                    // 11/ high - high - high - high >> high
                    assertCommonColumnRowEqual(geneId, "Gname11", geneName, "STid", stageId, "STname", 
                            stageName, "AEid", anatEntityId,  "AEname", anatEntityName);
                    if (isSiplifiedFile) {
                        this.assertSimpleColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION, expressionData);
                    } else {
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION, affymetrixData,
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION, estData,
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION, inSituData,
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION, rnaSeqData);
                    }
                }
            }
            assertEquals("Incorrect number of lines in TSV output", 11, i);
        }
    }

    /**
     * Assert that common columns row are equal. It checks gene name, stage ID, stage name, 
     * anatomical entity ID, and anatomical entity name columns.
     * 
     * @param geneId            A {@code String} that is the gene ID of the row.
     * @param expGeneName       A {@code String} that is the expected gene name.
     * @param geneName          A {@code String} that is the actual gene name.
     * @param expStageId        A {@code String} that is the expected stage ID.
     * @param stageId           A {@code String} that is the actual stage ID.
     * @param expStageName      A {@code String} that is the expected stage name.
     * @param stageName         A {@code String} that is the actual stage name.
     * @param expAnatEntityId   A {@code String} that is the expected anatomical entity ID.
     * @param anatEntityId      A {@code String} that is the actual anatomical entity ID.
     * @param expAnatEntityName A {@code String} that is the expected anatomical entity name.
     * @param anatEntityName    A {@code String} that is the actual anatomical entity name.
     */
    private void assertCommonColumnRowEqual(String geneId, String expGeneName, String geneName, 
            String expStageId, String stageId, String expStageName, String stageName, 
            String expAnatEntityId, String anatEntityId, 
            String expAnatEntityName, String anatEntityName) {
        assertEquals("Incorrect gene name for " + geneId, expGeneName, geneName);
        assertEquals("Incorrect stage ID for " + geneId, expStageId, stageId);
        assertEquals("Incorrect stage name for " + geneId, expStageName, stageName);
        assertEquals("Incorrect anaEntity ID for " + geneId, expAnatEntityId, anatEntityId);
        assertEquals("Incorrect anaEntity name for " + geneId, expAnatEntityName, anatEntityName);
    }

    /**
     * Assert that specific complete file columns row are equal. It checks affymetrix data, 
     * EST data, <em>in Situ</em> data, and RNA-seq data columns. 
     * 
     * @param geneId            A {@code String} that is the gene ID of the row.
     * @param expAffyData       An {@code ExpressionData} that is the expected affymetrix data. 
     * @param affyData          A {@code String} that is the actual affymetrix data.
     * @param expESTData        An {@code ExpressionData} that is the expected EST data.
     * @param estData           A {@code String} that is the actual EST data.
     * @param expInSituData     An {@code ExpressionData} that is the expected <em>in Situ</em> data.
     * @param inSituData        A {@code String} that is the actual <em>in Situ</em> data.
     * @param expRNAseqData     An {@code ExpressionData} that is the expected RNA-seq data.
     * @param rnaSeqData        A {@code String} that is the actual RNA-seq data.
     */
    private void assertCompleteColumnRowEqual(String geneId, ExpressionData expAffyData, 
            String affyData, ExpressionData expESTData, String estData, ExpressionData expInSituData,
            String inSituData, ExpressionData expRNAseqData, String rnaSeqData) {

        assertEquals("Incorrect Affymetrix data for " + geneId, 
                expAffyData.getStringRepresentation(), affyData);
        assertEquals("Incorrect EST data for " + geneId, 
                expESTData.getStringRepresentation(), estData);
        assertEquals("Incorrect in situ data for " + geneId, 
                expInSituData.getStringRepresentation(), inSituData);
        assertEquals("Incorrect RNA-seq data for " + geneId, 
                expRNAseqData.getStringRepresentation(), rnaSeqData);
    }

    /**
     * Assert that specific simple file columns row are equal. It checks expression data columns. 
     * 
     * @param geneId            A {@code String} that is the gene ID of the row.
     * @param expExprData       An {@code ExpressionData} that is the expected expression data. 
     * @param exprData          A {@code String} that is the actual expression data.
     */
    private void assertSimpleColumnRowEqual(String geneId, 
            ExpressionData expExprData, String exprData) {
        assertEquals("Incorrect expression data for " + geneId, 
                expExprData.getStringRepresentation(), exprData);
    }
}