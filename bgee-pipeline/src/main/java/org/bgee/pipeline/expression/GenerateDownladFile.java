package org.bgee.pipeline.expression;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.constraint.IsElementOf;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;


/**
 * Class responsible to generate TSV download files (simple and complete files) 
 * from the Bgee database. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public class GenerateDownladFile {
    
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = 
            LogManager.getLogger(GenerateDownladFile.class.getName());
    
    /**
     * An {@code int} that is the number of headers in the simple file.
     */
    private final static int SIMPLE_HEADER_COUNT = 10;
    
    /**
     * An {@code int} that is the number of headers in the complete file.
     */
    private final static int COMPLETE_HEADER_COUNT = 10;
    
    /**
     * A {@code String} that is the name of the column containing gene IDs, in the download file.
     */
    public final static String GENE_ID_COLUMN_NAME = "Gene ID";

    /**
     * A {@code String} that is the name of the column containing gene names, in the download file.
     */
    public final static String GENE_NAME_COLUMN_NAME = "Gene name";

    /**
     * A {@code String} that is the name of the column containing developmental stage IDs, 
     * in the download file.
     */
    public final static String STAGE_ID_COLUMN_NAME = "Developmental stage ID";

    /**
     * A {@code String} that is the name of the column containing developmental stage names, 
     * in the download file.
     */
    public final static String STAGE_NAME_COLUMN_NAME = "Developmental stage name";

    /**
     * A {@code String} that is the name of the column containing anatomical entity IDs, 
     * in the download file.
     */
    public final static String ANATENTITY_ID_COLUMN_NAME = "Anatomical entity ID";

    /**
     * A {@code String} that is the name of the column containing anatomical entity names, 
     * in the download file.
     */
    public final static String ANATENTITY_NAME_COLUMN_NAME = "Anatomical entity name";

    /**
     * A {@code String} that is the name of the column containing the contribution of Affymetrix 
     * data, in the download file.
     */
    public final static String AFFYMETRIXDATA_NAME_COLUMN_NAME = "Contribution of Affymetrix data";

    /**
     * A {@code String} that is the name of the column containing the contribution of EST data, 
     * in the download file.
     */
    public final static String ESTDATA_NAME_COLUMN_NAME = "Contribution of EST data";

    /**
     * A {@code String} that is the name of the column containing the contribution of 
     * <em>in situ</em> data, in the download file.
     */
    public final static String INSITUDATA_NAME_COLUMN_NAME = "Contribution of in situ data";

    /**
     * A {@code String} that is the name of the column containing the contribution of RNA-Seq data, 
     * in the download file.
     */
    public final static String RNASEQDATA_NAME_COLUMN_NAME = "Contribution of RNA-Seq data";

    /**
     * A {@code String} that is the name of the column containing the expression, 
     * in the download file.
     */
    public final static String EXPRESSION_NAME_COLUMN_NAME = "Expression";

    /**
     * A {@code String} that is the no data value for data contributions, 
     * in the download file.
     */
    public final static String NODATA_VALUE = "no data";

    /**
     * A {@code String} that is the no-expression value for data contributions, 
     * in the download file.
     */
    public final static String NOEXPRESSION_VALUE = "no-expression";

    /**
     * A {@code String} that is the low expression value for data contributions, 
     * in the download file.
     */
    public final static String LOWEXPRESSION_VALUE = "low expression";

    /**
     * A {@code String} that is the high expression value for data contributions, 
     * in the download file.
     */
    public final static String HIGHEXPRESSION_VALUE = "high expression";
    
    /**
     * A {@code String} that is the expression value when it is ambiguous between different 
     * data types, in the download file.
     */
    public final static String AMBIGUOUS_VALUE = "ambiguous";

    public static void main(String[] args) throws IOException {
        log.entry((Object[]) args);

        GenerateDownladFile generate = new GenerateDownladFile();
        List<Map<String,String>> list = null;
        generate.writeDownloadFile(list, "OUTPUT_SIMPE_FILE", "OUTPUT_COMPLETE_FILE");

        log.exit();
    }

    private void writeDownloadFile(List<Map<String, String>> inputList, String outputSimpleFile,
            String outputCompleteFile) throws IOException {
        log.entry(inputList, outputSimpleFile, outputCompleteFile);
        // TODO 
        
        CellProcessor[] processorSimpleFile = new CellProcessor[SIMPLE_HEADER_COUNT];
        CellProcessor[] processorCompleteFile = new CellProcessor[COMPLETE_HEADER_COUNT];
        String[] headerSimpleFile = new String[SIMPLE_HEADER_COUNT];
        String[] headerCompleteFile = new String[COMPLETE_HEADER_COUNT];

        //BOTH FILES
        // The gene (must be unique)
        processorSimpleFile[0] = processorCompleteFile[0] = new UniqueHashCode(new NotNull());
        headerSimpleFile[0] = headerCompleteFile[0] = GENE_ID_COLUMN_NAME;
        
        processorSimpleFile[1] = processorCompleteFile[1] = new NotNull();
        headerSimpleFile[1] = headerCompleteFile[1] = GENE_ID_COLUMN_NAME;
        
        // The developmental stage
        processorSimpleFile[2] = processorCompleteFile[2] = new NotNull();
        headerSimpleFile[2] = headerCompleteFile[2] = STAGE_ID_COLUMN_NAME;
        
        processorSimpleFile[3] = processorCompleteFile[3] = new NotNull();
        headerSimpleFile[3] = headerCompleteFile[3] = STAGE_ID_COLUMN_NAME;
        
        // The anatomical entity
        processorSimpleFile[4] = processorCompleteFile[4] = new NotNull();
        headerSimpleFile[4] = headerCompleteFile[4] = ANATENTITY_ID_COLUMN_NAME;
        
        processorSimpleFile[5] = processorCompleteFile[5] = new NotNull();
        headerSimpleFile[5] = headerCompleteFile[5] = ANATENTITY_ID_COLUMN_NAME;
        
        // Different possible elements for data contributions
        List<Object> list = Arrays.asList(new Object[] {
                NOEXPRESSION_VALUE, LOWEXPRESSION_VALUE, HIGHEXPRESSION_VALUE, 
                AMBIGUOUS_VALUE, NODATA_VALUE});

        //SIMPLE FILE
        // The expression column (combining all data types)
        processorSimpleFile[6] = new IsElementOf(list);
        headerSimpleFile[6] = ANATENTITY_ID_COLUMN_NAME;

        //COMPLETE FILE
        // The contribution of Affimetrix data
        processorCompleteFile[6] = new IsElementOf(list);
        headerCompleteFile[6] = AFFYMETRIXDATA_NAME_COLUMN_NAME;
        
        // The contribution of EST data
        processorCompleteFile[7] = new IsElementOf(list);
        headerCompleteFile[7] = ESTDATA_NAME_COLUMN_NAME;

        // The contribution of in Situ data
        processorCompleteFile[8] = new IsElementOf(list);
        headerCompleteFile[8] = INSITUDATA_NAME_COLUMN_NAME;

        // The contribution of RNA-seq data
        processorCompleteFile[8] = new IsElementOf(list);
        headerCompleteFile[8] = RNASEQDATA_NAME_COLUMN_NAME;

        // Write complete download file
        //TODO
        try (ICsvMapWriter mapCompleteWriter = new CsvMapWriter(new FileWriter(outputCompleteFile),
                Utils.TSVCOMMENTED)) {
            
            mapCompleteWriter.writeHeader(headerCompleteFile);
            
            for (Map<String, String> map: inputList) {
                Map<String, Object> row = new HashMap<String, Object>();

                row.put(headerCompleteFile[0], "");
                
                mapCompleteWriter.write(row, headerCompleteFile, processorCompleteFile);
            }
        }
        
        // Write simple download file.
        //TODO
        try (ICsvMapWriter mapSimpleWriter = new CsvMapWriter(new FileWriter(outputSimpleFile),
                Utils.TSVCOMMENTED)) {
            
            mapSimpleWriter.writeHeader(headerSimpleFile);
            
            for (Map<String, String> map: inputList) {
                Map<String, Object> row = new HashMap<String, Object>();

                row.put(headerSimpleFile[0], "");
                
                mapSimpleWriter.write(row, headerSimpleFile, processorSimpleFile);
            }
        }
        

        
        log.exit();
    }

}
