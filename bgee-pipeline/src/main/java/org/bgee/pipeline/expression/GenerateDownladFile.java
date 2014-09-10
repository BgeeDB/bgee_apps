package org.bgee.pipeline.expression;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.pipeline.Utils;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
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
    private final static Logger log = LogManager.getLogger(GenerateDownladFile.class.getName());
        
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
     * A {@code String} that is the name of the column containing expression/no-expression found 
     * with Affymetrix experiment, in the download file.
     */
    public final static String AFFYMETRIXDATA_NAME_COLUMN_NAME = "Affymetrix data";

    /**
     * A {@code String} that is the name of the column containing expression/no-expression found 
     * with EST experiment, in the download file.
     */
    public final static String ESTDATA_NAME_COLUMN_NAME = "EST data";

    /**
     * A {@code String} that is the name of the column containing expression/no-expression found 
     * with <em>in situ</em> experiment, in the download file.
     */
    public final static String INSITUDATA_NAME_COLUMN_NAME = "In situ data";

    /**
     * A {@code String} that is the name of the column containing expression/no-expression found 
     * with RNA-Seq experiment, in the download file.
     */
    public final static String RNASEQDATA_NAME_COLUMN_NAME = "RNA-Seq data";

    /**
     * A {@code String} that is the name of the column containing merged expression/no-expression 
     * from different data types, in the download file.
     */
    public final static String EXPRESSION_COLUMN_NAME = "Expression/No-expression";

    /**
     * A {@code List} of {@code Object} containing possible elements for expression data, 
     * in the download file.
     */
    public final static List<Object> EXPRESSIONDATA = 
            Arrays.asList(new Object[] {
                    ExpressionData.NODATA.getStringRepresentation(),
                    ExpressionData.NOEXPRESSION.getStringRepresentation(),
                    ExpressionData.LOWEXPRESSION.getStringRepresentation(),
                    ExpressionData.HIGHEXPRESSION.getStringRepresentation(),
                    ExpressionData.AMBIGUOUS.getStringRepresentation()});

    /**
     * An {@code Enum} used to define, for each data type (Affymetrix, RNA-Seq, ...), the 
     * expression/no-expression of the call.
     * <ul>
     * <li>{@code NODATA}:         no data from the associated data type allowed to produce the call.
     * <li>{@code NOEXPRESSION}:   no-expression was detected from the associated data type.
     * <li>{@code LOWEXPRESSION}:  low expression was detected from the associated data type.
     * <li>{@code HIGHEXPRESSION}: high expression was detected from the associated data type.
     * <li>{@code AMBIGUOUS}:      different data types are not coherent (for instance, Affymetrix 
     *                             data reveals an low expression while <em>in situ</em> data 
     *                             reveals a no-expression).
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum ExpressionData {
        NODATA("no data"), NOEXPRESSION("no-expression"), LOWEXPRESSION("low expression"), 
        HIGHEXPRESSION("high expression"), AMBIGUOUS("ambiguous");

        private final String stringRepresentation;

        /**
         * Constructor providing the {@code String} representation 
         * of this {@code DataState}.
         * 
         * @param stringRepresentation  A {@code String} corresponding to 
         *                              this {@code DataState}.
         */
        private ExpressionData(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }

        public String getStringRepresentation() {
            return this.stringRepresentation;
        }

        public String toString() {
            return this.getStringRepresentation();
        }

    }

    /**
     * Main method to trigger the generate TSV download files (simple and complete files) from Bgee 
     * database. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the single download file to generate.
     * <li>path to the complete download file to generate.
     * </ol>
     * 
     * @param args          An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IOException  If some files could not be used.
     */
    public static void main(String[] args) throws IOException {
        log.entry((Object[]) args);

        if (args.length != 2) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect number of arguments provided, expected " + 
                    "2 arguments, " + args.length + " provided."));
        }

        // TODO Get data and set in a List<Map<String,String>>
        GenerateDownladFile generate = new GenerateDownladFile();
        List<Map<String,String>> list = null;
        generate.writeDownloadFiles(list, args[0], args[1]);

        log.exit();
    }

    /**
     * Write the download TSV files (simple and complete files). The data are provided 
     * by a {@code List} of {@code Map}s where keys are column names and values are data associated 
     * to the column name. 
     * <p>
     * The generated TSV file will have one header line. Both files do not have the same headers:
     * in the simple file, expression/no-expression from different data types are merged in a single
     * column called Expression/No-expression.
     * 
     * @param inputList          A {@code List} of {@code Map}s where keys are column names and 
     *                           values are data associated to the column name. 
     * @param outputSimpleFile   A {@code String} that is the path to the simple output file
     *                           were data will be written as TSV.
     * @param outputCompleteFile A {@code String} that is the path to the complete output file
     *                           were data will be written as TSV.
     * @throws IOException      If an error occurred while trying to write the 
     *                          {@code outputSimpleFile} or the {@code outputCompleteFile}.
     */
    public void writeDownloadFiles(List<Map<String, String>> inputList, String outputSimpleFile,
            String outputCompleteFile) throws IOException {
        log.entry(inputList, outputSimpleFile, outputCompleteFile);
        
        CellProcessor[] processorCompleteFile = generateCellProcessor(false);;
        final String[] headerCompleteFile = new String[] {
                GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,
                ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                AFFYMETRIXDATA_NAME_COLUMN_NAME, ESTDATA_NAME_COLUMN_NAME, 
                INSITUDATA_NAME_COLUMN_NAME, RNASEQDATA_NAME_COLUMN_NAME};
        writeDownloadFile(
                inputList, outputCompleteFile, headerCompleteFile, processorCompleteFile, false);
        
        CellProcessor[] processorSimpleFile = generateCellProcessor(true);
        final String[] headerSimpleFile = new String[] { 
                GENE_ID_COLUMN_NAME, GENE_NAME_COLUMN_NAME, 
                STAGE_ID_COLUMN_NAME, STAGE_NAME_COLUMN_NAME,
                ANATENTITY_ID_COLUMN_NAME, ANATENTITY_NAME_COLUMN_NAME,
                EXPRESSION_COLUMN_NAME};
        writeDownloadFile(inputList, outputSimpleFile, headerSimpleFile, processorSimpleFile, true);

        log.exit();
    }

    /**
     * Generate a {@code CellProcessor} needed to write a download file. 
     * 
     * @param isSimplifiedFile  A {@code boolean} defining whether the output file is a simple file.
     *                          If {@code true}, expression data from different data types are 
     *                          merged into a single column called Expression/No-expression.
     * @return                  A {@code CellProcessor} needed to write a simple or complete 
     *                          download file.
     */
    public static CellProcessor[] generateCellProcessor(boolean isSimplifiedFile) {
        log.entry(isSimplifiedFile);
        
        final CellProcessor[] processors;
        if (isSimplifiedFile) {
            processors = new CellProcessor[] { 
                    new UniqueHashCode(new NotNull()), // gene ID (must be unique)
                    new NotNull(), // gene Name
                    new NotNull(), // developmental stage ID
                    new NotNull(), // developmental stage name
                    new NotNull(), // anatomical entity ID
                    new NotNull(), // anatomical entity name
                    new IsElementOf(EXPRESSIONDATA)}; // Expression/No-expression
        } else {
            processors = new CellProcessor[] { 
                    new UniqueHashCode(new NotNull()), // gene ID (must be unique)
                    new NotNull(), // gene Name
                    new NotNull(), // developmental stage ID
                    new NotNull(), // developmental stage name
                    new NotNull(), // anatomical entity ID
                    new NotNull(), // anatomical entity name
                    new IsElementOf(EXPRESSIONDATA), // Affymetrix data
                    new IsElementOf(EXPRESSIONDATA), // EST data
                    new IsElementOf(EXPRESSIONDATA), // In Situ data
                    new IsElementOf(EXPRESSIONDATA)};  // RNA-seq data
        }
        return log.exit(processors);
    }

    /**
     * Write a download TSV file according to the data are provided by a {@code List} of 
     * {@code Map}s where keys are column names and values are data associated to the column name, 
     * the given output file path, headers, cell processors and {@code boolean} defining whether 
     * it is a simple download file.
     * <p>
     * If the {@code isSimplifiedFile} is {code true}, expression data from different data types 
     * are merged in a single column called Expression.
     * 
     * @param inputList         A {@code List} of {@code Map}s where keys are column names and 
     *                          values are data associated to the column name. 
     * @param outputFile        A {@code String} that is the path to the output file
     *                          were data will be written as TSV.
     * @param headers           An {@code Array} of {@code String}s containing headers of the file.
     * @param processors        An {@code Array} of {@code CellProcessor}s containing cell 
     *                          processors which automates the data type conversions, and enforce 
     *                          column constraints.
     * @param isSimplifiedFile  A {@code boolean} defining whether the output file is a simple file.
     *                          If {code true}, expression data from different data types are merged
     *                          in a single column called Expression/No-expression.
     * @throws IOException      If an error occurred while trying to write {@code outputFile}.
     */
    private void writeDownloadFile(List<Map<String, String>> inputList, String outputFile, 
            String[] headers, CellProcessor[] processors, boolean isSimplifiedFile) 
            throws IOException {
        log.entry(inputList, outputFile, headers, processors);
        
        try (ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(outputFile),
                Utils.TSVCOMMENTED)) {
            
            mapWriter.writeHeader(headers);
            
            for (Map<String, String> map: inputList) {
                Map<String, Object> row = new HashMap<String, Object>();
                if (isSimplifiedFile) {
                    String mergedData = this.mergeExprData(
                            map.get(AFFYMETRIXDATA_NAME_COLUMN_NAME), 
                            map.get(ESTDATA_NAME_COLUMN_NAME), 
                            map.get(INSITUDATA_NAME_COLUMN_NAME), 
                            map.get(RNASEQDATA_NAME_COLUMN_NAME));
                    row.put(EXPRESSION_COLUMN_NAME, mergedData);
                }
                for (String key : map.keySet()) {
                    if (!isSimplifiedFile ||
                            (!key.equals(AFFYMETRIXDATA_NAME_COLUMN_NAME) &&
                             !key.equals(ESTDATA_NAME_COLUMN_NAME) &&
                             !key.equals(INSITUDATA_NAME_COLUMN_NAME) && 
                             !key.equals(RNASEQDATA_NAME_COLUMN_NAME))) {
                        row.put(key, map.get(key));
                    }
                }
                mapWriter.write(row, headers, processors);
            }
        }
        
        log.exit();
    }

    /**
     * Merge expression data from different data types.
     * 
     * @param affymetrixData    A {@code String} that is the expression/no-expression data from 
     *                          Affymetrix experiment.
     * @param estData           A {@code String} that is the expression/no-expression data from 
     *                          EST experiment.
     * @param inSituData        A {@code String} that is the expression/no-expression data from 
     *                          <em> in situ</em> experiment.
     * @param rnaSeqData        A {@code String} that is the expression/no-expression data from
     *                          RNA-seq experiment.
     * @return                  A {@code String} that is the merged expression/no-expression data
     *                          generated from provided expression/no-expression data. 
     */
    private String mergeExprData(
            String affymetrixData, String estData, String inSituData, String rnaSeqData) {
        log.entry(affymetrixData, estData, inSituData, rnaSeqData);
        
        Set<String> allData = new HashSet<String>(
                Arrays.asList(affymetrixData, estData, inSituData, rnaSeqData));
        
        if (allData.contains(ExpressionData.NOEXPRESSION.getStringRepresentation()) &&
                (allData.contains(ExpressionData.LOWEXPRESSION.getStringRepresentation()) ||
                 allData.contains(ExpressionData.HIGHEXPRESSION.getStringRepresentation()))) {
            // No-expression AND expression from different data types
            return log.exit(ExpressionData.AMBIGUOUS.getStringRepresentation());
        } else if (allData.contains(ExpressionData.HIGHEXPRESSION.getStringRepresentation())) {
            // At least one 'high expression' (with or without 'low expression' and/or 'no data')
            return log.exit(ExpressionData.HIGHEXPRESSION.getStringRepresentation());
        } else if (allData.contains(ExpressionData.LOWEXPRESSION.getStringRepresentation())) {
            // At least one 'low expression' (with or without 'no data')
            return log.exit(ExpressionData.LOWEXPRESSION.getStringRepresentation());
        } else if (allData.contains(ExpressionData.NOEXPRESSION.getStringRepresentation())) {
            // At least one 'no-expression' (with or without 'no data')
            return log.exit(ExpressionData.NOEXPRESSION.getStringRepresentation());
        }
        // Only 'no data'        
        return log.exit(ExpressionData.NODATA.getStringRepresentation());
    }
}
