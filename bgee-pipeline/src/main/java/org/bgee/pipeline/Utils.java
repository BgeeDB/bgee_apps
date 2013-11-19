package org.bgee.pipeline;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 * This class allows to perform operations that are widely used during execution 
 * of the Bgee pipeline. For instance, unzipping an archive, or removing 
 * a directory.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class Utils {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(Utils.class.getName());

    
    /**
     * Get IDs of taxa from the TSV file named {@code taxonFile}.
     * The IDs are {@code String}s corresponding to the NCBI ID, with an ontology 
     * prefix added (e.g., "NCBITaxon:9606" for human). The first line should be 
     * a header line, the first column should contain the IDs, and the second column, 
     * optional, be present only for human readability.
     * 
     * @param taxonFile     A {@code String} that is the path to the TSV file 
     *                      containing the list of taxon IDs.
     * @return              A {@code Set} of {String}s that are the ontology IDs 
     *                      of the taxa present in {@code taxonFile}.
     * @throws FileNotFoundException    If {@code taxonFile} could not be found.
     * @throws IOException              If {@code taxonFile} could not be read.
     * @throws IllegalArgumentException If the file located at {@code taxonFile} 
     *                                  did not allow to obtain any valid taxon ID.
     */
    public static Set<String> getTaxonIds(String taxonFile) throws IllegalArgumentException, 
        FileNotFoundException, IOException {
        log.entry(taxonFile);
        
        CellProcessor processor = new NotNull(new UniqueHashCode());
        Set<String> taxonIds = new HashSet<String>(
                parseColumnAsString(taxonFile, 0, 2, processor));
        
        if (taxonIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The taxon file " +
                    taxonFile + " did not contain any valid taxon ID"));
        }
        
        return log.exit(taxonIds);
    }
    
    /**
     * Parse {@code tsvFile} and retrieve the values in the column with index 
     * {@code columnIndex}, as {@code String}s. First value of {@code columnIndex} 
     * is 0. The total number of column in the file must be provided.
     * <p>
     * This method returned the values retrieved from the specified column in 
     * the order they were read from the file.
     * 
     * @param tsvFile           A {@code String} that is the path to a TSV file 
     *                          to parse.
     * @param columnIndex       An {@code int} that is the index of the column 
     *                          to retrieve.
     * @param columnCount       An {@code int} that is the total number of columns 
     *                          in {@code tsvFile}.
     * @return  A {@code List} of {@code String}s that the value in the column specified, 
     *          in the order they were read from {@code tsvFiles}.
     * @throws FileNotFoundException    If {@code tsvFile} could not be found.
     * @throws IOException              If {@code tsvFile} could not be read.
     */
    public static List<String> parseColumnAsString(String tsvFile, int columnIndex, 
            int columnCount) throws FileNotFoundException, 
            IOException {
        log.entry(tsvFile, columnIndex, columnCount);
        
        return log.exit(Utils.parseColumnAsString(tsvFile, columnIndex, 
                columnCount, null));
    }
    
    /**
     * Parse {@code tsvFile} and retrieve the values in the column with index 
     * {@code columnIndex}, as {@code String}s. First value of {@code columnIndex} 
     * is 0. To define how the column should be processed, a {@code CellProcessor} 
     * is provided. Also, the total number of column in the file must be known.
     * <p>
     * This method returned the values retrieved from the specified column in 
     * the order they were read from the file.
     * 
     * @param tsvFile           A {@code String} that is the path to a TSV file 
     *                          to parse.
     * @param columnIndex       An {@code int} that is the index of the column 
     *                          to retrieve.
     * @param columnCount       An {@code int} that is the total number of columns 
     *                          in {@code tsvFile}.
     * @param columnProcessor   A {@code CellProcessor} defining how to parse 
     *                          the specify column.
     * @return  A {@code List} of {@code String}s that the value in the column specified, 
     *          in the order they were read from {@code tsvFiles}.
     * @throws FileNotFoundException    If {@code tsvFile} could not be found.
     * @throws IOException              If {@code tsvFile} could not be read.
     */
    public static List<String> parseColumnAsString(String tsvFile, int columnIndex, 
            int columnCount, CellProcessor columnProcessor) throws FileNotFoundException, 
            IOException {
        log.entry(tsvFile, columnIndex, columnCount, columnProcessor);
        
        List<String> values = new ArrayList<String>();
        
        try (ICsvMapReader mapReader = new CsvMapReader(
                new FileReader(tsvFile), CsvPreference.TAB_PREFERENCE)) {
            mapReader.getHeader(true); 
            //define our own headers, because only 1 column is used
            String[] headers = new String[columnCount];
            CellProcessor[] processors = new CellProcessor[columnCount];
            String columnName = "myColumn";
            for (int i = 0; i < columnCount; i++) {
                if (i == columnIndex) {
                    headers[i] = columnName;
                    processors[i] = columnProcessor;
                } else {
                    headers[i] = null;
                    processors[i] = null;
                }
            }
            Map<String, Object> rowMap;
            while( (rowMap = mapReader.read(headers, processors)) != null ) {
                values.add((String) rowMap.get(columnName));
            }
        }
        
        return log.exit(values);
    }
}
