package org.bgee.pipeline.expression;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.Utils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.supercsv.cellprocessor.constraint.IsElementOf;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
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
     * Test {@link GenerateDownladFile#writeDownloadFiles(List<Map<String, String>>, String, String)},
     * which is the central method of the class doing all the job.
     * @throws IOException 
     */
    @Test
    public void shouldWriteDownloadFiles() throws IOException {
        log.entry();

        String outputSimpleFile = testFolder.newFile("simpleFile.tsv").getPath();
        String outputCompleteFile = testFolder.newFile("completeFile.tsv").getPath();

        GenerateDownladFile generate = new GenerateDownladFile();

        Map<String, String> data1 = new HashMap<String, String>();
        data1.put(GenerateDownladFile.GENE_ID_COLUMN_NAME, "Gid1");
        data1.put(GenerateDownladFile.GENE_NAME_COLUMN_NAME, "Gname1");
        data1.put(GenerateDownladFile.STAGE_ID_COLUMN_NAME, "STid1");
        data1.put(GenerateDownladFile.STAGE_NAME_COLUMN_NAME, "STname1");
        data1.put(GenerateDownladFile.ANATENTITY_ID_COLUMN_NAME, "AEid1");
        data1.put(GenerateDownladFile.ANATENTITY_NAME_COLUMN_NAME, "AEname1");
        // no data - no expr - low expr - high expr 
        data1.put(GenerateDownladFile.AFFYMETRIXDATA_NAME_COLUMN_NAME, 
                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation());
        data1.put(GenerateDownladFile.ESTDATA_NAME_COLUMN_NAME,
                GenerateDownladFile.ExpressionData.NOEXPRESSION.getStringRepresentation());
        data1.put(GenerateDownladFile.INSITUDATA_NAME_COLUMN_NAME, 
                GenerateDownladFile.ExpressionData.LOWEXPRESSION.getStringRepresentation());
        data1.put(GenerateDownladFile.RNASEQDATA_NAME_COLUMN_NAME, 
                GenerateDownladFile.ExpressionData.HIGHEXPRESSION.getStringRepresentation());

        Map<String, String> data2 = new HashMap<String, String>();
        data2.put(GenerateDownladFile.GENE_ID_COLUMN_NAME, "Gid2");
        data2.put(GenerateDownladFile.GENE_NAME_COLUMN_NAME, "Gname2");
        data2.put(GenerateDownladFile.STAGE_ID_COLUMN_NAME, "STid2");
        data2.put(GenerateDownladFile.STAGE_NAME_COLUMN_NAME, "STname2");
        data2.put(GenerateDownladFile.ANATENTITY_ID_COLUMN_NAME, "AEid2");
        data2.put(GenerateDownladFile.ANATENTITY_NAME_COLUMN_NAME, "AEname2");
        // no data - low expr - high expr - high expr 
        data2.put(GenerateDownladFile.AFFYMETRIXDATA_NAME_COLUMN_NAME, 
                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation());
        data2.put(GenerateDownladFile.ESTDATA_NAME_COLUMN_NAME,
                GenerateDownladFile.ExpressionData.LOWEXPRESSION.getStringRepresentation());
        data2.put(GenerateDownladFile.INSITUDATA_NAME_COLUMN_NAME, 
                GenerateDownladFile.ExpressionData.HIGHEXPRESSION.getStringRepresentation());
        data2.put(GenerateDownladFile.RNASEQDATA_NAME_COLUMN_NAME, 
                GenerateDownladFile.ExpressionData.HIGHEXPRESSION.getStringRepresentation());

        Map<String, String> data3 = new HashMap<String, String>();
        data3.put(GenerateDownladFile.GENE_ID_COLUMN_NAME, "Gid3");
        data3.put(GenerateDownladFile.GENE_NAME_COLUMN_NAME, "Gname3");
        data3.put(GenerateDownladFile.STAGE_ID_COLUMN_NAME, "STid3");
        data3.put(GenerateDownladFile.STAGE_NAME_COLUMN_NAME, "STname3");
        data3.put(GenerateDownladFile.ANATENTITY_ID_COLUMN_NAME, "AEid3");
        data3.put(GenerateDownladFile.ANATENTITY_NAME_COLUMN_NAME, "AEname3");
        // no data - low expr - low expr - no data 
        data3.put(GenerateDownladFile.AFFYMETRIXDATA_NAME_COLUMN_NAME, 
                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation());
        data3.put(GenerateDownladFile.ESTDATA_NAME_COLUMN_NAME,
                GenerateDownladFile.ExpressionData.LOWEXPRESSION.getStringRepresentation());
        data3.put(GenerateDownladFile.INSITUDATA_NAME_COLUMN_NAME, 
                GenerateDownladFile.ExpressionData.LOWEXPRESSION.getStringRepresentation());
        data3.put(GenerateDownladFile.RNASEQDATA_NAME_COLUMN_NAME, 
                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation());

        Map<String, String> data4 = new HashMap<String, String>();
        data4.put(GenerateDownladFile.GENE_ID_COLUMN_NAME, "Gid4");
        data4.put(GenerateDownladFile.GENE_NAME_COLUMN_NAME, "Gname4");
        data4.put(GenerateDownladFile.STAGE_ID_COLUMN_NAME, "STid4");
        data4.put(GenerateDownladFile.STAGE_NAME_COLUMN_NAME, "STname4");
        data4.put(GenerateDownladFile.ANATENTITY_ID_COLUMN_NAME, "AEid4");
        data4.put(GenerateDownladFile.ANATENTITY_NAME_COLUMN_NAME, "AEname4");
        // no data - no data - no data - no data 
        data4.put(GenerateDownladFile.AFFYMETRIXDATA_NAME_COLUMN_NAME, 
                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation());
        data4.put(GenerateDownladFile.ESTDATA_NAME_COLUMN_NAME,
                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation());
        data4.put(GenerateDownladFile.INSITUDATA_NAME_COLUMN_NAME, 
                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation());
        data4.put(GenerateDownladFile.RNASEQDATA_NAME_COLUMN_NAME, 
                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation());

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        list.add(data1);
        list.add(data2);
        list.add(data3);
        list.add(data4);
        generate.writeDownloadFiles(list, outputSimpleFile, outputCompleteFile);

        //now read the TSV files

        checkReadFile(outputSimpleFile, true);

        checkReadFile(outputCompleteFile, false);

        log.exit();
    }

    /**
     * Read given file and check whether the file contents corresponds to what is expected. 
     * 
     * @param file              A {@code String} that is the path to the file were data was written 
     *                          as TSV.
     * @param isSimplifiedFile  A {@code boolean} defining whether the file is a simple file.
     * @throws IOException      If the file could not be used.
     */
    private void checkReadFile(String file, boolean isSiplifiedFile) throws IOException {
        log.entry(file, isSiplifiedFile);

        try (ICsvMapReader mapReader = new CsvMapReader(new FileReader(file), Utils.TSVCOMMENTED)) {
            String[] headers = mapReader.getHeader(true);
            log.trace("Headers: {}", (Object[]) headers);
            CellProcessor[] processors;
            if (isSiplifiedFile) {
                processors = new CellProcessor[] {
                        new UniqueHashCode(new NotNull()),
                        new NotNull(),
                        new NotNull(),
                        new NotNull(),
                        new NotNull(),
                        new NotNull(),
                        new IsElementOf(GenerateDownladFile.EXPRESSIONDATA)};
            } else {
                processors = new CellProcessor[] {
                        new UniqueHashCode(new NotNull()),
                        new NotNull(),
                        new NotNull(),
                        new NotNull(),
                        new NotNull(),
                        new NotNull(),
                        new IsElementOf(GenerateDownladFile.EXPRESSIONDATA),
                        new IsElementOf(GenerateDownladFile.EXPRESSIONDATA),
                        new IsElementOf(GenerateDownladFile.EXPRESSIONDATA),
                        new IsElementOf(GenerateDownladFile.EXPRESSIONDATA)};
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

                if (geneId.equals("Gid1")) {
                    assertEquals("Incorrect gene name for Gid1", "Gname1", geneName);
                    assertEquals("Incorrect stage ID for Gid1", "STid1", stageId);
                    assertEquals("Incorrect satge name for Gid1", "STname1", stageName);
                    assertEquals("Incorrect anaEntity ID for Gid1", "AEid1", anatEntityId);
                    assertEquals("Incorrect anaEntity name for Gid1", "AEname1", anatEntityName);
                    if (isSiplifiedFile) {
                        // ambiguous
                        assertEquals("Incorrect expression data for Gid1", 
                                GenerateDownladFile.ExpressionData.AMBIGUOUS.getStringRepresentation(), 
                                expressionData);
                    } else {
                        // no data - no expr - low expr - high expr 
                        assertEquals("Incorrect Affymetrix data for Gid1", 
                                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation(),
                                affymetrixData);
                        assertEquals("Incorrect EST data for Gid1", 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION.getStringRepresentation(),
                                estData);
                        assertEquals("Incorrect in Situ Data for Gid1", 
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION.getStringRepresentation(),
                                inSituData);
                        assertEquals("Incorrect RNA-seq data for Gid1", 
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION.getStringRepresentation(),
                                rnaSeqData);
                    }
                }

                if (geneId.equals("Gid2")) {
                    assertEquals("Incorrect gene name for Gid2", "Gname2", geneName);
                    assertEquals("Incorrect stage ID for Gid2", "STid2", stageId);
                    assertEquals("Incorrect satge name for Gid2", "STname2", stageName);
                    assertEquals("Incorrect anaEntity ID for Gid2", "AEid2", anatEntityId);
                    assertEquals("Incorrect anaEntity name for Gid2", "AEname2", anatEntityName);
                    if (isSiplifiedFile) {
                        // high expr 
                        assertEquals("Incorrect expression data for Gid2", 
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION.getStringRepresentation(), 
                                expressionData);
                    } else {
                        // no data - low expr - high expr - high expr 
                        assertEquals("Incorrect Affymetrix data for Gid2", 
                                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation(),
                                affymetrixData);
                        assertEquals("Incorrect EST data for Gid2", 
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION.getStringRepresentation(),
                                estData);
                        assertEquals("Incorrect in Situ Data for Gid2", 
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION.getStringRepresentation(), 
                                inSituData);
                        assertEquals("Incorrect RNA-seq data for Gid2", 
                                GenerateDownladFile.ExpressionData.HIGHEXPRESSION.getStringRepresentation(), 
                                rnaSeqData);
                    }
                }

                if (geneId.equals("Gid3")) {
                    assertEquals("Incorrect gene name for Gid3", "Gname3", geneName);
                    assertEquals("Incorrect stage ID for Gid3", "STid3", stageId);
                    assertEquals("Incorrect satge name for Gid3", "STname3", stageName);
                    assertEquals("Incorrect anaEntity ID for Gid3", "AEid3", anatEntityId);
                    assertEquals("Incorrect anaEntity name for Gid3", "AEname3", anatEntityName);
                    if (isSiplifiedFile) {
                        assertEquals("Incorrect expression data for Gid3", 
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION.getStringRepresentation(), 
                                expressionData);
                    } else {
                        // no data - low expr - low expr - no data 
                        assertEquals("Incorrect Affymetrix data for Gid3", 
                                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation(),
                                affymetrixData);
                        assertEquals("Incorrect EST data for Gid3", 
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION.getStringRepresentation(),
                                estData);
                        assertEquals("Incorrect in Situ Data for Gid3", 
                                GenerateDownladFile.ExpressionData.LOWEXPRESSION.getStringRepresentation(),
                                inSituData);
                        assertEquals("Incorrect RNA-seq data for Gid3", 
                                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation(),
                                rnaSeqData);
                    }
                }

                if (geneId.equals("Gid4")) {
                    assertEquals("Incorrect gene name for Gid4", "Gname4", geneName);
                    assertEquals("Incorrect stage ID for Gid4", "STid4", stageId);
                    assertEquals("Incorrect satge name for Gid4", "STname4", stageName);
                    assertEquals("Incorrect anaEntity ID for Gid4", "AEid4", anatEntityId);
                    assertEquals("Incorrect anaEntity name for Gid4", "AEname4", anatEntityName);
                    if (isSiplifiedFile) {
                        assertEquals("Incorrect expression data for Gid4", 
                                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation(), 
                                expressionData);
                    } else {
                        // no data - no data - no data - no data
                        assertEquals("Incorrect Affymetrix data for Gid4", 
                                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation(),
                                affymetrixData);
                        assertEquals("Incorrect EST data for Gid4", 
                                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation(),
                                estData);
                        assertEquals("Incorrect in Situ Data for Gid4", 
                                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation(),
                                inSituData);
                        assertEquals("Incorrect RNA-seq data for Gid4", 
                                GenerateDownladFile.ExpressionData.NODATA.getStringRepresentation(),
                                rnaSeqData);
                    }
                }
            }
            assertEquals("Incorrect number of lines in TSV output", 4, i);

            log.exit();
        }
    }
}